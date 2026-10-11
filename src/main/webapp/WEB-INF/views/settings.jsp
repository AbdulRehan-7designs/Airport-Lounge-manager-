<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Global Settings — Airport Lounge Manager</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                rel="stylesheet">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        </head>

        <body>
            <div class="app-shell">
                <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
                    <div class="main-content">
                        <c:set var="pageTitle" value="System Configuration" scope="request" />
                        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
                            <div class="page-content">
                                <div class="page-header">
                                    <h1>⚙️ System Parameters</h1>
                                    <p>Configure global behavior, thresholds, and operational constraints for the entire
                                        network.</p>
                                </div>

                                <c:if test="${param.saved}">
                                    <div class="alert alert-success" style="margin-bottom: 2rem;">✓ Configuration
                                        updated successfully.</div>
                                </c:if>

                                <div class="glass-panel" style="max-width: 800px;">
                                    <div class="glass-panel-header">
                                        <h2>Core Configuration</h2>
                                    </div>
                                    <div class="glass-panel-body">
                                        <form action="settings" method="post">
                                            <c:forEach var="entry" items="${settings}">
                                                <div class="form-group" style="margin-bottom: 2rem;">
                                                    <label class="form-label"
                                                        style="display:flex; justify-content:space-between;">
                                                        <span>${entry.key}</span>
                                                        <code
                                                            style="color:var(--clr-primary-lit); font-size: 0.7rem; font-family: monospace;">ENV_VAR</code>
                                                    </label>
                                                    <c:choose>
                                                        <c:when
                                                            test="${entry.value[0] == 'true' || entry.value[0] == 'false'}">
                                                            <select name="${entry.key}" class="form-control">
                                                                <option value="true" ${entry.value[0]=='true'
                                                                    ? 'selected' : '' }>Enabled</option>
                                                                <option value="false" ${entry.value[0]=='false'
                                                                    ? 'selected' : '' }>Disabled</option>
                                                            </select>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <input type="text" name="${entry.key}"
                                                                value="${entry.value[0]}" class="form-control">
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <p
                                                        style="font-size: 0.75rem; color: var(--clr-muted); margin-top: 0.5rem; line-height: 1.4;">
                                                        ${entry.value[1]}</p>
                                                </div>
                                            </c:forEach>

                                            <hr
                                                style="border: none; border-top: 1px solid var(--clr-border); margin: 2rem 0;">

                                            <div style="display:flex; justify-content:flex-end;">
                                                <button type="submit" class="btn btn-primary">Commit Changes</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>

                                <div
                                    style="margin-top: 2rem; border-left: 2px solid var(--clr-amber); padding-left: 1rem; max-width: 800px;">
                                    <p style="font-size: 0.75rem; color: var(--clr-amber); font-weight: 600;">⚠️
                                        Performance
                                        Warning</p>
                                    <p style="font-size: 0.75rem; color: var(--clr-muted);">System-wide changes may
                                        cause a
                                        brief reload of terminal nodes. Ensure no critical check-ins are in progress.
                                    </p>
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