package com.alm.ui.shell;

import com.alm.ui.core.NavigationManager;
import javax.swing.*;
import java.awt.*;

/**
 * The main application shell: Sidebar | (Topbar + Content).
 * This is the "logged-in" view of the application.
 */
public class AppShell extends JPanel {

    private SidebarPanel sidebar;
    private TopbarPanel topbar;

    public AppShell() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // ── Sidebar (LEFT) ───────────────────────────────────────────────────
        String roleStr = com.alm.ui.core.SessionContext.getInstance().getCurrentUser() != null
                ? com.alm.ui.core.SessionContext.getInstance().getCurrentUser().getRole()
                : "PASSENGER";
        com.alm.config.Role role = com.alm.config.Role.fromString(roleStr);

        if (role == com.alm.config.Role.ADMIN) {
            sidebar = new AdminSidebar();
        } else if (role == com.alm.config.Role.PASSENGER || role == null) {
            sidebar = new PassengerSidebar();
        } else {
            sidebar = new StaffSidebar();
        }
        add(sidebar, BorderLayout.WEST);

        // ── Right area: topbar + content ─────────────────────────────────────
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);

        topbar = new TopbarPanel();
        rightPanel.add(topbar, BorderLayout.NORTH);

        // Content area from NavigationManager
        JPanel contentArea = NavigationManager.getInstance().getContentArea();
        contentArea.setBackground(Color.WHITE);
        rightPanel.add(contentArea, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);
    }

    public TopbarPanel getTopbar() {
        return topbar;
    }

    public SidebarPanel getSidebar() {
        return sidebar;
    }

    public JPanel getContentPanel() {
        return NavigationManager.getInstance().getContentArea();
    }
}
