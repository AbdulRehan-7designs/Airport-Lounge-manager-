package com.alm.ui.shell;

import com.alm.ui.theme.ALMTheme;

/**
 * Admin sidebar — strategic control-tower navigation.
 * Groups: Dashboard, Lounge Mgmt, Staff, Passengers, Access, Analytics,
 * Revenue, Complaints, Settings.
 */
public class AdminSidebar extends SidebarPanel {

    private static final Object[][] NAV_ITEMS = {
            // --- Core overview ---
            { "Dashboard", ALMTheme.ICON_HOME, "dashboard" },

            // --- Operations ---
            { "Lounges", ALMTheme.ICON_LOUNGE, "lounges" },
            { "Locations", ALMTheme.ICON_BUILDING, "locations" },
            { "Front Desk", ALMTheme.ICON_KEYBOARD, "passenger-dashboard" },
            { "Floor Ops", ALMTheme.ICON_DOCUMENT, "staff-dashboard" },
            { "Data Sync Center", "\u21c4", "sync-center" }, // Added Sync Center under Operations

            // --- People ---
            { "Staff", ALMTheme.ICON_PEOPLE, "staff" },
            { "Passengers", ALMTheme.ICON_PERSON, "passengers" },
            { "Access Control", ALMTheme.ICON_LOCK, "access-control" },
            { "Contracts", ALMTheme.ICON_DOCUMENT, "contracts" },

            // --- Reporting ---
            { "Analytics", ALMTheme.ICON_CHART, "analytics" },
            { "Revenue", ALMTheme.ICON_MONEY, "revenue" },
            { "Complaints", ALMTheme.ICON_MAIL, "complaints" },

            // --- Configuration ---
            { "Settings", ALMTheme.ICON_SETTINGS, "settings" }
    };

    public AdminSidebar() {
        super(NAV_ITEMS);
    }
}
