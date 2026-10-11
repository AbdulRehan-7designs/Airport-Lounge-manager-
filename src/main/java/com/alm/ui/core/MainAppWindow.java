package com.alm.ui.core;

import com.alm.ui.auth.LoginPanel;
import com.alm.ui.shell.AppShell;
import com.alm.ui.theme.ALMTheme;
import com.alm.ui.panels.*;
import com.alm.config.Permission;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point for the ALM Desktop Application.
 * Manages the top-level window and card-based navigation.
 */
public class MainAppWindow extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_APP = "app";

    public MainAppWindow() {
        setTitle("LoungeOS \u2014 Airport Lounge Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(Color.WHITE);

        // View 1: Login
        LoginPanel loginPanel = new LoginPanel(this::onLoginSuccess);
        mainContainer.add(loginPanel, VIEW_LOGIN);

        add(mainContainer);
        cardLayout.show(mainContainer, VIEW_LOGIN);
    }

    public void logout() {
        SessionContext.getInstance().logout();
        NavigationManager.getInstance().clearHistory();
        cardLayout.show(mainContainer, VIEW_LOGIN);
    }

    private void onLoginSuccess() {
        // Build the main app shell and register panels
        AppShell shell = new AppShell();
        mainContainer.add(shell, VIEW_APP);

        NavigationManager nav = NavigationManager.getInstance();

        // Register core panels with permissions
        nav.registerPanel("dashboard", new AdminDashboardPanel(), Permission.VIEW_DAILY_STATISTICS);
        nav.registerPanel("visit-history", new VisitHistoryPanel(), Permission.VIEW_OWN_VISIT_HISTORY);
        nav.registerPanel("lounges", new LoungeListPanel(), Permission.VIEW_LOUNGE_AVAILABILITY);
        nav.registerPanel("lounge-detail", new LoungeDetailPanel(), Permission.VIEW_LOUNGE_AVAILABILITY);
        nav.registerPanel("sync-center", new SyncCenterPanel(), Permission.MANAGE_LOUNGES);
        nav.registerPanel("passengers", new PassengerListPanel(), Permission.MANAGE_PASSENGERS);
        nav.registerPanel("access-control", new AccessControlPanel(), Permission.VIEW_ACCESS_REQUESTS);
        nav.registerPanel("revenue", new RevenuePanel(), Permission.VIEW_FINANCIAL_REPORTS);
        nav.registerPanel("complaints", new ComplaintsPanel(), Permission.HANDLE_GUEST_DISPUTE);

        // Register additional sidebar panels
        nav.registerPanel("contracts", new ContractListPanel(), Permission.MANAGE_ACCESS_PROVIDERS);
        nav.registerPanel("analytics", new AnalyticsPanel(), Permission.VIEW_CAPACITY_REPORTS);
        nav.registerPanel("staff", new StaffManagementPanel(), Permission.MANAGE_STAFF);
        nav.registerPanel("locations", new LocationManagementPanel(), Permission.MANAGE_LOUNGES);
        nav.registerPanel("settings", new SettingsPanel(), null);

        // Full-featured panels from screenshots
        nav.registerPanel("passenger-dashboard", new PassengerDashboardPanel(), Permission.CHECK_IN_GUEST);
        nav.registerPanel("reception-services", new ReceptionServicesPanel(), Permission.CHECK_IN_GUEST);
        nav.registerPanel("passenger-home", new PassengerHomePanel(), Permission.VIEW_LOUNGE_AVAILABILITY);
        nav.registerPanel("staff-dashboard", new StaffDashboardPanel(), Permission.VIEW_LOUNGE_INVENTORY);
        nav.registerPanel("billing", new BillingRecordsPanel(), Permission.VIEW_TRANSACTION_HISTORY);

        // Default view based on role
        if (SessionContext.getInstance().getCurrentUser() != null
                && com.alm.config.Role.isPassenger(SessionContext.getInstance().getCurrentUser().getRole())) {
            nav.navigateTo("passenger-home");
        } else if (SessionContext.getInstance().hasPermission(Permission.VIEW_DAILY_STATISTICS)) {
            nav.navigateTo("dashboard");
        } else if (SessionContext.getInstance().hasPermission(Permission.CHECK_IN_GUEST)) {
            nav.navigateTo("passenger-dashboard");
        } else if (SessionContext.getInstance().hasPermission(Permission.VIEW_OWN_VISIT_HISTORY)) {
            nav.navigateTo("visit-history");
        } else {
            nav.navigateTo("lounges");
        }

        // Transition
        cardLayout.show(mainContainer, VIEW_APP);
    }

    // ── Entry Point ──────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Global error handling for production reliability
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("Uncaught exception in thread " + t.getName());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "An unexpected error occurred: " + (e.getMessage() != null ? e.getMessage() : e.toString()),
                        "System Error", JOptionPane.ERROR_MESSAGE);
            });
        });

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                // Override defaults for clean white theme
                UIManager.put("Panel.background", Color.WHITE);
                UIManager.put("ScrollBar.thumb", ALMTheme.BORDER);
                UIManager.put("ScrollBar.track", ALMTheme.BG_DEEPEST);
                UIManager.put("ScrollPane.background", Color.WHITE);
                UIManager.put("Label.foreground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("TextField.background", ALMTheme.BG_INPUT);
                UIManager.put("TextField.foreground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("TextField.caretForeground", ALMTheme.ACCENT_BLUE);
                UIManager.put("TextField.selectionBackground", ALMTheme.ACCENT_BLUE_LIGHT);
                UIManager.put("TextField.selectionForeground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("PasswordField.background", ALMTheme.BG_INPUT);
                UIManager.put("PasswordField.foreground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("PasswordField.caretForeground", ALMTheme.ACCENT_BLUE);
                UIManager.put("ComboBox.background", ALMTheme.BG_INPUT);
                UIManager.put("ComboBox.foreground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("ComboBox.selectionBackground", ALMTheme.ACCENT_BLUE);
                UIManager.put("ComboBox.selectionForeground", Color.WHITE);
                UIManager.put("CheckBox.foreground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("Table.background", Color.WHITE);
                UIManager.put("Table.foreground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("Table.selectionBackground", ALMTheme.ACCENT_BLUE_LIGHT);
                UIManager.put("Table.selectionForeground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("Table.gridColor", ALMTheme.BORDER_LIGHT);
                UIManager.put("TableHeader.background", ALMTheme.BG_DEEPEST);
                UIManager.put("TableHeader.foreground", ALMTheme.TEXT_SECONDARY);
                UIManager.put("PopupMenu.background", Color.WHITE);
                UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(ALMTheme.BORDER));
                UIManager.put("MenuItem.foreground", ALMTheme.TEXT_PRIMARY);
                UIManager.put("MenuItem.selectionBackground", ALMTheme.ACCENT_BLUE_LIGHT);
                UIManager.put("MenuItem.selectionForeground", ALMTheme.TEXT_PRIMARY);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MainAppWindow window = new MainAppWindow();
            window.setVisible(true);
        });
    }
}
