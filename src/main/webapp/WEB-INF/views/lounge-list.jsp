<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="com.alm.model.Lounge" %>
        <%@ page import="java.util.List" %>
            <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Lounges — Airport Lounge Manager</title>
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                        rel="stylesheet">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                    <style>
                        .lounge-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                            gap: 1.25rem;
                            margin-top: 1.5rem;
                        }

                        .lounge-card {
                            background: var(--clr-card);
                            border: 1px solid var(--clr-border);
                            border-radius: var(--radius-lg);
                            overflow: hidden;
                            position: relative;
                            transition: transform 0.2s var(--ease-out), box-shadow 0.2s var(--ease-out), border-color 0.2s;
                        }

                        .lounge-card:hover {
                            transform: translateY(-4px);
                            box-shadow: 0 20px 40px rgba(6, 10, 30, 0.7), var(--shadow-glow);
                            border-color: var(--clr-border-glow);
                        }

                        .lounge-card-header {
                            padding: 1.25rem 1.25rem 0;
                            display: flex;
                            justify-content: space-between;
                            align-items: flex-start;
                            gap: 0.5rem;
                        }

                        .lounge-card-icon {
                            width: 44px;
                            height: 44px;
                            border-radius: 12px;
                            background: linear-gradient(135deg, rgba(99, 102, 241, 0.4), rgba(34, 211, 238, 0.2));
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 1.3rem;
                            flex-shrink: 0;
                        }

                        .lounge-card-body {
                            padding: 1rem 1.25rem;
                        }

                        .lounge-card-name {
                            font-size: 1rem;
                            font-weight: 700;
                            color: var(--clr-text);
                            margin-bottom: 0.25rem;
                        }

                        .lounge-card-terminal {
                            font-size: 0.78rem;
                            color: var(--clr-muted);
                            margin-bottom: 1rem;
                        }

                        .lounge-card-stats {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 0.6rem;
                            margin-bottom: 1rem;
                        }

                        .lounge-stat {
                            background: rgba(8, 13, 26, 0.6);
                            border: 1px solid var(--clr-border);
                            border-radius: var(--radius-sm);
                            padding: 0.6rem 0.75rem;
                        }

                        .lounge-stat-label {
                            font-size: 0.65rem;
                            font-weight: 600;
                            text-transform: uppercase;
                            letter-spacing: 0.05em;
                            color: var(--clr-muted);
                            margin-bottom: 0.2rem;
                        }

                        .lounge-stat-value {
                            font-size: 1.1rem;
                            font-weight: 700;
                            color: var(--clr-text);
                        }

                        .lounge-card-footer {
                            padding: 0.9rem 1.25rem;
                            border-top: 1px solid var(--clr-border);
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                        }

                        /* Shimmer on hover */
                        .lounge-card::after {
                            content: '';
                            position: absolute;
                            inset: 0;
                            background: linear-gradient(135deg, rgba(99, 102, 241, 0.06) 0%, transparent 60%);
                            pointer-events: none;
                            opacity: 0;
                            transition: opacity 0.25s;
                        }

                        .lounge-card:hover::after {
                            opacity: 1;
                        }

                        /* Filter bar */
                        .filter-bar {
                            display: flex;
                            align-items: center;
                            gap: 0.75rem;
                            flex-wrap: wrap;
                        }

                        .filter-btn {
                            padding: 0.4rem 0.9rem;
                            border-radius: 999px;
                            border: 1px solid var(--clr-border);
                            background: transparent;
                            color: var(--clr-muted);
                            font-size: 0.78rem;
                            font-weight: 500;
                            cursor: pointer;
                            transition: all 0.15s;
                        }

                        .filter-btn.active,
                        .filter-btn:hover {
                            background: rgba(99, 102, 241, 0.15);
                            border-color: var(--clr-border-glow);
                            color: var(--clr-primary-lit);
                        }
                    </style>
                </head>

                <body>
                    <div class="app-shell">
                        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

                            <div class="main-content">
                                <div class="topbar">
                                    <button class="sidebar-toggle" onclick="toggleSidebar()">☰</button>
                                    <span class="topbar-title">Lounges</span>
                                    <div class="topbar-right">
                                        <span class="topbar-clock" id="live-clock"></span>
                                    </div>
                                </div>

                                <div class="page-content">
                                    <div class="page-header"
                                        style="display:flex;align-items:flex-start;justify-content:space-between;flex-wrap:wrap;gap:1rem;">
                                        <div>
                                            <h1>🛋 Lounge Management</h1>
                                            <p>Monitor capacity, status, and amenities across all airport lounges.</p>
                                        </div>
                                    </div>

                                    <!-- Filter bar -->
                                    <div class="filter-bar" style="margin-bottom:1.5rem;">
                                        <button class="filter-btn active"
                                            onclick="filterCards('all', this)">All</button>
                                        <button class="filter-btn" onclick="filterCards('ACTIVE', this)">🟢
                                            Active</button>
                                        <button class="filter-btn" onclick="filterCards('CLOSED', this)">🔴
                                            Closed</button>
                                        <button class="filter-btn" onclick="filterCards('MAINTENANCE', this)">🔧
                                            Maintenance</button>
                                    </div>

                                    <!-- Lounge grid -->
                                    <c:choose>
                                        <c:when test="${empty lounges}">
                                            <div class="glass-panel">
                                                <div class="empty-state">
                                                    <div class="empty-state-icon">🛋</div>
                                                    <h3 style="font-size:1rem;color:var(--clr-text);">No lounges found
                                                    </h3>
                                                    <p>No lounge records exist yet. Contact your administrator to add
                                                        airport lounges.</p>
                                                </div>
                                            </div>
                                            <c:otherwise>
                                                <div class="lounge-grid" id="lounge-grid">
                                                    <c:forEach var="l" items="${lounges}">
                                                        <c:set var="status"
                                                            value="${not empty l.status ? l.status.toUpperCase() : 'ACTIVE'}" />
                                                        <c:set var="badgeClass"
                                                            value="${status == 'ACTIVE' ? 'badge-active' : (status == 'CLOSED' ? 'badge-denied' : 'badge-maintenance')}" />
                                                        <div class="lounge-card" data-status="${status}">
                                                            <div class="lounge-card-header">
                                                                <div class="lounge-card-icon">🛋</div>
                                                                <span class="badge ${badgeClass}">
                                                                    <span class="badge-dot"></span>
                                                                    ${status}
                                                                </span>
                                                            </div>
                                                            <div class="lounge-card-body">
                                                                <div class="lounge-card-name">
                                                                    ${l.name}
                                                                </div>
                                                                <div class="lounge-card-terminal">
                                                                    ✈ Terminal ${not empty l.terminal ? l.terminal :
                                                                    "—"}
                                                                </div>
                                                                <div class="lounge-card-stats">
                                                                    <div class="lounge-stat">
                                                                        <div class="lounge-stat-label">Capacity</div>
                                                                        <div class="lounge-stat-value accent-indigo"
                                                                            style="color:var(--clr-primary-lit)">
                                                                            ${l.capacity}
                                                                        </div>
                                                                    </div>
                                                                    <div class="lounge-stat">
                                                                        <div class="lounge-stat-label">Status</div>
                                                                        <div class="lounge-stat-value"
                                                                            style="font-size:0.78rem;padding-top:0.2rem;">
                                                                            ${status}
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                                <!-- Capacity bar (decorative — full utilisation shown as 70% for illustration) -->
                                                                <div style="margin-bottom:0.5rem;">
                                                                    <div
                                                                        style="display:flex;justify-content:space-between;font-size:0.72rem;color:var(--clr-muted);margin-bottom:0.3rem;">
                                                                        <span>Capacity utilisation</span><span
                                                                            style="color:var(--clr-cyan);">Live</span>
                                                                    </div>
                                                                    <div class="cap-bar">
                                                                        <div class="cap-bar-fill"
                                                                            style="width: ${status == 'ACTIVE' ? '60' : '0'}%">
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div class="lounge-card-footer">
                                                                <span
                                                                    style="font-size:0.75rem;color:var(--clr-muted);">ID
                                                                    #${l.id}</span>
                                                                <a href="${ctx}/lounges?id=${l.id}"
                                                                    class="btn btn-secondary btn-sm">View & Manage →</a>
                                                            </div>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </c:otherwise>
                                    </c:choose>

                                </div>
                            </div>
                    </div>

                    <script>
                        function updateClock() {
                            const now = new Date();
                            const el = document.getElementById('live-clock');
                            if (el) el.textContent = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
                        }
                        updateClock(); setInterval(updateClock, 1000);

                        function toggleSidebar() {
                            document.getElementById('app-sidebar').classList.toggle('open');
                        }

                        function filterCards(status, btn) {
                            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
                            btn.classList.add('active');
                            document.querySelectorAll('.lounge-card').forEach(card => {
                                card.style.display = (status === 'all' || card.dataset.status === status) ? '' : 'none';
                            });
                        }

                        /* Animate capacity bars on load */
                        window.addEventListener('load', () => {
                            document.querySelectorAll('.cap-bar-fill').forEach(bar => {
                                const w = bar.style.width;
                                bar.style.width = '0%';
                                setTimeout(() => { bar.style.width = w; }, 200);
                            });
                        });
                    </script>
                </body>

                </html>