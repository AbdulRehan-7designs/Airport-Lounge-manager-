package com.alm.dao;

import com.alm.util.DBConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SettingDAO {

    static {
        // Ensure table exists
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS `system_settings` (" +
                    "`setting_key` varchar(100) NOT NULL, " +
                    "`setting_value` text DEFAULT NULL, " +
                    "`description` varchar(255) DEFAULT NULL, " +
                    "PRIMARY KEY (`setting_key`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Seed defaults
            stmt.execute("INSERT IGNORE INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES "
                    +
                    "('BUSY_THRESHOLD_PCT', '90', 'Capacity percentage after which a lounge is marked as BUSY.')," +
                    "('AUTO_DENY_EXPIRED', 'true', 'Automatically deny requests if membership is expired.')," +
                    "('MAINTENANCE_MODE', 'false', 'Global toggle to put the system in read-only mode.')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String[]> getAll() {
        Map<String, String[]> map = new HashMap<>(); // key -> [value, description]
        String sql = "SELECT * FROM system_settings";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("setting_key"), new String[] {
                        rs.getString("setting_value"),
                        rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean update(String key, String value) {
        String sql = "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setString(2, key);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
