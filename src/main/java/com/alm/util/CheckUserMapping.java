package com.alm.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckUserMapping {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            System.out.println("--- USERS ---");
            ResultSet rs = stmt.executeQuery("SELECT user_id, username, role FROM users");
            while (rs.next()) {
                System.out.println("U_ID:" + rs.getInt("user_id") + " Name:" + rs.getString("username") + " Role:"
                        + rs.getString("role"));
            }
            rs.close();

            System.out.println("\n--- PASSENGERS ---");
            ResultSet rs2 = stmt.executeQuery("SELECT passenger_id, full_name, user_id, access_status FROM passengers");
            while (rs2.next()) {
                System.out.println("P_ID:" + rs2.getInt("passenger_id") + " Name:" + rs2.getString("full_name")
                        + " UserID:" + rs2.getLong("user_id") + " Status:" + rs2.getString("access_status"));
            }
            rs2.close();

            System.out.println("\n--- VISITS ---");
            ResultSet rs3 = stmt.executeQuery(
                    "SELECT visit_id, passenger_id, lounge_id, check_in_time, check_out_time FROM visits");
            while (rs3.next()) {
                System.out.println("V_ID:" + rs3.getInt("visit_id") + " P_ID:" + rs3.getInt("passenger_id") + " L_ID:"
                        + rs3.getInt("lounge_id") + " IN:" + rs3.getTimestamp("check_in_time") + " OUT:"
                        + rs3.getTimestamp("check_out_time"));
            }
            rs3.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
