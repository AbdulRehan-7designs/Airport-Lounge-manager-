package com.alm.filter;

import com.alm.config.Permission;
import com.alm.config.Role;
import com.alm.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enterprise RBAC filter — guards every URL pattern against the permission
 * model
 * defined in {@link Role} and {@link Permission}.
 *
 * <h3>Decision flow for each request:</h3>
 * <ol>
 * <li>Public paths (login, register, assets) → pass through immediately.</li>
 * <li>No valid session → redirect to /login.</li>
 * <li>URL matches a permission rule → check {@link Role#hasPermission}.</li>
 * <li>No rule matches → any authenticated user may proceed (e.g.
 * /dashboard).</li>
 * </ol>
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = { "/*" })
public class AuthFilter implements Filter {

    // ── Public paths — no authentication required ─────────────────────────────
    private static final Set<String> PUBLIC_EXACT = new HashSet<>(Arrays.asList(
            "", "/", "/index.jsp", "/status.jsp", "/login-debug.jsp", "/health.jsp"));

    private static final Set<String> PUBLIC_PREFIXES = new HashSet<>(Arrays.asList(
            "/login", "/logout"));

    /**
     * URL-prefix → required Permission mapping.
     * Evaluated in insertion order (LinkedHashMap) — most specific first.
     * Any authenticated URL not listed here is accessible by ALL logged-in roles.
     */
    private static final Map<String, Permission> URL_PERMISSION_RULES = new LinkedHashMap<>();

    static {
        // ── System Administrator only ────────────────────────────────────────
        URL_PERMISSION_RULES.put("/users", Permission.MANAGE_USER_ACCOUNTS);
        URL_PERMISSION_RULES.put("/settings", Permission.CONFIGURE_SYSTEM);
        URL_PERMISSION_RULES.put("/airports", Permission.MANAGE_AIRPORTS);
        URL_PERMISSION_RULES.put("/staff", Permission.MANAGE_STAFF);
        URL_PERMISSION_RULES.put("/access-providers", Permission.MANAGE_ACCESS_PROVIDERS);

        // ── Manager+ and Auditor (financial read access) ──────────────────────
        URL_PERMISSION_RULES.put("/reports", Permission.VIEW_FINANCIAL_REPORTS);
        URL_PERMISSION_RULES.put("/transactions", Permission.VIEW_TRANSACTION_HISTORY);

        // ── Supervisor+ (lounge status + inventory) ───────────────────────────
        URL_PERMISSION_RULES.put("/lounges", Permission.VIEW_LOUNGE_INVENTORY);

        // ── Reception desk (check-in / access-requests / passengers) ─────────
        URL_PERMISSION_RULES.put("/access-requests", Permission.VIEW_ACCESS_REQUESTS);
        URL_PERMISSION_RULES.put("/passengers", Permission.MANAGE_PASSENGERS);

        // NOTE: /dashboard, /profile — accessible by all authenticated roles.
    }

    // ── Filter logic ──────────────────────────────────────────────────────────

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // getServletPath() is always context-stripped and query-string-free.
        String path = req.getServletPath();
        if (path == null)
            path = "";

        // ① Public resources — no auth needed
        if (isPublic(path) || path.startsWith("/assets/")) {
            chain.doFilter(req, res);
            return;
        }

        // ② Authentication check
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // ③ Permission check — find the first matching URL rule
        String userRole = user.getRole() != null ? user.getRole() : "";
        for (Map.Entry<String, Permission> rule : URL_PERMISSION_RULES.entrySet()) {
            String prefix = rule.getKey();
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                if (!Role.hasPermission(userRole, rule.getValue())) {
                    Role resolved = Role.fromString(userRole);
                    String roleLabel = resolved != null ? resolved.getDisplayName() : userRole;
                    res.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Access denied — your role (" + roleLabel
                                    + ") does not have permission for this resource.");
                    return;
                }
                break; // Matched and authorised — no need to check further rules
            }
        }

        // ④ No specific rule → any authenticated user (e.g. /dashboard, /profile)
        chain.doFilter(req, res);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isPublic(String path) {
        if (PUBLIC_EXACT.contains(path))
            return true;
        for (String prefix : PUBLIC_PREFIXES) {
            // Exact match OR sub-path (e.g. /login/sso), but NOT /loginExtra
            if (path.equals(prefix) || path.startsWith(prefix + "/"))
                return true;
        }
        return false;
    }
}
