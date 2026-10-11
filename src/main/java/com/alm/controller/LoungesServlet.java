package com.alm.controller;

import com.alm.dao.LoungeAmenityDAO;
import com.alm.dao.LoungeDAO;
import com.alm.model.Lounge;
import com.alm.model.LoungeAmenity;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/lounges")
public class LoungesServlet extends HttpServlet {
    private final LoungeDAO loungeDAO = new LoungeDAO();
    private final LoungeAmenityDAO amenityDAO = new LoungeAmenityDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);
                Lounge lounge = loungeDAO.getById(id);
                if (lounge == null) {
                    resp.sendRedirect(req.getContextPath() + "/lounges");
                    return;
                }
                List<LoungeAmenity> amenities = amenityDAO.getByLoungeId(id);
                req.setAttribute("lounge", lounge);
                req.setAttribute("amenities", amenities);
                req.getRequestDispatcher("/WEB-INF/views/lounge-detail.jsp").forward(req, resp);
                return;
            } catch (NumberFormatException e) {
                resp.sendRedirect(req.getContextPath() + "/lounges");
                return;
            }
        }
        List<Lounge> lounges = loungeDAO.getAllLounges();
        req.setAttribute("lounges", lounges);
        req.getRequestDispatcher("/WEB-INF/views/lounge-list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("addAmenity".equals(action)) {
            String loungeIdParam = req.getParameter("loungeId");
            String itemName = req.getParameter("itemName");
            String totalParam = req.getParameter("totalCount");
            String availableParam = req.getParameter("availableCount");
            if (loungeIdParam != null && itemName != null && !itemName.trim().isEmpty() && totalParam != null && availableParam != null) {
                try {
                    int loungeId = Integer.parseInt(loungeIdParam);
                    int total = Integer.parseInt(totalParam);
                    int available = Integer.parseInt(availableParam);
                    LoungeAmenity a = new LoungeAmenity();
                    a.setLoungeId(loungeId);
                    a.setItemName(itemName.trim());
                    a.setTotalCount(total);
                    a.setAvailableCount(available);
                    amenityDAO.add(a);
                } catch (NumberFormatException ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/lounges?id=" + req.getParameter("loungeId"));
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/lounges");
    }
}
