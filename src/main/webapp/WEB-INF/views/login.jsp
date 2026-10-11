<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Login — Airport Lounge Manager</title>
        <meta name="description" content="Secure access portal for Airport Lounge Manager operations.">
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
    </head>

    <body class="login-page">
        <!-- Floating background -->
        <div class="login-bg">
            <span class="orb1"></span>
            <span class="orb2"></span>
            <span class="orb3"></span>
            <span class="orb4"></span>
            <span class="particle"></span>
            <span class="particle"></span>
            <span class="particle"></span>
            <span class="particle"></span>
            <span class="particle"></span>
            <span class="particle"></span>
            <span class="particle"></span>
        </div>

        <div class="login-card">
            <div class="login-logo">
                <div class="login-logo-icon">&#9992;</div>
                <h1>Lounge Access</h1>
                <p id="login-tagline"></p>
            </div>

            <c:if test="${not empty error}">
                <div class="login-error">
                    ${error}
                </div>
            </c:if>

            <form class="login-form" action="login" method="post">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" placeholder="Enter your username" required autofocus>

                <label for="password">Password</label>
                <input type="password" id="password" name="password" placeholder="Enter your password" required>

                <button type="submit" class="btn-login">Sign In</button>
            </form>
            <p style="margin-top: 0.75rem; color: #94a3b8;">
                No account? <a href="${pageContext.request.contextPath}/register" style="color: #818cf8;">Register</a>
            </p>
            <div class="login-links">
                <button type="button" onclick="openModal('forgot-modal')">Forgot password?</button>
                <button type="button" onclick="openModal('help-modal')">Help</button>
            </div>

            <div class="login-status">
                <span class="login-status-dot"></span>
                <span>Secure access &bull; <span id="clock-login"></span></span>
            </div>
        </div>

        <!-- Forgot password modal -->
        <div id="forgot-modal" class="login-modal-overlay" onclick="closeModalOnOverlay(event, 'forgot-modal')">
            <div class="login-modal" onclick="event.stopPropagation()">
                <h3>Forgot password?</h3>
                <p>Contact your system administrator or airport IT support to reset your password. Lounge staff
                    credentials are managed centrally.</p>
                <button type="button" class="btn-close" onclick="closeModal('forgot-modal')">Close</button>
            </div>
        </div>

        <!-- Help modal -->
        <div id="help-modal" class="login-modal-overlay" onclick="closeModalOnOverlay(event, 'help-modal')">
            <div class="login-modal" onclick="event.stopPropagation()">
                <h3>Help</h3>
                <p>Use your assigned username and password to access the Airport Lounge Manager. For connection status,
                    visit the <strong>Connection Status</strong> page from the home screen. Default credentials are
                    provided by your administrator.</p>
                <button type="button" class="btn-close" onclick="closeModal('help-modal')">Close</button>
            </div>
        </div>

        <script>
            /* Typewriter tagline */
            const phrases = ['Airport Lounge Manager', 'Passenger Operations Hub', 'Enterprise RBAC System', 'Real-time Lounge Dashboard'];
            let pi = 0, ci = 0, deleting = false;
            function typewriter() {
                const el = document.getElementById('login-tagline');
                if (!el) return;
                const phrase = phrases[pi % phrases.length];
                if (!deleting) {
                    el.textContent = phrase.substring(0, ++ci);
                    if (ci === phrase.length) { deleting = true; setTimeout(typewriter, 1800); return; }
                } else {
                    el.textContent = phrase.substring(0, --ci);
                    if (ci === 0) { deleting = false; pi++; }
                }
                setTimeout(typewriter, deleting ? 40 : 75);
            }
            typewriter();
            /* Live mini-clock */
            function loginClock() {
                const el = document.getElementById('clock-login');
                if (el) el.textContent = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
            }
            loginClock(); setInterval(loginClock, 5000);
            function openModal(id) {
                document.getElementById(id).classList.add('active');
                document.body.style.overflow = 'hidden';
            }
            function closeModal(id) {
                document.getElementById(id).classList.remove('active');
                document.body.style.overflow = '';
            }
            function closeModalOnOverlay(event, id) {
                if (event.target === event.currentTarget) closeModal(id);
            }
            document.addEventListener('keydown', function (e) {
                if (e.key === 'Escape') {
                    document.querySelectorAll('.login-modal-overlay.active').forEach(function (el) {
                        el.classList.remove('active');
                        document.body.style.overflow = '';
                    });
                }
            });
        </script>
    </body>

    </html>