<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <title>Dev Console — Airport Lounge Manager</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
        <style>
            :root {
                --clr-bg: #050505;
                --clr-accent: #00ff41;
                --clr-text: #00ff41;
                --clr-dim: #008f11;
            }

            body {
                background: var(--clr-bg);
                color: var(--clr-text);
                font-family: 'JetBrains Mono', monospace;
                padding: 2rem;
                font-size: 0.9rem;
                line-height: 1.5;
            }

            .console-wrap {
                border: 1px solid var(--clr-dim);
                padding: 2rem;
                border-radius: 4px;
                box-shadow: 0 0 20px rgba(0, 143, 17, 0.1);
                max-width: 800px;
                margin: 0 auto;
            }

            .cursor {
                display: inline-block;
                width: 0.6em;
                height: 1.2em;
                background: var(--clr-text);
                vertical-align: middle;
                animation: blink 1s infinite;
            }

            @keyframes blink {

                0%,
                49% {
                    opacity: 1;
                }

                50%,
                100% {
                    opacity: 0;
                }
            }

            .ok {
                color: var(--clr-text);
            }

            .fail {
                color: #ff0000;
                text-shadow: 0 0 10px #ff0000;
            }

            .label {
                color: var(--clr-dim);
                font-weight: bold;
            }

            a {
                color: #fff;
                text-decoration: none;
                border: 1px solid #fff;
                padding: 0.5rem 1rem;
                border-radius: 4px;
                display: inline-block;
                margin-top: 2rem;
                transition: background 0.2s, color 0.2s;
            }

            a:hover {
                background: #fff;
                color: #000;
            }
        </style>
    </head>

    <body>
        <div class="console-wrap">
            <div>[SYSTEM DIAGNOSTIC MODE SELECTED]</div>
            <div>PATH: /console/auth_debug</div>
            <div style="margin-bottom: 2rem;">----------------------------------------</div>

            <c:choose>
                <c:when test="${not empty error}">
                    <div class="fail">CRITICAL ERROR DETECTED</div>
                    <div>COMPONENT: ${step}</div>
                    <div>STACK: ${error}</div>
                </c:when>
                <c:otherwise>
                    <div>&gt; CONNECTION INITIALIZED... <span class="ok">[SUCCESS]</span></div>
                    <div>&gt; USER LOOKUP ('admin')...
                        <c:choose>
                            <c:when test="${not empty user}">
                                <span class="ok">[FOUND]</span> (UUID=${user.id}, ACL=${user.role})
                            </c:when>
                            <c:otherwise>
                                <span class="fail">[NOT_FOUND]</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${not empty user}">
                        <div>&gt; HASH_PREVIEW: ${storedPasswordPreview}</div>
                        <div>&gt; AUTH_VALIDATION:
                            <c:choose>
                                <c:when test="${passwordOk}"><span class="ok">[VERIFIED]</span></c:when>
                                <c:otherwise><span class="fail">[INVALID_MATCH]</span></c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                    <div style="margin-top: 1rem;">STATUS: Infrastructure nominal.<span class="cursor"></span></div>
                </c:otherwise>
            </c:choose>

            <a href="login">RETURN TO INTERFACE</a>
        </div>
    </body>

    </html>