<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>SkyLounge | Next-Gen Airport Lounge Management</title>

            <!-- Fonts -->
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link
                href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Inter:wght@400;500;600&display=swap"
                rel="stylesheet">

            <style>
                :root {
                    --clr-bg: #030712;
                    --clr-primary: #6366f1;
                    --clr-primary-glow: rgba(99, 102, 241, 0.4);
                    --clr-secondary: #0ea5e9;
                    --clr-accent: #10b981;
                    --clr-text: #f8fafc;
                    --clr-text-muted: #94a3b8;
                    --clr-card: rgba(15, 23, 42, 0.6);
                    --clr-border: rgba(255, 255, 255, 0.1);
                    --font-main: 'Outfit', sans-serif;
                    --font-body: 'Inter', sans-serif;
                }

                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }

                body {
                    background-color: var(--clr-bg);
                    color: var(--clr-text);
                    font-family: var(--font-body);
                    overflow-x: hidden;
                    scroll-behavior: smooth;
                }

                h1,
                h2,
                h3,
                .nav-logo {
                    font-family: var(--font-main);
                }

                /* --- Scroll Reveal Animations --- */
                .reveal {
                    opacity: 0;
                    transform: translateY(30px);
                    transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
                }

                .reveal.active {
                    opacity: 1;
                    transform: translateY(0);
                }

                /* --- Navigation --- */
                header {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    z-index: 1000;
                    padding: 1.5rem 2rem;
                    transition: all 0.3s;
                }

                header.scrolled {
                    background: rgba(3, 7, 18, 0.8);
                    backdrop-filter: blur(12px);
                    padding: 1rem 2rem;
                    border-bottom: 1px solid var(--clr-border);
                }

                .nav-container {
                    max-width: 1400px;
                    margin: 0 auto;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }

                .nav-logo {
                    font-size: 1.5rem;
                    font-weight: 800;
                    color: #fff;
                    text-decoration: none;
                    display: flex;
                    align-items: center;
                    gap: 0.75rem;
                }

                .nav-logo span {
                    background: linear-gradient(135deg, var(--clr-primary), var(--clr-secondary));
                    -webkit-background-clip: text;
                    background-clip: text;
                    -webkit-text-fill-color: transparent;
                }

                .nav-links {
                    display: flex;
                    gap: 2.5rem;
                    align-items: center;
                }

                .nav-links a {
                    text-decoration: none;
                    color: var(--clr-text-muted);
                    font-size: 0.95rem;
                    font-weight: 500;
                    transition: color 0.3s;
                }

                .nav-links a:hover {
                    color: var(--clr-text);
                }

                .btn-login {
                    background: rgba(255, 255, 255, 0.05);
                    border: 1px solid var(--clr-border);
                    padding: 0.6rem 1.5rem;
                    border-radius: 99px;
                    color: white !important;
                    transition: all 0.3s !important;
                }

                .btn-login:hover {
                    background: var(--clr-primary);
                    box-shadow: 0 0 20px var(--clr-primary-glow);
                    transform: translateY(-2px);
                }

                /* --- Hero Section --- */
                .hero {
                    position: relative;
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    text-align: center;
                    padding: 8rem 1rem 4rem;
                    overflow: hidden;
                }

                .hero-bg {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    z-index: -1;
                    background: url('${pageContext.request.contextPath}/assets/img/hero-bg.png') center/cover no-repeat;
                    filter: brightness(0.4) saturate(1.2);
                    transform: scale(1.1);
                    transition: transform 0.5s;
                }

                .hero::after {
                    content: '';
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    width: 100%;
                    height: 40%;
                    background: linear-gradient(to top, var(--clr-bg), transparent);
                    z-index: -1;
                }

                .hero-content {
                    max-width: 900px;
                    z-index: 10;
                }

                .hero-tag {
                    display: inline-flex;
                    align-items: center;
                    background: rgba(99, 102, 241, 0.1);
                    border: 1px solid rgba(99, 102, 241, 0.2);
                    padding: 0.5rem 1rem;
                    border-radius: 99px;
                    color: var(--clr-primary);
                    font-size: 0.85rem;
                    font-weight: 600;
                    margin-bottom: 2rem;
                    text-transform: uppercase;
                    letter-spacing: 0.1em;
                }

                .hero-title {
                    font-size: clamp(3rem, 8vw, 5.5rem);
                    line-height: 1;
                    font-weight: 800;
                    margin-bottom: 1.5rem;
                    background: linear-gradient(135deg, #fff 30%, #94a3b8);
                    -webkit-background-clip: text;
                    background-clip: text;
                    -webkit-text-fill-color: transparent;
                    letter-spacing: -0.03em;
                }

                .hero-subtitle {
                    font-size: 1.25rem;
                    color: var(--clr-text-muted);
                    max-width: 600px;
                    margin: 0 auto 3rem;
                    line-height: 1.6;
                }

                .cta-group {
                    display: flex;
                    gap: 1.5rem;
                    justify-content: center;
                    align-items: center;
                }

                .btn-primary {
                    background: var(--clr-primary);
                    color: white;
                    padding: 1rem 2.5rem;
                    border-radius: 12px;
                    text-decoration: none;
                    font-weight: 700;
                    font-size: 1.1rem;
                    transition: all 0.3s;
                    box-shadow: 0 10px 40px var(--clr-primary-glow);
                }

                .btn-primary:hover {
                    transform: translateY(-4px);
                    filter: brightness(1.1);
                    box-shadow: 0 15px 50px var(--clr-primary-glow);
                }

                .btn-secondary {
                    color: #fff;
                    padding: 1rem 2.5rem;
                    text-decoration: none;
                    font-weight: 600;
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    transition: 0.3s;
                }

                .btn-secondary:hover {
                    opacity: 0.7;
                }

                /* --- Partner Marquee --- */
                .marquee {
                    padding: 4rem 0;
                    background: var(--clr-bg);
                    border-top: 1px solid var(--clr-border);
                    overflow: hidden;
                    white-space: nowrap;
                }

                .marquee-content {
                    display: inline-block;
                    animation: scroll 30s linear infinite;
                }

                .marquee-content span {
                    display: inline-block;
                    font-family: var(--font-main);
                    font-size: 2rem;
                    font-weight: 800;
                    color: rgba(255, 255, 255, 0.1);
                    margin-right: 4rem;
                    letter-spacing: 0.1em;
                }

                @keyframes scroll {
                    from {
                        transform: translateX(0);
                    }

                    to {
                        transform: translateX(-50%);
                    }
                }

                /* --- Features Bento Grid --- */
                .section {
                    padding: 8rem 2rem;
                    max-width: 1400px;
                    margin: 0 auto;
                }

                .section-header {
                    text-align: center;
                    margin-bottom: 5rem;
                }

                .section-header h2 {
                    font-size: clamp(2rem, 5vw, 3.5rem);
                    margin-bottom: 1.5rem;
                }

                .bento-grid {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    grid-auto-rows: 240px;
                    gap: 1.5rem;
                }

                .bento-item {
                    background: var(--clr-card);
                    border: 1px solid var(--clr-border);
                    border-radius: 24px;
                    padding: 2.5rem;
                    position: relative;
                    overflow: hidden;
                    backdrop-filter: blur(8px);
                    transition: all 0.4s;
                }

                .bento-item:hover {
                    border-color: var(--clr-primary);
                    transform: scale(1.02);
                    background: rgba(15, 23, 42, 0.8);
                }

                .bento-item.large {
                    grid-column: span 2;
                    grid-row: span 2;
                }

                .bento-item h3 {
                    font-size: 1.5rem;
                    margin-bottom: 1rem;
                    color: white;
                }

                .bento-item p {
                    color: var(--clr-text-muted);
                    line-height: 1.6;
                }

                .bento-icon {
                    font-size: 2.5rem;
                    margin-bottom: 2rem;
                    display: block;
                }

                /* --- Stats Section --- */
                .stats-bar {
                    display: flex;
                    justify-content: space-around;
                    padding: 5rem 2rem;
                    background: linear-gradient(to right, transparent, rgba(99, 102, 241, 0.05), transparent);
                    border-top: 1px solid var(--clr-border);
                    border-bottom: 1px solid var(--clr-border);
                    margin: 4rem 0;
                }

                .stat-item {
                    text-align: center;
                }

                .stat-number {
                    font-size: 3.5rem;
                    font-weight: 800;
                    display: block;
                    color: white;
                    font-family: var(--font-main);
                }

                .stat-label {
                    color: var(--clr-primary);
                    text-transform: uppercase;
                    font-size: 0.8rem;
                    font-weight: 700;
                    letter-spacing: 0.2em;
                }

                /* --- Footer --- */
                footer {
                    padding: 6rem 2rem 3rem;
                    background: #000;
                    border-top: 1px solid var(--clr-border);
                }

                .footer-content {
                    max-width: 1400px;
                    margin: 0 auto;
                    display: grid;
                    grid-template-columns: 2fr 1fr 1fr 1fr;
                    gap: 4rem;
                    margin-bottom: 4rem;
                }

                .footer-logo {
                    font-size: 1.5rem;
                    font-weight: 800;
                    color: #fff;
                    margin-bottom: 1.5rem;
                    display: block;
                }

                .footer-links h4 {
                    color: white;
                    margin-bottom: 1.5rem;
                    font-size: 1rem;
                }

                .footer-links ul {
                    list-style: none;
                }

                .footer-links li {
                    margin-bottom: 0.8rem;
                }

                .footer-links a {
                    color: var(--clr-text-muted);
                    text-decoration: none;
                    transition: color 0.3s;
                }

                .footer-links a:hover {
                    color: white;
                }

                .footer-bottom {
                    max-width: 1400px;
                    margin: 0 auto;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding-top: 3rem;
                    border-top: 1px solid rgba(255, 255, 255, 0.05);
                    color: var(--clr-text-muted);
                    font-size: 0.85rem;
                }

                /* --- Scroll Progress Bar --- */
                .progress-bar {
                    position: fixed;
                    top: 0;
                    left: 0;
                    height: 3px;
                    background: linear-gradient(to right, var(--clr-primary), var(--clr-secondary));
                    width: 0%;
                    z-index: 2000;
                }

                @media (max-width: 1024px) {
                    .bento-grid {
                        grid-template-columns: 1fr 1fr;
                    }

                    .bento-item.large {
                        grid-column: span 2;
                    }
                }

                @media (max-width: 768px) {
                    .nav-links {
                        display: none;
                    }

                    .bento-grid {
                        grid-template-columns: 1fr;
                    }

                    .bento-item.large {
                        grid-column: span 1;
                    }

                    .footer-content {
                        grid-template-columns: 1fr 1fr;
                    }
                }
            </style>
        </head>

        <body>

            <div class="progress-bar" id="progressBar"></div>

            <header id="mainHeader">
                <div class="nav-container">
                    <a href="#" class="nav-logo">✈ <span>SkyLounge</span></a>
                    <nav class="nav-links">
                        <a href="#features">Features</a>
                        <a href="#operational">Operational Hub</a>
                        <a href="#network">Network</a>
                        <a href="${pageContext.request.contextPath}/login" class="btn-login">Operations Login</a>
                    </nav>
                </div>
            </header>

            <section class="hero">
                <div class="hero-bg" id="heroBg"></div>
                <div class="hero-content">
                    <div class="hero-tag reveal">Next-Gen Aviation OS</div>
                    <h1 class="hero-title reveal">Elevate Your Lounge Intelligence.</h1>
                    <p class="hero-subtitle reveal">The most advanced operations platform for premium airport lounges.
                        Real-time telemetry, smart access control, and predictive capacity orchestration.</p>
                    <div class="cta-group reveal">
                        <a href="${pageContext.request.contextPath}/login" class="btn-primary">Launch Dashboard</a>
                        <a href="#features" class="btn-secondary">Explore Platform Explorer <span>→</span></a>
                    </div>
                </div>
            </section>

            <div class="marquee">
                <div class="marquee-content">
                    <span>O'HARE INTL</span> <span>HEATHROW TERMINAL 5</span> <span>SINGAPORE CHANGI</span> <span>TOKYO
                        NARITA</span> <span>DUBAI WORLD</span> <span>O'HARE INTL</span> <span>HEATHROW TERMINAL 5</span>
                    <span>SINGAPORE CHANGI</span> <span>TOKYO NARITA</span> <span>DUBAI WORLD</span>
                </div>
            </div>

            <section class="section" id="features">
                <div class="section-header reveal">
                    <h2>Infinite Scale for Modern Hubs.</h2>
                    <p style="color: var(--clr-text-muted); max-width: 600px; margin: 0 auto;">Designed for the velocity
                        of Tier-1 airports, SkyLounge handles millions of visits with zero latency.</p>
                </div>

                <div class="bento-grid">
                    <div class="bento-item large reveal">
                        <span class="bento-icon">📊</span>
                        <h3>Predictive Capacity Engine</h3>
                        <p>Advanced ML models forecast passenger flow 4 hours in advance, allowing you to optimize
                            staffing levels and inventory before the surge hits. See occupancy peaks and troughs with
                            surgical precision.</p>
                        <div style="margin-top: 2.5rem; height: 100px; display: flex; align-items: flex-end; gap: 8px;">
                            <div
                                style="flex:1; background: var(--clr-primary); height: 40%; border-radius: 4px; opacity: 0.3;">
                            </div>
                            <div
                                style="flex:1; background: var(--clr-primary); height: 60%; border-radius: 4px; opacity: 0.5;">
                            </div>
                            <div
                                style="flex:1; background: var(--clr-primary); height: 90%; border-radius: 4px; box-shadow: 0 0 15px var(--clr-primary);">
                            </div>
                            <div
                                style="flex:1; background: var(--clr-primary); height: 75%; border-radius: 4px; opacity: 0.8;">
                            </div>
                            <div
                                style="flex:1; background: var(--clr-primary); height: 50%; border-radius: 4px; opacity: 0.4;">
                            </div>
                        </div>
                    </div>

                    <div class="bento-item reveal">
                        <span class="bento-icon">🛂</span>
                        <h3>Universal Access</h3>
                        <p>Connect seamlessly with 50+ access providers including Amex, Priority Pass, and global
                            airline networks.</p>
                    </div>

                    <div class="bento-item reveal">
                        <span class="bento-icon">🛡</span>
                        <h3>Audit Trail 2.0</h3>
                        <p>Every decision, visit, and transaction is immutable and logged for total operational
                            transparency.</p>
                    </div>

                    <div class="bento-item reveal">
                        <span class="bento-icon">🔋</span>
                        <h3>Resilient Backend</h3>
                        <p>Engineered for 99.99% uptime. Even in network outages, your front desk stays operational.</p>
                    </div>

                    <div class="bento-item large reveal">
                        <span class="bento-icon">🌌</span>
                        <h3>Immersive Visual Experience</h3>
                        <p>Built for the modern workstation. Dark-mode aesthetics reduce eye strain for operators on
                            long international shifts, while high-contrast indicators ensure critical alerts never go
                            missed.</p>
                    </div>
                </div>
            </section>

            <div class="stats-bar">
                <div class="stat-item reveal">
                    <span class="stat-number">24/7</span>
                    <span class="stat-label">Live Ops</span>
                </div>
                <div class="stat-item reveal">
                    <span class="stat-number">1.2M+</span>
                    <span class="stat-label">Guests Monthly</span>
                </div>
                <div class="stat-item reveal">
                    <span class="stat-number">
                        < 2s</span>
                            <span class="stat-label">Access Verdict</span>
                </div>
                <div class="stat-item reveal">
                    <span class="stat-number">180+</span>
                    <span class="stat-label">Partner Airports</span>
                </div>
            </div>

            <section class="section" id="operational">
                <div class="section-header reveal">
                    <h2>The Operator's Advantage.</h2>
                    <p style="color: var(--clr-text-muted);">From the front desk to the executive office, everyone gets
                        the intelligence they need.</p>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 4rem; align-items: center;">
                    <div class="reveal">
                        <div
                            style="background: rgba(99,102,241,0.05); border: 1px solid var(--clr-border); border-radius: 12px; padding: 1.5rem; margin-bottom: 2rem;">
                            <h4 style="margin-bottom: 0.5rem; color: var(--clr-secondary);">⚡ Front-Desk Velocity</h4>
                            <p style="color: var(--clr-text-muted); font-size: 0.9rem;">Check-in passengers in under 15
                                seconds with automated passport scanning and entitlement lookups.</p>
                        </div>
                        <div
                            style="background: rgba(16,185,129,0.05); border: 1px solid var(--clr-border); border-radius: 12px; padding: 1.5rem; margin-bottom: 2rem;">
                            <h4 style="margin-bottom: 0.5rem; color: var(--clr-accent);">💰 Financial Transparency</h4>
                            <p style="color: var(--clr-text-muted); font-size: 0.9rem;">Automated settlement reports for
                                airlines and payment networks. No more manual reconciliation.</p>
                        </div>
                        <div
                            style="background: rgba(244,63,94,0.05); border: 1px solid var(--clr-border); border-radius: 12px; padding: 1.5rem;">
                            <h4 style="margin-bottom: 0.5rem; color: #f43f5e;">⚠️ Incident Management</h4>
                            <p style="color: var(--clr-text-muted); font-size: 0.9rem;">One-touch management of capacity
                                protocols and guest disputes with logged resolutions.</p>
                        </div>
                    </div>
                    <div class="reveal" style="position: relative;">
                        <div
                            style="background: var(--clr-card); border: 1px solid var(--clr-primary); border-radius: 20px; padding: 2rem; box-shadow: 0 30px 60px rgba(0,0,0,0.5);">
                            <div style="display:flex; justify-content:space-between; margin-bottom: 2rem;">
                                <span
                                    style="font-weight: 800; font-family: var(--font-main);">LoungeTelemetry_v4.2</span>
                                <span style="color: var(--clr-accent);">● LIVE</span>
                            </div>
                            <div style="display:grid; gap: 1rem;">
                                <div
                                    style="height: 12px; background: rgba(255,255,255,0.03); border-radius: 6px; position:relative; overflow:hidden;">
                                    <div
                                        style="position:absolute; top:0; left:0; height:100%; width: 72%; background: var(--clr-primary);">
                                    </div>
                                </div>
                                <div
                                    style="height: 12px; background: rgba(255,255,255,0.03); border-radius: 6px; position:relative; overflow:hidden;">
                                    <div
                                        style="position:absolute; top:0; left:0; height:100%; width: 45%; background: var(--clr-secondary);">
                                    </div>
                                </div>
                                <div
                                    style="height: 12px; background: rgba(255,255,255,0.03); border-radius: 6px; position:relative; overflow:hidden;">
                                    <div
                                        style="position:absolute; top:0; left:0; height:100%; width: 88%; background: var(--clr-accent);">
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div
                            style="position: absolute; top: -20px; right: -20px; background: var(--clr-primary); width: 80px; height: 80px; border-radius: 50%; filter: blur(40px); opacity: 0.3; z-index: -1;">
                        </div>
                    </div>
                </div>
            </section>

            <footer>
                <div class="footer-content">
                    <div>
                        <a href="#" class="footer-logo">✈ SkyLounge</a>
                        <p style="color: var(--clr-text-muted); font-size: 0.9rem; margin-bottom: 2rem;">Revolutionizing
                            airport hospitality through superior engineering and design-first operations systems.</p>
                        <div style="display: flex; gap: 1rem;">
                            <!-- Social icons placeholder -->
                            <div
                                style="width: 32px; height: 32px; background: rgba(255,255,255,0.05); border-radius: 8px;">
                            </div>
                            <div
                                style="width: 32px; height: 32px; background: rgba(255,255,255,0.05); border-radius: 8px;">
                            </div>
                            <div
                                style="width: 32px; height: 32px; background: rgba(255,255,255,0.05); border-radius: 8px;">
                            </div>
                        </div>
                    </div>
                    <div class="footer-links">
                        <h4>Platform</h4>
                        <ul>
                            <li><a href="#">LoungeFlow</a></li>
                            <li><a href="#">AccessEngine</a></li>
                            <li><a href="#">AnalyticsHub</a></li>
                            <li><a href="#">Security</a></li>
                        </ul>
                    </div>
                    <div class="footer-links">
                        <h4>Network</h4>
                        <ul>
                            <li><a href="#">Partners</a></li>
                            <li><a href="#">Integrations</a></li>
                            <li><a href="#">API Docs</a></li>
                        </ul>
                    </div>
                    <div class="footer-links">
                        <h4>Company</h4>
                        <ul>
                            <li><a href="#">About Us</a></li>
                            <li><a href="#">Case Studies</a></li>
                            <li><a href="#">Press</a></li>
                            <li><a href="#">Contact</a></li>
                        </ul>
                    </div>
                </div>
                <div class="footer-bottom">
                    <p>&copy;
                        <jsp:useBean id="now" class="java.util.Date" />
                        <fmt:formatDate value="${now}" pattern="yyyy" /> SkyLounge Management Systems. All rights
                        reserved.
                    </p>
                    <p>Built for the future of aviation.</p>
                </div>
            </footer>

            <script>
                // --- Header Scroll Effect ---
                const header = document.getElementById('mainHeader');
                window.addEventListener('scroll', () => {
                    if (window.scrollY > 50) {
                        header.classList.add('scrolled');
                    } else {
                        header.classList.remove('scrolled');
                    }

                    // Progress Bar
                    const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
                    const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
                    const scrolled = (winScroll / height) * 100;
                    document.getElementById("progressBar").style.width = scrolled + "%";
                });

                // --- Reveal on Scroll ---
                const observerOptions = {
                    threshold: 0.1
                };

                const observer = new IntersectionObserver((entries) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            entry.target.classList.add('active');
                        }
                    });
                }, observerOptions);

                document.querySelectorAll('.reveal').forEach(el => observer.observe(el));

                // --- Parallax Hero ---
                const heroBg = document.getElementById('heroBg');
                window.addEventListener('mousemove', (e) => {
                    const x = e.clientX / window.innerWidth;
                    const y = e.clientY / window.innerHeight;
                    heroBg.style.transform = `scale(1.1) translate(${x * 10}px, ${y * 10}px)`;
                });
            </script>
        </body>

        </html>