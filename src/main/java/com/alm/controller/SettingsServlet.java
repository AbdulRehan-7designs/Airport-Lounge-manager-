package com.alm.controller;

import com.alm.dao.SettingDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

@WebServlet("/settings")
public class SettingsServlet extends HttpServlet {
    private final SettingDAO settingDAO = new SettingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String[]> settings = settingDAO.getAll();
        req.setAttribute("settings", settings);
        req.getRequestDispatcher("/WEB-INF/views/settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Enumeration<String> params = req.getParameterNames();
        while (params.hasMoreElements()) {
            String key = params.nextElement();
            if (!key.equals("submit")) {
                settingDAO.update(key, req.getParameter(key));
            }
        }
        resp.sendRedirect(req.getContextPath() + "/settings?saved=true");
    }
}
