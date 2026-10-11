package com.alm.dao;

import com.alm.model.Passenger;
import com.alm.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAO {

    static {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Check if user_id column exists
            try {
                stmt.execute("SELECT user_id FROM passengers LIMIT 1");
            } catch (SQLException e) {
                stmt.execute("ALTER TABLE passengers ADD COLUMN user_id BIGINT DEFAULT NULL");
            }

            // Auto-patch passengers schema for Live Status Updates
            try {
                stmt.execute("SELECT access_status FROM passengers LIMIT 1");
            } catch (SQLException e) {
                stmt.execute("ALTER TABLE passengers " +
                        "ADD COLUMN membership_type varchar(100) DEFAULT NULL, " +
                        "ADD COLUMN ticket_class varchar(100) DEFAULT NULL, " +
                        "ADD COLUMN flight_time datetime DEFAULT NULL, " +
                        "ADD COLUMN access_status enum('ELIGIBLE', 'NOT_ELIGIBLE', 'CHECKED_IN') DEFAULT 'NOT_ELIGIBLE', "
                        +
                        "ADD COLUMN checked_in_lounge_id int(11) DEFAULT NULL, " +
                        "ADD COLUMN assigned_seat varchar(20) DEFAULT NULL, " +
                        "ADD COLUMN check_in_time datetime DEFAULT NULL");
            }

            // Auto-create session tokens table
            stmt.execute("CREATE TABLE IF NOT EXISTS session_tokens (" +
                    "token_id int(11) NOT NULL AUTO_INCREMENT, " +
                    "passenger_id int(11) NOT NULL, " +
                    "token_string varchar(64) NOT NULL, " +
                    "generated_at datetime DEFAULT current_timestamp(), " +
                    "expiry_time datetime NOT NULL, " +
                    "is_used tinyint(1) DEFAULT 0, " +
                    "PRIMARY KEY (token_id), " +
                    "UNIQUE KEY token_string (token_string)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<Passenger> getAllPassengers() {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT * "
                +
                "FROM passengers ORDER BY full_name";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Passenger findById(int id) {
        String sql = "SELECT * "
                +
                "FROM passengers WHERE passenger_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Passenger findByPassport(String passportNumber) {
        String sql = "SELECT * "
                +
                "FROM passengers WHERE passport_number = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, passportNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.Optional<Long> getUserIdByPassengerId(int passengerId) {
        String sql = "SELECT user_id FROM passengers WHERE passenger_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, passengerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long uid = rs.getLong("user_id");
                    return rs.wasNull() ? java.util.Optional.empty() : java.util.Optional.of(uid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return java.util.Optional.empty();
    }

    public Passenger findByUserId(long userId) {
        String sql = "SELECT * "
                +
                "FROM passengers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Passenger findByFullName(String fullName) {
        String sql = "SELECT * "
                +
                "FROM passengers WHERE full_name = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public boolean addPassenger(Passenger p) {
        String sql = "INSERT INTO passengers (full_name, passport_number, nationality, phone, email, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getFullName());
            stmt.setString(2, p.getPassportNumber());
            stmt.setString(3, p.getNationality());
            stmt.setString(4, p.getPhone());
            stmt.setString(5, p.getEmail());
            stmt.setBoolean(6, p.isActive());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next())
                        p.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            // Duplicate key (unique constraint) - return false instead of propagating
            String state = e.getSQLState();
            int code = e.getErrorCode();
            if (state != null && (state.startsWith("23") || "23505".equals(state) || "23000".equals(state)))
                return false;
            if (code == 23505 || code == 1062) // H2 duplicate, MySQL duplicate
                return false;
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassenger(Passenger p) {
        String sql = "UPDATE passengers SET full_name = ?, passport_number = ?, nationality = ?, " +
                "phone = ?, email = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE passenger_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getFullName());
            stmt.setString(2, p.getPassportNumber());
            stmt.setString(3, p.getNationality());
            stmt.setString(4, p.getPhone());
            stmt.setString(5, p.getEmail());
            stmt.setBoolean(6, p.isActive());
            stmt.setInt(7, p.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassengerLiveContext(int passengerId, String accessStatus, Integer loungeId,
            String assignedSeat) {
        String sql = "UPDATE passengers SET access_status = ?, checked_in_lounge_id = ?, assigned_seat = ?, " +
                "check_in_time = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE passenger_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accessStatus);
            if (loungeId != null) {
                stmt.setInt(2, loungeId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, assignedSeat);
            stmt.setInt(4, passengerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ACID Check-In Transaction ensuring seat allocation integrity.
     */
    public boolean transactionalCheckIn(int passengerId, int loungeId, String assignedSeat, String accessType)
            throws IllegalStateException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. One Active Visit Rule! (Data Integrity check out visits)
            String activeVisitSql = "SELECT visit_id FROM visits WHERE passenger_id = ? AND check_out_time IS NULL FOR UPDATE";
            try (PreparedStatement activeStmt = conn.prepareStatement(activeVisitSql)) {
                activeStmt.setInt(1, passengerId);
                try (ResultSet rs = activeStmt.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        throw new IllegalStateException("Passenger already has an active check-in (Active Visit ID: "
                                + rs.getInt(1) + "). Please Check-Out first!");
                    }
                }
            }

            // 2. Check if seat is already occupied in this specific lounge (Row Lock
            // concept)
            String verifySeatSql = "SELECT passenger_id FROM passengers WHERE checked_in_lounge_id = ? AND assigned_seat = ? AND access_status = 'CHECKED_IN' FOR UPDATE";
            try (PreparedStatement checkStmt = conn.prepareStatement(verifySeatSql)) {
                checkStmt.setInt(1, loungeId);
                checkStmt.setString(2, assignedSeat);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        throw new IllegalStateException(
                                "Seat " + assignedSeat + " is already occupied by Passenger ID " + rs.getInt(1) + "!");
                    }
                }
            }

            // 3. Write into Visits (Unified Engine integration)
            String insertVisitSql = "INSERT INTO visits (passenger_id, lounge_id, access_type, check_in_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement visitStmt = conn.prepareStatement(insertVisitSql)) {
                visitStmt.setInt(1, passengerId);
                visitStmt.setInt(2, loungeId);
                visitStmt.setString(3, accessType);
                visitStmt.executeUpdate();
            }

            // 4. Assign the seat internally
            String checkInSql = "UPDATE passengers SET access_status = 'CHECKED_IN', checked_in_lounge_id = ?, assigned_seat = ?, check_in_time = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE passenger_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(checkInSql)) {
                updateStmt.setInt(1, loungeId);
                updateStmt.setString(2, assignedSeat);
                updateStmt.setInt(3, passengerId);
                int rows = updateStmt.executeUpdate();

                if (rows == 0) {
                    conn.rollback();
                    throw new IllegalStateException("Could not lock passenger record for update.");
                }
            }

            // 5. Commit the check-in transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean checkoutPassenger(int passengerId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Close the open visit (stamp check_out_time)
            String closeVisitSql = "UPDATE visits SET check_out_time = CURRENT_TIMESTAMP WHERE passenger_id = ? AND check_out_time IS NULL";
            try (PreparedStatement visitStmt = conn.prepareStatement(closeVisitSql)) {
                visitStmt.setInt(1, passengerId);
                visitStmt.executeUpdate();
            }

            // 2. Clear passenger live context
            String sql = "UPDATE passengers SET access_status = 'ELIGIBLE', checked_in_lounge_id = NULL, " +
                    "assigned_seat = NULL, check_in_time = NULL, updated_at = CURRENT_TIMESTAMP WHERE passenger_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, passengerId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean deactivatePassenger(int id) {
        String sql = "UPDATE passengers SET is_active = 0, updated_at = CURRENT_TIMESTAMP WHERE passenger_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static Passenger mapRow(ResultSet rs) throws SQLException {
        Passenger p = new Passenger(
                rs.getInt("passenger_id"),
                rs.getString("full_name"),
                rs.getString("passport_number"),
                rs.getString("nationality"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getBoolean("is_active"));

        // Map new live extensions
        p.setMembershipType(rs.getString("membership_type"));
        p.setTicketClass(rs.getString("ticket_class"));
        p.setFlightTime(rs.getTimestamp("flight_time"));
        p.setAccessStatus(rs.getString("access_status"));

        int loungeId = rs.getInt("checked_in_lounge_id");
        if (!rs.wasNull()) {
            p.setCheckedInLoungeId(loungeId);
        }

        p.setAssignedSeat(rs.getString("assigned_seat"));
        p.setCheckInTime(rs.getTimestamp("check_in_time"));

        // Unified Engine extensions
        try {
            p.setBookingReference(rs.getString("booking_reference"));
            int fId = rs.getInt("flight_id");
            if (!rs.wasNull())
                p.setFlightId(fId);

            int mId = rs.getInt("membership_id");
            if (!rs.wasNull())
                p.setMembershipId(mId);

            p.setMembershipValid(rs.getBoolean("membership_valid"));
            p.setVisitCountThisMonth(rs.getInt("visit_count_this_month"));
        } catch (SQLException ignored) {
        }

        return p;
    }
}
