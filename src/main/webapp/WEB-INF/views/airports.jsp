<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Airports — Airport Lounge Manager</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                rel="stylesheet">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            <style>
                .airport-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                    gap: 1.5rem;
                    margin-top: 1.5rem;
                }

                .airport-card {
                    background: var(--clr-card);
                    border: 1px solid var(--clr-border);
                    border-radius: var(--radius-lg);
                    padding: 1.5rem;
                    display: flex;
                    flex-direction: column;
                    gap: 0.75rem;
                    transition: transform 0.2s;
                }

                .airport-card:hover {
                    transform: translateY(-4px);
                    border-color: var(--clr-primary);
                }

                .iata-pill {
                    background: rgba(99, 102, 241, 0.1);
                    color: var(--clr-primary-lit);
                    padding: 0.25rem 0.5rem;
                    border-radius: 6px;
                    font-weight: 800;
                    font-size: 0.8rem;
                }
            </style>
        </head>

        <body>
            <div class="app-shell">
                <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
                    <div class="main-content">
                        <div class="topbar">
                            <button class="sidebar-toggle" onclick="toggleSidebar()">☰</button>
                            <span class="topbar-title">Global Network</span>
                            <div class="topbar-right"><span class="topbar-clock" id="live-clock"></span></div>
                        </div>
                        <div class="page-content">
                            <div class="page-header"
                                style="display:flex; justify-content:space-between; align-items:start;">
                                <div>
                                    <h1>🛫 Global Hubs</h1>
                                    <p>Manage airport locations and their operational status across the network.</p>
                                </div>
                                <button class="btn btn-primary" onclick="toggleForm()">＋ Add Hub</button>
                            </div>

                            <!-- Add Form (Hidden by default) -->
                            <div id="add-form" class="glass-panel"
                                style="display:none; margin-bottom: 2rem; max-width: 600px; animation: slideDown 0.3s ease-out;">
                                <div class="glass-panel-header">
                                    <h2>Register New Hub</h2>
                                </div>
                                <div class="glass-panel-body">
                                    <form action="airports" method="post">
                                        <input type="hidden" name="action" value="create">
                                        <div class="form-row">
                                            <div class="form-group" style="flex:2;"><label class="form-label">Airport
                                                    Name</label><input type="text" name="name" class="form-control"
                                                    placeholder="e.g. Heathrow Airport" required></div>
                                            <div class="form-group"><label class="form-label">IATA</label><input
                                                    type="text" name="iataCode" class="form-control" placeholder="LHR"
                                                    maxlength="3" required></div>
                                        </div>
                                        <div class="form-row">
                                            <div class="form-group"><label class="form-label">City</label><input
                                                    type="text" name="city" class="form-control" placeholder="London"
                                                    required></div>
                                            <div class="form-group"><label class="form-label">Country</label><input
                                                    type="text" name="country" class="form-control"
                                                    placeholder="United Kingdom" required></div>
                                        </div>
                                        <div
                                            style="display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1.5rem;">
                                            <button type="button" class="btn btn-ghost"
                                                onclick="toggleForm()">Cancel</button>
                                            <button type="submit" class="btn btn-primary">Initialize Hub</button>
                                        </div>
                                    </form>
                                </div>
                            </div>

                            <div class="airport-grid">
                                <c:forEach var="a" items="${airports}">
                                    <div class="airport-card">
                                        <div style="display:flex; justify-content:space-between; align-items:center;">
                                            <span class="iata-pill">${a.iataCode}</span>
                                            <span class="badge ${a.active ? 'badge-active' : 'badge-inactive'}"><span
                                                    class="badge-dot"></span>${a.active ? 'Operational' :
                                                'Suspended'}</span>
                                        </div>
                                        <div
                                            style="font-size: 1.2rem; font-weight: 700; color: #fff; margin-top: 0.5rem;">
                                            ${a.name}</div>
                                        <div style="font-size: 0.85rem; color: var(--clr-muted);">${a.city},
                                            ${a.country}</div>
                                        <div style="margin-top:auto; display:flex; justify-content:flex-end;">
                                            <form action="airports" method="post">
                                                <input type="hidden" name="action" value="toggle">
                                                <input type="hidden" name="id" value="${a.id}">
                                                <input type="hidden" name="active" value="${!a.active}">
                                                <button type="submit" class="btn btn-ghost btn-sm">${a.active ? 'Suspend
                                                    Link' : 'Restore Link'}</button>
                                            </form>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
            </div>
            <script>
                function toggleForm() {
                    const f = document.getElementById('add-form');
                    f.style.display = f.style.display === 'none' ? 'block' : 'none';
                }
                function updateClock() {
                    const el = document.getElementById('live-clock');
                    if (el) el.textContent = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
                }
                updateClock(); setInterval(updateClock, 1000);
                function toggleSidebar() { document.getElementById('app-sidebar').classList.toggle('open'); }
            </script>
        </body>

        </html>