package com.alm.ui.shell;

import com.alm.config.Role;
import com.alm.ui.core.SessionContext;
import com.alm.ui.theme.ALMTheme;

/**
 * Role-aware staff sidebar — uses flat icon constants from ALMTheme.
 */
public class StaffSidebar extends SidebarPanel {

    public StaffSidebar() {
        super(getNavItems());
    }

    private static Object[][] getNavItems() {
        if (!SessionContext.getInstance().isLoggedIn()) {
            return new Object[][] { { "Dashboard", ALMTheme.ICON_HOME, "dashboard" } };
        }

        Role role = Role.fromString(SessionContext.getInstance().getCurrentUser().getRole());
        if (role == null) {
            return new Object[][] { { "Dashboard", ALMTheme.ICON_HOME, "dashboard" } };
        }

        switch (role) {
            case OPERATIONS_MANAGER:
                return new Object[][] {
                        { "Dashboard", ALMTheme.ICON_HOME, "dashboard" },
                        { "Front Desk", ALMTheme.ICON_KEYBOARD, "passenger-dashboard" },
                        { "Floor Ops", ALMTheme.ICON_DOCUMENT, "staff-dashboard" },
                        { "Lounges", ALMTheme.ICON_LOUNGE, "lounges" },
                        { "Passengers", ALMTheme.ICON_PERSON, "passengers" },
                        { "Access Control", ALMTheme.ICON_LOCK, "access-control" },
                        { "Analytics", ALMTheme.ICON_CHART, "analytics" },
                        { "Revenue", ALMTheme.ICON_MONEY, "revenue" },
                        { "Complaints", ALMTheme.ICON_MAIL, "complaints" },
                        { "Staff", ALMTheme.ICON_PEOPLE, "staff" },
                        { "Settings", ALMTheme.ICON_SETTINGS, "settings" }
                };
            case SUPERVISOR:
                return new Object[][] {
                        { "Dashboard", ALMTheme.ICON_HOME, "dashboard" },
                        { "Front Desk", ALMTheme.ICON_KEYBOARD, "passenger-dashboard" },
                        { "Floor Ops", ALMTheme.ICON_DOCUMENT, "staff-dashboard" },
                        { "Lounges", ALMTheme.ICON_LOUNGE, "lounges" },
                        { "Passengers", ALMTheme.ICON_PERSON, "passengers" },
                        { "Access Control", ALMTheme.ICON_LOCK, "access-control" },
                        { "Complaints", ALMTheme.ICON_MAIL, "complaints" },
                        { "Settings", ALMTheme.ICON_SETTINGS, "settings" }
                };
            case RECEPTION:
                return new Object[][] {
                        { "Front Desk", ALMTheme.ICON_KEYBOARD, "passenger-dashboard" },
                        { "Services", ALMTheme.ICON_DOCUMENT, "reception-services" },
                        { "Lounges", ALMTheme.ICON_LOUNGE, "lounges" },
                        { "Passengers", ALMTheme.ICON_PERSON, "passengers" },
                        { "Billing", ALMTheme.ICON_MONEY, "billing" },
                        { "Visit History", ALMTheme.ICON_CALENDAR, "visit-history" },
                        { "Complaints", ALMTheme.ICON_MAIL, "complaints" },
                        { "Access Control", ALMTheme.ICON_LOCK, "access-control" },
                        { "Settings", ALMTheme.ICON_SETTINGS, "settings" }
                };
            case AUDITOR:
                return new Object[][] {
                        { "Dashboard", ALMTheme.ICON_HOME, "dashboard" },
                        { "Lounges", ALMTheme.ICON_LOUNGE, "lounges" },
                        { "Analytics", ALMTheme.ICON_CHART, "analytics" },
                        { "Revenue", ALMTheme.ICON_MONEY, "revenue" },
                        { "Settings", ALMTheme.ICON_SETTINGS, "settings" }
                };
            default:
                return new Object[][] { { "Dashboard", ALMTheme.ICON_HOME, "dashboard" } };
        }
    }
}
