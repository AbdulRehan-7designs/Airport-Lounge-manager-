<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <c:set var="statusCode"
        value="${requestScope['javax.servlet.error.status_code'] != null ? requestScope['javax.servlet.error.status_code'] : 500}" />
    <c:set var="message" value="${requestScope['javax.servlet.error.message']}" />
    <c:choose>
        <c:when test="${empty message}">
            <c:choose>
                <c:when test="${statusCode == 404}">
                    <c:set var="message" value="The page you requested could not be found." />
                </c:when>
                <c:when test="${statusCode == 403}">
                    <c:set var="message" value="You don't have permission to access this resource." />
                </c:when>
                <c:otherwise>
                    <c:set var="message" value="An unexpected error occurred. Please try again." />
                </c:otherwise>
            </c:choose>
        </c:when>
    </c:choose>
    <c:if test="${statusCode == 500 && not empty pageContext.exception}">
        <c:set var="message" value="${pageContext.exception.message}" />
    </c:if>
    <c:set var="emoji" value="${statusCode == 404 ? '🔍' : (statusCode == 403 ? '🔒' : '⚡')}" />
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Error <%= statusCode %> — Airport Lounge Manager</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
        <style>
            .error-screen {
                min-height: 100vh;
                display: flex;
                align-items: center;
                justify-content: center;
                background: var(--clr-bg);
                position: relative;
                overflow: hidden;
            }

            .error-screen::before {
                content: '<%= statusCode %>';
                position: absolute;
                font-size: clamp(180px, 30vw, 320px);
                font-weight: 900;
                color: rgba(99, 102, 241, 0.04);
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                pointer-events: none;
                user-select: none;
                letter-spacing: -0.05em;
            }

            .error-card {
                background: var(--clr-card);
                border: 1px solid var(--clr-border);
                border-radius: var(--radius-lg);
                padding: 3rem 2.5rem;
                max-width: 460px;
                width: 100%;
                text-align: center;
                box-shadow: var(--shadow-card), var(--shadow-glow);
                position: relative;
                z-index: 1;
                animation: cardIn 0.5s var(--ease-out);
            }

            @keyframes cardIn {
                from {
                    opacity: 0;
                    transform: translateY(24px);
                }

                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            .error-emoji {
                font-size: 3rem;
                margin-bottom: 1rem;
                display: block;
            }

            .error-code {
                font-size: 4rem;
                font-weight: 900;
                letter-spacing: -0.04em;
                background: linear-gradient(135deg, var(--clr-rose), var(--clr-violet));
                -webkit-background-clip: text;
                background-clip: text;
                -webkit-text-fill-color: transparent;
                margin-bottom: 0.5rem;
            }

            .error-msg {
                font-size: 0.9rem;
                color: var(--clr-muted);
                margin-bottom: 2rem;
                line-height: 1.6;
            }

            .error-actions {
                display: flex;
                gap: 0.75rem;
                justify-content: center;
                flex-wrap: wrap;
            }
        </style>
    </head>

    <body>
        <div class="login-bg">
            <span class="orb1"></span>
            <span class="orb2"></span>
            <span class="orb3"></span>
        </div>
        <div class="error-screen">
            <div class="error-card">
                <span class="error-emoji">${emoji}</span>
                <div class="error-code">${statusCode}</div>
                <p class="error-msg">${message}</p>
                <div class="error-actions">
                    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">🏠 Home</a>
                    <a href="javascript:history.back()" class="btn btn-ghost">← Go Back</a>
                    <c:if test="${statusCode == 403}">
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-ghost">Sign In</a>
                    </c:if>
                </div>
            </div>
        </div>
        </div>
    </body>

    </html>