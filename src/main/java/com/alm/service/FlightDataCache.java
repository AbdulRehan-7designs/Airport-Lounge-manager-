package com.alm.service;

import com.alm.model.Flight;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory flight data cache with TTL-based expiry.
 * 
 * Design Principles:
 * ─ Aggregate Data: One API call fills the cache, all UI panels read from it.
 * ─ Thread-safe: Swing worker threads + timer threads can access concurrently.
 * ─ TTL-controlled: Stale data is automatically detected; callers decide
 * whether to refresh from API or serve stale data gracefully.
 * ─ Quota-friendly: Prevents redundant API calls by serving from cache.
 */
public final class FlightDataCache {

    private static final FlightDataCache INSTANCE = new FlightDataCache();

    /** Cache entry wrapper with timestamp. */
    private static class CacheEntry<T> {
        final T data;
        final long cachedAt; // epoch millis

        CacheEntry(T data) {
            this.data = data;
            this.cachedAt = System.currentTimeMillis();
        }

        boolean isExpired(int ttlMinutes) {
            return (System.currentTimeMillis() - cachedAt) > (ttlMinutes * 60_000L);
        }

        long getAgeSeconds() {
            return (System.currentTimeMillis() - cachedAt) / 1000;
        }
    }

    // ── Cached Data ─────────────────────────────────────────────────────────

    /** Flights keyed by flight IATA code (e.g., "AI-202") for instant lookups. */
    private final ConcurrentHashMap<String, Flight> flightsByNumber = new ConcurrentHashMap<>();

    /** Full list of cached flights (arrivals + departures for an airport). */
    private volatile CacheEntry<List<Flight>> cachedFlightList;

    /** Cached reference data: airport IATA → airport name. */
    private volatile CacheEntry<Map<String, String>> cachedAirportNames;

    /** Cached reference data: airline IATA → airline name. */
    private volatile CacheEntry<Map<String, String>> cachedAirlineNames;

    // ── Quota Tracking ──────────────────────────────────────────────────────

    private int apiCallsThisMonth = 0;
    private int apiCallsThisSession = 0;

    private FlightDataCache() {
    }

    public static FlightDataCache getInstance() {
        return INSTANCE;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FLIGHT DATA
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Store a batch of flights into the cache (replaces old data).
     * This is called ONCE per API sync — all consumers then read from cache.
     */
    public void putFlights(List<Flight> flights) {
        flightsByNumber.clear();
        for (Flight f : flights) {
            if (f.getFlightNumber() != null) {
                flightsByNumber.put(f.getFlightNumber().toUpperCase(), f);
            }
        }
        // Trim to max if needed
        int maxFlights = AviationstackConfig.getMaxCachedFlights();
        List<Flight> toStore = flights.size() > maxFlights
                ? flights.subList(0, maxFlights)
                : flights;
        cachedFlightList = new CacheEntry<>(new ArrayList<>(toStore));
    }

    /**
     * Get all cached flights, or null if cache is empty.
     */
    public List<Flight> getFlights() {
        CacheEntry<List<Flight>> entry = cachedFlightList;
        return entry != null ? entry.data : null;
    }

    /**
     * Fast lookup of a single flight by its IATA code (e.g., "AI-202").
     * Used by the reception desk to validate passenger flights instantly
     * without consuming another API call.
     */
    public Flight lookupFlight(String flightNumber) {
        if (flightNumber == null)
            return null;
        return flightsByNumber.get(flightNumber.toUpperCase().trim());
    }

    /**
     * Check if flight data is fresh enough (within TTL).
     */
    public boolean isFlightDataFresh() {
        CacheEntry<List<Flight>> entry = cachedFlightList;
        if (entry == null)
            return false;
        return !entry.isExpired(AviationstackConfig.getFlightCacheTTL());
    }

    /**
     * Returns true if we have ANY flight data (even stale) for fallback.
     */
    public boolean hasFlightData() {
        return cachedFlightList != null && cachedFlightList.data != null
                && !cachedFlightList.data.isEmpty();
    }

    /**
     * Get cache age in a human-readable format.
     */
    public String getFlightCacheAge() {
        CacheEntry<List<Flight>> entry = cachedFlightList;
        if (entry == null)
            return "No data";
        long secs = entry.getAgeSeconds();
        if (secs < 60)
            return secs + "s ago";
        if (secs < 3600)
            return (secs / 60) + "m ago";
        return (secs / 3600) + "h " + ((secs % 3600) / 60) + "m ago";
    }

    /**
     * Get flights filtered by status (e.g., "SCHEDULED", "BOARDING").
     * No API call — purely from cache.
     */
    public List<Flight> getFlightsByStatus(String status) {
        List<Flight> all = getFlights();
        if (all == null)
            return Collections.emptyList();
        return all.stream()
                .filter(f -> status.equalsIgnoreCase(f.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Get departures from the cached list.
     */
    public List<Flight> getDepartures(String airportCode) {
        List<Flight> all = getFlights();
        if (all == null)
            return Collections.emptyList();
        return all.stream()
                .filter(f -> airportCode.equalsIgnoreCase(f.getOrigin()))
                .collect(Collectors.toList());
    }

    /**
     * Get arrivals from the cached list.
     */
    public List<Flight> getArrivals(String airportCode) {
        List<Flight> all = getFlights();
        if (all == null)
            return Collections.emptyList();
        return all.stream()
                .filter(f -> airportCode.equalsIgnoreCase(f.getDestination()))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REFERENCE DATA (Airports, Airlines — very rarely changes)
    // ═══════════════════════════════════════════════════════════════════════

    public void putAirportNames(Map<String, String> data) {
        cachedAirportNames = new CacheEntry<>(new HashMap<>(data));
    }

    public Map<String, String> getAirportNames() {
        CacheEntry<Map<String, String>> entry = cachedAirportNames;
        return entry != null ? entry.data : Collections.emptyMap();
    }

    public boolean isReferenceDataFresh() {
        CacheEntry<Map<String, String>> entry = cachedAirportNames;
        if (entry == null)
            return false;
        return !entry.isExpired(AviationstackConfig.getReferenceCacheTTL());
    }

    public void putAirlineNames(Map<String, String> data) {
        cachedAirlineNames = new CacheEntry<>(new HashMap<>(data));
    }

    public Map<String, String> getAirlineNames() {
        CacheEntry<Map<String, String>> entry = cachedAirlineNames;
        return entry != null ? entry.data : Collections.emptyMap();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // QUOTA TRACKING
    // ═══════════════════════════════════════════════════════════════════════

    /** Record that an API call was made. */
    public void recordApiCall() {
        apiCallsThisMonth++;
        apiCallsThisSession++;
    }

    /** Record multiple API calls (batch endpoints). */
    public void recordApiCalls(int count) {
        apiCallsThisMonth += count;
        apiCallsThisSession += count;
    }

    public int getApiCallsThisMonth() {
        return apiCallsThisMonth;
    }

    public int getApiCallsThisSession() {
        return apiCallsThisSession;
    }

    public int getRemainingQuota() {
        return Math.max(0, AviationstackConfig.getMonthlyQuotaLimit() - apiCallsThisMonth);
    }

    /**
     * Check if we should avoid API calls to preserve quota.
     */
    public boolean isQuotaLow() {
        return getRemainingQuota() <= AviationstackConfig.getFallbackThreshold();
    }

    /**
     * Check if we should use the API (has quota + is configured).
     */
    public boolean shouldUseApi() {
        if (!AviationstackConfig.isApiConfigured())
            return false;
        if (AviationstackConfig.isFallbackEnabled() && isQuotaLow())
            return false;
        return true;
    }

    /** Reset quota counter (call at the start of each month). */
    public void resetMonthlyQuota() {
        apiCallsThisMonth = 0;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CACHE STATS (for Sync Center UI)
    // ═══════════════════════════════════════════════════════════════════════

    public int getCachedFlightCount() {
        return flightsByNumber.size();
    }

    /** Clear all cached data. */
    public void clearAll() {
        flightsByNumber.clear();
        cachedFlightList = null;
        cachedAirportNames = null;
        cachedAirlineNames = null;
    }
}
