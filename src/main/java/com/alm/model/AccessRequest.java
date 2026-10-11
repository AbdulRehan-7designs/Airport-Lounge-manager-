package com.alm.model;

import java.sql.Timestamp;

/**
 * Maps to the 'access_requests' table.
 *
 * status : PENDING | APPROVED | DENIED (matches DB enum exactly)
 */
public class AccessRequest {

    private int id;
    private int passengerId;
    private int loungeId;
    private Integer providerId; // nullable FK → access_providers
    private String status; // PENDING, APPROVED, DENIED
    private String decisionReason; // nullable
    private Timestamp requestTime;
    private Timestamp decisionTime; // nullable — set when approved/denied
    private Timestamp checkedInAt; // nullable — set when guest enters
    private Timestamp checkedOutAt; // nullable — set when guest leaves

    // Intelligence fields for Decision Engine
    private String ticketClass; // ECONOMY, BUSINESS, FIRST
    private String eliteStatus; // NONE, SILVER, GOLD, PLATINUM
    private String cardType; // STANDARD, PREMIUM
    private String membershipType; // PRIORITY_PASS, LOUNGEKEY
    private Double amountPaid; // For PAY_PER_USE

    // Optional join fields (populated by DAO join queries — not stored in this
    // table)
    private String passengerName;
    private String loungeName;
    private String providerName;

    public AccessRequest() {
    }

    public AccessRequest(int id, int passengerId, int loungeId, Integer providerId,
            String status, String decisionReason,
            Timestamp requestTime, Timestamp decisionTime) {
        this.id = id;
        this.passengerId = passengerId;
        this.loungeId = loungeId;
        this.providerId = providerId;
        this.status = status;
        this.decisionReason = decisionReason;
        this.requestTime = requestTime;
        this.decisionTime = decisionTime;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public int getLoungeId() {
        return loungeId;
    }

    public void setLoungeId(int loungeId) {
        this.loungeId = loungeId;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }

    public Timestamp getDecisionTime() {
        return decisionTime;
    }

    public void setDecisionTime(Timestamp decisionTime) {
        this.decisionTime = decisionTime;
    }

    public Timestamp getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Timestamp checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public Timestamp getCheckedOutAt() {
        return checkedOutAt;
    }

    public void setCheckedOutAt(Timestamp checkedOutAt) {
        this.checkedOutAt = checkedOutAt;
    }

    public String getTicketClass() {
        return ticketClass;
    }

    public void setTicketClass(String ticketClass) {
        this.ticketClass = ticketClass;
    }

    public String getEliteStatus() {
        return eliteStatus;
    }

    public void setEliteStatus(String eliteStatus) {
        this.eliteStatus = eliteStatus;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    // ── Join helpers (read-only display fields) ───────────────────────────────

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getLoungeName() {
        return loungeName;
    }

    public void setLoungeName(String loungeName) {
        this.loungeName = loungeName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
