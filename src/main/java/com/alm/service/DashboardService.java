package com.alm.service;

import com.alm.dao.LoungeDAO;
import com.alm.dao.PassengerDAO;
import com.alm.dao.TransactionDAO;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import com.alm.model.Transaction;

/**
 * Service to aggregate metrics for the Admin Dashboard.
 */
public class DashboardService {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final LoungeDAO loungeDAO = new LoungeDAO();
    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final com.alm.dao.UserDAO userDAO = new com.alm.dao.UserDAO();

    public static class DashboardData {
        public int activeLounges;
        public int passengerCount;
        public int staffOnDuty;
        public int pendingComplaints;
        public BigDecimal totalRevenue;
        public BigDecimal monthlyRevenue;
        public double occupancyRate;
        public Map<String, BigDecimal> revenueTrend;
        public Map<String, BigDecimal> revenueByLounge;
        public List<Transaction> recentTransactions;
    }

    public DashboardData getDashboardStats() {
        DashboardData data = new DashboardData();
        data.activeLounges = loungeDAO.countActive();
        data.passengerCount = passengerDAO.getAllPassengers().size();
        data.staffOnDuty = userDAO.countUsers();
        data.pendingComplaints = 2; // Mock data since Complaint module DB isn't finalized
        data.totalRevenue = transactionDAO.getTotalRevenue();

        // Monthly revenue (simplified: last 30 days)
        Map<String, BigDecimal> dailyRev = transactionDAO.getRevenueByDay(30);
        data.monthlyRevenue = dailyRev.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.revenueTrend = dailyRev;
        data.revenueByLounge = transactionDAO.getRevenueByLounge();
        data.recentTransactions = transactionDAO.getRecent(10);

        // Mock occupancy for now as we don't have a real occupancy log yet
        data.occupancyRate = 72.5;

        return data;
    }
}
