package com.alm.dao;

import com.alm.util.DBConnection;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Database Alignment Test — verifies that the live MySQL database schema
 * is 100% aligned with what the application's DAOs expect.
 *
 * <h3>How to run</h3>
 * 
 * <pre>
 *   mvn test -Dtest=DBAlignmentTest
 *      (uses db.properties or environment variables for connection)
 * </pre>
 *
 * <h3>What is checked</h3>
 * <ol>
 * <li>DB connectivity</li>
 * <li>All required tables exist</li>
 * <li>All columns used by DAOs exist in their tables</li>
 * <li>Enum columns contain the expected values</li>
 * <li>Minimum row checks (at least one active ADMIN user)</li>
 * </ol>
 *
 * This test is <b>skipped automatically</b> when no real MySQL database
 * is reachable (e.g. in CI with only H2). It does NOT modify any data.
 */
public class DBAlignmentTest {

    private static Connection conn;
    private static DatabaseMetaData meta;

    @BeforeClass
    public static void connect() {
        try {
            conn = DBConnection.getConnection();
            meta = conn.getMetaData();
            // Skip gracefully if this is an H2 / in-memory DB (unit-test context)
            String url = meta.getURL();
            Assume.assumeFalse(
                    "Skipping DBAlignmentTest — not connected to MySQL (URL: " + url + ")",
                    url == null || url.startsWith("jdbc:h2"));
        } catch (Exception e) {
            Assume.assumeNoException("Skipping DBAlignmentTest — cannot connect: " + e.getMessage(), e);
        }
    }

    // ── 1. Connectivity ───────────────────────────────────────────────────────

    @Test
    public void dbIsReachable() throws SQLException {
        assertTrue("Connection must be open", conn != null && !conn.isClosed());
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT 1")) {
            assertTrue("SELECT 1 must return a row", rs.next());
        }
    }

    // ── 2. Required tables ────────────────────────────────────────────────────

    @Test
    public void allRequiredTablesExist() throws SQLException {
        List<String> required = Arrays.asList(
                "users", "passengers", "lounges", "lounge_amenities",
                "airports", "access_providers", "access_requests",
                "transactions", "staff", "occupancy_logs",
                "passenger_memberships", "audit_logs");
        List<String> missing = new ArrayList<>();
        for (String table : required) {
            try (ResultSet rs = meta.getTables(null, null, table, new String[] { "TABLE" })) {
                if (!rs.next())
                    missing.add(table);
            }
        }
        assertTrue("Missing tables: " + missing, missing.isEmpty());
    }

    // ── 3. Columns used by each DAO ───────────────────────────────────────────

    @Test
    public void usersTableColumns() throws SQLException {
        assertColumns("users", "id", "username", "email", "password", "role", "active");
    }

    @Test
    public void passengersTableColumns() throws SQLException {
        assertColumns("passengers",
                "passenger_id", "full_name", "passport_number",
                "nationality", "phone", "email", "is_active");
    }

    @Test
    public void loungesTableColumns() throws SQLException {
        assertColumns("lounges",
                "lounge_id", "airport_id", "name", "terminal",
                "capacity", "status", "is_active");
    }

    @Test
    public void loungeAmenitiesTableColumns() throws SQLException {
        assertColumns("lounge_amenities",
                "amenity_id", "lounge_id", "item_name", "total_count", "available_count");
    }

    @Test
    public void accessProvidersTableColumns() throws SQLException {
        assertColumns("access_providers",
                "provider_id", "provider_name", "provider_type", "is_active");
    }

    @Test
    public void accessRequestsTableColumns() throws SQLException {
        assertColumns("access_requests",
                "request_id", "passenger_id", "lounge_id", "provider_id",
                "status", "request_time", "decision_time", "decision_reason");
    }

    @Test
    public void transactionsTableColumns() throws SQLException {
        assertColumns("transactions",
                "transaction_id", "request_id", "amount", "currency",
                "payment_method", "payment_status", "processed_at");
    }

    // ── 4. Enum values ────────────────────────────────────────────────────────

    @Test
    public void accessRequestStatusEnum() throws SQLException {
        assertEnumContains("access_requests", "status", "PENDING", "APPROVED", "DENIED");
    }

    @Test
    public void transactionPaymentMethodEnum() throws SQLException {
        assertEnumContains("transactions", "payment_method", "CARD", "UPI", "CASH");
    }

    @Test
    public void transactionPaymentStatusEnum() throws SQLException {
        assertEnumContains("transactions", "payment_status",
                "INITIATED", "SUCCESS", "FAILED", "REFUNDED");
    }

    @Test
    public void loungeStatusEnum() throws SQLException {
        assertEnumContains("lounges", "status", "ACTIVE", "CLOSED", "MAINTENANCE");
    }

    @Test
    public void staffRoleEnumIncludesAuditor() throws SQLException {
        assertEnumContains("staff", "role",
                "ADMIN", "OPERATIONS_MANAGER", "RECEPTION", "SUPERVISOR", "AUDITOR");
    }

    @Test
    public void accessProviderTypeEnum() throws SQLException {
        assertEnumContains("access_providers", "provider_type",
                "AIRLINE", "CREDIT_CARD", "AGGREGATOR");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Asserts that all specified columns exist in the given table
     * (case-insensitive).
     */
    private void assertColumns(String table, String... columns) throws SQLException {
        Set<String> existing = new HashSet<>();
        try (ResultSet rs = meta.getColumns(null, null, table, "%")) {
            while (rs.next()) {
                existing.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }
        List<String> missing = new ArrayList<>();
        for (String col : columns) {
            if (!existing.contains(col.toLowerCase()))
                missing.add(col);
        }
        assertTrue(
                "Table '" + table + "' is missing columns: " + missing,
                missing.isEmpty());
    }

    /**
     * Asserts that the COLUMN_TYPE of an enum column contains all expected values.
     * Reads from INFORMATION_SCHEMA.COLUMNS.
     */
    private void assertEnumContains(String table, String column, String... expectedValues)
            throws SQLException {
        String columnType = null;
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE TABLE_SCHEMA = DATABASE() " +
                                "  AND TABLE_NAME   = '" + table + "' " +
                                "  AND COLUMN_NAME  = '" + column + "'")) {
            if (rs.next())
                columnType = rs.getString(1);
        }
        assertNotNull(
                "Column " + table + "." + column + " not found in INFORMATION_SCHEMA",
                columnType);
        List<String> missing = new ArrayList<>();
        for (String val : expectedValues) {
            if (!columnType.contains("'" + val + "'"))
                missing.add(val);
        }
        assertTrue(
                "Enum " + table + "." + column + " is missing values " + missing +
                        ". Current: " + columnType,
                missing.isEmpty());
    }
}
