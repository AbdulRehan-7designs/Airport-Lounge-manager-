package com.alm.dao;

import com.alm.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for all receptionist service operations:
 * - Premium services (shower, pod, meeting room, etc.)
 * - Visit guests
 * - Waitlist
 * - Complaints
 * - Visit extensions
 * - Seat changes
 */
public class ReceptionServiceDAO {

    // ── PREMIUM SERVICES ─────────────────────────────────────────────────────

    /** Get all available services for a lounge */
    public List<Map<String, Object>> getServicesForLounge(int loungeId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM lounge_services WHERE lounge_id = ? AND is_available = TRUE ORDER BY category, service_name";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loungeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("service_id", rs.getInt("service_id"));
                    m.put("service_name", rs.getString("service_name"));
                    m.put("category", rs.getString("category"));
                    m.put("price", rs.getBigDecimal("price"));
                    m.put("description", rs.getString("description"));
                    m.put("max_slots", rs.getInt("max_slots"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Add a service to a visit (creates billing line item) */
    public boolean addServiceToVisit(int visitId, int serviceId, int passengerId, int qty, double totalPrice) {
        String sql = "INSERT INTO visit_services (visit_id, service_id, passenger_id, quantity, total_price) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitId);
            stmt.setInt(2, serviceId);
            stmt.setInt(3, passengerId);
            stmt.setInt(4, qty);
            stmt.setDouble(5, totalPrice);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Get all services booked for a visit */
    public List<Map<String, Object>> getServicesForVisit(int visitId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT vs.*, ls.service_name, ls.category FROM visit_services vs " +
                "JOIN lounge_services ls ON vs.service_id = ls.service_id WHERE vs.visit_id = ? ORDER BY vs.requested_at";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("vs_id", rs.getInt("vs_id"));
                    m.put("service_name", rs.getString("service_name"));
                    m.put("category", rs.getString("category"));
                    m.put("quantity", rs.getInt("quantity"));
                    m.put("total_price", rs.getBigDecimal("total_price"));
                    m.put("status", rs.getString("status"));
                    m.put("requested_at", rs.getTimestamp("requested_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateServiceStatus(int vsId, String status) {
        String sql = "UPDATE visit_services SET status = ? WHERE vs_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, vsId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Get active service requests for the entire lounge (live queue) */
    public List<Map<String, Object>> getActiveServiceQueue(int loungeId) {
        List<Map<String, Object>> list = new ArrayList<>();
        // Query to get all pending/in-progress services for passengers currently
        // checked into the lounge
        String sql = "SELECT vs.vs_id, p.full_name, p.assigned_seat, ls.service_name, vs.status " +
                "FROM visit_services vs " +
                "JOIN visits v ON vs.visit_id = v.visit_id " +
                "JOIN passengers p ON v.passenger_id = p.passenger_id " +
                "JOIN lounge_services ls ON vs.service_id = ls.service_id " +
                "WHERE v.lounge_id = ? AND v.check_out_time IS NULL " +
                "AND vs.status IN ('PENDING', 'IN_PROGRESS') " +
                "ORDER BY vs.requested_at ASC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loungeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("vs_id", rs.getInt("vs_id"));
                    m.put("full_name", rs.getString("full_name"));
                    m.put("assigned_seat", rs.getString("assigned_seat"));
                    m.put("service_name", rs.getString("service_name"));
                    m.put("status", rs.getString("status"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── GUEST MANAGEMENT ─────────────────────────────────────────────────────

    public boolean addGuestToVisit(int visitId, int passengerId, String guestName, String guestDoc, double fee) {
        String sql = "INSERT INTO visit_guests (visit_id, passenger_id, guest_name, guest_document, fee) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitId);
            stmt.setInt(2, passengerId);
            stmt.setString(3, guestName);
            stmt.setString(4, guestDoc);
            stmt.setDouble(5, fee);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getGuestsForVisit(int visitId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM visit_guests WHERE visit_id = ? ORDER BY added_at";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("guest_id", rs.getInt("guest_id"));
                    m.put("guest_name", rs.getString("guest_name"));
                    m.put("guest_document", rs.getString("guest_document"));
                    m.put("fee", rs.getBigDecimal("fee"));
                    m.put("added_at", rs.getTimestamp("added_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── WAITLIST MANAGEMENT ──────────────────────────────────────────────────

    public boolean addToWaitlist(int passengerId, int loungeId, String priority) {
        String sql = "INSERT INTO waitlist (passenger_id, lounge_id, priority) VALUES (?,?,?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, passengerId);
            stmt.setInt(2, loungeId);
            stmt.setString(3, priority);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getWaitlist(int loungeId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT w.*, p.full_name FROM waitlist w JOIN passengers p ON w.passenger_id = p.passenger_id " +
                "WHERE w.lounge_id = ? AND w.status = 'WAITING' ORDER BY FIELD(w.priority,'VIP','HIGH','NORMAL'), w.requested_at";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loungeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("waitlist_id", rs.getInt("waitlist_id"));
                    m.put("passenger_id", rs.getInt("passenger_id"));
                    m.put("full_name", rs.getString("full_name"));
                    m.put("priority", rs.getString("priority"));
                    m.put("requested_at", rs.getTimestamp("requested_at"));
                    m.put("status", rs.getString("status"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateWaitlistStatus(int waitlistId, String status) {
        String sql = "UPDATE waitlist SET status = ?, notified_at = CASE WHEN ? = 'NOTIFIED' THEN CURRENT_TIMESTAMP ELSE notified_at END WHERE waitlist_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, status);
            stmt.setInt(3, waitlistId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── COMPLAINTS ───────────────────────────────────────────────────────────

    public boolean logComplaint(int passengerId, int loungeId, Integer visitId, String category,
            String description, String severity, String reportedBy) {
        String sql = "INSERT INTO complaints (passenger_id, lounge_id, visit_id, category, description, severity, reported_by) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, passengerId);
            stmt.setInt(2, loungeId);
            if (visitId != null)
                stmt.setInt(3, visitId);
            else
                stmt.setNull(3, java.sql.Types.INTEGER);
            stmt.setString(4, category);
            stmt.setString(5, description);
            stmt.setString(6, severity);
            stmt.setString(7, reportedBy);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getOpenComplaints(int loungeId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.*, p.full_name FROM complaints c LEFT JOIN passengers p ON c.passenger_id = p.passenger_id "
                +
                "WHERE c.lounge_id = ? AND c.status IN ('OPEN','IN_PROGRESS') ORDER BY FIELD(c.severity,'CRITICAL','HIGH','MEDIUM','LOW'), c.reported_at DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loungeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("complaint_id", rs.getInt("complaint_id"));
                    m.put("full_name", rs.getString("full_name"));
                    m.put("category", rs.getString("category"));
                    m.put("description", rs.getString("description"));
                    m.put("severity", rs.getString("severity"));
                    m.put("status", rs.getString("status"));
                    m.put("reported_by", rs.getString("reported_by"));
                    m.put("reported_at", rs.getTimestamp("reported_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── ACTIVE VISITS ────────────────────────────────────────────────────────

    /** Get all currently active visits (not checked out) for a lounge */
    public List<Map<String, Object>> getActiveVisits(int loungeId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT v.visit_id, v.passenger_id, v.access_type, v.check_in_time, " +
                "p.full_name, p.assigned_seat, p.ticket_class, p.membership_type " +
                "FROM visits v JOIN passengers p ON v.passenger_id = p.passenger_id " +
                "WHERE v.lounge_id = ? AND v.check_out_time IS NULL ORDER BY v.check_in_time";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loungeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("visit_id", rs.getInt("visit_id"));
                    m.put("passenger_id", rs.getInt("passenger_id"));
                    m.put("full_name", rs.getString("full_name"));
                    m.put("assigned_seat", rs.getString("assigned_seat"));
                    m.put("access_type", rs.getString("access_type"));
                    m.put("ticket_class", rs.getString("ticket_class"));
                    m.put("membership_type", rs.getString("membership_type"));
                    m.put("check_in_time", rs.getTimestamp("check_in_time"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Find the active visit ID for a passenger */
    public Integer getActiveVisitId(int passengerId) {
        String sql = "SELECT visit_id FROM visits WHERE passenger_id = ? AND check_out_time IS NULL ORDER BY check_in_time DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, passengerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("visit_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── SEAT CHANGES ─────────────────────────────────────────────────────────

    public boolean changeSeat(int passengerId, String newSeat) {
        String sql = "UPDATE passengers SET assigned_seat = ? WHERE passenger_id = ? AND access_status = 'CHECKED_IN'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newSeat);
            stmt.setInt(2, passengerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── FLIGHT INFO ──────────────────────────────────────────────────────────

    public List<Map<String, Object>> getUpcomingFlights(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT f.flight_id, f.flight_number, f.airline, f.scheduled_departure as departure_time, f.scheduled_arrival as arrival_time, "
                +
                "f.origin, f.destination, f.gate as gate_number, f.status " +
                "FROM flights f WHERE f.scheduled_departure >= NOW() ORDER BY f.scheduled_departure LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("flight_id", rs.getInt("flight_id"));
                    m.put("flight_number", rs.getString("flight_number"));
                    m.put("airline", rs.getString("airline"));
                    m.put("departure_time", rs.getTimestamp("departure_time"));
                    m.put("origin", rs.getString("origin"));
                    m.put("destination", rs.getString("destination"));
                    m.put("gate_number", rs.getString("gate_number"));
                    m.put("status", rs.getString("status"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
