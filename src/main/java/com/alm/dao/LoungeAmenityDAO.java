package com.alm.dao;

import com.alm.model.LoungeAmenity;
import com.alm.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoungeAmenityDAO {

    public List<LoungeAmenity> getByLoungeId(int loungeId) {
        List<LoungeAmenity> list = new ArrayList<>();
        String sql = "SELECT amenity_id, lounge_id, item_name, total_count, available_count FROM lounge_amenities WHERE lounge_id = ? ORDER BY item_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loungeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new LoungeAmenity(
                            rs.getInt("amenity_id"),
                            rs.getInt("lounge_id"),
                            rs.getString("item_name"),
                            rs.getInt("total_count"),
                            rs.getInt("available_count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean add(LoungeAmenity a) {
        String sql = "INSERT INTO lounge_amenities (lounge_id, item_name, total_count, available_count) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a.getLoungeId());
            stmt.setString(2, a.getItemName());
            stmt.setInt(3, a.getTotalCount());
            stmt.setInt(4, a.getAvailableCount());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(LoungeAmenity a) {
        String sql = "UPDATE lounge_amenities SET item_name = ?, total_count = ?, available_count = ? WHERE amenity_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getItemName());
            stmt.setInt(2, a.getTotalCount());
            stmt.setInt(3, a.getAvailableCount());
            stmt.setInt(4, a.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
