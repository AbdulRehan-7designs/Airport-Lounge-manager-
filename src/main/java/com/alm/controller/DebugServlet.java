package com.alm.controller;

import com.alm.dao.UserDAO;
import com.alm.model.User;
import com.alm.util.PasswordUtil;
import com.alm.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

@WebServlet("/debug")
public class DebugServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String step = "";
        String error = null;
        User user = null;
        Boolean passwordOk = null;
        String storedPasswordPreview = "";

        try {
            step = "Link initialization...";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn != null) {
                    // Just testing connection
                }
            }

            step = "Resolving user 'admin'...";
            user = new UserDAO().findByUsername("admin");
            if (user != null) {
                String stored = user.getPassword();
                String roleInDb = user.getRole();
                storedPasswordPreview = (stored != null && stored.length() >= 5)
                        ? (stored.substring(0, 4) + "****" + " [Role: '" + roleInDb + "']")
                        : "invalid_format";

                step = "Executing validation...";
                passwordOk = PasswordUtil.verifyPassword("admin", stored);
            }
        } catch (Exception e) {
            error = e.getClass().getName() + ": " + e.getMessage();
        }

        req.setAttribute("step", step);
        req.setAttribute("error", error);
        req.setAttribute("user", user);
        req.setAttribute("passwordOk", passwordOk);
        req.setAttribute("storedPasswordPreview", storedPasswordPreview);

        req.getRequestDispatcher("/login-debug.jsp").forward(req, resp);
    }
}
