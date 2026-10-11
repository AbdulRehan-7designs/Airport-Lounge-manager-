package com.alm.dao;

import com.alm.util.DBConnection;
import com.alm.model.SessionToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

public class SessionTokenDAO {
    private static final Logger logger = LoggerFactory.getLogger(SessionTokenDAO.class);

    public String createTokenForPassenger(int passengerId, int validityMinutes) {
        String tokenString = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        String sql = "INSERT INTO session_tokens (passenger_id, token_string, expiry_time) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE))";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, passengerId);
            stmt.setString(2, tokenString);
            stmt.setInt(3, validityMinutes);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return tokenString;
            }
        } catch (SQLException e) {
            logger.error("Error creating session token for passenger {}: {}", passengerId, e.getMessage());
        }
        return null;
    }

    public SessionToken validateToken(String tokenString) {
        String sql = "SELECT * FROM session_tokens WHERE token_string = ? AND is_used = 0 AND expiry_time > NOW()";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tokenString);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new SessionToken(
                            rs.getInt("token_id"),
                            rs.getInt("passenger_id"),
                            rs.getString("token_string"),
                            rs.getTimestamp("generated_at"),
                            rs.getTimestamp("expiry_time"),
                            rs.getBoolean("is_used"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error validating session token: {}", e.getMessage());
        }
        return null;
    }

    public boolean markTokenUsed(int tokenId) {
        String sql = "UPDATE session_tokens SET is_used = 1 WHERE token_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tokenId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking token used {}: {}", tokenId, e.getMessage());
        }
        return false;
    }
}
