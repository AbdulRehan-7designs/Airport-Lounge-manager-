package com.alm.config;

/**
 * Every atomic capability in the system.
 *
 * Permissions are grouped by the lowest role that receives them for
 * readability.
 * A role's full capability set is defined in {@link Role}.
 */
public enum Permission {

    // ── Passenger / Guest (self-service) ─────────────────────────────────────
    /** Browse lounges and see their current status / capacity. */
    VIEW_LOUNGE_AVAILABILITY,
    /** Submit a new access request for a lounge. */
    REQUEST_LOUNGE_ACCESS,
    /** View one's own visit and transaction history. */
    VIEW_OWN_VISIT_HISTORY,

    // ── Front Desk / Receptionist ─────────────────────────────────────────────
    /** Check a guest into a lounge. */
    CHECK_IN_GUEST,
    /** Check a guest out of a lounge. */
    CHECK_OUT_GUEST,
    /** Verify airline status, Priority Pass, or other memberships. */
    VERIFY_MEMBERSHIP,
    /** Process walk-in or top-up payments. */
    PROCESS_PAYMENT,
    /** Read the access-request queue. */
    VIEW_ACCESS_REQUESTS,
    /** Approve or deny access requests. */
    APPROVE_ACCESS_REQUESTS,
    /** Create, update, and deactivate passenger records. */
    MANAGE_PASSENGERS,

    // ── Lounge Supervisor ─────────────────────────────────────────────────────
    /** View real-time and daily check-in statistics for a lounge. */
    VIEW_DAILY_STATISTICS,
    /** Read inventory and amenity counts. */
    VIEW_LOUNGE_INVENTORY,
    /** Update amenity/inventory counts. */
    MANAGE_AMENITIES,
    /** Mark a lounge as ACTIVE, CLOSED, or AT_CAPACITY. */
    UPDATE_LOUNGE_STATUS,
    /** Override a system decision or handle a guest dispute. */
    HANDLE_GUEST_DISPUTE,
    /** View occupancy curves and capacity utilisation reports. */
    VIEW_CAPACITY_REPORTS,

    // ── Lounge Manager ────────────────────────────────────────────────────────
    /** View revenue and payment summaries for a lounge. */
    VIEW_FINANCIAL_REPORTS,
    /** Read individual transaction records. */
    VIEW_TRANSACTION_HISTORY,
    /** Manage shift schedules and staff assignments. */
    MANAGE_SHIFTS,

    // ── Auditor (read-only cross-lounge intelligence) ─────────────────────────
    /** View audit trail for staff actions. */
    VIEW_AUDIT_LOGS,
    /** View system error and application logs. */
    VIEW_ERROR_LOGS,

    // ── System Administrator (global control) ─────────────────────────────────
    /** Add, edit, or remove lounge locations. */
    MANAGE_LOUNGES,
    /** Add, edit, or deactivate airports in the system. */
    MANAGE_AIRPORTS,
    /** Create, suspend, or modify any user/staff account. */
    MANAGE_USER_ACCOUNTS,
    /**
     * Specifically create Lounge Manager accounts (subset of MANAGE_USER_ACCOUNTS).
     */
    CREATE_MANAGER_ACCOUNT,
    /** Manage third-party access providers (airlines, credit-card networks). */
    MANAGE_ACCESS_PROVIDERS,
    /** Add, edit, or remove staff assignments. */
    MANAGE_STAFF,
    /** Configure global system settings (API keys, payment gateways, etc.). */
    CONFIGURE_SYSTEM,
}
