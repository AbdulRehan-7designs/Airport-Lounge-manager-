package com.alm.controller;

import com.alm.config.Role;
import com.alm.dao.AccessRequestDAO;
import com.alm.dao.LoungeDAO;
import com.alm.dao.PassengerDAO;
import com.alm.dao.AccessProviderDAO;
import com.alm.model.AccessRequest;
import com.alm.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/access-requests")
public class AccessRequestServlet extends HttpServlet {

    private final AccessRequestDAO requestDAO = new AccessRequestDAO();
    private final LoungeDAO loungeDAO = new LoungeDAO();
    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final AccessProviderDAO providerDAO = new AccessProviderDAO();
    private final com.alm.dao.UserDAO userDAO = new com.alm.dao.UserDAO();
    private final com.alm.dao.NotificationDAO notifDAO = new com.alm.dao.NotificationDAO();

    // ── GET: list all requests (or just PENDING) ──────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Choose list: ?filter=all shows everything; default shows PENDING only
        String filter = req.getParameter("filter");
        List<AccessRequest> requests = "all".equals(filter)
                ? requestDAO.getAll()
                : requestDAO.getPending();

        req.setAttribute("requests", requests);
        req.setAttribute("filter", filter);
        req.setAttribute("pendingCount", requestDAO.countPending());

        // Pre-selection logic
        String selectedLoungeId = req.getParameter("loungeId");
        if (selectedLoungeId != null && !selectedLoungeId.isEmpty()) {
            req.setAttribute("preSelectedLoungeId", selectedLoungeId);
        }

        if ("PASSENGER".equals(user.getRole())) {
            com.alm.model.Passenger p = passengerDAO.findByUserId(user.getId());
            if (p != null) {
                req.setAttribute("preSelectedPassengerId", p.getId());
            }
        }

        // Provide dropdowns for the "New Request" form
        req.setAttribute("lounges", loungeDAO.getAllLounges());
        req.setAttribute("passengers", passengerDAO.getAllPassengers());
        req.setAttribute("providers", providerDAO.getAll());

        req.setAttribute("pageTitle", "Access Control");
        req.getRequestDispatcher("/WEB-INF/views/access-request.jsp").forward(req, resp);
    }

    // ── POST: approve / deny / create ─────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");

        if ("approve".equals(action) || "deny".equals(action)) {
            // Only reception-level and above may decide
            if (!Role.canAccessReception(user.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            String idParam = req.getParameter("requestId");
            String reason = req.getParameter("reason");
            try {
                int requestId = Integer.parseInt(idParam);
                String status = "approve".equals(action) ? "APPROVED" : "DENIED";
                if (requestDAO.decide(requestId, status, reason)) {
                    // Notify passenger (if they are a user)
                    AccessRequest r = requestDAO.getById(requestId);
                    if (r != null) {
                        passengerDAO.getUserIdByPassengerId(r.getPassengerId())
                                .ifPresent(uid -> notifDAO.addNotification(uid, "Request Update",
                                        "Your access to " + r.getLoungeName() + " was " + status.toLowerCase() + ".",
                                        "approve".equals(action) ? "SUCCESS" : "ERROR"));
                    }
                }
            } catch (NumberFormatException ignored) {
            }

        } else if ("create".equals(action)) {
            // Any logged-in user can submit a request
            String passengerIdParam = req.getParameter("passengerId");
            String loungeIdParam = req.getParameter("loungeId");
            String providerIdParam = req.getParameter("providerId");

            // Intelligence parameters
            String ticketClass = req.getParameter("ticketClass");
            String eliteStatus = req.getParameter("eliteStatus");
            String cardType = req.getParameter("cardType");
            String membershipType = req.getParameter("membershipType");
            String amountPaidStr = req.getParameter("amountPaid");

            try {
                AccessRequest r = new AccessRequest();
                r.setPassengerId(Integer.parseInt(passengerIdParam));
                r.setLoungeId(Integer.parseInt(loungeIdParam));
                if (providerIdParam != null && !providerIdParam.isEmpty()) {
                    r.setProviderId(Integer.parseInt(providerIdParam));
                }

                // Populate intelligence fields
                r.setTicketClass(ticketClass);
                r.setEliteStatus(eliteStatus);
                r.setCardType(cardType);
                r.setMembershipType(membershipType);
                if (amountPaidStr != null && !amountPaidStr.isEmpty()) {
                    r.setAmountPaid(Double.parseDouble(amountPaidStr));
                }

                // Run Decision Engine
                com.alm.service.AccessFlowEngine engine = new com.alm.service.AccessFlowEngine();
                com.alm.service.AccessFlowEngine.Decision decision = engine.evaluate(r);

                r.setStatus(decision.status);
                r.setDecisionReason(decision.reason);

                if (requestDAO.create(r)) {
                    // Notify all reception staff if it's still PENDING
                    if ("PENDING".equals(r.getStatus())) {
                        List<User> staff = userDAO.findByRole("RECEPTION");
                        for (User s : staff) {
                            notifDAO.addNotification(s.getId(), "New Request",
                                    "A new access request is awaiting review.", "INFO");
                        }
                    } else {
                        // Notify passenger of auto-decision
                        passengerDAO.getUserIdByPassengerId(r.getPassengerId())
                                .ifPresent(uid -> notifDAO.addNotification(uid, "Access Request Update",
                                        "Your request for " + r.getLoungeName() + " has been auto-"
                                                + r.getStatus().toLowerCase() + " by SmartFlow system. Reason: "
                                                + r.getDecisionReason(),
                                        "APPROVED".equals(r.getStatus()) ? "SUCCESS" : "ERROR"));
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        resp.sendRedirect(req.getContextPath() + "/access-requests");
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private User getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null ? (User) session.getAttribute("user") : null;
    }
}
