package com.alm.controller;

import com.alm.dao.AccessRequestDAO;
import com.alm.dao.LoungeDAO;
import com.alm.dao.TransactionDAO;
import com.alm.dao.AuditDAO;
import com.alm.dao.UserDAO;
import com.alm.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final LoungeDAO loungeDAO = new LoungeDAO();
    private final AccessRequestDAO requestDAO = new AccessRequestDAO();
    private final TransactionDAO txDAO = new TransactionDAO();
    private final AuditDAO auditDAO = new AuditDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            HttpSession session = req.getSession(false);
            User user = session != null ? (User) session.getAttribute("user") : null;
            if (user == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            String role = user.getRole() != null ? user.getRole().toUpperCase().trim() : "";
            // Normalise common DB typo: PASSANGER → PASSENGER
            if ("PASSANGER".equals(role)) {
                role = "PASSENGER";
            }

            // ── Resolve Role enum for permission checks ────────────────────
            com.alm.config.Role resolvedRole = com.alm.config.Role.fromString(role);

            // ── Sidebar Boolean Flags ──────────────────────────────────────
            boolean isPassenger = "PASSENGER".equals(role);
            boolean isAdmin = com.alm.config.Role.isAdmin(role);
            boolean isManager = com.alm.config.Role.canAccessManager(role);
            boolean isAuditor = resolvedRole == com.alm.config.Role.AUDITOR;
            boolean canDesk = com.alm.config.Role.hasPermission(role, com.alm.config.Permission.CHECK_IN_GUEST)
                    || com.alm.config.Role.hasPermission(role, com.alm.config.Permission.VIEW_ACCESS_REQUESTS);
            boolean canInventory = com.alm.config.Role.hasPermission(role,
                    com.alm.config.Permission.VIEW_LOUNGE_INVENTORY)
                    || com.alm.config.Role.hasPermission(role, com.alm.config.Permission.UPDATE_LOUNGE_STATUS);

            req.setAttribute("isPassenger", isPassenger);
            req.setAttribute("isAdmin", isAdmin);
            req.setAttribute("isManager", isManager);
            req.setAttribute("isAuditor", isAuditor);
            req.setAttribute("canDesk", canDesk);
            req.setAttribute("canInventory", canInventory);
            req.setAttribute("roleDisplayName", resolvedRole != null ? resolvedRole.getDisplayName() : role);

            // ── User Initials for sidebar avatar ───────────────────────────
            String username = user.getUsername() != null ? user.getUsername() : "?";
            String initials = username.length() >= 1
                    ? String.valueOf(username.charAt(0)).toUpperCase()
                    : "?";
            req.setAttribute("userInitials", initials);

            // ── Common Metrics ─────────────────────────────────────────────
            req.setAttribute("totalCapacity", loungeDAO.getTotalCapacity());
            req.setAttribute("activeLounges", loungeDAO.countActive());
            req.setAttribute("totalLounges", loungeDAO.countAll());
            req.setAttribute("pendingCount", requestDAO.countPending());

            BigDecimal revenue = txDAO.getTotalRevenue();
            req.setAttribute("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);

            // ── Time-based Greeting ────────────────────────────────────────
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String greeting = (hour < 12) ? "Good morning" : (hour < 17) ? "Good afternoon" : "Good evening";
            req.setAttribute("greeting", greeting);

            // ── Role-Specific Data ─────────────────────────────────────────
            if (isPassenger) {
                req.setAttribute("myRecentRequests", requestDAO.getByPassenger(user.getId()));
                req.setAttribute("allLounges", loungeDAO.getAllLounges());
            } else if ("RECEPTION".equals(role)) {
                req.setAttribute("guestRoster", requestDAO.getLiveGuestRoster());
                req.setAttribute("pendingRequests", requestDAO.getPending());
                req.setAttribute("primaryLounge", loungeDAO.getAllLounges().stream().findFirst().orElse(null));
            } else if ("OPERATIONS_MANAGER".equals(role)) {
                req.setAttribute("avgStayDuration", 120);
                req.setAttribute("waitlistCount", requestDAO.countPending());
                req.setAttribute("recentTransactions", txDAO.getRecent(10));
                req.setAttribute("allLounges", loungeDAO.getAllLounges());
            } else if (isAdmin) {
                req.setAttribute("totalUsers", userDAO.countUsers());
                req.setAttribute("systemLogs", auditDAO.getRecentActivity(20));
                req.setAttribute("allLounges", loungeDAO.getAllLounges());
                req.setAttribute("recentTransactions", txDAO.getRecent(10));
                req.setAttribute("revenueByLounge", txDAO.getRevenueByLounge());
                req.setAttribute("revenueByProvider", txDAO.getRevenueByProvider());
                req.setAttribute("revenueLast7Days", txDAO.getRevenueByDay(7));
                java.util.List<User> allUsers = userDAO.getAll();
                req.setAttribute("recentUsers", allUsers.subList(0, Math.min(5, allUsers.size())));
            } else if (isAuditor) {
                req.setAttribute("recentTransactions", txDAO.getRecent(10));
                req.setAttribute("allLounges", loungeDAO.getAllLounges());
            } else if ("SUPERVISOR".equals(role)) {
                req.setAttribute("guestRoster", requestDAO.getLiveGuestRoster());
                req.setAttribute("pendingRequests", requestDAO.getPending());
                req.setAttribute("allLounges", loungeDAO.getAllLounges());
            }

            req.setAttribute("currentUser", user);
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/html");
            resp.getWriter().println("<html><body style='font-family:sans-serif; padding:2rem;'>");
            resp.getWriter().println("<h2 style='color:#e11d48;'>Dashboard Execution Error</h2>");
            resp.getWriter().println("<pre style='background:#f1f5f9; padding:1rem; border-radius:8px;'>");
            e.printStackTrace(new java.io.PrintWriter(resp.getWriter()));
            resp.getWriter().println("</pre></body></html>");
        }
    }
}
