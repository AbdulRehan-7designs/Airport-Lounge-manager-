package com.alm.util;

import java.sql.*;

/**
 * Quick diagnostic + fix tool for database state issues.
 */
public class DBDiagnostic {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("=== PASSENGER STATUS ===");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT passenger_id, full_name, access_status, checked_in_lounge_id, assigned_seat, is_active FROM passengers")) {
                while (rs.next()) {
                    System.out.printf("  ID=%d  Name=%-25s  Status=%-15s  Lounge=%s  Seat=%s  Active=%s%n",
                            rs.getInt("passenger_id"),
                            rs.getString("full_name"),
                            rs.getString("access_status"),
                            rs.getString("checked_in_lounge_id"),
                            rs.getString("assigned_seat"),
                            rs.getBoolean("is_active"));
                }
            }

            System.out.println("\n=== OPEN VISITS (check_out_time IS NULL) ===");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT * FROM visits WHERE check_out_time IS NULL")) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    StringBuilder sb = new StringBuilder("  ");
                    for (int i = 1; i <= cols; i++) {
                        sb.append(meta.getColumnName(i)).append("=").append(rs.getString(i)).append("  ");
                    }
                    System.out.println(sb);
                }
            }

            System.out.println("\n=== ALL VISITS ===");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM visits ORDER BY visit_id DESC LIMIT 10")) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    StringBuilder sb = new StringBuilder("  ");
                    for (int i = 1; i <= cols; i++) {
                        sb.append(meta.getColumnName(i)).append("=").append(rs.getString(i)).append("  ");
                    }
                    System.out.println(sb);
                }
            }

            // FIX: Close any orphaned open visits and reset passenger states to ELIGIBLE
            System.out.println("\n=== APPLYING FIX: Closing stale visits & resetting passengers ===");
            try (Statement stmt = conn.createStatement()) {
                int visits = stmt.executeUpdate(
                        "UPDATE visits SET check_out_time = CURRENT_TIMESTAMP WHERE check_out_time IS NULL");
                System.out.println("  Closed " + visits + " stale open visits.");
                int passengers = stmt.executeUpdate(
                        "UPDATE passengers SET access_status='ELIGIBLE', checked_in_lounge_id=NULL, assigned_seat=NULL, check_in_time=NULL WHERE access_status='CHECKED_IN'");
                System.out.println("  Reset " + passengers + " passengers from CHECKED_IN to ELIGIBLE.");
            }

            System.out.println("\n=== POST-FIX PASSENGER STATUS ===");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT passenger_id, full_name, access_status, checked_in_lounge_id, assigned_seat FROM passengers")) {
                while (rs.next()) {
                    System.out.printf("  ID=%d  Name=%-25s  Status=%-15s  Lounge=%s  Seat=%s%n",
                            rs.getInt("passenger_id"),
                            rs.getString("full_name"),
                            rs.getString("access_status"),
                            rs.getString("checked_in_lounge_id"),
                            rs.getString("assigned_seat"));
                }
            }

            System.out.println("\nDone! Database is now clean for fresh check-in testing.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
