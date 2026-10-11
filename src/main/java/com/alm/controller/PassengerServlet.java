package com.alm.controller;

import com.alm.config.Role;
import com.alm.dao.PassengerDAO;
import com.alm.model.Passenger;
import com.alm.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/passengers")
public class PassengerServlet extends HttpServlet {

    private final PassengerDAO passengerDAO = new PassengerDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Only reception-level and above may view passengers
        if (!hasAccess(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            // Single passenger detail
            try {
                int id = Integer.parseInt(idParam);
                Passenger p = passengerDAO.findById(id);
                if (p == null) {
                    resp.sendRedirect(req.getContextPath() + "/passengers");
                    return;
                }
                req.setAttribute("passenger", p);
                req.getRequestDispatcher("/WEB-INF/views/passenger-list.jsp").forward(req, resp);
            } catch (NumberFormatException e) {
                resp.sendRedirect(req.getContextPath() + "/passengers");
            }
            return;
        }

        // List all passengers
        List<Passenger> passengers = passengerDAO.getAllPassengers();
        req.setAttribute("passengers", passengers);
        req.getRequestDispatcher("/WEB-INF/views/passenger-list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!hasAccess(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String action = req.getParameter("action");

        if ("add".equals(action)) {
            String fullName = req.getParameter("fullName");
            String passportNo = req.getParameter("passportNumber");
            String nationality = req.getParameter("nationality");
            String phone = req.getParameter("phone");
            String email = req.getParameter("email");

            if (fullName == null || fullName.trim().isEmpty()
                    || passportNo == null || passportNo.trim().isEmpty()) {
                req.setAttribute("error", "Full name and passport number are required.");
                req.setAttribute("passengers", passengerDAO.getAllPassengers());
                req.getRequestDispatcher("/WEB-INF/views/passenger-list.jsp").forward(req, resp);
                return;
            }

            // Duplicate passport check
            if (passengerDAO.findByPassport(passportNo.trim()) != null) {
                req.setAttribute("error", "A passenger with that passport number already exists.");
                req.setAttribute("passengers", passengerDAO.getAllPassengers());
                req.getRequestDispatcher("/WEB-INF/views/passenger-list.jsp").forward(req, resp);
                return;
            }

            Passenger p = new Passenger();
            p.setFullName(fullName.trim());
            p.setPassportNumber(passportNo.trim());
            p.setNationality(nationality != null ? nationality.trim() : null);
            p.setPhone(phone != null ? phone.trim() : null);
            p.setEmail(email != null ? email.trim() : null);
            p.setActive(true);

            if (passengerDAO.addPassenger(p)) {
                req.setAttribute("message", "Passenger added successfully.");
            } else {
                req.setAttribute("error", "Failed to add passenger. Please try again.");
            }

        } else if ("deactivate".equals(action)) {
            String idParam = req.getParameter("id");
            try {
                int id = Integer.parseInt(idParam);
                passengerDAO.deactivatePassenger(id);
            } catch (NumberFormatException ignored) {
            }
        }

        req.setAttribute("passengers", passengerDAO.getAllPassengers());
        req.getRequestDispatcher("/WEB-INF/views/passenger-list.jsp").forward(req, resp);
    }

    private boolean hasAccess(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null)
            return false;
        User user = (User) session.getAttribute("user");
        if (user == null)
            return false;
        return Role.canAccessReception(user.getRole());
    }
}
