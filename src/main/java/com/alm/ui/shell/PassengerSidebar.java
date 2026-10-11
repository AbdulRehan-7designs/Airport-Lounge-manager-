package com.alm.ui.shell;

import com.alm.ui.theme.ALMTheme;

public class PassengerSidebar extends SidebarPanel {

    private static final Object[][] NAV_ITEMS = {
            { "Home", ALMTheme.ICON_HOME, "passenger-home" },
            { "My Visits", ALMTheme.ICON_DOCUMENT, "visit-history" },
            { "Lounges", ALMTheme.ICON_LOUNGE, "lounges" },
            { "Settings", ALMTheme.ICON_SETTINGS, "settings" }
    };

    public PassengerSidebar() {
        super(NAV_ITEMS);
    }
}
