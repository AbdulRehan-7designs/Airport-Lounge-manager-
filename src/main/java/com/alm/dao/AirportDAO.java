package com.alm.dao;

import com.alm.model.Airport;
import com.alm.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AirportDAO {

    public List<Airport> getAll() {
        List<Airport> list = new ArrayList<>();
        String sql = "SELECT * FROM airports ORDER BY name ASC";
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

    public boolean create(Airport a) {
        String sql = "INSERT INTO airports (name, iata_code, city, country, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getName());
            stmt.setString(2, a.getIataCode());
            stmt.setString(3, a.getCity());
            stmt.setString(4, a.getCountry());
            stmt.setBoolean(5, a.isActive());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleActive(int id, boolean active) {
        String sql = "UPDATE airports SET is_active = ? WHERE airport_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Airport mapRow(ResultSet rs) throws SQLException {
        return new Airport(
                rs.getInt("airport_id"),
                rs.getString("name"),
                rs.getString("iata_code"),
                rs.getString("city"),
                rs.getString("country"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at"));
    }
}
