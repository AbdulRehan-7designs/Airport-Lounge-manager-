package com.alm.dao;

import com.alm.model.Transaction;
import com.alm.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // ── Read ──────────────────────────────────────────────────────────────────

    /** All transactions — used for reports / dashboard. Ordered newest first. */
    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, request_id, amount, currency, payment_method, " +
                "payment_status, processed_at FROM transactions ORDER BY processed_at DESC";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** All transactions for a specific access request. */
    public List<Transaction> getByRequestId(int requestId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, request_id, amount, currency, payment_method, " +
                "payment_status, processed_at FROM transactions WHERE request_id = ? " +
                "ORDER BY processed_at DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Transactions filtered by payment status (e.g. "SUCCESS", "FAILED"). */
    public List<Transaction> getByStatus(String paymentStatus) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, request_id, amount, currency, payment_method, " +
                "payment_status, processed_at FROM transactions WHERE payment_status = ? " +
                "ORDER BY processed_at DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paymentStatus);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Sum of all successful transactions — used for dashboard revenue figure. */
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE payment_status = 'SUCCESS'";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getBigDecimal(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Inserts a new transaction record.
     * Sets the generated transaction_id back on the object.
     */
    public boolean logTransaction(Transaction tx) {
        String sql = "INSERT INTO transactions (request_id, amount, currency, payment_method, payment_status) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, tx.getRequestId());
            stmt.setBigDecimal(2, tx.getAmount());
            stmt.setString(3, tx.getCurrency() != null ? tx.getCurrency() : "INR");
            stmt.setString(4, tx.getPaymentMethod());
            stmt.setString(5, tx.getPaymentStatus());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next())
                        tx.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates only the payment_status of an existing transaction.
     * Typical flow: INITIATED → SUCCESS / FAILED → REFUNDED
     */
    public boolean updateStatus(int transactionId, String newStatus) {
        String sql = "UPDATE transactions SET payment_status = ? WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, transactionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    // ── Analytics ─────────────────────────────────────────────────────────────

    /** Returns daily revenue for the last N days. Key is 'YYYY-MM-DD'. */
    public java.util.Map<String, BigDecimal> getRevenueByDay(int days) {
        java.util.Map<String, BigDecimal> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT DATE(processed_at) as day, SUM(amount) as total " +
                "FROM transactions WHERE payment_status = 'SUCCESS' " +
                "GROUP BY day ORDER BY day DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    map.put(rs.getString("day"), rs.getBigDecimal("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /** Returns total revenue per lounge. */
    public java.util.Map<String, BigDecimal> getRevenueByLounge() {
        java.util.Map<String, BigDecimal> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT l.name, SUM(t.amount) as total " +
                "FROM transactions t " +
                "JOIN access_requests ar ON t.request_id = ar.request_id " +
                "JOIN lounges l ON ar.lounge_id = l.lounge_id " +
                "WHERE t.payment_status = 'SUCCESS' " +
                "GROUP BY l.name ORDER BY total DESC";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(rs.getString("name"), rs.getBigDecimal("total"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /** Returns total revenue per access provider. */
    public java.util.Map<String, BigDecimal> getRevenueByProvider() {
        java.util.Map<String, BigDecimal> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT ap.provider_name, SUM(t.amount) as total " +
                "FROM transactions t " +
                "JOIN access_requests ar ON t.request_id = ar.request_id " +
                "JOIN access_providers ap ON ar.provider_id = ap.provider_id " +
                "WHERE t.payment_status = 'SUCCESS' " +
                "GROUP BY ap.provider_name ORDER BY total DESC";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(rs.getString("provider_name"), rs.getBigDecimal("total"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<Transaction> getRecent(int n) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY processed_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, n);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("request_id"),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                rs.getString("payment_method"),
                rs.getString("payment_status"),
                rs.getTimestamp("processed_at"));
    }
}
