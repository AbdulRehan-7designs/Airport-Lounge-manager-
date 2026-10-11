package com.alm.controller;

import com.alm.dao.NotificationDAO;
import com.alm.model.Notification;
import com.alm.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/notifications")
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO notifDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.setStatus(401);
            return;
        }

        List<Notification> list = notifDAO.getByUser(user.getId());

        // Manual JSON serialization to avoid dependencies for now
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Notification n = list.get(i);
            sb.append("{")
                    .append("\"id\":").append(n.getId()).append(",")
                    .append("\"title\":\"").append(escape(n.getTitle())).append("\",")
                    .append("\"message\":\"").append(escape(n.getMessage())).append("\",")
                    .append("\"type\":\"").append(n.getType()).append("\",")
                    .append("\"read\":").append(n.isRead()).append(",")
                    .append("\"createdAt\":\"").append(n.getCreatedAt()).append("\"")
                    .append("}");
            if (i < list.size() - 1)
                sb.append(",");
        }
        sb.append("]");

        resp.setContentType("application/json");
        resp.getWriter().write(sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.setStatus(401);
            return;
        }

        String action = req.getParameter("action");
        if ("markRead".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            notifDAO.markAsRead(id);
        } else if ("markAllRead".equals(action)) {
            notifDAO.markAllAsRead(user.getId());
        }
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
