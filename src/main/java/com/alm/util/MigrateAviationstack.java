package com.alm.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Runs the Aviationstack migration to add terminal, gate, and delay_minutes
 * columns to the flights table. Safe to run multiple times.
 * 
 * Checks information_schema before attempting ALTER to be compatible
 * with all MySQL versions (5.7+, 8.x, MariaDB).
 */
public class MigrateAviationstack {
    public static void main(String[] args) {
        System.out.println("Running Aviationstack DB migration...");
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Check which columns exist already
            boolean hasTerminal = columnExists(conn, "flights", "terminal");
            boolean hasGate = columnExists(conn, "flights", "gate");
            boolean hasDelay = columnExists(conn, "flights", "delay_minutes");

            // Add terminal column
            if (!hasTerminal) {
                stmt.execute("ALTER TABLE flights ADD COLUMN terminal VARCHAR(20) DEFAULT ''");
                System.out.println("  + Added column: terminal");
            } else {
                System.out.println("  = Column 'terminal' already exists");
            }

            // Add gate column
            if (!hasGate) {
                stmt.execute("ALTER TABLE flights ADD COLUMN gate VARCHAR(20) DEFAULT ''");
                System.out.println("  + Added column: gate");
            } else {
                System.out.println("  = Column 'gate' already exists");
            }

            // Add delay_minutes column
            if (!hasDelay) {
                stmt.execute("ALTER TABLE flights ADD COLUMN delay_minutes INT DEFAULT 0");
                System.out.println("  + Added column: delay_minutes");
            } else {
                System.out.println("  = Column 'delay_minutes' already exists");
            }

            // Update status enum to include ARRIVED and DIVERTED
            try {
                stmt.execute("ALTER TABLE flights MODIFY COLUMN status " +
                        "ENUM('SCHEDULED','BOARDING','DEPARTED','DELAYED','CANCELLED','ARRIVED','DIVERTED') " +
                        "DEFAULT 'SCHEDULED'");
                System.out.println("  + Updated status enum (added ARRIVED, DIVERTED)");
            } catch (Exception e) {
                System.err.println("  ! status enum: " + e.getMessage());
            }

            System.out.println("\nMigration complete!");
            System.out.println("You can now run the application with: mvn compile exec:java");

        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Force exit (don't let HikariCP keep the JVM alive)
        System.exit(0);
    }

    private static boolean columnExists(Connection conn, String table, String column) {
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, null, table, column);
            boolean exists = rs.next();
            rs.close();
            return exists;
        } catch (Exception e) {
            return false;
        }
    }
}
