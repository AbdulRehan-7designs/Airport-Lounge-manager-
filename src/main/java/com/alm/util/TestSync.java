package com.alm.util;

import com.alm.service.FlightSyncService;

public class TestSync {
    public static void main(String[] args) {
        System.out.println("Starting Test Sync...");
        FlightSyncService service = new FlightSyncService();
        int updated = service.syncNow();
        System.out.println("Rows updated: " + updated);
        System.out.println("Error: " + service.getLastSyncError());
        System.exit(0);
    }
}
