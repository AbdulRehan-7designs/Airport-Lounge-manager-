package com.alm.controller;

import com.alm.dao.AirportDAO;
import com.alm.model.Airport;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/airports")
public class AirportsServlet extends HttpServlet {
    private final AirportDAO airportDAO = new AirportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Airport> airports = airportDAO.getAll();
        req.setAttribute("airports", airports);
        req.getRequestDispatcher("/WEB-INF/views/airports.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("create".equals(action)) {
            Airport a = new Airport();
            a.setName(req.getParameter("name"));
            a.setIataCode(req.getParameter("iataCode"));
            a.setCity(req.getParameter("city"));
            a.setCountry(req.getParameter("country"));
            a.setActive(true);
            airportDAO.create(a);
        } else if ("toggle".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            boolean active = "true".equals(req.getParameter("active"));
            airportDAO.toggleActive(id, active);
        }

        resp.sendRedirect(req.getContextPath() + "/airports");
    }
}
