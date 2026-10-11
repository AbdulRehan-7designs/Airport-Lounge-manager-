package com.alm.controller;

import com.alm.dao.UserDAO;
import com.alm.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/users")
public class UserManagementServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = userDAO.getAll();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/WEB-INF/views/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String idStr = req.getParameter("id");

        if (idStr != null) {
            long id = Long.parseLong(idStr);
            if ("updateRole".equals(action)) {
                String role = req.getParameter("role");
                userDAO.updateRole(id, role);
            } else if ("toggleActive".equals(action)) {
                String active = req.getParameter("active");
                userDAO.updateActiveStatus(id, "true".equals(active));
            }
        }
        resp.sendRedirect(req.getContextPath() + "/users");
    }
}
