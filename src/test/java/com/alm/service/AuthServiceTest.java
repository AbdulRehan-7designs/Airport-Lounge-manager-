package com.alm.service;

import com.alm.model.User;
import com.alm.util.PasswordUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuthServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:authdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";

    @BeforeClass
    public static void switchToH2() {
        // Override DB settings BEFORE any DBConnection call is made.
        com.alm.util.DBConnection.closePool();
        System.setProperty("db.driver", "org.h2.Driver");
        System.setProperty("db.url", H2_URL);
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");
        // Eagerly register H2 driver so it is available for DriverManager
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not found on test classpath", e);
        }
    }

    @org.junit.AfterClass
    public static void restoreProperties() {
        com.alm.util.DBConnection.closePool();
        System.clearProperty("db.driver");
        System.clearProperty("db.url");
        System.clearProperty("db.user");
        System.clearProperty("db.password");
    }

    @Before
    public void setUp() {
        try (java.sql.Connection conn = com.alm.util.DBConnection.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(100) NOT NULL UNIQUE, " +
                    "email VARCHAR(150), " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL, " +
                    "active INT DEFAULT 1)");
            stmt.executeUpdate("DELETE FROM users WHERE username = 'admin'");
            String hashedAdmin = PasswordUtil.hashPassword("admin");
            try (java.sql.PreparedStatement ps = conn
                    .prepareStatement(
                            "INSERT INTO users (username, email, password, role, active) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, "admin");
                ps.setString(2, null);
                ps.setString(3, hashedAdmin);
                ps.setString(4, "ADMIN");
                ps.setInt(5, 1);
                ps.executeUpdate();
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            org.junit.Assert.fail("Database initialization failed: " + e.getMessage());
        }
    }

    @Test
    public void testLoginSuccess() {
        AuthService authService = new AuthService();
        User user = authService.login("admin", "admin");
        assertNotNull("User should not be null for valid credentials", user);
        assertEquals("User role should be ADMIN", "ADMIN", user.getRole());
    }

    @Test
    public void testLoginFailure() {
        AuthService authService = new AuthService();
        User user = authService.login("admin", "wrongpassword");
        assertNull("User should be null for invalid credentials", user);
    }
}
