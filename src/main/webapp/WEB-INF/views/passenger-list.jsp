<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Passengers — Airport Lounge Manager</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                rel="stylesheet">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            <style>
                .search-bar {
                    position: relative;
                    max-width: 340px;
                }

                .search-bar input {
                    width: 100%;
                    padding: 0.55rem 0.9rem 0.55rem 2.4rem;
                    background: rgba(8, 13, 26, 0.8);
                    border: 1px solid var(--clr-border);
                    border-radius: 999px;
                    color: var(--clr-text);
                    font-size: 0.85rem;
                    font-family: inherit;
                    outline: none;
                    transition: border-color 0.15s, box-shadow 0.15s;
                }

                .search-bar input:focus {
                    border-color: var(--clr-primary);
                    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
                }

                .search-icon {
                    position: absolute;
                    left: 0.85rem;
                    top: 50%;
                    transform: translateY(-50%);
                    color: var(--clr-muted);
                    font-size: 0.9rem;
                    pointer-events: none;
                }

                .passenger-avatar {
                    width: 34px;
                    height: 34px;
                    border-radius: 50%;
                    background: linear-gradient(135deg, var(--clr-primary), var(--clr-violet));
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 0.8rem;
                    font-weight: 700;
                    color: white;
                    flex-shrink: 0;
                }

                .passenger-name-cell {
                    display: flex;
                    align-items: center;
                    gap: 0.75rem;
                }

                .add-panel {
                    background: var(--clr-card);
                    border: 1px solid var(--clr-border);
                    border-radius: var(--radius-lg);
                    overflow: hidden;
                    margin-bottom: 1.5rem;
                    display: none;
                }

                .add-panel.open {
                    display: block;
                    animation: slideDown 0.25s var(--ease-out);
                }
            </style>
        </head>

        <body>
            <div class="app-shell">
                <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

                    <div class="main-content">
                        <c:set var="pageTitle" value="Passenger Registry" scope="request" />
                        <%@ include file="/WEB-INF/includes/topbar.jsp" %>

                            <div class="page-content">

                                <div class="page-header"
                                    style="display:flex;align-items:flex-start;justify-content:space-between;flex-wrap:wrap;gap:1rem;">
                                    <div>
                                        <h1>🪪 Passenger Registry</h1>
                                        <p>Manage, search, and register passengers for lounge access.</p>
                                    </div>
                                    <button class="btn btn-primary" onclick="toggleAddPanel()" id="add-btn">
                                        ＋ Add Passenger
                                    </button>
                                </div>

                                <!-- Flash messages -->
                                <c:if test="${not empty error}">
                                    <div class="alert alert-error">⚠ ${error}</div>
                                </c:if>
                                <c:if test="${not empty success}">
                                    <div class="alert alert-success">✓ ${success}</div>
                                </c:if>

                                <!-- Add passenger panel (slide toggle) -->
                                <div class="add-panel" id="add-panel">
                                    <div class="glass-panel-header"
                                        style="padding:1.1rem 1.5rem;border-bottom:1px solid var(--clr-border);margin:0;">
                                        <h2 style="font-size:0.95rem;font-weight:600;color:var(--clr-text);margin:0;">
                                            Register New Passenger</h2>
                                        <button onclick="toggleAddPanel()"
                                            style="background:none;border:none;color:var(--clr-muted);cursor:pointer;font-size:1.1rem;">✕</button>
                                    </div>
                                    <div style="padding:1.5rem;">
                                        <form method="post" action="${pageContext.request.contextPath}/passengers">
                                            <input type="hidden" name="action" value="add">
                                            <div class="form-row">
                                                <div class="form-group">
                                                    <label class="form-label" for="fullName">Full Name *</label>
                                                    <input class="form-control" type="text" id="fullName"
                                                        name="fullName" required placeholder="Jane Smith">
                                                </div>
                                                <div class="form-group">
                                                    <label class="form-label" for="passport">Passport Number *</label>
                                                    <input class="form-control" type="text" id="passport"
                                                        name="passportNumber" required placeholder="A12345678">
                                                </div>
                                            </div>
                                            <div class="form-row">
                                                <div class="form-group">
                                                    <label class="form-label" for="nationality">Nationality</label>
                                                    <input class="form-control" type="text" id="nationality"
                                                        name="nationality" placeholder="United States">
                                                </div>
                                                <div class="form-group">
                                                    <label class="form-label" for="phone">Phone</label>
                                                    <input class="form-control" type="tel" id="phone" name="phone"
                                                        placeholder="+1 555 0123">
                                                </div>
                                                <div class="form-group">
                                                    <label class="form-label" for="email">Email</label>
                                                    <input class="form-control" type="email" id="email" name="email"
                                                        placeholder="jane@example.com">
                                                </div>
                                            </div>
                                            <div style="display:flex;gap:0.75rem;margin-top:0.5rem;">
                                                <button type="submit" class="btn btn-primary">Register
                                                    Passenger</button>
                                                <button type="button" onclick="toggleAddPanel()"
                                                    class="btn btn-ghost">Cancel</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>

                                <!-- Table -->
                                <div class="glass-panel">
                                    <div class="glass-panel-header">
                                        <h2>👥 All Passengers</h2>
                                        <div class="search-bar">
                                            <span class="search-icon">🔍</span>
                                            <input type="text" id="passenger-search"
                                                placeholder="Search name or passport…"
                                                oninput="searchPassengers(this.value)">
                                        </div>
                                    </div>
                                    <div class="data-table-wrap">
                                        <table class="data-table" id="passenger-table">
                                            <thead>
                                                <tr>
                                                    <th>Passenger</th>
                                                    <th>Passport</th>
                                                    <th>Nationality</th>
                                                    <th>Contact</th>
                                                    <th>Status</th>
                                                    <th>Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:choose>
                                                    <c:when test="${empty passengers}">
                                                        <tr>
                                                            <td colspan="6">
                                                                <div class="empty-state" style="padding:2.5rem;">
                                                                    <div class="empty-state-icon">🪪</div>
                                                                    <p>No passengers registered yet. Add the first one
                                                                        above.</p>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:forEach var="p" items="${passengers}">
                                                            <tr>
                                                                <td>
                                                                    <div class="passenger-name-cell">
                                                                        <div class="passenger-avatar">
                                                                            ${not empty p.fullName ?
                                                                            p.fullName.substring(0,1).toUpperCase() :
                                                                            '?'}
                                                                        </div>
                                                                        <span style="font-weight:600;">${not empty
                                                                            p.fullName ? p.fullName : 'Unknown
                                                                            Passenger'}</span>
                                                                    </div>
                                                                </td>
                                                                <td
                                                                    style="font-family:monospace;color:var(--clr-muted);">
                                                                    ${p.passportNumber}</td>
                                                                <td>${not empty p.nationality ? p.nationality : '—'}
                                                                </td>
                                                                <td style="font-size:0.8rem;color:var(--clr-muted);">
                                                                    <c:if test="${not empty p.email}">${p.email}<br>
                                                                    </c:if>
                                                                    <c:if test="${not empty p.phone}">${p.phone}</c:if>
                                                                    <c:if test="${empty p.email && empty p.phone}">—
                                                                    </c:if>
                                                                </td>
                                                                <td>
                                                                    <c:choose>
                                                                        <c:when test="${p.active}">
                                                                            <span class="badge badge-active"><span
                                                                                    class="badge-dot"></span>Active</span>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <span class="badge badge-inactive"><span
                                                                                    class="badge-dot"></span>Inactive</span>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                                <td>
                                                                    <div
                                                                        style="display:flex;gap:0.4rem;align-items:center;">
                                                                        <c:if test="${p.active}">
                                                                            <form method="post"
                                                                                action="${pageContext.request.contextPath}/passengers"
                                                                                style="display:inline;"
                                                                                onsubmit="return confirm('Deactivate this passenger?')">
                                                                                <input type="hidden" name="action"
                                                                                    value="deactivate">
                                                                                <input type="hidden" name="passengerId"
                                                                                    value="${p.id}">
                                                                                <button type="submit"
                                                                                    class="btn btn-danger btn-sm">Deactivate</button>
                                                                            </form>
                                                                        </c:if>
                                                                    </div>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </c:otherwise>
                                                </c:choose>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>

                            </div>
                    </div>
            </div>

            <script>
                function toggleAddPanel() {
                    const panel = document.getElementById('add-panel');
                    panel.classList.toggle('open');
                    document.getElementById('add-btn').textContent = panel.classList.contains('open') ? '✕ Cancel' : '＋ Add Passenger';
                }

                function searchPassengers(query) {
                    const q = query.toLowerCase();
                    document.querySelectorAll('#passenger-table tbody tr').forEach(row => {
                        const text = row.textContent.toLowerCase();
                        row.style.display = text.includes(q) ? '' : 'none';
                    });
                }
            </script>
        </body>

        </html>