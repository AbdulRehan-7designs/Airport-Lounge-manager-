package com.alm.config;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Enterprise RBAC role hierarchy for the Airport Lounge Management System.
 *
 * Each role carries an immutable {@link EnumSet} of {@link Permission}s.
 * Role strings stored in the {@code users.role} column (VARCHAR 50) are the
 * enum {@code name()} values — e.g. "ADMIN", "RECEPTION", "AUDITOR".
 *
 * <pre>
 * Privilege ladder (highest → lowest):
 *   ADMIN  ⊇  OPERATIONS_MANAGER  ⊇  SUPERVISOR  ⊇  RECEPTION
 *   AUDITOR  — read-only financial branch (≠ the operations ladder)
 *   PASSENGER — self-service only
 * </pre>
 */
public enum Role {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. SYSTEM ADMINISTRATOR — absolute access, every action is audit-logged
    // ─────────────────────────────────────────────────────────────────────────
    ADMIN(
            "System Administrator",
            EnumSet.allOf(Permission.class)),

    // ─────────────────────────────────────────────────────────────────────────
    // 2. OPERATIONS / LOUNGE MANAGER — tactical oversight of lounge(s)
    // Can view financials, override disputes, manage shifts.
    // Cannot: delete lounges, create other manager accounts, configure system.
    // ─────────────────────────────────────────────────────────────────────────
    OPERATIONS_MANAGER(
            "Lounge Manager",
            EnumSet.of(
                    // Self-service
                    Permission.VIEW_LOUNGE_AVAILABILITY,
                    Permission.REQUEST_LOUNGE_ACCESS,
                    Permission.VIEW_OWN_VISIT_HISTORY,
                    // Reception desk
                    Permission.CHECK_IN_GUEST,
                    Permission.CHECK_OUT_GUEST,
                    Permission.VERIFY_MEMBERSHIP,
                    Permission.PROCESS_PAYMENT,
                    Permission.VIEW_ACCESS_REQUESTS,
                    Permission.APPROVE_ACCESS_REQUESTS,
                    Permission.MANAGE_PASSENGERS,
                    // Supervisor
                    Permission.VIEW_DAILY_STATISTICS,
                    Permission.VIEW_LOUNGE_INVENTORY,
                    Permission.MANAGE_AMENITIES,
                    Permission.UPDATE_LOUNGE_STATUS,
                    Permission.HANDLE_GUEST_DISPUTE,
                    Permission.VIEW_CAPACITY_REPORTS,
                    // Manager
                    Permission.VIEW_FINANCIAL_REPORTS,
                    Permission.VIEW_TRANSACTION_HISTORY,
                    Permission.MANAGE_SHIFTS,
                    Permission.MANAGE_STAFF,
                    // Auditor (can read logs within their lounge scope)
                    Permission.VIEW_AUDIT_LOGS)),

    // ─────────────────────────────────────────────────────────────────────────
    // 3. SUPERVISOR — shift-level authority; can override, update lounge status
    // Cannot: view financial reports, manage shifts (those are manager-level)
    // ─────────────────────────────────────────────────────────────────────────
    SUPERVISOR(
            "Lounge Supervisor",
            EnumSet.of(
                    Permission.VIEW_LOUNGE_AVAILABILITY,
                    Permission.REQUEST_LOUNGE_ACCESS,
                    Permission.VIEW_OWN_VISIT_HISTORY,
                    Permission.CHECK_IN_GUEST,
                    Permission.CHECK_OUT_GUEST,
                    Permission.VERIFY_MEMBERSHIP,
                    Permission.PROCESS_PAYMENT,
                    Permission.VIEW_ACCESS_REQUESTS,
                    Permission.APPROVE_ACCESS_REQUESTS,
                    Permission.MANAGE_PASSENGERS,
                    Permission.VIEW_DAILY_STATISTICS,
                    Permission.VIEW_LOUNGE_INVENTORY,
                    Permission.MANAGE_AMENITIES,
                    Permission.UPDATE_LOUNGE_STATUS,
                    Permission.HANDLE_GUEST_DISPUTE,
                    Permission.VIEW_CAPACITY_REPORTS)),

    // ─────────────────────────────────────────────────────────────────────────
    // 4. RECEPTION — Front Desk / Receptionist — guest throughput focus
    // Can: scan boarding passes, verify memberships, process payments, check in/out
    // Cannot: modify lounge capacity, view financial reports, manage users
    // ─────────────────────────────────────────────────────────────────────────
    RECEPTION(
            "Front Desk Receptionist",
            EnumSet.of(
                    Permission.VIEW_LOUNGE_AVAILABILITY,
                    Permission.VIEW_OWN_VISIT_HISTORY,
                    Permission.CHECK_IN_GUEST,
                    Permission.CHECK_OUT_GUEST,
                    Permission.VERIFY_MEMBERSHIP,
                    Permission.PROCESS_PAYMENT,
                    Permission.VIEW_ACCESS_REQUESTS,
                    Permission.APPROVE_ACCESS_REQUESTS,
                    Permission.MANAGE_PASSENGERS,
                    Permission.VIEW_CAPACITY_REPORTS)),

    // ─────────────────────────────────────────────────────────────────────────
    // 5. AUDITOR — Data & Financial Auditor — read-only business intelligence
    // Cross-lounge financial visibility (billing airlines / Priority Pass).
    // Cannot: check in guests, alter anything in the system.
    // ─────────────────────────────────────────────────────────────────────────
    AUDITOR(
            "Data & Financial Auditor",
            EnumSet.of(
                    Permission.VIEW_LOUNGE_AVAILABILITY,
                    Permission.VIEW_OWN_VISIT_HISTORY,
                    Permission.VIEW_ACCESS_REQUESTS,
                    Permission.VIEW_DAILY_STATISTICS,
                    Permission.VIEW_LOUNGE_INVENTORY,
                    Permission.VIEW_CAPACITY_REPORTS,
                    Permission.VIEW_FINANCIAL_REPORTS,
                    Permission.VIEW_TRANSACTION_HISTORY,
                    Permission.VIEW_AUDIT_LOGS,
                    Permission.VIEW_ERROR_LOGS)),

    // ─────────────────────────────────────────────────────────────────────────
    // 6. PASSENGER — Self-service guest portal
    // Can: view lounges, check capacity, request access, view own history
    // ─────────────────────────────────────────────────────────────────────────
    PASSENGER(
            "Guest / Passenger",
            EnumSet.of(
                    Permission.VIEW_LOUNGE_AVAILABILITY,
                    Permission.REQUEST_LOUNGE_ACCESS,
                    Permission.VIEW_OWN_VISIT_HISTORY));

    // ── Fields ────────────────────────────────────────────────────────────────

    private final String displayName;
    private final Set<Permission> permissions;

    Role(String displayName, EnumSet<Permission> permissions) {
        this.displayName = displayName;
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    // ── Instance methods ──────────────────────────────────────────────────────

    public String getDisplayName() {
        return displayName;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    /** True if this role holds the given permission. */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    // ── Static helpers ────────────────────────────────────────────────────────

    /**
     * Parse a DB role string (e.g. "ADMIN", "reception") to a Role enum constant.
     * Returns {@code null} for unknown/null values — callers must guard.
     */
    public static Role fromString(String roleStr) {
        if (roleStr == null || roleStr.isBlank())
            return null;
        String normalized = roleStr.toUpperCase().trim();
        // Legacy / DB typo aliases
        if ("RECEPTIONIST".equals(normalized))
            return RECEPTION;
        if ("PASSANGER".equals(normalized)) // DB typo: missing E
            return PASSENGER;
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Check if a role string (from DB / session) holds a specific permission.
     * This is the primary entry point for all permission checks.
     */
    public static boolean hasPermission(String roleStr, Permission permission) {
        Role role = fromString(roleStr);
        return role != null && role.hasPermission(permission);
    }

    // ── Legacy convenience helpers (backward-compat with existing servlets) ───
    // These delegate to the permission model — no separate logic to maintain.

    /** True for roles that can access admin-only user management screens. */
    public static boolean canAccessAdmin(String roleStr) {
        return hasPermission(roleStr, Permission.MANAGE_USER_ACCOUNTS);
    }

    /** True if role is ADMIN. */
    public static boolean isAdmin(String roleStr) {
        return fromString(roleStr) == ADMIN;
    }

    /** True if role is PASSENGER. */
    public static boolean isPassenger(String roleStr) {
        return fromString(roleStr) == PASSENGER;
    }

    /** True for roles that can access manager-level screens (reports, shifts). */
    public static boolean canAccessManager(String roleStr) {
        return hasPermission(roleStr, Permission.VIEW_FINANCIAL_REPORTS);
    }

    /**
     * True for roles that can access the reception desk (passengers,
     * access-requests).
     * Note: AUDITOR can VIEW access-requests but cannot APPROVE — servlet logic
     * must check {@code APPROVE_ACCESS_REQUESTS} for write operations.
     */
    public static boolean canAccessReception(String roleStr) {
        return hasPermission(roleStr, Permission.CHECK_IN_GUEST)
                || hasPermission(roleStr, Permission.VIEW_ACCESS_REQUESTS);
    }

    /** True for any staff role (not a guest/passenger). */
    public static boolean isStaff(String roleStr) {
        return hasPermission(roleStr, Permission.CHECK_IN_GUEST)
                || hasPermission(roleStr, Permission.VIEW_FINANCIAL_REPORTS)
                || fromString(roleStr) == AUDITOR
                || fromString(roleStr) == ADMIN;
    }
}
