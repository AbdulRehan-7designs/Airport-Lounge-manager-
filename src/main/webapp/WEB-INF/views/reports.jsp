<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Analytics — Airport Lounge Manager</title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    .reports-grid {
                        display: grid;
                        grid-template-columns: 2fr 1fr;
                        gap: 1.5rem;
                        margin-top: 1.5rem;
                    }

                    @media (max-width: 1024px) {
                        .reports-grid {
                            grid-template-columns: 1fr;
                        }
                    }

                    .chart-container {
                        position: relative;
                        height: 300px;
                        width: 100%;
                        margin-top: 1rem;
                    }

                    .report-card {
                        background: var(--clr-card);
                        border: 1px solid var(--clr-border);
                        border-radius: var(--radius-lg);
                        padding: 1.5rem;
                    }

                    .report-card h3 {
                        font-size: 0.9rem;
                        text-transform: uppercase;
                        letter-spacing: 0.05em;
                        color: var(--clr-muted);
                        margin-bottom: 1rem;
                    }
                </style>
            </head>

            <body>
                <div class="app-shell">
                    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
                        <div class="main-content">
                            <c:set var="pageTitle" value="Intelligence & Reports" scope="request" />
                            <%@ include file="/WEB-INF/includes/topbar.jsp" %>
                                <div class="page-content">
                                    <div class="page-header">
                                        <h1>📈 Business Intelligence</h1>
                                        <p>Real-time revenue flows and performance metrics.</p>
                                    </div>
                                    <div
                                        style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
                                        <div class="report-card">
                                            <h3>7-Day Revenue</h3>
                                            <div style="font-size: 1.8rem; font-weight: 800; color: #fff;">
                                                <c:set var="total7" value="0" />
                                                <c:forEach var="entry" items="${dailyRevenue}">
                                                    <c:set var="total7" value="${total7 + entry.value}" />
                                                </c:forEach>
                                                $${total7}
                                            </div>
                                        </div>
                                        <div class="report-card">
                                            <h3>Peak Lounge</h3>
                                            <div
                                                style="font-size: 1.3rem; font-weight: 700; color: var(--clr-primary-lit);">
                                                <c:forEach var="e" items="${loungeRevenue}" varStatus="s">
                                                    <c:if test="${s.first}">${e.key}</c:if>
                                                </c:forEach>
                                            </div>
                                        </div>
                                        <div class="report-card">
                                            <h3>Top Provider</h3>
                                            <div style="font-size: 1.3rem; font-weight: 700; color: var(--clr-cyan);">
                                                <c:forEach var="e" items="${providerRevenue}" varStatus="s">
                                                    <c:if test="${s.first}">${e.key}</c:if>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="reports-grid">
                                        <div class="report-card">
                                            <h3>Revenue Trend (Last 7 Days)</h3>
                                            <div class="chart-container"><canvas id="revChart"></canvas></div>
                                        </div>
                                        <div class="report-card">
                                            <h3>Provider Mix</h3>
                                            <div class="chart-container" style="height: 250px;"><canvas
                                                    id="mixChart"></canvas></div>
                                        </div>
                                        <div class="report-card" style="grid-column: 1 / -1;">
                                            <h3>Lounge Distribution</h3>
                                            <div class="chart-container" style="height: 200px;"><canvas
                                                    id="distChart"></canvas></div>
                                        </div>
                                    </div>
                                </div>
                        </div>
                </div>
                <div id="report-data"
                    data-daily-labels="<c:forEach var='e' items='${dailyRevenue}' varStatus='s'>${fn:substring(e.key, 5, 10)}${not s.last ? ',' : ''}</c:forEach>"
                    data-daily-values="<c:forEach var='e' items='${dailyRevenue}' varStatus='s'>${e.value}${not s.last ? ',' : ''}</c:forEach>"
                    data-prov-labels="<c:forEach var='e' items='${providerRevenue}' varStatus='s'>${e.key}${not s.last ? ',' : ''}</c:forEach>"
                    data-prov-values="<c:forEach var='e' items='${providerRevenue}' varStatus='s'>${e.value}${not s.last ? ',' : ''}</c:forEach>"
                    data-lng-labels="<c:forEach var='e' items='${loungeRevenue}' varStatus='s'>${e.key}${not s.last ? ',' : ''}</c:forEach>"
                    data-lng-values="<c:forEach var='e' items='${loungeRevenue}' varStatus='s'>${e.value}${not s.last ? ',' : ''}</c:forEach>"
                    style="display:none;"></div>

                <script>
                    const colors = { indigo: '#6366f1', cyan: '#22d3ee', brd: 'rgba(255, 255, 255, 0.1)' };
                    Chart.defaults.color = '#94a3b8';
                    Chart.defaults.borderColor = colors.brd;

                    const dataEl = document.getElementById('report-data');
                    const dailyLabels = dataEl.dataset.dailyLabels.split(',').reverse();
                    const dailyValues = dataEl.dataset.dailyValues.split(',').map(Number).reverse();
                    const provLabels = dataEl.dataset.provLabels.split(',');
                    const provValues = dataEl.dataset.provValues.split(',').map(Number);
                    const lngLabels = dataEl.dataset.lngLabels.split(',');
                    const lngValues = dataEl.dataset.lngValues.split(',').map(Number);

                    new Chart(document.getElementById('revChart'), {
                        type: 'line', data: { labels: dailyLabels, datasets: [{ data: dailyValues, borderColor: colors.indigo, fill: true, tension: 0.4 }] },
                        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } }
                    });
                    new Chart(document.getElementById('mixChart'), {
                        type: 'doughnut', data: { labels: provLabels, datasets: [{ data: provValues, backgroundColor: [colors.indigo, colors.cyan, '#8b5cf6', '#10b981'] }] },
                        options: { responsive: true, maintainAspectRatio: false, cutout: '70%', plugins: { legend: { position: 'bottom' } } }
                    });
                    new Chart(document.getElementById('distChart'), {
                        type: 'bar', data: { labels: lngLabels, datasets: [{ data: lngValues, backgroundColor: colors.cyan }] },
                        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } }
                    });
                    function updateClock() {
                        const el = document.getElementById('live-clock');
                        if (el) el.textContent = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
                    }
                    updateClock(); setInterval(updateClock, 1000);
                    function toggleSidebar() { document.getElementById('app-sidebar').classList.toggle('open'); }
                </script>
            </body>

            </html>