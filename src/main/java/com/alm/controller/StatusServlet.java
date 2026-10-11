package com.alm.controller;

import com.alm.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/status")
public class StatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean sqlOk = false;
        String sqlMessage = "";
        long start = System.currentTimeMillis();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                sqlOk = true;
                String dbName = conn.getCatalog();
                sqlMessage = "Primary node reachable: " + (dbName != null ? dbName : "default");
            } else {
                sqlMessage = "Link established but connection is null/closed.";
            }
        } catch (SQLException e) {
            sqlMessage = e.getMessage();
        }

        long latency = System.currentTimeMillis() - start;

        req.setAttribute("sqlOk", sqlOk);
        req.setAttribute("sqlMessage", sqlMessage);
        req.setAttribute("latency", latency);

        req.getRequestDispatcher("/status.jsp").forward(req, resp);
    }
}
