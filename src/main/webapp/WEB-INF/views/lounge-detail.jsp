<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <c:if test="${empty lounge}">
                <c:redirect url="/lounges" />
            </c:if>
            <c:set var="status" value="${not empty lounge.status ? lounge.status.toUpperCase() : 'ACTIVE'}" />
            <c:set var="badgeClass"
                value="${status == 'ACTIVE' ? 'badge-active' : (status == 'CLOSED' ? 'badge-denied' : 'badge-maintenance')}" />
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>
                    ${lounge.name} — Airport Lounge Manager
                </title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                <style>
                    .lounge-header-visual {
                        background: linear-gradient(135deg, rgba(15, 23, 42, 0.8), rgba(30, 41, 59, 0.4));
                        border: 1px solid var(--clr-border);
                        border-radius: var(--radius-lg);
                        padding: 2.5rem;
                        margin-bottom: 2rem;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        position: relative;
                        overflow: hidden;
                    }

                    .lounge-header-visual::after {
                        content: '🛋';
                        position: absolute;
                        right: -2rem;
                        bottom: -2rem;
                        font-size: 10rem;
                        opacity: 0.05;
                        transform: rotate(-10deg);
                    }

                    .lounge-title-area h3 {
                        font-size: 0.8rem;
                        text-transform: uppercase;
                        color: var(--clr-muted);
                        letter-spacing: 0.15em;
                        margin-bottom: 0.5rem;
                    }

                    .lounge-title-area h1 {
                        font-size: 2.5rem;
                        font-weight: 800;
                        margin: 0;
                    }

                    .amenity-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
                        gap: 1rem;
                    }

                    .amenity-card {
                        background: var(--clr-card);
                        border: 1px solid var(--clr-border);
                        border-radius: 12px;
                        padding: 1.25rem;
                        display: flex;
                        flex-direction: column;
                        gap: 0.75rem;
                        transition: transform 0.2s;
                    }

                    .amenity-card:hover {
                        transform: translateY(-3px);
                        border-color: var(--clr-primary-lit);
                    }

                    .amenity-val-row {
                        display: flex;
                        align-items: baseline;
                        gap: 0.4rem;
                    }

                    .amenity-current {
                        font-size: 1.5rem;
                        font-weight: 800;
                        color: #fff;
                    }

                    .amenity-total {
                        font-size: 0.8rem;
                        color: var(--clr-faint);
                    }

                    .status-rose {
                        --status-clr: var(--clr-rose);
                    }

                    .status-amber {
                        --status-clr: var(--clr-amber);
                    }

                    .status-emerald {
                        --status-clr: var(--clr-emerald);
                    }

                    .amenity-card .status-text {
                        color: var(--status-clr, var(--clr-muted));
                    }

                    .amenity-card .cap-bar-fill {
                        background: var(--status-clr, #fff);
                    }
                </style>
            </head>

            <body>
                <div class="app-shell">
                    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

                        <div class="main-content">
                            <div class="topbar">
                                <button class="sidebar-toggle" onclick="toggleSidebar()">☰</button>
                                <span class="topbar-title">Lounge Detail</span>
                                <div class="topbar-right">
                                    <span class="topbar-clock" id="live-clock"></span>
                                </div>
                            </div>

                            <div class="page-content">
                                <div class="lounge-header-visual">
                                    <div class="lounge-title-area">
                                        <h3>Terminal <%= lounge.getTerminal() %> &bull; ID #<%= lounge.getId() %>
                                        </h3>
                                        <h1>
                                            <%= lounge.getName() %>
                                        </h1>
                                        <div
                                            style="margin-top: 1rem; display: flex; gap: 0.75rem; align-items: center;">
                                            <span class="badge <%= badgeClass %>"><span class="badge-dot"></span>
                                                <%= status %>
                                            </span>
                                            <span style="color: var(--clr-muted); font-size: 0.85rem;">Max
                                                Capacity: <strong>
                                                    <%= lounge.getCapacity() %>
                                                </strong></span>
                                        </div>
                                    </div>
                                    <div style="text-align: right; display: flex; gap: 0.75rem; align-items: center;">
                                        <a href="lounges" class="btn btn-ghost">&larr; Back to Fleet</a>
                                        <c:if test="${currentUser.role == 'PASSENGER'}">
                                            <a href="${ctx}/access-requests?loungeId=${lounge.id}"
                                                class="btn btn-primary">Book Access Now</a>
                                        </c:if>
                                        <button class="btn btn-secondary"
                                            onclick="document.getElementById('add-amenity').scrollIntoView({behavior:'smooth'})">Add
                                            Inventory</button>
                                    </div>
                                </div>

                                <div class="page-header">
                                    <h2>📦 Inventory & Amenities</h2>
                                    <p>Real-time availability tracking for lounge assets.</p>
                                </div>

                                <c:choose>
                                    <c:when test="${empty amenities}">
                                        <div class="glass-panel" style="grid-column: 1 / -1;">
                                            <div class="empty-state">
                                                <p>No inventory items registered. Add items like 'Chairs' or 'Power
                                                    Outlets' below.
                                                </p>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="a" items="${amenities}">
                                            <c:set var="pct"
                                                value="${a.totalCount > 0 ? (a.availableCount * 100 / a.totalCount) : 0}" />
                                            <c:set var="statusClass"
                                                value="${pct < 20 ? 'status-rose' : (pct < 50 ? 'status-amber' : 'status-emerald')}" />
                                            <div class="amenity-card ${statusClass}">
                                                <div
                                                    style="display:flex; justify-content:space-between; align-items:center;">
                                                    <span
                                                        style="font-weight:700; color:var(--clr-muted); font-size:0.75rem; text-transform:uppercase;">
                                                        ${a.itemName}
                                                    </span>
                                                    <span class="status-text" style="font-size:0.75rem;">
                                                        <fmt:formatNumber value="${pct}" maxFractionDigits="0" />%
                                                        Available
                                                    </span>
                                                </div>
                                                <div class="amenity-val-row">
                                                    <span class="amenity-current">${a.availableCount}</span>
                                                    <span class="amenity-total">/ ${a.totalCount} units</span>
                                                </div>
                                                <div class="cap-bar">
                                                    <div class="cap-bar-fill" style="width: ${pct}%;"></div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>

                                <!-- Add section -->
                                <div class="glass-panel" id="add-amenity" style="margin-top: 2.5rem; max-width: 600px;">
                                    <div class="glass-panel-header">
                                        <h2>Track New Asset</h2>
                                    </div>
                                    <div class="glass-panel-body">
                                        <form action="lounges" method="post">
                                            <input type="hidden" name="action" value="addAmenity">
                                            <input type="hidden" name="loungeId" value="${lounge.id}">
                                            <div class="form-row">
                                                <div class="form-group" style="flex:2;">
                                                    <label class="form-label">Item / Asset Name</label>
                                                    <input class="form-control" type="text" name="itemName"
                                                        placeholder="e.g. Ergonomic Chairs" required>
                                                </div>
                                            </div>
                                            <div class="form-row">
                                                <div class="form-group">
                                                    <label class="form-label">Total Inventory</label>
                                                    <input class="form-control" type="number" name="totalCount" min="0"
                                                        value="0" required>
                                                </div>
                                                <div class="form-group">
                                                    <label class="form-label">Live Availability</label>
                                                    <input class="form-control" type="number" name="availableCount"
                                                        min="0" value="0" required>
                                                </div>
                                            </div>
                                            <div style="margin-top: 1rem; display: flex; justify-content: flex-end;">
                                                <button type="submit" class="btn btn-primary">Add to
                                                    Inventory</button>
                                            </div>
                                        </form>
                                    </div>
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