package com.alm.controller;

import com.alm.dao.TransactionDAO;
import com.alm.model.Transaction;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/transactions")
public class TransactionsServlet extends HttpServlet {
    private final TransactionDAO txDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String status = req.getParameter("status");
        List<Transaction> transactions;

        if (status != null && !status.isEmpty()) {
            transactions = txDAO.getByStatus(status.toUpperCase());
        } else {
            transactions = txDAO.getAll();
        }

        req.setAttribute("transactions", transactions);
        req.getRequestDispatcher("/WEB-INF/views/transactions.jsp").forward(req, resp);
    }
}
