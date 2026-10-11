<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="com.alm.model.AccessProvider" %>
        <%@ page import="java.util.List" %>
            <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Providers — Airport Lounge Manager</title>
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                        rel="stylesheet">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                    <style>
                        .provider-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
                            gap: 1.25rem;
                            margin-top: 1.5rem;
                        }

                        .provider-card {
                            background: var(--clr-card);
                            border: 1px solid var(--clr-border);
                            border-radius: var(--radius-lg);
                            padding: 1.5rem;
                            display: flex;
                            flex-direction: column;
                            gap: 1rem;
                            transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
                            position: relative;
                            overflow: hidden;
                        }

                        .provider-card:hover {
                            transform: translateY(-4px);
                            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                            border-color: var(--clr-primary);
                        }

                        .provider-icon {
                            width: 48px;
                            height: 48px;
                            border-radius: 12px;
                            background: rgba(99, 102, 241, 0.1);
                            color: var(--clr-primary-lit);
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 1.5rem;
                        }

                        .provider-type {
                            font-size: 0.65rem;
                            text-transform: uppercase;
                            letter-spacing: 0.05em;
                            color: var(--clr-faint);
                            font-weight: 700;
                        }

                        .provider-name {
                            font-size: 1.1rem;
                            font-weight: 700;
                            color: var(--clr-text);
                        }

                        .provider-status {
                            display: flex;
                            align-items: center;
                            gap: 0.5rem;
                            font-size: 0.75rem;
                            margin-top: auto;
                        }
                    </style>
                </head>

                <body>
                    <div class="app-shell">
                        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

                            <div class="main-content">
                                <div class="topbar">
                                    <button class="sidebar-toggle" onclick="toggleSidebar()">☰</button>
                                    <span class="topbar-title">Access Providers</span>
                                    <div class="topbar-right">
                                        <span class="topbar-clock" id="live-clock"></span>
                                    </div>
                                </div>

                                <div class="page-content">
                                    <div class="page-header"
                                        style="display:flex; justify-content:space-between; align-items:flex-start;">
                                        <div>
                                            <h1>💳 Access Providers</h1>
                                            <p>Integrate and manage third-party access networks (Airlines, Credit Cards,
                                                Priority Pass).</p>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/access-providers?new=1"
                                            class="btn btn-primary">＋ New Provider</a>
                                    </div>

                                    <% List<AccessProvider> providers = (List<AccessProvider>)
                                            request.getAttribute("providers");
                                            if (providers == null || providers.isEmpty()) {
                                            %>
                                            <div class="glass-panel">
                                                <div class="empty-state">
                                                    <div class="empty-state-icon">📡</div>
                                                    <p>No providers configured. Connect your first access network to
                                                        start processing guests.</p>
                                                </div>
                                            </div>
                                            <% } else { %>
                                                <div class="provider-grid">
                                                    <% for (AccessProvider p : providers) { String
                                                        type=p.getProviderType() !=null ? p.getProviderType()
                                                        : "NETWORK" ; String icon=type.contains("AIRLINE") ? "✈️" :
                                                        type.contains("CARD") ? "💳" : "🌐" ; %>
                                                        <div class="provider-card">
                                                            <div
                                                                style="display:flex; justify-content:space-between; align-items:start;">
                                                                <div class="provider-icon">
                                                                    <%= icon %>
                                                                </div>
                                                                <span class="badge <%= p.isActive() ? " badge-active"
                                                                    : "badge-inactive" %>">
                                                                    <span class="badge-dot"></span>
                                                                    <%= p.isActive() ? "Active" : "Disabled" %>
                                                                </span>
                                                            </div>
                                                            <div>
                                                                <div class="provider-type">
                                                                    <%= type %>
                                                                </div>
                                                                <div class="provider-name">
                                                                    <%= p.getProviderName() %>
                                                                </div>
                                                            </div>
                                                            <div style="display:flex; gap:0.5rem; margin-top:0.5rem;">
                                                                <a href="access-providers?id=<%= p.getId() %>"
                                                                    class="btn btn-ghost btn-sm"
                                                                    style="flex:1;">Configure</a>
                                                            </div>
                                                            <div style="position:absolute; bottom:0; left:0; right:0; height:2px; background: <%= p.isActive() ? "
                                                                var(--clr-emerald)" : "var(--clr-muted)" %>;
                                                                opacity:0.3;"></div>
                                                        </div>
                                                        <% } %>
                                                </div>
                                                <% } %>

                                </div>
                            </div>
                    </div>

                    <script>
                        function updateClock() {
                            const el = document.getElementById('live-clock');
                            if (el) el.textContent = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
                        }
                        updateClock(); setInterval(updateClock, 1000);

                        function toggleSidebar() {
                            document.getElementById('app-sidebar').classList.toggle('open');
                        }
                    </script>
                </body>

                </html>