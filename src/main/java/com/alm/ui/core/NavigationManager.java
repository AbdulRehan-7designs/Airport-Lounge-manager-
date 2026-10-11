package com.alm.ui.core;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Manages navigation between panels using CardLayout.
 * Supports a back-stack for history navigation.
 */
public final class NavigationManager {

    private static NavigationManager instance;

    private final JPanel contentArea;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> panels = new HashMap<>();
    private final Map<String, com.alm.config.Permission> permissions = new HashMap<>();
    private final Stack<String> history = new Stack<>();
    private String currentView;

    // Listeners
    private Runnable onViewChanged;

    private NavigationManager() {
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public JPanel getContentArea() {
        return contentArea;
    }

    public void clearHistory() {
        history.clear();
        currentView = null;
        panels.clear();
        permissions.clear();
        contentArea.removeAll();
        contentArea.revalidate();
        contentArea.repaint();
    }

    /**
     * Register a panel for lazy loading with permission check.
     */
    public void registerPanel(String name, JPanel panel, com.alm.config.Permission p) {
        panels.put(name, panel);
        permissions.put(name, p);
        contentArea.add(panel, name);
    }

    /**
     * Navigate to a registered panel with security check.
     */
    public void navigateTo(String name) {
        if (!panels.containsKey(name)) {
            System.err.println("NavigationManager: Unknown panel: " + name);
            return;
        }

        // Security check
        com.alm.config.Permission p = permissions.get(name);
        if (p != null && !SessionContext.getInstance().hasPermission(p)) {
            System.err.println("Access Denied to: " + name);
            JOptionPane.showMessageDialog(null,
                    "You do not have permission to access " + name,
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentView != null) {
            history.push(currentView);
        }
        currentView = name;
        cardLayout.show(contentArea, name);

        if (onViewChanged != null) {
            onViewChanged.run();
        }
    }

    public void goBack() {
        if (!history.isEmpty()) {
            currentView = history.pop();
            cardLayout.show(contentArea, currentView);
            if (onViewChanged != null) {
                onViewChanged.run();
            }
        }
    }

    public String getCurrentView() {
        return currentView;
    }

    public JPanel getPanel(String name) {
        return panels.get(name);
    }

    public void setOnViewChanged(Runnable callback) {
        this.onViewChanged = callback;
    }
}
