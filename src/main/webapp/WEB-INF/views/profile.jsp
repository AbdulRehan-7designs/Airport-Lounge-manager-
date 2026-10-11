<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>My Profile — Airport Lounge Manager</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                rel="stylesheet">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            <style>
                .profile-layout {
                    display: grid;
                    grid-template-columns: 320px 1fr;
                    gap: 2rem;
                    align-items: start;
                }

                @media (max-width: 900px) {
                    .profile-layout {
                        grid-template-columns: 1fr;
                    }
                }

                .profile-avatar-large {
                    width: 120px;
                    height: 120px;
                    border-radius: 50%;
                    background: linear-gradient(135deg, var(--clr-primary), var(--clr-violet));
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 3rem;
                    font-weight: 800;
                    color: white;
                    margin: 0 auto 1.5rem;
                    box-shadow: 0 0 40px rgba(99, 102, 241, 0.25);
                    border: 4px solid var(--clr-bg);
                }

                .info-card {
                    background: var(--clr-card);
                    border: 1px solid var(--clr-border);
                    border-radius: var(--radius-lg);
                    padding: 2rem;
                    text-align: center;
                }

                .info-card h3 {
                    font-size: 1.25rem;
                    margin-bottom: 0.25rem;
                }

                .info-card p {
                    font-size: 0.875rem;
                    color: var(--clr-muted);
                    margin-bottom: 1.5rem;
                }
            </style>
        </head>

        <body>
            <div class="app-shell">
                <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

                    <div class="main-content">
                        <c:set var="pageTitle" value="Account Settings" scope="request" />
                        <%@ include file="/WEB-INF/includes/topbar.jsp" %>

                            <div class="page-content">
                                <div class="page-header">
                                    <h1>👤 My Profile</h1>
                                    <p>Manage your account information and preferences.</p>
                                </div>

                                <c:if test="${not empty message}">
                                    <div class="alert alert-success">✓ ${message}</div>
                                </c:if>
                                <c:if test="${not empty error}">
                                    <div class="alert alert-error">⚠ ${error}</div>
                                </c:if>

                                <div class="profile-layout">
                                    <!-- User card -->
                                    <div class="info-card">
                                        <div class="profile-avatar-large">
                                            ${userInitials}
                                        </div>
                                        <h3>${currentUser.username}</h3>
                                        <p>${roleDisplayName}</p>

                                        <div
                                            style="text-align: left; background: rgba(8,13,26,0.4); border-radius: 12px; padding: 1rem; border: 1px solid var(--clr-border);">
                                            <div
                                                style="font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--clr-faint); margin-bottom: 0.5rem; font-weight: 700;">
                                                Account Data</div>
                                            <div style="font-size: 0.82rem; margin-bottom: 0.25rem;">Email: <span
                                                    style="color: var(--clr-text);">${empty currentUser.email ? 'Not
                                                    set' :
                                                    currentUser.email}</span></div>
                                        </div>
                                    </div>

                                    <!-- Edit form -->
                                    <div class="glass-panel">
                                        <div class="glass-panel-header">
                                            <h2>Personal Details</h2>
                                        </div>
                                        <div class="glass-panel-body">
                                            <form action="${pageContext.request.contextPath}/profile" method="post">
                                                <div class="form-row">
                                                    <div class="form-group">
                                                        <label class="form-label" for="username">Display
                                                            Username</label>
                                                        <input class="form-control" type="text" id="username"
                                                            name="username" value="${currentUser.username}" required>
                                                    </div>
                                                    <div class="form-group">
                                                        <label class="form-label" for="email">Primary Email</label>
                                                        <input class="form-control" type="email" id="email" name="email"
                                                            value="${currentUser.email}" placeholder="your@email.com">
                                                    </div>
                                                </div>

                                                <hr
                                                    style="border: none; border-top: 1px solid var(--clr-border); margin: 1.5rem 0;">

                                                <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
                                                    <button type="reset" class="btn btn-ghost">Reset
                                                        Changes</button>
                                                    <button type="submit" class="btn btn-primary">Update
                                                        Profile</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>

                            </div>
                    </div>
            </div>


        </body>

        </html>