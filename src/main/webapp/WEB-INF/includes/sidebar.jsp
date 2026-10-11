<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

            <!-- ★ SIDEBAR -->
            <aside class="sidebar" id="app-sidebar">

                <!-- Logo -->
                <a class="sidebar-logo" href="${ctx}/dashboard">
                    <div class="sidebar-logo-icon">✈</div>
                    <div class="sidebar-logo-text">
                        <strong>LoungeManager</strong>
                        <span>Airport Operations</span>
                    </div>
                </a>

                <!-- Navigation -->
                <nav class="sidebar-nav">
                    <c:set var="path" value="${pageContext.request.servletPath}" />

                    <c:choose>
                        <c:when test="${isPassenger}">
                            <span class="sidebar-section-label">Passenger Hub</span>
                            <a class="sidebar-link ${path == '/dashboard' ? 'active' : ''}" href="${ctx}/dashboard">
                                <em class="sidebar-link-icon">🏠</em> Home
                            </a>
                            <a class="sidebar-link ${path == '/lounges' || fn:startsWith(path, '/lounges/') ? 'active' : ''}"
                                href="${ctx}/lounges">
                                <em class="sidebar-link-icon">🛋</em> Browse Lounges
                            </a>
                            <a class="sidebar-link ${path == '/access-requests' || fn:startsWith(path, '/access-requests/') ? 'active' : ''}"
                                href="${ctx}/access-requests">
                                <em class="sidebar-link-icon">🔑</em> My Requests
                            </a>
                            <a class="sidebar-link ${path == '/profile' ? 'active' : ''}" href="${ctx}/profile">
                                <em class="sidebar-link-icon">👤</em> My Profile
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span class="sidebar-section-label">Operations</span>
                            <a class="sidebar-link ${path == '/dashboard' ? 'active' : ''}" href="${ctx}/dashboard">
                                <em class="sidebar-link-icon">📊</em> Dashboard
                            </a>

                            <c:if test="${canDesk}">
                                <a class="sidebar-link ${path == '/passengers' || fn:startsWith(path, '/passengers/') ? 'active' : ''}"
                                    href="${ctx}/passengers">
                                    <em class="sidebar-link-icon">🪪</em> Passenger Registry
                                </a>
                                <a class="sidebar-link ${path == '/access-requests' || fn:startsWith(path, '/access-requests/') ? 'active' : ''}"
                                    href="${ctx}/access-requests">
                                    <em class="sidebar-link-icon">🔑</em> Access Requests
                                    <c:if test="${not empty pendingCount && pendingCount > 0}">
                                        <span class="sidebar-badge">${pendingCount}</span>
                                    </c:if>
                                </a>
                            </c:if>

                            <c:if test="${canInventory}">
                                <a class="sidebar-link ${path == '/lounges' || fn:startsWith(path, '/lounges/') ? 'active' : ''}"
                                    href="${ctx}/lounges">
                                    <em class="sidebar-link-icon">🛋</em> Lounge Records
                                </a>
                            </c:if>

                            <c:if test="${isManager || isAuditor}">
                                <span class="sidebar-section-label">Financials</span>
                                <c:if test="${isManager}">
                                    <a class="sidebar-link ${path == '/reports' || fn:startsWith(path, '/reports/') ? 'active' : ''}"
                                        href="${ctx}/reports">
                                        <em class="sidebar-link-icon">📈</em> Yield Reports
                                    </a>
                                </c:if>
                                <a class="sidebar-link ${path == '/transactions' || fn:startsWith(path, '/transactions/') ? 'active' : ''}"
                                    href="${ctx}/transactions">
                                    <em class="sidebar-link-icon">💳</em> Ledger
                                </a>
                            </c:if>

                            <c:if test="${isAdmin}">
                                <span class="sidebar-section-label">System Control</span>
                                <a class="sidebar-link ${path == '/users' || fn:startsWith(path, '/users/') ? 'active' : ''}"
                                    href="${ctx}/users">
                                    <em class="sidebar-link-icon">👥</em> User Management
                                </a>
                                <a class="sidebar-link ${path == '/lounges' || fn:startsWith(path, '/lounges/') ? 'active' : ''}"
                                    href="${ctx}/lounges">
                                    <em class="sidebar-link-icon">🛋</em> Manage Lounges
                                </a>
                                <a class="sidebar-link ${path == '/access-providers' || fn:startsWith(path, '/access-providers/') ? 'active' : ''}"
                                    href="${ctx}/access-providers">
                                    <em class="sidebar-link-icon">🏢</em> Service Providers
                                </a>
                                <a class="sidebar-link ${path == '/airports' || fn:startsWith(path, '/airports/') ? 'active' : ''}"
                                    href="${ctx}/airports">
                                    <em class="sidebar-link-icon">🛫</em> Airport Nodes
                                </a>
                                <a class="sidebar-link ${path == '/settings' || fn:startsWith(path, '/settings/') ? 'active' : ''}"
                                    href="${ctx}/settings">
                                    <em class="sidebar-link-icon">⚙</em> System Config
                                </a>
                            </c:if>
                        </c:otherwise>
                    </c:choose>

                </nav>

                <!-- User + Logout -->
                <div class="sidebar-footer">
                    <div class="sidebar-user">
                        <div class="sidebar-avatar">
                            ${userInitials}
                        </div>
                        <div class="sidebar-user-info">
                            <strong>${currentUser.username}</strong>
                            <span>${roleDisplayName}</span>
                        </div>
                    </div>
                    <a class="sidebar-logout" href="${ctx}/logout">
                        <em>↩</em> Sign Out
                    </a>
                </div>

            </aside>