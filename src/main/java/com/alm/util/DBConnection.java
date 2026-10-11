package com.alm.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides JDBC connections using a layered config:
 * 1. Environment variables (DB_URL, DB_USER, DB_PASSWORD, DB_DRIVER)
 * 2. System properties (db.url, db.user, db.password, db.driver)
 * 3. /db.properties on the classpath
 * 4. Hardcoded defaults (MySQL localhost/alm)
 *
 * Driver loading is deferred to getConnection() so that tests can override
 * system properties via @BeforeClass before any JDBC class is touched.
 */
public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/alm";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final Properties FILE_PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                FILE_PROPS.load(in);
            }
        } catch (Exception ignored) {
            // Optional file; fall through to env / system props / defaults
        }
    }

    private static String getDriver() {
        String d = System.getenv("DB_DRIVER");
        if (d == null || d.isEmpty())
            d = System.getProperty("db.driver");
        if (d == null || d.isEmpty())
            d = FILE_PROPS.getProperty("db.driver");
        return (d != null && !d.isEmpty()) ? d : DEFAULT_DRIVER;
    }

    private static String getUrl() {
        String url = System.getenv("DB_URL");
        if (url == null || url.isEmpty())
            url = System.getProperty("db.url");
        if (url == null || url.isEmpty())
            url = FILE_PROPS.getProperty("db.url");
        return (url != null && !url.isEmpty()) ? url : DEFAULT_URL;
    }

    private static String getUser() {
        String user = System.getenv("DB_USER");
        if (user == null)
            user = System.getProperty("db.user");
        if (user == null)
            user = FILE_PROPS.getProperty("db.user");
        return (user != null && !user.isEmpty()) ? user : DEFAULT_USER;
    }

    private static String getPassword() {
        String password = System.getenv("DB_PASSWORD");
        if (password == null)
            password = System.getProperty("db.password");
        if (password == null)
            password = FILE_PROPS.getProperty("db.password");
        return password != null ? password : DEFAULT_PASSWORD;
    }

    private static volatile com.zaxxer.hikari.HikariDataSource dataSource;

    /**
     * Returns a new JDBC connection. Driver class is loaded on each call so
     * that test @BeforeClass property overrides are respected even if this class
     * was loaded earlier.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            synchronized (DBConnection.class) {
                if (dataSource == null) {
                    String url = getUrl();
                    System.out.println("Connecting to: " + url + " as " + getUser());
                    com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
                    config.setJdbcUrl(url);
                    config.setUsername(getUser());
                    config.setPassword(getPassword());
                    config.setDriverClassName(getDriver());
                    config.setMaximumPoolSize(10);
                    config.setConnectionTimeout(5000); // Shorter timeout for faster fail feedback
                    dataSource = new com.zaxxer.hikari.HikariDataSource(config);
                }
            }
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("DB Connection Failed: " + e.getMessage());
            throw e;
        }
    }

    // For tests, if they need to reset the connection pool
    public static void closePool() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
