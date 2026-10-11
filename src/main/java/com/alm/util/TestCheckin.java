package com.alm.util;

import com.alm.dao.PassengerDAO;

public class TestCheckin {
    public static void main(String[] args) {
        PassengerDAO dao = new PassengerDAO();
        try {
            System.out.println("Starting checkin test...");
            boolean result = dao.transactionalCheckIn(1, 1, "E22", "COMPLIMENTARY_AIRLINE");
            System.out.println("Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Test Complete.");
            System.exit(0);
        }
    }
}
