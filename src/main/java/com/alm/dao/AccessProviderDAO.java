package com.alm.dao;

import com.alm.model.AccessProvider;
import com.alm.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccessProviderDAO {

    public List<AccessProvider> getAll() {
        List<AccessProvider> list = new ArrayList<>();
        String sql = "SELECT provider_id, provider_name, provider_type, is_active FROM access_providers ORDER BY provider_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AccessProvider(
                        rs.getInt("provider_id"),
                        rs.getString("provider_name"),
                        rs.getString("provider_type"),
                        rs.getBoolean("is_active")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public AccessProvider getById(int id) {
        String sql = "SELECT provider_id, provider_name, provider_type, is_active FROM access_providers WHERE provider_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AccessProvider(rs.getInt("provider_id"), rs.getString("provider_name"),
                            rs.getString("provider_type"), rs.getBoolean("is_active"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(AccessProvider p) {
        String sql = "INSERT INTO access_providers (provider_name, provider_type, is_active) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getProviderName());
            stmt.setString(2, p.getProviderType());
            stmt.setBoolean(3, p.isActive());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(AccessProvider p) {
        String sql = "UPDATE access_providers SET provider_name = ?, provider_type = ?, is_active = ? WHERE provider_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getProviderName());
            stmt.setString(2, p.getProviderType());
            stmt.setBoolean(3, p.isActive());
            stmt.setInt(4, p.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
