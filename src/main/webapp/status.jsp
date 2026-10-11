<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Telemetry — Airport Lounge Manager</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link
            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
        <style>
            .telemetry-root {
                font-family: 'JetBrains Mono', monospace;
                min-height: 100vh;
                background: #080d1a;
                padding: 2rem;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
            }

            .status-header {
                text-align: center;
                margin-bottom: 3rem;
            }

            .status-header h1 {
                font-size: 1.25rem;
                letter-spacing: 0.1em;
                text-transform: uppercase;
                color: var(--clr-muted);
            }

            .grid-status {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
                gap: 1.5rem;
                width: 100%;
                max-width: 1000px;
            }

            .status-panel {
                background: rgba(14, 22, 40, 0.7);
                border: 1px solid var(--clr-border);
                border-radius: 16px;
                padding: 2rem;
                position: relative;
                overflow: hidden;
                backdrop-filter: blur(10px);
            }

            .status-panel::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                height: 3px;
                background: var(--line-color, var(--clr-primary));
            }

            .status-label {
                font-size: 0.7rem;
                color: var(--clr-faint);
                text-transform: uppercase;
                margin-bottom: 1rem;
                display: block;
            }

            .status-value {
                font-size: 1.5rem;
                font-weight: 700;
                color: #fff;
                margin-bottom: 0.5rem;
                display: flex;
                align-items: center;
                gap: 0.75rem;
            }

            .status-desc {
                font-size: 0.8rem;
                color: var(--clr-muted);
                line-height: 1.6;
            }

            .binary-rain {
                color: var(--clr-faint);
                font-size: 0.65rem;
                opacity: 0.2;
                line-height: 1.1;
                margin-top: 1rem;
                word-break: break-all;
                height: 40px;
                overflow: hidden;
            }

            .nav-back {
                margin-top: 3rem;
                text-decoration: none;
                color: var(--clr-primary-lit);
                font-size: 0.9rem;
                font-weight: 600;
                display: flex;
                align-items: center;
                gap: 0.5rem;
            }

            .nav-back:hover {
                text-decoration: underline;
            }
        </style>
    </head>

    <body style="margin:0;">
        <div class="login-bg">
            <span class="orb2"></span>
            <span class="orb4"></span>
        </div>

        <div class="telemetry-root">
            <header class="status-header">
                <h1>Infrastructure Status</h1>
                <p style="color: var(--clr-emerald); font-size: 0.9rem; margin-top: 0.5rem;">System
                    heartbeat: OK</p>
            </header>

            <div class="grid-status">
                <!-- Node Panel -->
                <div class="status-panel" style="--line-color: var(--clr-emerald);">
                    <span class="status-label">Node Status</span>
                    <div class="status-value">
                        <span class="live-dot"></span> Java Runtime
                    </div>
                    <p class="status-desc">Tomcat servlet container is listening on port 8080. Thread pool
                        initialized.</p>
                    <div class="binary-rain" id="rain-1"></div>
                </div>

                <!-- Database Panel -->
                <div class="status-panel" style="--line-color: ${sqlOk ? 'var(--clr-emerald)' : 'var(--clr-rose)'};">
                    <span class="status-label">Persistence Layer</span>
                    <div class="status-value">
                        <c:choose>
                            <c:when test="${sqlOk}">🛰️ Connectivity Link</c:when>
                            <c:otherwise>🚫 Link Severed</c:otherwise>
                        </c:choose>
                    </div>
                    <p class="status-desc">
                        ${sqlMessage}
                    </p>
                    <div style="margin-top: 1rem; font-size: 0.75rem; color: var(--clr-faint);">
                        Latency: <span style="color:var(--clr-primary-lit);">${latency}ms</span> &bull;
                        Pool: <span style="color:var(--clr-primary-lit);">Active</span>
                    </div>
                    <div class="binary-rain" id="rain-2"></div>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/" class="nav-back">
                <span>←</span> Return to Launchpad
            </a>
        </div>

        <script>
            function genRain(id) {
                const el = document.getElementById(id);
                if (!el) return;
                setInterval(() => {
                    let s = '';
                    for (let i = 0; i < 300; i++) s += Math.round(Math.random());
                    el.textContent = s;
                }, 100);
            }
            genRain('rain-1');
            genRain('rain-2');
        </script>
    </body>

    </html>