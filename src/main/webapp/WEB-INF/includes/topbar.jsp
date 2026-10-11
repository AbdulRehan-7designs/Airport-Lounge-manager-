<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="com.alm.dao.NotificationDAO" %>
        <%@ page import="com.alm.model.User" %>
            <%@ page import="com.alm.model.Notification" %>
                <%@ page import="java.util.List" %>
                    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
                        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                            <% User topUser=(User) session.getAttribute("user"); int unreadCount=0; List<Notification>
                                topNotifications = null;
                                if (topUser != null) {
                                NotificationDAO notificationDAO = new NotificationDAO();
                                unreadCount = notificationDAO.getUnreadCount(topUser.getId());
                                topNotifications = notificationDAO.getByUser(topUser.getId());
                                }
                                request.setAttribute("topNotifications", topNotifications);

                                String topTitle = (String) request.getAttribute("pageTitle");
                                if (topTitle == null) {
                                topTitle = "Management Console";
                                }
                                %>
                                <div class="topbar">
                                    <button class="sidebar-toggle" onclick="toggleSidebar()"
                                        aria-label="Toggle sidebar">☰</button>
                                    <span class="topbar-title">
                                        <%= topTitle %>
                                    </span>

                                    <div class="topbar-right">
                                        <!-- Notification Center -->
                                        <div class="notif-wrapper">
                                            <button class="notif-trigger <%= unreadCount > 0 ? " has-unread" : "" %>"
                                                id="notif-bell"
                                                onclick="toggleNotifications()">
                                                <em class="notif-icon">🔔</em>
                                                <% if (unreadCount> 0) { %>
                                                    <span class="notif-badge">
                                                        <%= unreadCount %>
                                                    </span>
                                                    <% } %>
                                            </button>

                                            <div class="notif-panel" id="notif-panel">
                                                <div class="notif-header">
                                                    <h3>Notifications</h3>
                                                    <button onclick="markAllRead()" class="btn-text">Mark all
                                                        read</button>
                                                </div>
                                                <div class="notif-list" id="notif-items">
                                                    <c:choose>
                                                        <c:when test="${empty topNotifications}">
                                                            <div class="notif-placeholder">All caught up! ✨</div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:forEach var="n" items="${topNotifications}">
                                                                <div class="notif-item ${n.read ? '' : 'unread'}"
                                                                    onclick="markRead('${n.id}', this)">
                                                                    <div class="notif-type-icon">
                                                                        <c:choose>
                                                                            <c:when test="${n.type == 'WARNING'}">⚠️
                                                                            </c:when>
                                                                            <c:when test="${n.type == 'SUCCESS'}">✅
                                                                            </c:when>
                                                                            <c:when test="${n.type == 'ERROR'}">🚨
                                                                            </c:when>
                                                                            <c:otherwise>ℹ️</c:otherwise>
                                                                        </c:choose>
                                                                    </div>
                                                                    <div class="notif-body">
                                                                        <div class="notif-title">
                                                                            <c:out value="${n.title}" />
                                                                        </div>
                                                                        <div class="notif-msg">
                                                                            <c:out value="${n.message}" />
                                                                        </div>
                                                                        <span class="notif-time">
                                                                            <fmt:formatDate value="${n.createdAt}"
                                                                                pattern="MMM dd, yyyy HH:mm" />
                                                                        </span>
                                                                    </div>
                                                                </div>
                                                            </c:forEach>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="live-status">
                                            <div class="live-dot" title="Live Link Active"></div>
                                            <span class="topbar-clock" id="live-clock"></span>
                                        </div>
                                    </div>
                                </div>

                                <style>
                                    .notif-wrapper {
                                        position: relative;
                                        margin-right: 1.5rem;
                                    }

                                    .notif-trigger {
                                        background: rgba(255, 255, 255, 0.05);
                                        border: 1px solid var(--clr-border);
                                        width: 40px;
                                        height: 40px;
                                        border-radius: 10px;
                                        cursor: pointer;
                                        display: flex;
                                        align-items: center;
                                        justify-content: center;
                                        transition: all 0.2s;
                                        position: relative;
                                    }

                                    .notif-trigger:hover {
                                        background: rgba(255, 255, 255, 0.1);
                                        border-color: var(--clr-primary);
                                    }

                                    .notif-trigger.has-unread .notif-icon {
                                        animation: bellShake 2s infinite ease-in-out;
                                    }

                                    .notif-badge {
                                        position: absolute;
                                        top: -5px;
                                        right: -5px;
                                        background: var(--clr-rose);
                                        color: white;
                                        font-size: 0.65rem;
                                        font-weight: 800;
                                        min-width: 18px;
                                        height: 18px;
                                        border-radius: 9px;
                                        display: flex;
                                        align-items: center;
                                        justify-content: center;
                                        border: 2px solid #0f172a;
                                    }

                                    .notif-panel {
                                        position: absolute;
                                        top: calc(100% + 15px);
                                        right: 0;
                                        width: 320px;
                                        background: rgba(30, 41, 59, 0.95);
                                        backdrop-filter: blur(20px);
                                        border: 1px solid var(--clr-border);
                                        border-radius: var(--radius-lg);
                                        box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
                                        display: none;
                                        flex-direction: column;
                                        overflow: hidden;
                                        z-index: 1000;
                                        animation: slideDown 0.3s ease-out;
                                    }

                                    .notif-panel.open {
                                        display: flex;
                                    }

                                    .notif-header {
                                        padding: 1rem 1.25rem;
                                        border-bottom: 1px solid var(--clr-border);
                                        display: flex;
                                        justify-content: space-between;
                                        align-items: center;
                                    }

                                    .notif-header h3 {
                                        font-size: 0.9rem;
                                        font-weight: 700;
                                        margin: 0;
                                    }

                                    .btn-text {
                                        background: none;
                                        border: none;
                                        color: var(--clr-primary-lit);
                                        font-size: 0.75rem;
                                        cursor: pointer;
                                        font-weight: 600;
                                        padding: 0.2rem 0.5rem;
                                        border-radius: 4px;
                                    }

                                    .btn-text:hover {
                                        background: rgba(99, 102, 241, 0.1);
                                    }

                                    .notif-list {
                                        max-height: 400px;
                                        overflow-y: auto;
                                    }

                                    .notif-item {
                                        padding: 1rem 1.25rem;
                                        border-bottom: 1px solid rgba(255, 255, 255, 0.05);
                                        display: flex;
                                        gap: 0.75rem;
                                        transition: background 0.2s;
                                        cursor: pointer;
                                    }

                                    .notif-item:hover {
                                        background: rgba(255, 255, 255, 0.03);
                                    }

                                    .notif-item.unread {
                                        background: rgba(99, 102, 241, 0.05);
                                    }

                                    .notif-type-icon {
                                        font-size: 1.2rem;
                                    }

                                    .notif-body {
                                        flex: 1;
                                    }

                                    .notif-title {
                                        font-size: 0.8rem;
                                        font-weight: 700;
                                        color: #fff;
                                        margin-bottom: 0.2rem;
                                    }

                                    .notif-msg {
                                        font-size: 0.75rem;
                                        color: var(--clr-muted);
                                        line-height: 1.4;
                                    }

                                    .notif-time {
                                        font-size: 0.65rem;
                                        color: var(--clr-faint);
                                        margin-top: 0.5rem;
                                        display: block;
                                    }

                                    .notif-placeholder {
                                        padding: 3rem;
                                        text-align: center;
                                        color: var(--clr-muted);
                                        font-size: 0.8rem;
                                    }

                                    @keyframes bellShake {

                                        0%,
                                        100% {
                                            transform: rotate(0);
                                        }

                                        10%,
                                        30%,
                                        50%,
                                        70%,
                                        90% {
                                            transform: rotate(10deg);
                                        }

                                        20%,
                                        40%,
                                        60%,
                                        80% {
                                            transform: rotate(-10deg);
                                        }
                                    }
                                </style>

                                <script>
                                    async function toggleNotifications() {
                                        const panel = document.getElementById('notif-panel');
                                        panel.classList.toggle('open');
                                    }

                                    async function markRead(id, el) {
                                        if (el && el.classList.contains('unread')) {
                                            el.classList.remove('unread');
                                            updateUnreadBadge(-1);
                                            await fetch('${pageContext.request.contextPath}/api/notifications?action=markRead&id=' + id, { method: 'POST' });
                                        }
                                    }

                                    async function markAllRead() {
                                        document.querySelectorAll('.notif-item.unread').forEach(el => el.classList.remove('unread'));
                                        document.querySelector('.notif-badge')?.remove();
                                        document.getElementById('notif-bell').classList.remove('has-unread');
                                        await fetch('${pageContext.request.contextPath}/api/notifications?action=markAllRead', { method: 'POST' });
                                    }

                                    function updateUnreadBadge(delta) {
                                        const badge = document.querySelector('.notif-badge');
                                        if (badge) {
                                            let count = parseInt(badge.textContent) + delta;
                                            if (count <= 0) {
                                                badge.remove();
                                                document.getElementById('notif-bell').classList.remove('has-unread');
                                            } else {
                                                badge.textContent = count;
                                            }
                                        }
                                    }

                                    // Close on outside click
                                    document.addEventListener('click', (e) => {
                                        const wrapper = document.querySelector('.notif-wrapper');
                                        if (!wrapper.contains(e.target)) {
                                            document.getElementById('notif-panel').classList.remove('open');
                                        }
                                    });
                                </script>