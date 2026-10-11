<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>User Directory — Airport Lounge Manager</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                rel="stylesheet">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            <style>
                .user-table {
                    width: 100%;
                    border-collapse: separate;
                    border-spacing: 0 0.75rem;
                }

                .user-table th {
                    text-align: left;
                    padding: 1rem;
                    color: var(--clr-muted);
                    font-size: 0.75rem;
                    text-transform: uppercase;
                    letter-spacing: 0.05em;
                }

                .user-row {
                    background: var(--clr-card);
                    transition: transform 0.2s, box-shadow 0.2s;
                }

                .user-row:hover {
                    transform: scale(1.005);
                    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
                }

                .user-row td {
                    padding: 1rem;
                    border-top: 1px solid var(--clr-border);
                    border-bottom: 1px solid var(--clr-border);
                }

                .user-row td:first-child {
                    border-left: 1px solid var(--clr-border);
                    border-radius: 12px 0 0 12px;
                }

                .user-row td:last-child {
                    border-right: 1px solid var(--clr-border);
                    border-radius: 0 12px 12px 0;
                }

                .role-pill {
                    padding: 0.25rem 0.75rem;
                    border-radius: 999px;
                    font-size: 0.7rem;
                    font-weight: 700;
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
                        <c:set var="pageTitle" value="User Registry" scope="request" />
                        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
                            <div class="page-content">
                                <div class="page-header">
                                    <h1>👥 User Directory</h1>
                                    <p>Global registry of all personnel and passengers authenticated within the
                                        network.
                                    </p>
                                </div>

                                <div class="glass-panel">
                                    <table class="user-table">
                                        <thead>
                                            <tr>
                                                <th>Member</th>
                                                <th>Role / Level</th>
                                                <th>Access Status</th>
                                                <th style="text-align: right;">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="u" items="${users}">
                                                <tr class="user-row">
                                                    <td>
                                                        <div style="display:flex; align-items:center; gap:0.75rem;">
                                                            <div class="avatar-sm"
                                                                style="background: var(--clr-primary);">
                                                                <c:out
                                                                    value="${not empty u.username ? u.username.substring(0,1).toUpperCase() : '?'}" />
                                                            </div>
                                                            <div>
                                                                <div style="font-weight: 600; color: #fff;">
                                                                    ${u.username}</div>
                                                                <div
                                                                    style="font-size: 0.75rem; color: var(--clr-muted);">
                                                                    ${u.email}</div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <form action="users" method="post" id="role-form-${u.id}"
                                                            style="margin:0;">
                                                            <input type="hidden" name="action" value="updateRole">
                                                            <input type="hidden" name="id" value="${u.id}">
                                                            <select name="role" class="form-control"
                                                                style="padding: 0.3rem 0.5rem; font-size: 0.8rem; width: auto;"
                                                                onchange="this.form.submit()">
                                                                <option value="ADMIN" ${u.role=='ADMIN' ? 'selected'
                                                                    : '' }>Admin</option>
                                                                <option value="OPERATIONS_MANAGER"
                                                                    ${u.role=='OPERATIONS_MANAGER' ? 'selected' : '' }>
                                                                    Ops Manager</option>
                                                                <option value="SUPERVISOR" ${u.role=='SUPERVISOR'
                                                                    ? 'selected' : '' }>Supervisor</option>
                                                                <option value="RECEPTION" ${u.role=='RECEPTION'
                                                                    ? 'selected' : '' }>Reception</option>
                                                                <option value="AUDITOR" ${u.role=='AUDITOR' ? 'selected'
                                                                    : '' }>Auditor</option>
                                                                <option value="PASSENGER" ${u.role=='PASSENGER'
                                                                    ? 'selected' : '' }>Passenger</option>
                                                            </select>
                                                        </form>
                                                    </td>
                                                    <td>
                                                        <span
                                                            class="badge ${u.active ? 'badge-active' : 'badge-inactive'}">
                                                            <span class="badge-dot"></span>
                                                            ${u.active ? 'Active' : 'Deactivated'}
                                                        </span>
                                                    </td>
                                                    <td style="text-align: right;">
                                                        <form action="users" method="post" style="display:inline;">
                                                            <input type="hidden" name="action" value="toggleActive">
                                                            <input type="hidden" name="id" value="${u.id}">
                                                            <input type="hidden" name="active" value="${!u.active}">
                                                            <c:choose>
                                                                <c:when test="${u.active}">
                                                                    <button type="submit" class="btn btn-ghost btn-sm"
                                                                        style="color: var(--clr-rose);">Deactivate</button>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <button type="submit" class="btn btn-ghost btn-sm"
                                                                        style="color: var(--clr-emerald);">Reactivate</button>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </form>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                    </div>
            </div>

        </body>

        </html>