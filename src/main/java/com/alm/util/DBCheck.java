package com.alm.util;

import java.sql.*;

public class DBCheck {
    public static void main(String[] args) {
        System.out.println("Checking DB Connection and Table Structures...");
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connection successful!");

            DatabaseMetaData md = conn.getMetaData();
            String[] tables = { "users", "staff", "audit_logs", "lounges" };

            for (String table : tables) {
                System.out.println("\n--- Table: " + table + " ---");
                try (ResultSet rs = md.getColumns(null, null, table, null)) {
                    boolean found = false;
                    while (rs.next()) {
                        System.out
                                .println("  > " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
                        found = true;
                    }
                    if (!found)
                        System.out.println("  [Table not found or no columns]");
                }
            }

            System.out.println("\n--- Users (Last 5) ---");
            String sql = "SELECT id, username, role, active FROM users ORDER BY id DESC LIMIT 5";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    System.out.printf("ID: %d | User: %s | Role: %s | Active: %b%n",
                            rs.getLong("id"), rs.getString("username"), rs.getString("role"), rs.getInt("active") == 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
