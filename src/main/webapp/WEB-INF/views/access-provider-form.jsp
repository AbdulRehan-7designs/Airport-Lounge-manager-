<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="com.alm.model.AccessProvider" %>
        <%@ page import="java.util.Arrays" %>
            <% AccessProvider p=(AccessProvider) request.getAttribute("provider"); String[] types=(String[])
                request.getAttribute("types"); if (p==null) p=new AccessProvider(); if (types==null) types=new String[]
                { "AIRLINE" , "CREDIT_CARD" , "AGGREGATOR" }; boolean isNew=p.getId()==0; %>
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>
                        <%= isNew ? "Add" : "Edit" %> Provider — Airport Lounge Manager
                    </title>
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                        rel="stylesheet">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                </head>

                <body>
                    <div class="app-shell">
                        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

                            <div class="main-content">
                                <div class="topbar">
                                    <button class="sidebar-toggle" onclick="toggleSidebar()">☰</button>
                                    <span class="topbar-title">Configuration</span>
                                    <div class="topbar-right">
                                        <span class="topbar-clock" id="live-clock"></span>
                                    </div>
                                </div>

                                <div class="page-content">
                                    <div class="page-header">
                                        <h1>
                                            <%= isNew ? "📡 Register Provider" : "⚙️ Edit Context" %>
                                        </h1>
                                        <p>Configure how this access network integrates with the lounge check-in system.
                                        </p>
                                    </div>

                                    <div class="glass-panel" style="max-width: 600px;">
                                        <div class="glass-panel-header">
                                            <h2>Provider Identity</h2>
                                        </div>
                                        <div class="glass-panel-body">
                                            <form action="${pageContext.request.contextPath}/access-providers"
                                                method="post">
                                                <% if (!isNew) { %><input type="hidden" name="id"
                                                        value="<%= p.getId() %>">
                                                    <% } %>

                                                        <div class="form-group">
                                                            <label class="form-label" for="providerName">Official
                                                                Name</label>
                                                            <input class="form-control" type="text" id="providerName"
                                                                name="providerName"
                                                                value="<%= p.getProviderName() != null ? p.getProviderName() : "" %>"
                                                                placeholder="e.g. Priority Pass, Emirates Skywards"
                                                                required>
                                                        </div>

                                                        <div class="form-group">
                                                            <label class="form-label" for="providerType">Integration
                                                                Type</label>
                                                            <select class="form-control" id="providerType"
                                                                name="providerType">
                                                                <% for (String t : types) { %>
                                                                    <option value="<%= t %>"
                                                                        <%=t.equals(p.getProviderType()) ? "selected"
                                                                        : "" %>><%= t %>
                                                                    </option>
                                                                    <% } %>
                                                            </select>
                                                            <p
                                                                style="font-size: 0.75rem; color: var(--clr-muted); margin-top: 0.4rem;">
                                                                Determines the validation protocol used during passenger
                                                                check-in.
                                                            </p>
                                                        </div>

                                                        <div class="form-group"
                                                            style="margin-top: 1.5rem; background: rgba(8,13,26,0.3); padding: 1rem; border-radius: 12px; border: 1px solid var(--clr-border);">
                                                            <label
                                                                style="display: flex; align-items: center; gap: 0.75rem; cursor: pointer;">
                                                                <input type="checkbox" name="active" <%=p.isActive()
                                                                    ? "checked" : "" %> style="width: 1.1rem; height:
                                                                1.1rem; accent-color: var(--clr-primary);">
                                                                <span
                                                                    style="font-weight: 600; font-size: 0.9rem;">Network
                                                                    Active</span>
                                                            </label>
                                                            <p
                                                                style="font-size: 0.75rem; color: var(--clr-muted); margin-left: 1.85rem; margin-top: 0.2rem;">
                                                                If disabled, no access requests from this provider will
                                                                be processed.
                                                            </p>
                                                        </div>

                                                        <hr
                                                            style="border: none; border-top: 1px solid var(--clr-border); margin: 2rem 0;">

                                                        <div
                                                            style="display: flex; gap: 0.75rem; justify-content: flex-end;">
                                                            <a href="access-providers" class="btn btn-ghost">Cancel</a>
                                                            <button type="submit" class="btn btn-primary">
                                                                <%= isNew ? "Create Context" : "Save Changes" %>
                                                            </button>
                                                        </div>
                                            </form>
                                        </div>
                                    </div>

                                    <div
                                        style="margin-top: 1.5rem; color: var(--clr-faint); font-size: 0.75rem; text-align: center; max-width: 600px;">
                                        Changes take effect immediately across all airport terminals in the network.
                                    </div>
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