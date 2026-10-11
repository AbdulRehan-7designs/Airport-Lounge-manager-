package com.alm.controller;

import com.alm.dao.TransactionDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@WebServlet("/reports")
public class ReportsServlet extends HttpServlet {
    private final TransactionDAO txDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Fetch analytical data
        Map<String, BigDecimal> dailyRevenue = txDAO.getRevenueByDay(7);
        Map<String, BigDecimal> loungeRevenue = txDAO.getRevenueByLounge();
        Map<String, BigDecimal> providerRevenue = txDAO.getRevenueByProvider();

        // Pass to JSP
        req.setAttribute("dailyRevenue", dailyRevenue);
        req.setAttribute("loungeRevenue", loungeRevenue);
        req.setAttribute("providerRevenue", providerRevenue);

        req.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(req, resp);
    }
}
