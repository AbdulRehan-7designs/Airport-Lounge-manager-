package com.alm.controller;

import com.alm.model.User;
import com.alm.service.AuthService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        User user = authService.login(u, p);
        if (user != null) {
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
            resp.sendRedirect(redirectByRole(role, req));
        } else {
            req.setAttribute("error", "Invalid Credentials");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    /**
     * Role-based redirect after login. Each role lands on their primary screen.
     * Compares against Role.name() (the DB string) — not the enum reference.
     */
    private String redirectByRole(String role, HttpServletRequest req) {
        String ctx = req.getContextPath();
        switch (role) {
            case "ADMIN":
                return ctx + "/dashboard";
            case "OPERATIONS_MANAGER":
                return ctx + "/dashboard";
            case "SUPERVISOR":
                return ctx + "/lounges";
            case "AUDITOR":
                return ctx + "/transactions";
            case "RECEPTION":
                return ctx + "/access-requests";
            case "PASSENGER":
                return ctx + "/dashboard";
            default:
                return ctx + "/dashboard";
        }
    }
}
