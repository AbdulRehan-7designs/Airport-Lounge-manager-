package com.alm.dao;

import com.alm.model.Lounge;
import com.alm.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoungeDAO {

    public List<Lounge> getAllLounges() {
        List<Lounge> list = new ArrayList<>();
        String sql = "SELECT l.*, a.city, a.country FROM lounges l JOIN airports a ON l.airport_id = a.airport_id ORDER BY l.name";
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

    public Lounge getById(int loungeId) {
        String sql = "SELECT l.*, a.city, a.country FROM lounges l JOIN airports a ON l.airport_id = a.airport_id WHERE l.lounge_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loungeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Sum of capacity across ALL lounges (regardless of status). */
    public int getTotalCapacity() {
        String sql = "SELECT COALESCE(SUM(capacity), 0) FROM lounges WHERE is_active = 1";
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

    /** Count of lounges with status = 'ACTIVE'. */
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM lounges WHERE status = 'ACTIVE' AND is_active = 1";
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

    /** Total count of all lounges (active flag = 1). */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM lounges WHERE is_active = 1";
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

    private static Lounge mapRow(ResultSet rs) throws SQLException {
        Lounge l = new Lounge(
                rs.getInt("lounge_id"),
                rs.getInt("airport_id"),
                rs.getString("name"),
                rs.getString("terminal"),
                rs.getInt("capacity"),
                rs.getString("status"));

        try {
            String city = rs.getString("city");
            String country = rs.getString("country");
            if (city != null)
                l.setLocation(city + ", " + country);
            l.setActive(rs.getInt("is_active") == 1);
        } catch (SQLException e) {
            // Some specialized queries might not include these columns, ignore if missing
        }
        return l;
    }

    public boolean create(Lounge l) {
        String sql = "INSERT INTO lounges (airport_id, name, terminal, capacity, status, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, l.getAirportId());
            stmt.setString(2, l.getName());
            stmt.setString(3, l.getTerminal());
            stmt.setInt(4, l.getCapacity());
            stmt.setString(5, l.getStatus() != null ? l.getStatus() : "ACTIVE");
            stmt.setInt(6, l.isActive() ? 1 : 1);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
