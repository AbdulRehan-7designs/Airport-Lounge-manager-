<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Transaction Ledger — Airport Lounge Manager</title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link
                    href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                <style>
                    .ledger-table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 1rem;
                    }

                    .ledger-table th {
                        text-align: left;
                        padding: 1.25rem 1rem;
                        color: var(--clr-muted);
                        font-size: 0.75rem;
                        text-transform: uppercase;
                        letter-spacing: 0.05em;
                        border-bottom: 2px solid var(--clr-border);
                    }

                    .ledger-table td {
                        padding: 1.25rem 1rem;
                        border-bottom: 1px solid var(--clr-border);
                        color: #fff;
                        font-size: 0.9rem;
                    }

                    .ledger-table tr:hover {
                        background: rgba(255, 255, 255, 0.02);
                    }

                    .mono {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.8rem;
                        color: var(--clr-primary-lit);
                    }

                    .status-pill {
                        padding: 0.25rem 0.6rem;
                        border-radius: 6px;
                        font-size: 0.7rem;
                        font-weight: 700;
                        text-transform: uppercase;
                    }

                    .status-success {
                        background: rgba(16, 185, 129, 0.1);
                        color: var(--clr-emerald);
                        border: 1px solid rgba(16, 185, 129, 0.2);
                    }

                    .status-failed {
                        background: rgba(239, 68, 68, 0.1);
                        color: var(--clr-rose);
                        border: 1px solid rgba(239, 68, 68, 0.2);
                    }

                    .status-initiated {
                        background: rgba(99, 102, 241, 0.1);
                        color: var(--clr-primary-lit);
                        border: 1px solid rgba(99, 102, 241, 0.2);
                    }
                </style>
            </head>

            <body>
                <div class="app-shell">
                    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
                        <div class="main-content">
                            <c:set var="pageTitle" value="Financial Ledger" scope="request" />
                            <%@ include file="/WEB-INF/includes/topbar.jsp" %>
                                <div class="page-content">
                                    <div class="page-header"
                                        style="display:flex; justify-content:space-between; align-items:flex-end;">
                                        <div>
                                            <h1>💳 Transaction Ledger</h1>
                                            <p>Verified record of all financial movements across the network.</p>
                                        </div>
                                        <!-- Filters -->
                                        <div style="display:flex; gap:0.75rem;">
                                            <a href="transactions"
                                                class="btn btn-ghost btn-sm ${empty param.status ? 'active' : ''}">All</a>
                                            <a href="transactions?status=SUCCESS"
                                                class="btn btn-ghost btn-sm ${param.status == 'SUCCESS' ? 'active' : ''}">Success</a>
                                            <a href="transactions?status=FAILED"
                                                class="btn btn-ghost btn-sm ${param.status == 'FAILED' ? 'active' : ''}">Failed</a>
                                        </div>
                                    </div>

                                    <div class="glass-panel">
                                        <table class="ledger-table">
                                            <thead>
                                                <tr>
                                                    <th>Transaction ID</th>
                                                    <th>Reference</th>
                                                    <th>Amount</th>
                                                    <th>Method</th>
                                                    <th>Timestamp</th>
                                                    <th>Status</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="tx" items="${transactions}">
                                                    <tr>
                                                        <td class="mono">#TX-${tx.id}</td>
                                                        <td class="mono">REQ-${tx.requestId}</td>
                                                        <td style="font-weight: 700;">${tx.currency} ${tx.amount}</td>
                                                        <td style="color: var(--clr-muted); font-size: 0.8rem;">
                                                            ${tx.paymentMethod}</td>
                                                        <td style="color: var(--clr-muted); font-size:0.8rem;">
                                                            <fmt:formatDate value="${tx.processedAt}"
                                                                pattern="yyyy-MM-dd HH:mm:ss" />
                                                        </td>
                                                        <td>
                                                            <c:set var="statusClass" value="status-initiated" />
                                                            <c:if test="${tx.paymentStatus == 'SUCCESS'}">
                                                                <c:set var="statusClass" value="status-success" />
                                                            </c:if>
                                                            <c:if test="${tx.paymentStatus == 'FAILED'}">
                                                                <c:set var="statusClass" value="status-failed" />
                                                            </c:if>
                                                            <span
                                                                class="status-pill ${statusClass}">${tx.paymentStatus}</span>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
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
                    function toggleSidebar() { document.getElementById('app-sidebar').classList.toggle('open'); }
                </script>
            </body>

            </html>