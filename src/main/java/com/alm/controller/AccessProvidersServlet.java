package com.alm.controller;

import com.alm.dao.AccessProviderDAO;
import com.alm.model.AccessProvider;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/access-providers")
public class AccessProvidersServlet extends HttpServlet {
    private static final String[] TYPES = { "AIRLINE", "CREDIT_CARD", "AGGREGATOR", "PAY_PER_USE" };
    private final AccessProviderDAO dao = new AccessProviderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);
                AccessProvider p = dao.getById(id);
                if (p != null) {
                    req.setAttribute("provider", p);
                    req.setAttribute("types", TYPES);
                    req.getRequestDispatcher("/WEB-INF/views/access-provider-form.jsp").forward(req, resp);
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if ("new".equals(req.getParameter("new"))) {
            req.setAttribute("provider", new AccessProvider());
            req.setAttribute("types", TYPES);
            req.getRequestDispatcher("/WEB-INF/views/access-provider-form.jsp").forward(req, resp);
            return;
        }
        List<AccessProvider> list = dao.getAll();
        req.setAttribute("providers", list);
        req.getRequestDispatcher("/WEB-INF/views/access-provider-list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        String name = req.getParameter("providerName");
        String type = req.getParameter("providerType");
        String activeStr = req.getParameter("active");
        if (name == null || name.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/access-providers");
            return;
        }
        AccessProvider p = new AccessProvider();
        p.setProviderName(name.trim());
        p.setProviderType(type.trim().toUpperCase());
        p.setActive(activeStr == null || "on".equals(activeStr) || "true".equalsIgnoreCase(activeStr));
        if (idParam != null && !idParam.isEmpty()) {
            try {
                p.setId(Integer.parseInt(idParam));
                dao.update(p);
            } catch (NumberFormatException ignored) {
            }
        } else {
            dao.insert(p);
        }
        resp.sendRedirect(req.getContextPath() + "/access-providers");
    }
}
