package com.alm.filter;

import com.alm.model.User;
import com.alm.config.Role;
import com.alm.config.Permission;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalTime;

/**
 * Filter to provide common view attributes to all JSPs,
 * reducing the need for scriptlets in the View layer.
 */
@WebFilter(filterName = "ViewContextFilter", urlPatterns = { "/*" })
public class ViewContextFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user != null) {
            String role = user.getRole() != null ? user.getRole().toUpperCase() : "";

            // Set basic user attributes
            req.setAttribute("currentUser", user);
            req.setAttribute("userRole", role);

            // Set Greeting
            int hour = LocalTime.now().getHour();
            String greeting = "Good day";
            if (hour < 12)
                greeting = "Good morning";
            else if (hour < 17)
                greeting = "Good afternoon";
            else
                greeting = "Good evening";
            req.setAttribute("greeting", greeting);

            // Set Permission Flags for Sidebar/UI
            req.setAttribute("isAdmin", Role.isAdmin(role));
            req.setAttribute("isManager", Role.canAccessManager(role));
            req.setAttribute("isAuditor", "AUDITOR".equals(role));
            req.setAttribute("isPassenger", Role.isPassenger(role));
            req.setAttribute("canDesk", Role.canAccessReception(role));
            req.setAttribute("canInventory", Role.hasPermission(role, Permission.VIEW_LOUNGE_INVENTORY));
            req.setAttribute("canManageLounges", Role.hasPermission(role, Permission.MANAGE_LOUNGES));

            // Set Avatar Initials
            String initials = "?";
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                initials = user.getUsername().trim().substring(0, 1).toUpperCase();
            }
            req.setAttribute("userInitials", initials);

            // Role Display Name and Permissions
            Role rEnum = Role.fromString(role);
            req.setAttribute("roleDisplayName", rEnum != null ? rEnum.getDisplayName() : role);

            if (rEnum != null) {
                java.util.List<String> permList = new java.util.ArrayList<>();
                for (Permission p : rEnum.getPermissions()) {
                    permList.add(p.name().toLowerCase().replace('_', ' '));
                }
                req.setAttribute("currentUserPermissions", permList);
            }

            // Context Path
            req.setAttribute("ctx", req.getContextPath());
        }

        chain.doFilter(request, response);
    }
}
