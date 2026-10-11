package com.alm.service;

import com.alm.dao.FlightDAO;
import com.alm.model.Flight;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Flight Sync Service — the central sync orchestrator.
 *
 * Strategy Pattern:
 * ─ Priority 1: Aviationstack live API (if key configured + quota available)
 * ─ Priority 2: CSV file upload (manual fallback)
 * ─ Priority 3: Mock data generator (always available, for demo/offline)
 *
 * Efficient API Usage:
 * ─ Batch: Fetches ALL flights for an airport in 2 API calls (departures +
 * arrivals)
 * ─ Cache: Results stored in FlightDataCache — all UI panels read from cache
 * ─ Smart Schedule: Flights every 30m, schedules daily, reference data weekly
 * ─ Fallback: Auto-switches to mock data when quota is low or API fails
 * ─ Aggregate: One sync serves all users (dashboard, reception, passenger
 * screens)
 */
public class FlightSyncService {

    private final FlightDAO flightDAO;
    private final FlightDataCache cache;
    private long lastSyncTime = 0;
    private int recordsUpdated = 0;
    private String lastSyncSource = "None"; // "API", "CSV", "Mock"
    private String lastSyncError = null;

    public FlightSyncService() {
        this.flightDAO = new FlightDAO();
        this.cache = FlightDataCache.getInstance();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PRIMARY SYNC — Smart Source Selection
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Main sync entry point. Automatically selects the best data source:
     * 1. Live API (if configured and quota available)
     * 2. Mock data (always available as fallback)
     *
     * All results are written to DB AND to the in-memory cache simultaneously.
     */
    public int syncNow() {
        lastSyncError = null;

        // Strategy 1: Try Aviationstack API
        if (cache.shouldUseApi()) {
            try {
                return syncFromApi();
            } catch (Exception e) {
                lastSyncError = "API Error: " + e.getMessage();
                System.err.println("API sync failed, falling back to mock: " + e.getMessage());
            }
        } else if (AviationstackConfig.isApiConfigured()) {
            lastSyncError = "Quota low (" + cache.getRemainingQuota() + " remaining). Using mock data.";
            System.out.println(lastSyncError);
        }

        // Strategy 2: Fallback to mock data
        return syncFromMockData();
    }

    /**
     * Smart sync — only refresh if cache is stale (respects TTL).
     * Returns cached data if still fresh, avoiding unnecessary API calls.
     */
    public int smartSync() {
        if (cache.isFlightDataFresh()) {
            System.out.println("Cache is fresh (" + cache.getFlightCacheAge() + "). Skipping sync.");
            return 0; // No sync needed
        }
        return syncNow();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AVIATIONSTACK API SYNC
    // ═══════════════════════════════════════════════════════════════════════

    private int syncFromApi() throws Exception {
        System.out.println("Starting Aviationstack API Sync...");
        AviationstackClient client = new AviationstackClient();
        String airportCode = AviationstackConfig.getDefaultAirport();

        // Batch request: 2 API calls for ALL flights (departures + arrivals)
        List<Flight> flights = client.fetchAllFlightsForAirport(airportCode);

        if (flights.isEmpty()) {
            throw new Exception("API returned 0 flights for " + airportCode);
        }

        // Batch write to DB
        flightDAO.batchUpsert(flights);
        int processedCount = flights.size(); // Show user the number of flights processed, not DB affected rows

        // Update in-memory cache (aggregation: one sync serves ALL users)
        cache.putFlights(flights);

        this.lastSyncTime = System.currentTimeMillis();
        this.recordsUpdated = processedCount;
        this.lastSyncSource = "API";

        System.out.println("API Sync Complete. Records: " + processedCount
                + " | Cache: " + cache.getCachedFlightCount() + " flights"
                + " | Quota remaining: " + cache.getRemainingQuota());
        return processedCount;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CSV FALLBACK SYNC
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Parse and sync a CSV file as a fallback without overwriting good data.
     * Expected CSV format:
     * FlightNum,Airline,Origin,Dest,DepTime,ArrTime,Status,Capacity,Eligibility
     * Date Format: yyyy-MM-dd HH:mm:ss
     */
    public int syncFromCSV(java.io.File csvFile) {
        System.out.println("Processing CSV Fallback: " + csvFile.getName());
        List<Flight> flights = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(csvFile))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                } // Skip header
                String[] parts = line.split(",");
                if (parts.length < 9)
                    continue;

                try {
                    Flight f = new Flight();
                    f.setFlightNumber(parts[0].trim());
                    f.setAirline(parts[1].trim());
                    f.setOrigin(parts[2].trim());
                    f.setDestination(parts[3].trim());
                    f.setScheduledDeparture(new Timestamp(sdf.parse(parts[4].trim()).getTime()));
                    f.setScheduledArrival(new Timestamp(sdf.parse(parts[5].trim()).getTime()));
                    f.setStatus(parts[6].trim().toUpperCase());
                    f.setPassengerCapacity(Integer.parseInt(parts[7].trim()));
                    f.setEligibilityPercentage(Double.parseDouble(parts[8].trim()));
                    flights.add(f);
                } catch (Exception e) {
                    System.err.println("Skipping malformed CSV line: " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Batch write to DB
        int updated = flightDAO.batchUpsert(flights);

        // Update cache
        cache.putFlights(flights);

        this.lastSyncTime = System.currentTimeMillis();
        this.recordsUpdated = updated;
        this.lastSyncSource = "CSV";
        return updated;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MOCK DATA GENERATOR (always available)
    // ═══════════════════════════════════════════════════════════════════════

    private int syncFromMockData() {
        System.out.println("Starting Mock Data Sync...");
        List<Flight> mockApiData = fetchMockAirportApi();
        int updated = flightDAO.batchUpsert(mockApiData);

        // Update cache
        cache.putFlights(mockApiData);

        this.lastSyncTime = System.currentTimeMillis();
        this.recordsUpdated = updated;
        this.lastSyncSource = "Mock";
        System.out.println("Mock Sync Complete. Records: " + updated);
        return updated;
    }

    private List<Flight> fetchMockAirportApi() {
        List<Flight> fetched = new ArrayList<>();
        long now = System.currentTimeMillis();

        String[] airlines = { "IndiGo", "Air India", "Vistara", "SpiceJet", "Emirates" };
        String[] origins = { "DEL", "BOM", "BLR", "DXB", "LHR" };
        String[] destinations = { "DEL", "BOM", "BLR", "DXB", "SIN" };
        String[] terminals = { "1", "2", "3", "I", "" };
        String[] gates = { "A1", "B4", "C7", "D11", "E2", "" };

        for (int i = 0; i < 15; i++) {
            Flight f = new Flight();
            f.setFlightNumber(airlines[i % airlines.length].substring(0, 2).toUpperCase() + "-" + (100 + i * 17));
            f.setAirline(airlines[i % airlines.length]);

            boolean isDeparture = (i % 2 == 0);
            f.setOrigin(isDeparture ? "HYD" : origins[i % origins.length]);
            f.setDestination(isDeparture ? destinations[i % destinations.length] : "HYD");

            long offset = (i * 15 * 60 * 1000L) - (60 * 60 * 1000L);
            f.setScheduledDeparture(new Timestamp(now + offset));
            f.setScheduledArrival(new Timestamp(now + offset + (2 * 60 * 60 * 1000L)));

            f.setStatus(offset < 0 ? "DEPARTED" : offset < (30 * 60 * 1000L) ? "BOARDING" : "SCHEDULED");

            f.setPassengerCapacity(180 + (i * 10));
            f.setEligibilityPercentage(0.12 + (i * 0.01));

            // Enriched mock data (simulating what the API would return)
            f.setTerminal(terminals[i % terminals.length]);
            f.setGate(gates[i % gates.length]);
            f.setDelayMinutes(i % 4 == 0 ? (15 + i * 5) : 0); // Some flights delayed

            fetched.add(f);
        }
        return fetched;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REFERENCE DATA SYNC (rarely called — static data)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Sync airport and airline reference data.
     * This is STATIC data — call once daily at most.
     */
    public void syncReferenceData() {
        if (cache.isReferenceDataFresh()) {
            System.out.println("Reference data is still fresh. Skipping.");
            return;
        }
        if (!cache.shouldUseApi()) {
            System.out.println("Skipping reference sync — API not available or quota low.");
            return;
        }

        try {
            AviationstackClient client = new AviationstackClient();
            cache.putAirportNames(client.fetchAirportNames());
            cache.putAirlineNames(client.fetchAirlineNames());
            System.out.println("Reference data synced: " + cache.getAirportNames().size()
                    + " airports, " + cache.getAirlineNames().size() + " airlines.");
        } catch (Exception e) {
            System.err.println("Reference sync failed: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATUS & METRICS
    // ═══════════════════════════════════════════════════════════════════════

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public int getLastRecordsUpdatedCount() {
        return recordsUpdated;
    }

    public String getLastSyncSource() {
        return lastSyncSource;
    }

    public String getLastSyncError() {
        return lastSyncError;
    }

    /** Returns a human-readable status summary for display in UI. */
    public String getStatusSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Source: ").append(lastSyncSource);
        sb.append(" | Cache: ").append(cache.getCachedFlightCount()).append(" flights");
        sb.append(" | Age: ").append(cache.getFlightCacheAge());
        if (AviationstackConfig.isApiConfigured()) {
            sb.append(" | Quota: ").append(cache.getRemainingQuota())
                    .append("/").append(AviationstackConfig.getMonthlyQuotaLimit());
        }
        if (lastSyncError != null) {
            sb.append(" | ⚠ ").append(lastSyncError);
        }
        return sb.toString();
    }
}
