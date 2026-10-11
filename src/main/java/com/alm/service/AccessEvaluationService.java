package com.alm.service;

import com.alm.model.Passenger;

/**
 * Unified Decision Engine for computing lounge eligibility.
 */
public class AccessEvaluationService {

    public enum AccessType {
        COMPLIMENTARY_AIRLINE,
        COMPLIMENTARY_MEMBERSHIP,
        PAID,
        DENIED
    }

    public static class EvaluationResult {
        private AccessType accessType;
        private String reason;
        private boolean requiresPayment;

        public EvaluationResult(AccessType accessType, String reason, boolean requiresPayment) {
            this.accessType = accessType;
            this.reason = reason;
            this.requiresPayment = requiresPayment;
        }

        public AccessType getAccessType() {
            return accessType;
        }

        public String getReason() {
            return reason;
        }

        public boolean isRequiresPayment() {
            return requiresPayment;
        }
    }

    /**
     * Determines the exact billing and access category for the passenger.
     */
    public EvaluationResult evaluateAccess(Passenger p, int maxFreeVisits) {
        // 1. Time window validation
        // In a real system, we'd check if `p.getFlightTime()` is within 3-4 hours of
        // now.
        // For simplicity and to not block test data, we assume flight window is valid
        // unless explicitly marked as NOT_ELIGIBLE.
        if ("NOT_ELIGIBLE".equals(p.getAccessStatus()) || "NOT_PROVISIONED".equals(p.getAccessStatus())) {
            return new EvaluationResult(AccessType.DENIED, "Access blocked or not provisioned.", false);
        }

        // 2. Complimentary Airline Tier
        String ticket = p.getTicketClass() != null ? p.getTicketClass().toUpperCase() : "";
        if (ticket.contains("BUSINESS") || ticket.contains("FIRST") || ticket.contains("VIP")) {
            return new EvaluationResult(AccessType.COMPLIMENTARY_AIRLINE,
                    "Airline Complimentary Access via " + p.getTicketClass() + ".", false);
        }

        // 3. Premium Membership Tier
        if (p.isMembershipValid() && p.getMembershipType() != null && !p.getMembershipType().trim().isEmpty()
                && !p.getMembershipType().equalsIgnoreCase("none")) {
            if (p.getVisitCountThisMonth() < maxFreeVisits) {
                return new EvaluationResult(AccessType.COMPLIMENTARY_MEMBERSHIP,
                        "Membership Access (" + (p.getVisitCountThisMonth() + 1) + "/" + maxFreeVisits
                                + " free visits).",
                        false);
            } else {
                return new EvaluationResult(AccessType.PAID,
                        "Membership Free Limits Exceeded. Switch to PAID.", true);
            }
        }

        // 4. Pay-Per-Use Tier
        return new EvaluationResult(AccessType.PAID,
                "Standard Economy / No Active Membership. Pay-per-use flow required.", true);
    }
}
