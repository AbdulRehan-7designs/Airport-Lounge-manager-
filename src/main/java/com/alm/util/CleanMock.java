package com.alm.util;

import java.sql.Connection;
import java.sql.Statement;

public class CleanMock {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            int deleted = stmt.executeUpdate("DELETE FROM flights WHERE flight_number LIKE '%-%'");
            System.out.println("Deleted " + deleted + " mock flights.");

            // For good measure, delete any flight scheduled outside today's +/- 2 days
            int oldDel = stmt
                    .executeUpdate("DELETE FROM flights WHERE scheduled_departure < DATE_SUB(NOW(), INTERVAL 2 DAY)");
            System.out.println("Deleted " + oldDel + " old flights.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
