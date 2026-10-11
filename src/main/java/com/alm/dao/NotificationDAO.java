package com.alm.dao;

import com.alm.model.Notification;
import com.alm.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    static {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS `notifications` (" +
                    "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "`user_id` BIGINT NOT NULL, " +
                    "`title` VARCHAR(255) NOT NULL, " +
                    "`message` TEXT NOT NULL, " +
                    "`type` VARCHAR(50) DEFAULT 'INFO', " +
                    "`is_read` BOOLEAN DEFAULT FALSE, " +
                    "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getByUser(long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("title"),
                            rs.getString("message"),
                            rs.getString("type"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getUnreadCount(long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addNotification(long userId, String title, String message, String type) {
        String sql = "INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, message);
            stmt.setString(4, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAsRead(long notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAllAsRead(long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
