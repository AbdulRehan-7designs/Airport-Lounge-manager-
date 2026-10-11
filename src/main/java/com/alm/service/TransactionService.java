package com.alm.service;

import com.alm.dao.TransactionDAO;
import com.alm.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    // ── Queries ───────────────────────────────────────────────────────────────

    /** All transactions — for report / admin screens. */
    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAll();
    }

    /** Transactions linked to one access request. */
    public List<Transaction> getByRequestId(int requestId) {
        return transactionDAO.getByRequestId(requestId);
    }

    /** Total revenue from all SUCCESS transactions — used by the dashboard. */
    public BigDecimal getTotalRevenue() {
        return transactionDAO.getTotalRevenue();
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    /**
     * Records a new payment in INITIATED state.
     * Call updateStatus() once the payment gateway responds.
     */
    public boolean initiateTransaction(int requestId, BigDecimal amount,
            String paymentMethod, String currency) {
        Transaction tx = new Transaction();
        tx.setRequestId(requestId);
        tx.setAmount(amount);
        tx.setPaymentMethod(paymentMethod);
        tx.setCurrency(currency != null ? currency : "INR");
        tx.setPaymentStatus("INITIATED");
        return transactionDAO.logTransaction(tx);
    }

    /**
     * Moves a transaction through its lifecycle:
     * INITIATED → SUCCESS | FAILED
     * SUCCESS → REFUNDED
     */
    public boolean updateStatus(int transactionId, String newStatus) {
        if (newStatus == null)
            return false;
        String s = newStatus.toUpperCase();
        if (!s.equals("INITIATED") && !s.equals("SUCCESS")
                && !s.equals("FAILED") && !s.equals("REFUNDED")) {
            throw new IllegalArgumentException("Invalid payment status: " + newStatus);
        }
        return transactionDAO.updateStatus(transactionId, s);
    }
}
