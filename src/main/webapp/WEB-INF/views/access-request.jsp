<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Access Requests — Airport Lounge Manager</title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                <style>
                    .request-tabs {
                        display: flex;
                        gap: 0.25rem;
                        background: rgba(8, 13, 26, 0.6);
                        border: 1px solid var(--clr-border);
                        border-radius: 10px;
                        padding: 4px;
                    }

                    .request-tab {
                        padding: 0.45rem 1rem;
                        border-radius: 7px;
                        border: none;
                        background: transparent;
                        color: var(--clr-muted);
                        font-size: 0.82rem;
                        font-weight: 500;
                        cursor: pointer;
                        transition: all 0.15s;
                        font-family: inherit;
                    }

                    .request-tab.active {
                        background: var(--clr-card);
                        color: var(--clr-text);
                        border: 1px solid var(--clr-border);
                    }

                    .decision-form {
                        display: inline;
                    }

                    .action-group {
                        display: flex;
                        gap: 0.4rem;
                        align-items: center;
                    }

                    .new-request-panel {
                        background: var(--clr-card);
                        border: 1px solid var(--clr-border);
                        border-radius: var(--radius-lg);
                        overflow: hidden;
                        margin-bottom: 1.5rem;
                        display: none;
                    }

                    .new-request-panel.open {
                        display: block;
                        animation: slideDown 0.25s var(--ease-out);
                    }

                    .request-row-highlight td {
                        background: rgba(251, 191, 36, 0.04);
                    }
                </style>
            </head>

            <body>
                <div class="app-shell">
                    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

                        <div class="main-content">
                            <c:set var="pageTitle" value="Access Intelligence" scope="request" />
                            <%@ include file="/WEB-INF/includes/topbar.jsp" %>

                                <div class="page-content">

                                    <div class="page-header"
                                        style="display:flex;align-items:flex-start;justify-content:space-between;flex-wrap:wrap;gap:1rem;">
                                        <div>
                                            <h1>🔑 Access Requests</h1>
                                            <p>Review pending requests and manage lounge entry approvals.</p>
                                        </div>
                                        <button class="btn btn-primary" onclick="toggleNewRequestPanel()"
                                            id="new-req-btn">＋
                                            New Request</button>
                                    </div>

                                    <!-- Ecosystem Flow Markers -->
                                    <div
                                        style="display:grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap:1rem; margin-bottom:2rem;">
                                        <div
                                            style="padding:1.25rem; border-radius:12px; background:linear-gradient(135deg, rgba(59,130,246,0.1), transparent); border:1px solid rgba(59,130,246,0.2);">
                                            <span
                                                style="font-size:0.65rem; color:var(--clr-primary-lit); font-weight:700; text-transform:uppercase; letter-spacing:0.05em;">1.
                                                Airline & Alliance</span>
                                            <h4 style="margin:0.25rem 0; font-size:0.85rem; color:#fff;">Cost Center
                                                Model</h4>
                                            <p style="font-size:0.75rem; color:var(--clr-muted); margin:0;">Focus on
                                                retention. Auto-approves Business/First & High Elite status.</p>
                                        </div>
                                        <div
                                            style="padding:1.25rem; border-radius:12px; background:linear-gradient(135deg, rgba(168,85,247,0.1), transparent); border:1px solid rgba(168,85,247,0.2);">
                                            <span
                                                style="font-size:0.65rem; color:#c084fc; font-weight:700; text-transform:uppercase; letter-spacing:0.05em;">2.
                                                Credit Card Owned</span>
                                            <h4 style="margin:0.25rem 0; font-size:0.85rem; color:#fff;">Marketing
                                                Center</h4>
                                            <p style="font-size:0.75rem; color:var(--clr-muted); margin:0;">Experience
                                                focus. Premium card holders accepted subject to capacity.</p>
                                        </div>
                                        <div
                                            style="padding:1.25rem; border-radius:12px; background:linear-gradient(135deg, rgba(234,179,8,0.1), transparent); border:1px solid rgba(234,179,8,0.2);">
                                            <span
                                                style="font-size:0.65rem; color:#facc15; font-weight:700; text-transform:uppercase; letter-spacing:0.05em;">3.
                                                Third-Party Aggregator</span>
                                            <h4 style="margin:0.25rem 0; font-size:0.85rem; color:#fff;">Access Seller
                                            </h4>
                                            <p style="font-size:0.75rem; color:var(--clr-muted); margin:0;">Membership
                                                focus. Priority Pass/LoungeKey often throttled at peak.</p>
                                        </div>
                                        <div
                                            style="padding:1.25rem; border-radius:12px; background:linear-gradient(135deg, rgba(34,197,94,0.1), transparent); border:1px solid rgba(34,197,94,0.2);">
                                            <span
                                                style="font-size:0.65rem; color:#4ade80; font-weight:700; text-transform:uppercase; letter-spacing:0.05em;">4.
                                                Pay-Per-Use</span>
                                            <h4 style="margin:0.25rem 0; font-size:0.85rem; color:#fff;">Direct Revenue
                                            </h4>
                                            <p style="font-size:0.75rem; color:var(--clr-muted); margin:0;">Flexible
                                                transactional entry. Maximize yield when space permits.</p>
                                        </div>
                                    </div>

                                    <!-- Flash messages -->
                                    <c:if test="${not empty error}">
                                        <div class="alert alert-error">⚠ ${error}</div>
                                    </c:if>
                                    <c:if test="${not empty success}">
                                        <div class="alert alert-success">✓ ${success}</div>
                                    </c:if>

                                    <!-- New Request panel -->
                                    <div class="new-request-panel" id="new-req-panel">
                                        <div
                                            style="display:flex;align-items:center;justify-content:space-between;padding:1.1rem 1.5rem;border-bottom:1px solid var(--clr-border);">
                                            <h2
                                                style="font-size:0.95rem;font-weight:600;color:var(--clr-text);margin:0;">
                                                Create New Access Request</h2>
                                            <button onclick="toggleNewRequestPanel()"
                                                style="background:none;border:none;color:var(--clr-muted);cursor:pointer;font-size:1.1rem;">✕</button>
                                        </div>
                                        <div style="padding:1.5rem;">
                                            <form method="post"
                                                action="${pageContext.request.contextPath}/access-requests">
                                                <input type="hidden" name="action" value="create">
                                                <div class="form-row">
                                                    <div class="form-group">
                                                        <label class="form-label">Passenger *</label>
                                                        <select class="form-control" name="passengerId" required>
                                                            <option value="">— Select passenger —</option>
                                                            <c:forEach var="p" items="${passengers}">
                                                                <option value="${p.id}" ${p.id==preSelectedPassengerId
                                                                    ? 'selected' : '' }>${p.fullName}
                                                                    (${p.passportNumber})</option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                    <div class="form-group">
                                                        <label class="form-label">Lounge *</label>
                                                        <select class="form-control" name="loungeId" id="loungeSelect"
                                                            required onchange="updateLoungePreview()">
                                                            <option value="">— Select lounge —</option>
                                                            <c:forEach var="l" items="${lounges}">
                                                                <option value="${l.id}" data-terminal="${l.terminal}"
                                                                    ${l.id==preSelectedLoungeId ? 'selected' : '' }>
                                                                    ${l.name} – T${l.terminal}
                                                                </option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                    <div class="form-group">
                                                        <label class="form-label">Access Provider</label>
                                                        <select class="form-control" name="providerId"
                                                            id="providerSelect" onchange="updateSmartFields()">
                                                            <option value="" data-type="NONE">— Optional —</option>
                                                            <c:forEach var="ap" items="${providers}">
                                                                <option value="${ap.id}" data-type="${ap.providerType}">
                                                                    ${ap.providerName} (${ap.providerType})
                                                                </option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                </div>

                                                <!-- Lounge Info Preview (Dynamic) -->
                                                <div id="lounge-preview"
                                                    style="display:none; margin-top:1rem; padding:1.25rem; background:rgba(99,102,241,0.05); border:1px dashed rgba(99,102,241,0.3); border-radius:12px; align-items:center; gap:1.5rem;">
                                                    <div style="font-size:2.5rem;">🛋</div>
                                                    <div style="flex:1;">
                                                        <div id="preview-lounge-name"
                                                            style="font-weight:700; color:#fff; font-size:1.1rem;">
                                                            Lounge Name</div>
                                                        <div id="preview-lounge-term"
                                                            style="font-size:0.8rem; color:var(--clr-muted);">Terminal X
                                                        </div>
                                                    </div>
                                                    <a id="preview-link" href="#" class="btn btn-ghost btn-sm"
                                                        style="border:1px solid rgba(255,255,255,0.1);">View Amenities &
                                                        Detail →</a>
                                                </div>

                                                <!-- SmartFlow Decision Info (Dynamic) -->
                                                <div id="smart-fields"
                                                    style="display:grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap:1rem; margin-top:1rem; padding:1rem; background:rgba(255,255,255,0.03); border-radius:8px; display:none;">
                                                    <div class="form-group" id="group-ticketClass">
                                                        <label class="form-label">Ticket Class</label>
                                                        <select class="form-control" name="ticketClass">
                                                            <option value="ECONOMY">Economy</option>
                                                            <option value="BUSINESS">Business</option>
                                                            <option value="FIRST">First Class</option>
                                                        </select>
                                                    </div>
                                                    <div class="form-group" id="group-eliteStatus">
                                                        <label class="form-label">Elite Status</label>
                                                        <select class="form-control" name="eliteStatus">
                                                            <option value="NONE">None</option>
                                                            <option value="SILVER">Silver</option>
                                                            <option value="GOLD">Gold</option>
                                                            <option value="PLATINUM">Platinum</option>
                                                        </select>
                                                    </div>
                                                    <div class="form-group" id="group-cardType">
                                                        <label class="form-label">Card Level</label>
                                                        <select class="form-control" name="cardType">
                                                            <option value="STANDARD">Standard</option>
                                                            <option value="PREMIUM">Premium / Centurion</option>
                                                        </select>
                                                    </div>
                                                    <div class="form-group" id="group-membershipType">
                                                        <label class="form-label">Membership Program</label>
                                                        <select class="form-control" name="membershipType">
                                                            <option value="PRIORITY_PASS">Priority Pass</option>
                                                            <option value="LOUNGEKEY">LoungeKey</option>
                                                        </select>
                                                    </div>
                                                    <div class="form-group" id="group-amountPaid">
                                                        <label class="form-label">One-time Fee (USD)</label>
                                                        <input type="number" step="0.01" class="form-control"
                                                            name="amountPaid" placeholder="e.g. 50.00">
                                                    </div>
                                                </div>
                                                <div style="display:flex;gap:0.75rem;margin-top:0.5rem;">
                                                    <button type="submit" class="btn btn-primary">Submit
                                                        Request</button>
                                                    <button type="button" onclick="toggleNewRequestPanel()"
                                                        class="btn btn-ghost">Cancel</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>

                                    <!-- Tab filter -->
                                    <div
                                        style="display:flex;align-items:center;justify-content:space-between;margin-bottom:1.25rem;flex-wrap:wrap;gap:0.75rem;">
                                        <div class="request-tabs">
                                            <button class="request-tab active" onclick="filterRequests('all', this)">All
                                                Requests</button>
                                            <button class="request-tab" onclick="filterRequests('PENDING', this)">⏳
                                                Pending</button>
                                            <button class="request-tab" onclick="filterRequests('APPROVED', this)">✅
                                                Approved</button>
                                            <button class="request-tab" onclick="filterRequests('DENIED', this)">❌
                                                Denied</button>
                                        </div>
                                    </div>

                                    <!-- Requests table -->
                                    <div class="glass-panel">
                                        <div class="glass-panel-header">
                                            <h2>📋 Request Queue</h2>
                                            <span
                                                style="font-size:0.78rem;color:var(--clr-muted);">${fn:length(requests)}
                                                total</span>
                                        </div>
                                        <div class="data-table-wrap">
                                            <table class="data-table" id="req-table">
                                                <thead>
                                                    <tr>
                                                        <th>#</th>
                                                        <th>Passenger</th>
                                                        <th>Lounge</th>
                                                        <th>Provider</th>
                                                        <th>Status</th>
                                                        <th>Requested</th>
                                                        <th>Actions</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:choose>
                                                        <c:when test="${empty requests}">
                                                            <tr>
                                                                <td colspan="7">
                                                                    <div class="empty-state" style="padding:2.5rem;">
                                                                        <div class="empty-state-icon">🔑</div>
                                                                        <p>No access requests yet. Create one above.</p>
                                                                    </div>
                                                                </td>
                                                            </tr>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:forEach var="r" items="${requests}">
                                                                <tr data-status="${r.status}"
                                                                    class="${r.status == 'PENDING' ? 'request-row-highlight' : ''}">
                                                                    <td
                                                                        style="color:var(--clr-muted);font-size:0.78rem;">
                                                                        #${r.id}</td>
                                                                    <td style="font-weight:600;">${r.passengerName}</td>
                                                                    <td>
                                                                        <a href="${pageContext.request.contextPath}/lounges?id=${r.loungeId}"
                                                                            style="color:var(--clr-primary-lit);text-decoration:none;font-weight:500;">
                                                                            ${r.loungeName}
                                                                        </a>
                                                                    </td>
                                                                    <td
                                                                        style="color:var(--clr-muted);font-size:0.82rem;">
                                                                        ${not empty r.providerName ? r.providerName :
                                                                        '—'}
                                                                    </td>
                                                                    <td>
                                                                        <c:choose>
                                                                            <c:when test="${r.status == 'PENDING'}">
                                                                                <span class="badge badge-pending"><span
                                                                                        class="badge-dot"></span>Pending</span>
                                                                            </c:when>
                                                                            <c:when test="${r.status == 'APPROVED'}">
                                                                                <span class="badge badge-approved"><span
                                                                                        class="badge-dot"></span>Approved</span>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <span class="badge badge-denied"><span
                                                                                        class="badge-dot"></span>Denied</span>
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </td>
                                                                    <td
                                                                        style="font-size:0.78rem;color:var(--clr-muted);">
                                                                        ${r.requestTime}</td>
                                                                    <td>
                                                                        <c:if test="${r.status == 'PENDING'}">
                                                                            <div class="action-group">
                                                                                <form class="decision-form"
                                                                                    method="post"
                                                                                    action="${pageContext.request.contextPath}/access-requests">
                                                                                    <input type="hidden" name="action"
                                                                                        value="approve">
                                                                                    <input type="hidden"
                                                                                        name="requestId"
                                                                                        value="${r.id}">
                                                                                    <button type="submit"
                                                                                        class="btn btn-success btn-sm"
                                                                                        data-tip="Approve entry">✓
                                                                                        Approve</button>
                                                                                </form>
                                                                                <form class="decision-form"
                                                                                    method="post"
                                                                                    action="${pageContext.request.contextPath}/access-requests"
                                                                                    onsubmit="return addDenialReason(this)">
                                                                                    <input type="hidden" name="action"
                                                                                        value="deny">
                                                                                    <input type="hidden"
                                                                                        name="requestId"
                                                                                        value="${r.id}">
                                                                                    <input type="hidden" name="reason"
                                                                                        id="reason-${r.id}">
                                                                                    <button type="submit"
                                                                                        class="btn btn-danger btn-sm"
                                                                                        data-tip="Deny entry">✕
                                                                                        Deny</button>
                                                                                </form>
                                                                            </div>
                                                                        </c:if>
                                                                        <c:if test="${r.status != 'PENDING'}">
                                                                            <span
                                                                                style="font-size:0.75rem;color:var(--clr-faint);">
                                                                                ${not empty r.decisionReason ?
                                                                                r.decisionReason : 'Decided'}
                                                                            </span>
                                                                        </c:if>
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
                    function updateClock() {
                        const el = document.getElementById('live-clock');
                        if (el) el.textContent = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
                    }
                    updateClock(); setInterval(updateClock, 1000);

                    function toggleSidebar() {
                        document.getElementById('app-sidebar').classList.toggle('open');
                    }

                    function toggleNewRequestPanel() {
                        const panel = document.getElementById('new-req-panel');
                        panel.classList.toggle('open');
                        document.getElementById('new-req-btn').textContent = panel.classList.contains('open') ? '✕ Cancel' : '＋ New Request';
                    }

                    function filterRequests(status, btn) {
                        document.querySelectorAll('.request-tab').forEach(b => b.classList.remove('active'));
                        btn.classList.add('active');
                        document.querySelectorAll('#req-table tbody tr[data-status]').forEach(row => {
                            row.style.display = (status === 'all' || row.dataset.status === status) ? '' : 'none';
                        });
                    }

                    function addDenialReason(form) {
                        const reason = prompt('Reason for denial (optional):');
                        const requestId = form.querySelector('[name="requestId"]').value;
                        const reasonInput = form.querySelector('[name="reason"]');
                        if (reasonInput) reasonInput.value = reason || '';
                        return true;
                    }

                    function updateSmartFields() {
                        const select = document.getElementById('providerSelect');
                        const type = select.options[select.selectedIndex].getAttribute('data-type');
                        const smartContainer = document.getElementById('smart-fields');

                        // Hide all first
                        smartContainer.style.display = (type && type !== 'NONE') ? 'grid' : 'none';
                        document.getElementById('group-ticketClass').style.display = 'none';
                        document.getElementById('group-eliteStatus').style.display = 'none';
                        document.getElementById('group-cardType').style.display = 'none';
                        document.getElementById('group-membershipType').style.display = 'none';
                        document.getElementById('group-amountPaid').style.display = 'none';

                        if (type === 'AIRLINE') {
                            document.getElementById('group-ticketClass').style.display = 'block';
                            document.getElementById('group-eliteStatus').style.display = 'block';
                        } else if (type === 'CREDIT_CARD') {
                            document.getElementById('group-cardType').style.display = 'block';
                        } else if (type === 'AGGREGATOR') {
                            document.getElementById('group-membershipType').style.display = 'block';
                        } else if (type === 'PAY_PER_USE') {
                            document.getElementById('group-amountPaid').style.display = 'block';
                        }
                    }

                    function updateLoungePreview() {
                        const select = document.getElementById('loungeSelect');
                        const preview = document.getElementById('lounge-preview');
                        const id = select.value;

                        if (!id) {
                            preview.style.display = 'none';
                            return;
                        }

                        const opt = select.options[select.selectedIndex];
                        const name = opt.text.split(' – ')[0];
                        const terminal = opt.getAttribute('data-terminal');

                        document.getElementById('preview-lounge-name').textContent = name;
                        document.getElementById('preview-lounge-term').textContent = '✈ Terminal ' + (terminal || 'N/A');
                        document.getElementById('preview-link').href = '${pageContext.request.contextPath}/lounges?id=' + id;
                        preview.style.display = 'flex';
                    }

                    // Auto-run on load for pre-selection
                    window.addEventListener('load', () => {
                        updateLoungePreview();
                        updateSmartFields();

                        // If pre-selected via URL, open the panel
                        <c:if test="${not empty preSelectedLoungeId}">
                            toggleNewRequestPanel();
                        </c:if>
                    });
                </script>
            </body>

            </html>