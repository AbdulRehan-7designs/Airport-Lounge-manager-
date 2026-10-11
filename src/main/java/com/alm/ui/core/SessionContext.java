package com.alm.ui.core;

import com.alm.config.Permission;
import com.alm.config.Role;
import com.alm.model.User;

/**
 * Singleton session context for the desktop application.
 * Replaces HttpSession / AuthFilter from the web layer.
 */
public final class SessionContext {

    private static SessionContext instance;
    private User currentUser;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }

    public String getRoleDisplay() {
        if (currentUser == null)
            return "Unknown";
        Role r = Role.fromString(currentUser.getRole());
        return r != null ? r.getDisplayName() : currentUser.getRole();
    }

    public boolean hasPermission(Permission p) {
        if (currentUser == null)
            return false;
        return Role.hasPermission(currentUser.getRole(), p);
    }

    private Integer currentLoungeId;
    private Runnable loungeChangeListener; // Simple notification mechanism

    public void setCurrentLoungeId(Integer id) {
        this.currentLoungeId = id;
        if (loungeChangeListener != null) {
            loungeChangeListener.run();
        }
    }

    public Integer getCurrentLoungeId() {
        return currentLoungeId;
    }

    public void setLoungeChangeListener(Runnable listener) {
        this.loungeChangeListener = listener;
    }

    public boolean isAdmin() {
        return currentUser != null && Role.isAdmin(currentUser.getRole());
    }
}
