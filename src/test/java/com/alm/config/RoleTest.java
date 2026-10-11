package com.alm.config;

import org.junit.Test;
import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * Tests for the enterprise RBAC Role/Permission model.
 *
 * Naming convention: test[Role]_[can|cannot]_[PermissionOrArea]
 */
public class RoleTest {

    // ── fromString ────────────────────────────────────────────────────────────

    @Test
    public void fromString_caseSensitiveUppercase() {
        assertEquals(Role.ADMIN, Role.fromString("ADMIN"));
    }

    @Test
    public void fromString_caseInsensitiveLower() {
        assertEquals(Role.RECEPTION, Role.fromString("reception"));
    }

    @Test
    public void fromString_caseInsensitiveMixed() {
        assertEquals(Role.AUDITOR, Role.fromString("Auditor"));
    }

    @Test
    public void fromString_null_returnsNull() {
        assertNull(Role.fromString(null));
    }

    @Test
    public void fromString_blank_returnsNull() {
        assertNull(Role.fromString("   "));
    }

    @Test
    public void fromString_unknown_returnsNull() {
        assertNull(Role.fromString("HACKER"));
    }

    // ── SYSTEM ADMINISTRATOR (ADMIN) ──────────────────────────────────────────

    @Test
    public void admin_hasAllPermissions() {
        Role admin = Role.ADMIN;
        for (Permission p : Permission.values()) {
            assertTrue("ADMIN should have " + p, admin.hasPermission(p));
        }
    }

    @Test
    public void admin_canManageUserAccounts() {
        assertTrue(Role.hasPermission("ADMIN", Permission.MANAGE_USER_ACCOUNTS));
    }

    @Test
    public void admin_canConfigureSystem() {
        assertTrue(Role.hasPermission("ADMIN", Permission.CONFIGURE_SYSTEM));
    }

    @Test
    public void admin_canManageLounges() {
        assertTrue(Role.hasPermission("ADMIN", Permission.MANAGE_LOUNGES));
    }

    // ── LOUNGE MANAGER (OPERATIONS_MANAGER) ───────────────────────────────────

    @Test
    public void manager_canViewFinancialReports() {
        assertTrue(Role.hasPermission("OPERATIONS_MANAGER", Permission.VIEW_FINANCIAL_REPORTS));
    }

    @Test
    public void manager_canApproveAccessRequests() {
        assertTrue(Role.hasPermission("OPERATIONS_MANAGER", Permission.APPROVE_ACCESS_REQUESTS));
    }

    @Test
    public void manager_canManageShifts() {
        assertTrue(Role.hasPermission("OPERATIONS_MANAGER", Permission.MANAGE_SHIFTS));
    }

    @Test
    public void manager_canHandleDisputes() {
        assertTrue(Role.hasPermission("OPERATIONS_MANAGER", Permission.HANDLE_GUEST_DISPUTE));
    }

    @Test
    public void manager_cannot_manageUserAccounts() {
        assertFalse("Manager must NOT manage user accounts",
                Role.hasPermission("OPERATIONS_MANAGER", Permission.MANAGE_USER_ACCOUNTS));
    }

    @Test
    public void manager_cannot_configureSystem() {
        assertFalse(Role.hasPermission("OPERATIONS_MANAGER", Permission.CONFIGURE_SYSTEM));
    }

    @Test
    public void manager_cannot_manageLounges() {
        assertFalse("Manager must NOT add/remove lounges",
                Role.hasPermission("OPERATIONS_MANAGER", Permission.MANAGE_LOUNGES));
    }

    // ── SUPERVISOR ────────────────────────────────────────────────────────────

    @Test
    public void supervisor_canCheckInGuest() {
        assertTrue(Role.hasPermission("SUPERVISOR", Permission.CHECK_IN_GUEST));
    }

    @Test
    public void supervisor_canUpdateLoungeStatus() {
        assertTrue(Role.hasPermission("SUPERVISOR", Permission.UPDATE_LOUNGE_STATUS));
    }

    @Test
    public void supervisor_canHandleDisputes() {
        assertTrue(Role.hasPermission("SUPERVISOR", Permission.HANDLE_GUEST_DISPUTE));
    }

    @Test
    public void supervisor_cannot_viewFinancialReports() {
        assertFalse("Supervisor must NOT view financial reports",
                Role.hasPermission("SUPERVISOR", Permission.VIEW_FINANCIAL_REPORTS));
    }

    @Test
    public void supervisor_cannot_manageShifts() {
        assertFalse("Supervisor must NOT manage shifts (manager-only)",
                Role.hasPermission("SUPERVISOR", Permission.MANAGE_SHIFTS));
    }

    // ── FRONT DESK RECEPTIONIST (RECEPTION) ───────────────────────────────────

    @Test
    public void reception_canCheckInGuest() {
        assertTrue(Role.hasPermission("RECEPTION", Permission.CHECK_IN_GUEST));
    }

    @Test
    public void reception_canVerifyMembership() {
        assertTrue(Role.hasPermission("RECEPTION", Permission.VERIFY_MEMBERSHIP));
    }

    @Test
    public void reception_canProcessPayment() {
        assertTrue(Role.hasPermission("RECEPTION", Permission.PROCESS_PAYMENT));
    }

    @Test
    public void reception_canViewAccessRequests() {
        assertTrue(Role.hasPermission("RECEPTION", Permission.VIEW_ACCESS_REQUESTS));
    }

    @Test
    public void reception_cannot_viewFinancialReports() {
        assertFalse("Reception must NOT view financial reports",
                Role.hasPermission("RECEPTION", Permission.VIEW_FINANCIAL_REPORTS));
    }

    @Test
    public void reception_cannot_updateLoungeStatus() {
        assertFalse("Reception must NOT update lounge status (supervisor+)",
                Role.hasPermission("RECEPTION", Permission.UPDATE_LOUNGE_STATUS));
    }

    @Test
    public void reception_cannot_manageUserAccounts() {
        assertFalse(Role.hasPermission("RECEPTION", Permission.MANAGE_USER_ACCOUNTS));
    }

    // ── AUDITOR ───────────────────────────────────────────────────────────────

    @Test
    public void auditor_canViewFinancialReports() {
        assertTrue(Role.hasPermission("AUDITOR", Permission.VIEW_FINANCIAL_REPORTS));
    }

    @Test
    public void auditor_canViewTransactionHistory() {
        assertTrue(Role.hasPermission("AUDITOR", Permission.VIEW_TRANSACTION_HISTORY));
    }

    @Test
    public void auditor_canViewCapacityReports() {
        assertTrue(Role.hasPermission("AUDITOR", Permission.VIEW_CAPACITY_REPORTS));
    }

    @Test
    public void auditor_canViewAuditLogs() {
        assertTrue(Role.hasPermission("AUDITOR", Permission.VIEW_AUDIT_LOGS));
    }

    @Test
    public void auditor_cannot_checkInGuest() {
        assertFalse("Auditor must NOT check in guests (read-only role)",
                Role.hasPermission("AUDITOR", Permission.CHECK_IN_GUEST));
    }

    @Test
    public void auditor_cannot_approveAccessRequests() {
        assertFalse("Auditor must NOT approve requests",
                Role.hasPermission("AUDITOR", Permission.APPROVE_ACCESS_REQUESTS));
    }

    @Test
    public void auditor_cannot_manageUserAccounts() {
        assertFalse(Role.hasPermission("AUDITOR", Permission.MANAGE_USER_ACCOUNTS));
    }

    @Test
    public void auditor_cannot_configureSystem() {
        assertFalse(Role.hasPermission("AUDITOR", Permission.CONFIGURE_SYSTEM));
    }

    // ── PASSENGER ─────────────────────────────────────────────────────────────

    @Test
    public void passenger_canViewLoungeAvailability() {
        assertTrue(Role.hasPermission("PASSENGER", Permission.VIEW_LOUNGE_AVAILABILITY));
    }

    @Test
    public void passenger_canRequestLoungeAccess() {
        assertTrue(Role.hasPermission("PASSENGER", Permission.REQUEST_LOUNGE_ACCESS));
    }

    @Test
    public void passenger_canViewOwnHistory() {
        assertTrue(Role.hasPermission("PASSENGER", Permission.VIEW_OWN_VISIT_HISTORY));
    }

    @Test
    public void passenger_cannot_checkInGuest() {
        assertFalse(Role.hasPermission("PASSENGER", Permission.CHECK_IN_GUEST));
    }

    @Test
    public void passenger_cannot_viewFinancialReports() {
        assertFalse(Role.hasPermission("PASSENGER", Permission.VIEW_FINANCIAL_REPORTS));
    }

    // ── Legacy helper methods (backward-compat with existing servlets) ─────────

    @Test
    public void canAccessAdmin_onlyAdmin() {
        assertTrue(Role.canAccessAdmin("ADMIN"));
        assertFalse(Role.canAccessAdmin("OPERATIONS_MANAGER"));
        assertFalse(Role.canAccessAdmin("AUDITOR"));
        assertFalse(Role.canAccessAdmin("RECEPTION"));
        assertFalse(Role.canAccessAdmin("PASSENGER"));
    }

    @Test
    public void canAccessManager_adminAndManager() {
        assertTrue(Role.canAccessManager("ADMIN"));
        assertTrue(Role.canAccessManager("OPERATIONS_MANAGER"));
        assertFalse(Role.canAccessManager("SUPERVISOR"));
        assertFalse(Role.canAccessManager("RECEPTION"));
    }

    @Test
    public void canAccessReception_allOperationalRoles() {
        assertTrue(Role.canAccessReception("ADMIN"));
        assertTrue(Role.canAccessReception("OPERATIONS_MANAGER"));
        assertTrue(Role.canAccessReception("SUPERVISOR"));
        assertTrue(Role.canAccessReception("RECEPTION"));
        // AUDITOR can VIEW but this still makes them pass the canAccessReception guard
        assertTrue(Role.canAccessReception("AUDITOR"));
        assertFalse(Role.canAccessReception("PASSENGER"));
    }

    // ── Permission set integrity (no role has more than ADMIN) ────────────────

    @Test
    public void noRoleHasMorePermissionsThanAdmin() {
        EnumSet<Permission> adminPerms = EnumSet.copyOf(Role.ADMIN.getPermissions());
        for (Role role : Role.values()) {
            if (role == Role.ADMIN)
                continue;
            EnumSet<Permission> extra = EnumSet.copyOf(role.getPermissions());
            extra.removeAll(adminPerms);
            assertTrue("Role " + role + " has permissions not held by ADMIN: " + extra,
                    extra.isEmpty());
        }
    }
}
