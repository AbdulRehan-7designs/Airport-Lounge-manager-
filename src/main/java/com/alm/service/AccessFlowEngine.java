package com.alm.service;

import com.alm.model.AccessRequest;
import com.alm.model.Lounge;
import com.alm.model.AccessProvider;
import com.alm.dao.LoungeDAO;
import com.alm.dao.AccessProviderDAO;

public class AccessFlowEngine {

    private final LoungeDAO loungeDAO = new LoungeDAO();
    private final AccessProviderDAO providerDAO = new AccessProviderDAO();

    public static class Decision {
        public final String status;
        public final String reason;
        public final String nextStep; // For UI visualization

        public Decision(String status, String reason, String nextStep) {
            this.status = status;
            this.reason = reason;
            this.nextStep = nextStep;
        }
    }

    public Decision evaluate(AccessRequest request) {
        Lounge lounge = loungeDAO.getById(request.getLoungeId());
        if (lounge == null)
            return new Decision("DENIED", "Invalid lounge selection.", "STOP");

        if (request.getProviderId() == null) {
            return new Decision("PENDING", "No access provider specified. Manual review required.", "MANUAL_CHECK");
        }

        AccessProvider provider = providerDAO.getById(request.getProviderId());
        if (provider == null)
            return new Decision("DENIED", "Invalid access provider.", "STOP");

        String type = provider.getProviderType();
        int capacity = lounge.getCapacity();
        // Mock occupancy (in a real system this would query occupancy_logs)
        int currentOccupancy = (int) (Math.random() * capacity * 0.9);

        switch (type) {
            case "AIRLINE":
                return evaluateAirline(request);
            case "CREDIT_CARD":
                return evaluateCreditCard(request, capacity, currentOccupancy);
            case "AGGREGATOR":
                return evaluateAggregator(request, capacity, currentOccupancy);
            case "PAY_PER_USE":
                return evaluatePayPerUse(request);
            default:
                return new Decision("PENDING", "Unknown provider type. Manual review needed.", "MANUAL_CHECK");
        }
    }

    private Decision evaluateAirline(AccessRequest req) {
        if ("FIRST".equalsIgnoreCase(req.getTicketClass()) || "BUSINESS".equalsIgnoreCase(req.getTicketClass())) {
            return new Decision("APPROVED", "Premium ticket access verified.", "ALLIANCE_RECIPROCITY");
        }
        if ("GOLD".equalsIgnoreCase(req.getEliteStatus()) || "PLATINUM".equalsIgnoreCase(req.getEliteStatus())) {
            return new Decision("APPROVED", "High elite status benefits applied.", "RETENTION_FOCUS");
        }
        return new Decision("PENDING", "Verification required for Tier 1 standard club access.", "TIER_VERIFICATION");
    }

    private Decision evaluateCreditCard(AccessRequest req, int cap, int occ) {
        if ("PREMIUM".equalsIgnoreCase(req.getCardType())) {
            if (occ < cap) {
                return new Decision("APPROVED", "Premium card holder. Space available.", "EXPERIENCE_FOCUS");
            } else {
                return new Decision("DENIED", "Lounge at capacity for credit card members.", "WAITLIST_FLOW");
            }
        }
        return new Decision("DENIED", "Standard card type does not include lounge access.", "STOP");
    }

    private Decision evaluateAggregator(AccessRequest req, int cap, int occ) {
        if (req.getMembershipType() != null) {
            double occPercent = (double) occ / cap;
            if (occPercent < 0.7) {
                return new Decision("APPROVED", "Good coverage. Sufficient capacity available.", "VOLUME_FOCUS");
            } else if (occPercent < 0.9) {
                return new Decision("PENDING", "Near capacity. Subject to capacity constraints.",
                        "ECONOMIC_HIERARCHY_CHECK");
            } else {
                return new Decision("DENIED", "Priority Pass members currently throttled due to high occupancy.",
                        "THROTTLE_FLOW");
            }
        }
        return new Decision("DENIED", "Valid membership not found.", "STOP");
    }

    private Decision evaluatePayPerUse(AccessRequest req) {
        if (req.getAmountPaid() != null && req.getAmountPaid() > 0) {
            return new Decision("APPROVED", "One-time fee transaction confirmed.", "FLEXIBLE_ENTRY");
        }
        return new Decision("PENDING", "Payment processing required.", "TRANSACTION_HUB");
    }
}
