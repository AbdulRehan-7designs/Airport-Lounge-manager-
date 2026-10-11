package com.alm.dao;

import com.alm.model.AccessRequest;
import com.alm.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccessRequestDAO {

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * All requests joined with passenger name, lounge name, and provider name
     * so the JSP can display human-readable values without extra lookups.
     * Ordered newest first.
     */
    public List<AccessRequest> getAll() {
        return query(
                "SELECT r.request_id, r.passenger_id, r.lounge_id, r.provider_id, " +
                        "       r.status, r.decision_reason, r.request_time, r.decision_time, " +
                        "       p.full_name AS passenger_name, " +
                        "       l.name     AS lounge_name, " +
                        "       ap.provider_name " +
                        "FROM access_requests r " +
                        "JOIN passengers      p  ON p.passenger_id  = r.passenger_id " +
                        "JOIN lounges         l  ON l.lounge_id     = r.lounge_id " +
                        "LEFT JOIN access_providers ap ON ap.provider_id = r.provider_id " +
                        "ORDER BY r.request_time DESC",
                null);
    }

    /** Only PENDING requests — the primary queue for reception staff. */
    public List<AccessRequest> getPending() {
        return query(
                "SELECT r.request_id, r.passenger_id, r.lounge_id, r.provider_id, " +
                        "       r.status, r.decision_reason, r.request_time, r.decision_time, " +
                        "       p.full_name AS passenger_name, " +
                        "       l.name     AS lounge_name, " +
                        "       ap.provider_name " +
                        "FROM access_requests r " +
                        "JOIN passengers      p  ON p.passenger_id  = r.passenger_id " +
                        "JOIN lounges         l  ON l.lounge_id     = r.lounge_id " +
                        "LEFT JOIN access_providers ap ON ap.provider_id = r.provider_id " +
                        "WHERE r.status = 'PENDING' " +
                        "ORDER BY r.request_time ASC",
                null);
    }

    /** Count of pending requests — used by the dashboard widget. */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM access_requests WHERE status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public AccessRequest getById(int requestId) {
        String sql = "SELECT r.request_id, r.passenger_id, r.lounge_id, r.provider_id, " +
                "       r.status, r.decision_reason, r.request_time, r.decision_time, " +
                "       p.full_name AS passenger_name, " +
                "       l.name     AS lounge_name, " +
                "       ap.provider_name " +
                "FROM access_requests r " +
                "JOIN passengers      p  ON p.passenger_id  = r.passenger_id " +
                "JOIN lounges         l  ON l.lounge_id     = r.lounge_id " +
                "LEFT JOIN access_providers ap ON ap.provider_id = r.provider_id " +
                "WHERE r.request_id = ?";
        List<AccessRequest> result = query(sql, requestId);
        return result.isEmpty() ? null : result.get(0);
    }

    public List<AccessRequest> getByPassenger(long userId) {
        String sql = "SELECT r.request_id, r.passenger_id, r.lounge_id, r.provider_id, " +
                "       r.status, r.decision_reason, r.request_time, r.decision_time, " +
                "       p.full_name AS passenger_name, " +
                "       l.name     AS lounge_name, " +
                "       ap.provider_name " +
                "FROM access_requests r " +
                "JOIN passengers      p  ON p.passenger_id  = r.passenger_id " +
                "JOIN lounges         l  ON l.lounge_id     = r.lounge_id " +
                "LEFT JOIN access_providers ap ON ap.provider_id = r.provider_id " +
                "WHERE p.user_id = ? " +
                "ORDER BY r.request_time DESC";
        return queryLong(sql, userId);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Submit a new access request. */
    public boolean create(AccessRequest r) {
        String status = r.getStatus() != null ? r.getStatus() : "PENDING";
        String sql = "INSERT INTO access_requests (passenger_id, lounge_id, provider_id, status, ticket_class, elite_status, card_type, membership_type, amount_paid) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, r.getPassengerId());
            stmt.setInt(2, r.getLoungeId());
            if (r.getProviderId() != null)
                stmt.setInt(3, r.getProviderId());
            else
                stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, status);
            stmt.setString(5, r.getTicketClass());
            stmt.setString(6, r.getEliteStatus());
            stmt.setString(7, r.getCardType());
            stmt.setString(8, r.getMembershipType());
            if (r.getAmountPaid() != null)
                stmt.setDouble(9, r.getAmountPaid());
            else
                stmt.setNull(9, Types.DOUBLE);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next())
                        r.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Approve or deny a request.
     * Sets status, decision_reason, and decision_time = NOW().
     */
    public boolean decide(int requestId, String status, String reason) {
        String sql = "UPDATE access_requests " +
                "SET status = ?, decision_reason = ?, decision_time = NOW() " +
                "WHERE request_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, reason);
            stmt.setInt(3, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Internal helper ───────────────────────────────────────────────────────

    private List<AccessRequest> query(String sql, Integer singleParam) {
        return queryLong(sql, singleParam != null ? (long) singleParam : null);
    }

    private List<AccessRequest> queryLong(String sql, Long singleParam) {
        List<AccessRequest> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (singleParam != null)
                stmt.setLong(1, singleParam);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AccessRequest r = new AccessRequest(
                            rs.getInt("request_id"),
                            rs.getInt("passenger_id"),
                            rs.getInt("lounge_id"),
                            (Integer) rs.getObject("provider_id"),
                            rs.getString("status"),
                            rs.getString("decision_reason"),
                            rs.getTimestamp("request_time"),
                            rs.getTimestamp("decision_time"));
                    r.setPassengerName(rs.getString("passenger_name"));
                    r.setLoungeName(rs.getString("lounge_name"));
                    r.setProviderName(rs.getString("provider_name"));

                    // Intelligence fields (matching columns added via migration)
                    try {
                        r.setTicketClass(rs.getString("ticket_class"));
                        r.setEliteStatus(rs.getString("elite_status"));
                        r.setCardType(rs.getString("card_type"));
                        r.setMembershipType(rs.getString("membership_type"));
                        r.setAmountPaid(rs.getDouble("amount_paid"));
                        r.setCheckedInAt(rs.getTimestamp("checked_in_at"));
                        r.setCheckedOutAt(rs.getTimestamp("checked_out_at"));
                    } catch (SQLException ignored) {
                    } // Fallback if columns don't exist yet

                    list.add(r);
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkIn(int requestId) {
        String sql = "UPDATE access_requests SET checked_in_at = NOW() WHERE request_id = ? AND status = 'APPROVED'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkOut(int requestId) {
        String sql = "UPDATE access_requests SET checked_out_at = NOW() WHERE request_id = ? AND checked_in_at IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AccessRequest> getLiveGuestRoster() {
        return query(
                "SELECT r.*, p.full_name AS passenger_name, l.name AS lounge_name, ap.provider_name " +
                        "FROM access_requests r " +
                        "JOIN passengers p ON p.passenger_id = r.passenger_id " +
                        "JOIN lounges l ON l.lounge_id = r.lounge_id " +
                        "LEFT JOIN access_providers ap ON ap.provider_id = r.provider_id " +
                        "WHERE r.checked_in_at IS NOT NULL AND r.checked_out_at IS NULL " +
                        "ORDER BY r.checked_in_at DESC",
                null);
    }
}
