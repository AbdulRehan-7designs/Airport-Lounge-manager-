package com.alm.init;

import com.alm.util.DBConnection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Applies schema DDL to the database on application startup so the schema
 * exists whenever the app runs. Uses CREATE TABLE IF NOT EXISTS (idempotent).
 */
@WebListener
public class SchemaInitializer implements ServletContextListener {

    private static final String SCHEMA_RESOURCE = "schema-init.sql";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ensureDatabaseExists();
        runSchemaInit();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }

    /** Optionally ensure database alm exists by connecting to server without DB and running CREATE DATABASE IF NOT EXISTS. */
    private void ensureDatabaseExists() {
        try {
            Connection conn = DBConnection.getConnection();
            conn.getCatalog();
            conn.close();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unknown database")) {
                try {
                    createDatabaseIfNeeded();
                } catch (Exception ignored) {
                    // runSchemaInit will fail with clear error if DB still missing
                }
            }
        }
    }

    private void createDatabaseIfNeeded() throws SQLException {
        String url = System.getenv("DB_URL");
        if (url == null) url = System.getProperty("db.url");
        if (url == null) {
            try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
                if (in != null) {
                    Properties p = new Properties();
                    p.load(in);
                    url = p.getProperty("db.url");
                }
            } catch (Exception ignored) {}
        }
        if (url == null || !url.contains("/alm")) return;
        String baseUrl = url.replaceAll("/alm.*$", "");
        if (baseUrl.equals(url)) return;
        String user = System.getenv("DB_USER");
        if (user == null) user = System.getProperty("db.user", "root");
        String password = System.getenv("DB_PASSWORD");
        if (password == null) password = System.getProperty("db.password", "");
        try (Connection c = DriverManager.getConnection(baseUrl, user, password);
             Statement s = c.createStatement()) {
            s.execute("CREATE DATABASE IF NOT EXISTS alm");
        }
    }

    private void runSchemaInit() {
        String sql;
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(SCHEMA_RESOURCE);
            if (in == null) return;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            return;
        }
        if (sql == null || sql.isBlank()) return;

        List<String> statements = splitStatements(sql);
        try (Connection conn = DBConnection.getConnection()) {
            for (String stmt : statements) {
                String t = stmt.trim();
                if (t.isEmpty() || t.startsWith("--")) continue;
                try (Statement s = conn.createStatement()) {
                    s.execute(stmt);
                } catch (SQLException e) {
                    if (e.getErrorCode() != 1061 && e.getErrorCode() != 1826 && e.getErrorCode() != 1022) {
                        System.err.println("[SchemaInitializer] Statement failed: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            // Do NOT throw — allow app to start even if DB is down (e.g. MySQL not running)
            System.err.println("[SchemaInitializer] Schema init failed (app will start): " + e.getMessage());
        }
    }

    private List<String> splitStatements(String sql) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inSingle = false, inDouble = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (!inSingle && !inDouble) {
                if (c == '\'') inSingle = true;
                else if (c == '"') inDouble = true;
                else if (c == ';') {
                    out.add(cur.toString().trim());
                    cur.setLength(0);
                    continue;
                }
            } else if (inSingle && c == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) inSingle = false;
            else if (inDouble && c == '"' && (i == 0 || sql.charAt(i - 1) != '\\')) inDouble = false;
            cur.append(c);
        }
        if (cur.length() > 0) out.add(cur.toString().trim());
        return out;
    }
}
