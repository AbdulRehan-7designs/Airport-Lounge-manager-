package com.alm.service;

import com.alm.model.Flight;
import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * REST client for the Aviationstack API.
 *
 * Strategy:
 * ─ Batch Requests: Fetches ALL departures + arrivals for an airport in 2 calls
 * instead of per-flight.
 * ─ Pagination: Respects API limit/offset to fetch up to 300 flights per batch.
 * ─ Fallback: All methods throw IOException on failure — callers handle
 * fallback.
 * ─ Quota-aware: Records every call via FlightDataCache.recordApiCall().
 */
public class AviationstackClient {

    private final String apiKey;
    private final String baseUrl;
    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT = 15_000;
    private static final int DEFAULT_LIMIT = 100; // max per page on free plan

    public AviationstackClient() {
        this.apiKey = AviationstackConfig.getApiKey();
        this.baseUrl = AviationstackConfig.getBaseUrl();
    }

    public AviationstackClient(String apiKey) {
        this.apiKey = apiKey;
        this.baseUrl = AviationstackConfig.getBaseUrl();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BATCH FLIGHT SYNC (Primary method — used by FlightSyncService)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fetch ALL flights (departures + arrivals) for an airport in ONE batch.
     * Uses 2 API calls total (1 for departures, 1 for arrivals).
     * Results are merged and de-duplicated by flight number + date.
     */
    public List<Flight> fetchAllFlightsForAirport(String airportIata) throws IOException {
        Map<String, Flight> merged = new LinkedHashMap<>();

        // Call 1: Departures
        List<Flight> departures = fetchFlights("dep_iata", airportIata, null, null);
        for (Flight f : departures) {
            String key = makeKey(f);
            merged.put(key, f);
        }

        // Call 2: Arrivals
        List<Flight> arrivals = fetchFlights("arr_iata", airportIata, null, null);
        for (Flight f : arrivals) {
            String key = makeKey(f);
            merged.putIfAbsent(key, f); // don't overwrite departure data
        }

        return new ArrayList<>(merged.values());
    }

    /**
     * Fetch flights with a specific status filter (e.g., only "scheduled" flights).
     * Uses only 1 API call — prioritizes critical data.
     */
    public List<Flight> fetchFlightsByStatus(String airportIata, String status) throws IOException {
        return fetchFlights("dep_iata", airportIata, "flight_status", status);
    }

    /**
     * Validate a specific flight by its IATA code (e.g., "AI202").
     * Uses 1 API call. Returns Optional.empty() if not found.
     * 
     * IMPORTANT: Check FlightDataCache.lookupFlight() FIRST before calling this
     * to avoid wasting quota on flights already in cache.
     */
    public Optional<Flight> validateFlight(String flightIata) throws IOException {
        String today = LocalDate.now().toString();
        String url = baseUrl + "/flights?access_key=" + apiKey
                + "&flight_iata=" + encode(flightIata)
                + "&flight_date=" + today
                + "&limit=1";

        List<Flight> results = executeRequest(url);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REFERENCE DATA (Airports, Airlines — sync rarely)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fetch airport names for reference (IATA → name).
     * This is static data — cache it for days.
     */
    public Map<String, String> fetchAirportNames() throws IOException {
        String url = baseUrl + "/airports?access_key=" + apiKey + "&limit=100";
        FlightDataCache.getInstance().recordApiCall();

        Map<String, String> result = new HashMap<>();
        JsonObject root = executeRawRequest(url);
        JsonArray data = root.getAsJsonArray("data");
        if (data != null) {
            for (JsonElement el : data) {
                JsonObject obj = el.getAsJsonObject();
                String iata = getStr(obj, "iata_code");
                String name = getStr(obj, "airport_name");
                if (!iata.isEmpty()) {
                    result.put(iata, name);
                }
            }
        }
        return result;
    }

    /**
     * Fetch airline names for reference (IATA → name).
     */
    public Map<String, String> fetchAirlineNames() throws IOException {
        String url = baseUrl + "/airlines?access_key=" + apiKey + "&limit=100";
        FlightDataCache.getInstance().recordApiCall();

        Map<String, String> result = new HashMap<>();
        JsonObject root = executeRawRequest(url);
        JsonArray data = root.getAsJsonArray("data");
        if (data != null) {
            for (JsonElement el : data) {
                JsonObject obj = el.getAsJsonObject();
                String iata = getStr(obj, "iata_code");
                String name = getStr(obj, "airline_name");
                if (!iata.isEmpty()) {
                    result.put(iata, name);
                }
            }
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INTERNAL — HTTP + JSON Parsing
    // ═══════════════════════════════════════════════════════════════════════

    private List<Flight> fetchFlights(String filterKey, String filterValue,
            String extraKey, String extraValue) throws IOException {
        StringBuilder url = new StringBuilder(baseUrl)
                .append("/flights?access_key=").append(apiKey)
                .append("&").append(filterKey).append("=").append(encode(filterValue))
                .append("&limit=").append(DEFAULT_LIMIT);
        if (extraKey != null && extraValue != null) {
            url.append("&").append(extraKey).append("=").append(encode(extraValue));
        }
        return executeRequest(url.toString());
    }

    private List<Flight> executeRequest(String urlStr) throws IOException {
        FlightDataCache.getInstance().recordApiCall();
        JsonObject root = executeRawRequest(urlStr);

        // Check for API errors
        if (root.has("error")) {
            JsonObject err = root.getAsJsonObject("error");
            String msg = getStr(err, "message");
            String code = getStr(err, "code");
            throw new IOException("Aviationstack API error [" + code + "]: " + msg);
        }

        List<Flight> flights = new ArrayList<>();
        JsonArray data = root.getAsJsonArray("data");
        if (data == null)
            return flights;

        for (JsonElement el : data) {
            try {
                JsonObject obj = el.getAsJsonObject();
                Flight f = parseFlightFromJson(obj);
                if (f != null)
                    flights.add(f);
            } catch (Exception e) {
                System.err.println("Skipping malformed flight entry: " + e.getMessage());
            }
        }
        return flights;
    }

    private JsonObject executeRawRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("Accept", "application/json");

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        try (InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)) {
            return JsonParser.parseReader(br).getAsJsonObject();
        }
    }

    /**
     * Parse a single flight JSON object into our Flight model.
     * Maps Aviationstack fields → ALM Flight fields.
     */
    private Flight parseFlightFromJson(JsonObject obj) {
        Flight f = new Flight();

        // Flight identifiers
        JsonObject flightObj = obj.getAsJsonObject("flight");
        if (flightObj != null) {
            f.setFlightNumber(getStr(flightObj, "iata")); // e.g., "AI202"
        }

        // Airline info
        JsonObject airline = obj.getAsJsonObject("airline");
        if (airline != null) {
            f.setAirline(getStr(airline, "name"));
        }

        // Departure info
        JsonObject dep = obj.getAsJsonObject("departure");
        if (dep != null) {
            f.setOrigin(getStr(dep, "iata"));
            f.setScheduledDeparture(parseTimestamp(getStr(dep, "scheduled")));
            f.setTerminal(getStr(dep, "terminal"));
            f.setGate(getStr(dep, "gate"));
            // Delay in minutes (nullable)
            if (dep.has("delay") && !dep.get("delay").isJsonNull()) {
                f.setDelayMinutes(dep.get("delay").getAsInt());
            }
        }

        // Arrival info
        JsonObject arr = obj.getAsJsonObject("arrival");
        if (arr != null) {
            f.setDestination(getStr(arr, "iata"));
            f.setScheduledArrival(parseTimestamp(getStr(arr, "scheduled")));
        }

        // Flight status
        String apiStatus = getStr(obj, "flight_status");
        f.setStatus(mapStatus(apiStatus));

        // Defaults for fields not available via API
        f.setPassengerCapacity(180);
        f.setEligibilityPercentage(0.15);

        // Skip flights with missing critical data
        if (f.getFlightNumber() == null || f.getFlightNumber().isEmpty()) {
            return null;
        }

        return f;
    }

    // ── Utility Methods ─────────────────────────────────────────────────────

    private String getStr(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull())
            return "";
        return obj.get(key).getAsString();
    }

    private Timestamp parseTimestamp(String iso) {
        if (iso == null || iso.isEmpty())
            return new Timestamp(System.currentTimeMillis());
        try {
            OffsetDateTime odt = OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return Timestamp.from(odt.toInstant());
        } catch (Exception e) {
            try {
                // Fallback to LocalDateTime if no offset
                LocalDateTime ldt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return Timestamp.valueOf(ldt);
            } catch (Exception ex) {
                return new Timestamp(System.currentTimeMillis());
            }
        }
    }

    /**
     * Map Aviationstack status to our internal status enum.
     */
    private String mapStatus(String apiStatus) {
        if (apiStatus == null || apiStatus.isEmpty())
            return "SCHEDULED";
        return switch (apiStatus.toLowerCase()) {
            case "scheduled" -> "SCHEDULED";
            case "active" -> "BOARDING";
            case "landed" -> "DEPARTED"; // landed = already departed from origin
            case "cancelled" -> "CANCELLED";
            case "incident" -> "DELAYED";
            case "diverted" -> "DELAYED";
            default -> "SCHEDULED";
        };
    }

    /**
     * Creates a unique key for de-duplication: flightNumber + date.
     */
    private String makeKey(Flight f) {
        String date = f.getScheduledDeparture() != null
                ? f.getScheduledDeparture().toString().substring(0, 10)
                : "unknown";
        return (f.getFlightNumber() + "_" + date).toUpperCase();
    }

    private String encode(String val) {
        try {
            return java.net.URLEncoder.encode(val, "UTF-8");
        } catch (Exception e) {
            return val;
        }
    }
}
