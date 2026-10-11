package com.alm.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Maps to the 'transactions' table.
 *
 * payment_method : CARD | UPI | CASH
 * payment_status : INITIATED | SUCCESS | FAILED | REFUNDED
 */
public class Transaction {

    private int id;
    private int requestId; // FK → access_requests.request_id
    private BigDecimal amount;
    private String currency; // e.g. "USD"
    private String paymentMethod; // CARD, UPI, CASH
    private String paymentStatus; // INITIATED, SUCCESS, FAILED, REFUNDED
    private Timestamp processedAt;

    public Transaction() {
    }

    public Transaction(int id, int requestId, BigDecimal amount, String currency,
            String paymentMethod, String paymentStatus, Timestamp processedAt) {
        this.id = id;
        this.requestId = requestId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.processedAt = processedAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Timestamp getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Timestamp processedAt) {
        this.processedAt = processedAt;
    }
}
