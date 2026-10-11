package com.alm.service;

import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration reader for the Aviationstack API integration.
 * Loads from api.properties on the classpath with fallback defaults.
 *
 * Priority: System Property > api.properties > Default
 */
public final class AviationstackConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AviationstackConfig.class.getResourceAsStream("/api.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (Exception ignored) {
        }
    }

    private AviationstackConfig() {
    }

    private static String get(String key, String defaultVal) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isEmpty())
            return sys;
        return PROPS.getProperty(key, defaultVal);
    }

    private static int getInt(String key, int defaultVal) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultVal)));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    // ── API Credentials ─────────────────────────────────────────────────────

    /** Returns the API key, or empty string if not configured. */
    public static String getApiKey() {
        return get("aviationstack.api.key", "");
    }

    /** Returns true if a valid API key is configured. */
    public static boolean isApiConfigured() {
        String key = getApiKey();
        return key != null && !key.isEmpty() && !key.equals("YOUR_KEY_HERE");
    }

    public static String getBaseUrl() {
        return get("aviationstack.base.url", "http://api.aviationstack.com/v1");
    }

    public static String getDefaultAirport() {
        return get("aviationstack.default.airport", "HYD");
    }

    // ── Sync Intervals (minutes) ────────────────────────────────────────────

    /** Real-time flight status sync interval (default: 30 minutes). */
    public static int getFlightStatusInterval() {
        return getInt("sync.interval.flight.status", 30);
    }

    /** Schedule sync interval (default: 1440 minutes = daily). */
    public static int getScheduleInterval() {
        return getInt("sync.interval.schedules", 1440);
    }

    /** Reference data sync interval (default: 10080 minutes = weekly). */
    public static int getReferenceInterval() {
        return getInt("sync.interval.reference", 10080);
    }

    // ── Cache Settings ──────────────────────────────────────────────────────

    /** Flight cache TTL (default: 15 minutes). */
    public static int getFlightCacheTTL() {
        return getInt("cache.ttl.flights", 15);
    }

    /** Reference data cache TTL (default: 1440 minutes = daily). */
    public static int getReferenceCacheTTL() {
        return getInt("cache.ttl.reference", 1440);
    }

    /** Max flights in cache (default: 500). */
    public static int getMaxCachedFlights() {
        return getInt("cache.max.flights", 500);
    }

    // ── Quota Management ────────────────────────────────────────────────────

    /** Monthly API call limit based on plan (default: 100 for free plan). */
    public static int getMonthlyQuotaLimit() {
        return getInt("quota.monthly.limit", 100);
    }

    /** Whether to auto-fallback to mock data when quota is low. */
    public static boolean isFallbackEnabled() {
        return Boolean.parseBoolean(get("quota.fallback.enabled", "true"));
    }

    /** Remaining calls threshold before triggering fallback. */
    public static int getFallbackThreshold() {
        return getInt("quota.fallback.threshold", 10);
    }
}
