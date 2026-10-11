package com.alm.dao;

import com.alm.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuditDAO {

    public List<Map<String, Object>> getRecentActivity(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name AS staff_name " +
                "FROM audit_logs a " +
                "JOIN staff s ON s.staff_id = a.staff_id " +
                "ORDER BY a.created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", rs.getInt("audit_id"));
                    map.put("staffName", rs.getString("staff_name"));
                    map.put("actionType", rs.getString("action_type"));
                    map.put("entityName", rs.getString("entity_name"));
                    map.put("description", rs.getString("description"));
                    map.put("createdAt", rs.getTimestamp("created_at"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void log(int staffId, String action, String entity, Integer entityId, String description) {
        String sql = "INSERT INTO audit_logs (staff_id, action_type, entity_name, entity_id, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            stmt.setString(2, action);
            stmt.setString(3, entity);
            if (entityId != null)
                stmt.setInt(4, entityId);
            else
                stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
