<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Dashboard — Airport Lounge Manager</title>
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                        rel="stylesheet">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                    <style>
                        /* Dashboard Layout & Refinement */
                        .dash-container {
                            display: flex;
                            flex-direction: column;
                            gap: 2.5rem;
                            animation: fadeIn 0.5s ease-out;
                        }

                        @keyframes fadeIn {
                            from {
                                opacity: 0;
                                transform: translateY(10px);
                            }

                            to {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }

                        .section-header {
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            margin-bottom: 1.5rem;
                        }

                        .section-header h2 {
                            font-size: 1.25rem;
                            font-weight: 800;
                            letter-spacing: -0.02em;
                            color: var(--clr-text);
                        }

                        /* KPI Grid */
                        .kpi-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
                            gap: 1.5rem;
                            margin-bottom: 1rem;
                        }

                        .kpi-card {
                            background: var(--clr-card);
                            border: 1px solid var(--clr-border);
                            border-radius: 20px;
                            padding: 1.75rem;
                            position: relative;
                            overflow: hidden;
                            transition: all 0.3s ease;
                        }

                        .kpi-card:hover {
                            transform: translateY(-5px);
                            border-color: var(--clr-primary-lit);
                            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
                        }

                        .kpi-icon {
                            position: absolute;
                            top: 1.25rem;
                            right: 1.25rem;
                            font-size: 1.5rem;
                            opacity: 0.2;
                        }

                        .kpi-value {
                            font-size: 2rem;
                            font-weight: 800;
                            margin-top: 0.5rem;
                            background: linear-gradient(to bottom right, #fff, #94a3b8);
                            -webkit-background-clip: text;
                            -webkit-text-fill-color: transparent;
                        }

                        .kpi-label {
                            font-size: 0.75rem;
                            font-weight: 700;
                            color: var(--clr-muted);
                            text-transform: uppercase;
                            letter-spacing: 0.05em;
                        }

                        /* Lounge Catalog */
                        .catalog-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
                            gap: 2rem;
                        }

                        .lounge-card {
                            background: var(--clr-card);
                            border: 1px solid var(--clr-border);
                            border-radius: 24px;
                            overflow: hidden;
                            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.1);
                            cursor: pointer;
                            position: relative;
                        }

                        .lounge-card:hover {
                            transform: translateY(-10px) scale(1.01);
                            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
                            border-color: var(--clr-primary);
                        }

                        .lounge-card-img {
                            height: 180px;
                            background: linear-gradient(45deg, #1e293b, #334155);
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 4rem;
                            position: relative;
                        }

                        .lounge-card-badge {
                            position: absolute;
                            top: 1.25rem;
                            right: 1.25rem;
                            padding: 0.4rem 0.8rem;
                            background: rgba(15, 23, 42, 0.8);
                            backdrop-filter: blur(8px);
                            border: 1px solid rgba(255, 255, 255, 0.1);
                            border-radius: 99px;
                            font-size: 0.7rem;
                            font-weight: 700;
                            color: #fff;
                        }

                        .lounge-card-body {
                            padding: 1.75rem;
                        }

                        .lounge-card-title {
                            font-size: 1.25rem;
                            font-weight: 800;
                            margin-bottom: 0.5rem;
                        }

                        .lounge-card-meta {
                            display: flex;
                            gap: 1rem;
                            font-size: 0.8rem;
                            color: var(--clr-muted);
                        }

                        /* Operation Widgets */
                        .dash-grid-2 {
                            display: grid;
                            grid-template-columns: 1fr 360px;
                            gap: 2rem;
                        }

                        @media (max-width: 1200px) {
                            .dash-grid-2 {
                                grid-template-columns: 1fr;
                            }
                        }

                        .scan-module {
                            background: linear-gradient(135deg, rgba(99, 102, 241, 0.05), rgba(168, 85, 247, 0.05));
                            border: 2px dashed rgba(99, 102, 241, 0.3);
                            border-radius: 24px;
                            padding: 3rem 2rem;
                            text-align: center;
                            margin-bottom: 2rem;
                            transition: all 0.3s;
                        }

                        .scan-module:focus-within {
                            border-color: var(--clr-primary);
                            background: rgba(99, 102, 241, 0.08);
                            box-shadow: 0 0 40px rgba(99, 102, 241, 0.1);
                        }

                        .scan-input {
                            background: #0f172a;
                            border: 1px solid var(--clr-border);
                            border-radius: 16px;
                            padding: 1.25rem;
                            width: 100%;
                            max-width: 480px;
                            color: #fff;
                            font-size: 1.25rem;
                            text-align: center;
                            font-family: 'JetBrains Mono', monospace;
                            transition: all 0.3s;
                        }

                        .scan-input:focus {
                            outline: none;
                            border-color: var(--clr-primary);
                            box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.2);
                        }

                        /* Gauge */
                        .gauge-wrapper {
                            padding: 2.5rem 1.5rem;
                            text-align: center;
                        }

                        .gauge-svg {
                            width: 220px;
                            height: 110px;
                            margin: 0 auto;
                        }

                        .gauge-bg {
                            fill: none;
                            stroke: rgba(255, 255, 255, 0.03);
                            stroke-width: 10;
                            stroke-linecap: round;
                        }

                        .gauge-progress {
                            fill: none;
                            stroke: var(--clr-primary);
                            stroke-width: 10;
                            stroke-linecap: round;
                            stroke-dasharray: 251.2;
                            stroke-dashoffset: 251.2;
                            transition: stroke-dashoffset 2s cubic-bezier(0.4, 0, 0.2, 1);
                        }

                        .gauge-val-big {
                            font-size: 2.5rem;
                            font-weight: 900;
                            margin-top: -30px;
                            display: block;
                        }

                        /* Activity List */
                        .activity-list {
                            list-style: none;
                            padding: 0;
                            margin: 0;
                        }

                        .activity-item {
                            display: flex;
                            align-items: center;
                            gap: 1.25rem;
                            padding: 1.25rem 0;
                            border-bottom: 1px solid rgba(255, 255, 255, 0.03);
                            transition: all 0.2s;
                        }

                        .activity-item:last-child {
                            border-bottom: none;
                        }

                        .activity-item:hover {
                            transform: translateX(5px);
                        }

                        .icon-circle {
                            width: 42px;
                            height: 42px;
                            border-radius: 12px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 1.25rem;
                        }

                        .icon-circle.emerald {
                            background: rgba(16, 185, 129, 0.1);
                            color: #10b981;
                        }

                        .icon-circle.amber {
                            background: rgba(245, 158, 11, 0.1);
                            color: #f59e0b;
                        }

                        .icon-circle.rose {
                            background: rgba(244, 63, 94, 0.1);
                            color: #f43f5e;
                        }

                        .admin-stat-row {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            padding: 1rem;
                            background: rgba(255, 255, 255, 0.02);
                            border-radius: 12px;
                            margin-bottom: 0.75rem;
                            border: 1px solid transparent;
                            transition: all 0.2s;
                        }

                        .admin-stat-row:hover {
                            background: rgba(255, 255, 255, 0.04);
                            border-color: rgba(255, 255, 255, 0.1);
                        }

                        /* Chart placeholder */
                        .chart-container {
                            height: 300px;
                            display: flex;
                            align-items: flex-end;
                            gap: 0.75rem;
                            padding: 1rem 0;
                        }

                        .forecast-bar {
                            flex: 1;
                            background: var(--clr-primary);
                            border-radius: 8px 8px 2px 2px;
                            opacity: 0.6;
                            transition: all 0.3s;
                            position: relative;
                        }

                        .forecast-bar:hover {
                            opacity: 1;
                            filter: drop-shadow(0 0 10px var(--clr-primary));
                        }

                        /* ═══════════════════════════════════════════
                           PASSENGER DASHBOARD — Enhanced Components
                           ═══════════════════════════════════════════ */

                        /* Passenger Hero Stats Strip */
                        .pax-stats-strip {
                            display: grid;
                            grid-template-columns: repeat(4, 1fr);
                            gap: 1rem;
                            margin-bottom: 2rem;
                        }

                        @media (max-width: 900px) {
                            .pax-stats-strip {
                                grid-template-columns: repeat(2, 1fr);
                            }
                        }

                        @media (max-width: 500px) {
                            .pax-stats-strip {
                                grid-template-columns: 1fr;
                            }
                        }

                        .pax-stat {
                            background: rgba(15, 23, 42, 0.7);
                            border: 1px solid rgba(99, 130, 220, 0.2);
                            border-radius: 18px;
                            padding: 1.25rem 1.5rem;
                            display: flex;
                            align-items: center;
                            gap: 1rem;
                            backdrop-filter: blur(12px);
                            -webkit-backdrop-filter: blur(12px);
                            transition: all 0.3s ease;
                            position: relative;
                            overflow: hidden;
                        }

                        .pax-stat::before {
                            content: '';
                            position: absolute;
                            inset: 0;
                            background: radial-gradient(circle at top left, rgba(99, 102, 241, 0.12), transparent 60%);
                            pointer-events: none;
                        }

                        .pax-stat:hover {
                            transform: translateY(-3px);
                            border-color: var(--clr-border-glow);
                            box-shadow: 0 8px 32px rgba(99, 102, 241, 0.2);
                        }

                        .pax-stat-icon {
                            width: 46px;
                            height: 46px;
                            border-radius: 14px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 1.3rem;
                            flex-shrink: 0;
                        }

                        .pax-stat-icon.indigo {
                            background: rgba(99, 102, 241, 0.18);
                        }

                        .pax-stat-icon.emerald {
                            background: rgba(52, 211, 153, 0.15);
                        }

                        .pax-stat-icon.amber {
                            background: rgba(251, 191, 36, 0.15);
                        }

                        .pax-stat-icon.cyan {
                            background: rgba(34, 211, 238, 0.15);
                        }

                        .pax-stat-val {
                            font-size: 1.7rem;
                            font-weight: 900;
                            letter-spacing: -0.03em;
                            line-height: 1;
                        }

                        .pax-stat-label {
                            font-size: 0.7rem;
                            font-weight: 600;
                            color: var(--clr-muted);
                            text-transform: uppercase;
                            letter-spacing: 0.05em;
                            margin-top: 0.2rem;
                        }

                        /* Featured lounge hero */
                        .featured-lounge-hero {
                            position: relative;
                            border-radius: 28px;
                            overflow: hidden;
                            margin-bottom: 2.5rem;
                            min-height: 280px;
                            display: flex;
                            align-items: flex-end;
                            cursor: pointer;
                            transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.1);
                        }

                        .featured-lounge-hero:hover {
                            transform: scale(1.01);
                        }

                        .featured-hero-bg {
                            position: absolute;
                            inset: 0;
                            background: linear-gradient(135deg, #1a0a3d 0%, #0d1b4a 40%, #0a2e1e 100%);
                            z-index: 0;
                        }

                        .featured-hero-bg-pattern {
                            position: absolute;
                            inset: 0;
                            background-image:
                                radial-gradient(circle at 20% 50%, rgba(99, 102, 241, 0.35) 0%, transparent 50%),
                                radial-gradient(circle at 80% 20%, rgba(34, 211, 238, 0.25) 0%, transparent 45%),
                                radial-gradient(circle at 60% 80%, rgba(168, 85, 247, 0.2) 0%, transparent 40%);
                            z-index: 1;
                        }

                        .featured-hero-glow {
                            position: absolute;
                            top: -60px;
                            right: -60px;
                            width: 340px;
                            height: 340px;
                            background: radial-gradient(circle, rgba(99, 102, 241, 0.3) 0%, transparent 70%);
                            z-index: 1;
                            animation: heroGlow 4s ease-in-out infinite alternate;
                        }

                        @keyframes heroGlow {
                            from {
                                opacity: 0.6;
                                transform: scale(1);
                            }

                            to {
                                opacity: 1;
                                transform: scale(1.1);
                            }
                        }

                        .featured-hero-emoji {
                            position: absolute;
                            right: 2rem;
                            top: 50%;
                            transform: translateY(-50%);
                            font-size: 9rem;
                            opacity: 0.18;
                            z-index: 2;
                            filter: blur(1px);
                        }

                        .featured-hero-content {
                            position: relative;
                            z-index: 3;
                            padding: 2.5rem 2.5rem 2rem;
                            width: 100%;
                            background: linear-gradient(to top, rgba(8, 13, 26, 0.9) 0%, transparent 100%);
                        }

                        .featured-hero-tag {
                            display: inline-flex;
                            align-items: center;
                            gap: 0.4rem;
                            background: rgba(99, 102, 241, 0.25);
                            border: 1px solid rgba(129, 140, 248, 0.4);
                            backdrop-filter: blur(8px);
                            border-radius: 99px;
                            padding: 0.3rem 0.9rem;
                            font-size: 0.7rem;
                            font-weight: 700;
                            color: var(--clr-primary-lit);
                            text-transform: uppercase;
                            letter-spacing: 0.08em;
                            margin-bottom: 0.9rem;
                        }

                        .featured-hero-title {
                            font-size: 2rem;
                            font-weight: 900;
                            letter-spacing: -0.04em;
                            margin-bottom: 0.5rem;
                            line-height: 1.1;
                        }

                        .featured-hero-meta {
                            display: flex;
                            align-items: center;
                            gap: 1.5rem;
                            font-size: 0.85rem;
                            color: rgba(226, 232, 248, 0.75);
                            margin-bottom: 1.5rem;
                            flex-wrap: wrap;
                        }

                        .featured-hero-meta span {
                            display: flex;
                            align-items: center;
                            gap: 0.4rem;
                        }

                        .featured-hero-actions {
                            display: flex;
                            gap: 0.75rem;
                            flex-wrap: wrap;
                        }

                        /* Book Now CTA button (glowing) */
                        .btn-book-now {
                            background: linear-gradient(135deg, #6366f1, #818cf8);
                            color: #fff;
                            border: none;
                            padding: 0.75rem 1.75rem;
                            border-radius: 12px;
                            font-size: 0.9rem;
                            font-weight: 700;
                            cursor: pointer;
                            transition: all 0.3s ease;
                            box-shadow: 0 0 24px rgba(99, 102, 241, 0.5);
                            letter-spacing: -0.01em;
                        }

                        .btn-book-now:hover {
                            transform: translateY(-2px) scale(1.03);
                            box-shadow: 0 0 40px rgba(99, 102, 241, 0.7);
                        }

                        .btn-book-ghost {
                            background: rgba(255, 255, 255, 0.08);
                            color: #fff;
                            border: 1px solid rgba(255, 255, 255, 0.2);
                            padding: 0.75rem 1.5rem;
                            border-radius: 12px;
                            font-size: 0.9rem;
                            font-weight: 600;
                            cursor: pointer;
                            backdrop-filter: blur(8px);
                            transition: all 0.2s;
                        }

                        .btn-book-ghost:hover {
                            background: rgba(255, 255, 255, 0.14);
                            border-color: rgba(255, 255, 255, 0.4);
                        }

                        /* Premium Lounge Cards */
                        .premium-catalog-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
                            gap: 1.75rem;
                            margin-bottom: 2.5rem;
                        }

                        @media (max-width: 800px) {
                            .premium-catalog-grid {
                                grid-template-columns: 1fr;
                            }
                        }

                        .premium-lounge-card {
                            background: rgba(21, 32, 64, 0.6);
                            border: 1px solid rgba(99, 130, 220, 0.2);
                            border-radius: 24px;
                            overflow: hidden;
                            backdrop-filter: blur(16px);
                            -webkit-backdrop-filter: blur(16px);
                            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.1);
                            cursor: pointer;
                            position: relative;
                        }

                        .premium-lounge-card:hover {
                            transform: translateY(-10px);
                            border-color: rgba(129, 140, 248, 0.5);
                            box-shadow: 0 24px 48px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(129, 140, 248, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
                        }

                        .plc-visual {
                            height: 160px;
                            position: relative;
                            overflow: hidden;
                        }

                        .plc-visual-bg {
                            position: absolute;
                            inset: 0;
                        }

                        .plc-visual-bg.v1 {
                            background: linear-gradient(135deg, #1a0a3d, #2d1b69, #0d1b4a);
                        }

                        .plc-visual-bg.v2 {
                            background: linear-gradient(135deg, #0d2a1f, #0a3d2e, #0a2547);
                        }

                        .plc-visual-bg.v3 {
                            background: linear-gradient(135deg, #2d1a00, #3d2a0a, #1a0d3d);
                        }

                        .plc-visual-bg.v4 {
                            background: linear-gradient(135deg, #1a0d2d, #0d1a3d, #0a2a2d);
                        }

                        .plc-visual-bg.v5 {
                            background: linear-gradient(135deg, #3d0a1a, #2d0a2d, #0d0a3d);
                        }

                        .plc-visual-bg.v6 {
                            background: linear-gradient(135deg, #0a1a3d, #1a0a2d, #0d2a1f);
                        }

                        .plc-glow-orb {
                            position: absolute;
                            width: 150px;
                            height: 150px;
                            border-radius: 50%;
                            top: -40px;
                            right: -30px;
                            opacity: 0.5;
                            filter: blur(40px);
                        }

                        .plc-emoji-bg {
                            position: absolute;
                            right: 1.5rem;
                            bottom: 0.5rem;
                            font-size: 4.5rem;
                            opacity: 0.15;
                            line-height: 1;
                        }

                        .plc-tier-badge {
                            position: absolute;
                            top: 1rem;
                            left: 1rem;
                            padding: 0.3rem 0.85rem;
                            border-radius: 99px;
                            font-size: 0.65rem;
                            font-weight: 800;
                            letter-spacing: 0.1em;
                            text-transform: uppercase;
                            backdrop-filter: blur(8px);
                        }

                        .plc-tier-badge.platinum {
                            background: rgba(226, 232, 248, 0.15);
                            color: #e2e8f8;
                            border: 1px solid rgba(226, 232, 248, 0.3);
                        }

                        .plc-tier-badge.gold {
                            background: rgba(251, 191, 36, 0.15);
                            color: #fbbf24;
                            border: 1px solid rgba(251, 191, 36, 0.35);
                        }

                        .plc-tier-badge.business {
                            background: rgba(99, 102, 241, 0.2);
                            color: #818cf8;
                            border: 1px solid rgba(99, 102, 241, 0.4);
                        }

                        .plc-tier-badge.standard {
                            background: rgba(52, 211, 153, 0.12);
                            color: #34d399;
                            border: 1px solid rgba(52, 211, 153, 0.3);
                        }

                        .plc-status-dot {
                            position: absolute;
                            top: 1rem;
                            right: 1rem;
                            display: flex;
                            align-items: center;
                            gap: 0.4rem;
                            background: rgba(8, 13, 26, 0.6);
                            backdrop-filter: blur(8px);
                            border-radius: 99px;
                            padding: 0.3rem 0.7rem;
                            font-size: 0.65rem;
                            font-weight: 700;
                            color: #fff;
                            border: 1px solid rgba(255, 255, 255, 0.1);
                        }

                        .plc-dot {
                            width: 6px;
                            height: 6px;
                            border-radius: 50%;
                            flex-shrink: 0;
                        }

                        .plc-dot.open {
                            background: #34d399;
                            box-shadow: 0 0 6px #34d399;
                            animation: pulse-ring 2s infinite;
                        }

                        .plc-dot.closed {
                            background: #fb7185;
                        }

                        .plc-dot.limited {
                            background: #fbbf24;
                            box-shadow: 0 0 6px #fbbf24;
                        }

                        .plc-body {
                            padding: 1.5rem;
                        }

                        .plc-name {
                            font-size: 1.15rem;
                            font-weight: 800;
                            letter-spacing: -0.02em;
                            margin-bottom: 0.35rem;
                            color: var(--clr-text);
                        }

                        .plc-location {
                            font-size: 0.78rem;
                            color: var(--clr-muted);
                            display: flex;
                            align-items: center;
                            gap: 0.35rem;
                            margin-bottom: 1rem;
                        }

                        .plc-amenities {
                            display: flex;
                            flex-wrap: wrap;
                            gap: 0.4rem;
                            margin-bottom: 1.25rem;
                        }

                        .plc-amenity-chip {
                            background: rgba(99, 102, 241, 0.1);
                            border: 1px solid rgba(99, 102, 241, 0.2);
                            border-radius: 6px;
                            padding: 0.2rem 0.6rem;
                            font-size: 0.68rem;
                            font-weight: 600;
                            color: var(--clr-primary-lit);
                            display: flex;
                            align-items: center;
                            gap: 0.3rem;
                        }

                        .plc-occupancy-row {
                            margin-bottom: 1.25rem;
                        }

                        .plc-occupancy-label {
                            display: flex;
                            justify-content: space-between;
                            font-size: 0.72rem;
                            color: var(--clr-muted);
                            margin-bottom: 0.4rem;
                        }

                        .plc-occ-pct {
                            font-weight: 700;
                        }

                        .plc-occ-bar {
                            height: 5px;
                            background: rgba(255, 255, 255, 0.06);
                            border-radius: 999px;
                            overflow: hidden;
                        }

                        .plc-occ-fill {
                            height: 100%;
                            border-radius: 999px;
                            transition: width 1.2s cubic-bezier(0.4, 0, 0.2, 1);
                        }

                        .plc-occ-fill.low {
                            background: linear-gradient(to right, #34d399, #22d3ee);
                        }

                        .plc-occ-fill.medium {
                            background: linear-gradient(to right, #fbbf24, #f59e0b);
                        }

                        .plc-occ-fill.high {
                            background: linear-gradient(to right, #fb7185, #f43f5e);
                        }

                        .plc-footer {
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            padding-top: 1rem;
                            border-top: 1px solid rgba(255, 255, 255, 0.05);
                        }

                        .plc-info-chip {
                            display: flex;
                            align-items: center;
                            gap: 0.4rem;
                            font-size: 0.75rem;
                            color: var(--clr-muted);
                        }

                        .plc-book-btn {
                            background: rgba(99, 102, 241, 0.15);
                            border: 1px solid rgba(99, 102, 241, 0.35);
                            color: var(--clr-primary-lit);
                            padding: 0.5rem 1.1rem;
                            border-radius: 10px;
                            font-size: 0.8rem;
                            font-weight: 700;
                            cursor: pointer;
                            transition: all 0.2s;
                        }

                        .plc-book-btn:hover {
                            background: rgba(99, 102, 241, 0.3);
                            border-color: var(--clr-primary-lit);
                            transform: translateY(-1px);
                            box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
                        }

                        /* Booking modal overlay */
                        .booking-overlay {
                            position: fixed;
                            inset: 0;
                            background: rgba(4, 8, 20, 0.85);
                            backdrop-filter: blur(12px);
                            -webkit-backdrop-filter: blur(12px);
                            z-index: 1000;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 1rem;
                            opacity: 0;
                            pointer-events: none;
                            transition: opacity 0.3s ease;
                        }

                        .booking-overlay.active {
                            opacity: 1;
                            pointer-events: all;
                        }

                        .booking-modal {
                            background: linear-gradient(145deg, #111c35, #0e1628);
                            border: 1px solid rgba(99, 130, 220, 0.3);
                            border-radius: 28px;
                            width: 100%;
                            max-width: 520px;
                            padding: 2.5rem;
                            position: relative;
                            box-shadow: 0 40px 80px rgba(0, 0, 0, 0.6);
                            transform: translateY(20px) scale(0.97);
                            transition: all 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.1);
                        }

                        .booking-overlay.active .booking-modal {
                            transform: translateY(0) scale(1);
                        }

                        .booking-modal-close {
                            position: absolute;
                            top: 1.25rem;
                            right: 1.25rem;
                            background: rgba(255, 255, 255, 0.06);
                            border: 1px solid rgba(255, 255, 255, 0.1);
                            color: var(--clr-muted);
                            width: 34px;
                            height: 34px;
                            border-radius: 50%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            cursor: pointer;
                            font-size: 1.1rem;
                            transition: all 0.2s;
                        }

                        .booking-modal-close:hover {
                            background: rgba(251, 113, 133, 0.15);
                            color: var(--clr-rose);
                        }

                        .booking-modal-lounge-header {
                            display: flex;
                            align-items: center;
                            gap: 1rem;
                            margin-bottom: 1.75rem;
                            padding-bottom: 1.25rem;
                            border-bottom: 1px solid rgba(99, 130, 220, 0.15);
                        }

                        .booking-modal-lounge-icon {
                            width: 52px;
                            height: 52px;
                            border-radius: 16px;
                            background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(34, 211, 238, 0.2));
                            border: 1px solid rgba(99, 130, 220, 0.3);
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 1.6rem;
                            flex-shrink: 0;
                        }

                        .booking-form-row {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 1rem;
                            margin-bottom: 1rem;
                        }

                        .booking-form-group {
                            margin-bottom: 1rem;
                        }

                        .booking-form-label {
                            font-size: 0.72rem;
                            font-weight: 700;
                            color: var(--clr-muted);
                            text-transform: uppercase;
                            letter-spacing: 0.05em;
                            display: block;
                            margin-bottom: 0.5rem;
                        }

                        .booking-form-input {
                            width: 100%;
                            background: rgba(8, 13, 26, 0.8);
                            border: 1px solid rgba(99, 130, 220, 0.25);
                            border-radius: 12px;
                            padding: 0.7rem 1rem;
                            color: var(--clr-text);
                            font-size: 0.875rem;
                            font-family: inherit;
                            outline: none;
                            transition: border-color 0.2s, box-shadow 0.2s;
                        }

                        .booking-form-input:focus {
                            border-color: var(--clr-primary);
                            box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
                        }

                        .booking-submit {
                            width: 100%;
                            background: linear-gradient(135deg, #6366f1, #818cf8);
                            border: none;
                            border-radius: 14px;
                            padding: 0.9rem;
                            color: #fff;
                            font-size: 0.95rem;
                            font-weight: 700;
                            cursor: pointer;
                            margin-top: 0.5rem;
                            transition: all 0.3s;
                            box-shadow: 0 0 24px rgba(99, 102, 241, 0.4);
                            letter-spacing: -0.01em;
                        }

                        .booking-submit:hover {
                            box-shadow: 0 0 40px rgba(99, 102, 241, 0.65);
                            transform: translateY(-2px);
                        }

                        /* Trip Timeline */
                        .trip-timeline {
                            list-style: none;
                            padding: 0;
                            margin: 0;
                        }

                        .trip-item {
                            display: grid;
                            grid-template-columns: 44px 1fr auto;
                            gap: 1rem;
                            padding: 1.25rem 0;
                            border-bottom: 1px solid rgba(255, 255, 255, 0.04);
                            align-items: start;
                            transition: all 0.2s;
                        }

                        .trip-item:last-child {
                            border-bottom: none;
                        }

                        .trip-item:hover {
                            transform: translateX(4px);
                        }

                        .trip-icon {
                            width: 44px;
                            height: 44px;
                            border-radius: 14px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 1.2rem;
                            flex-shrink: 0;
                        }

                        .trip-icon.approved {
                            background: rgba(52, 211, 153, 0.12);
                            border: 1px solid rgba(52, 211, 153, 0.2);
                        }

                        .trip-icon.pending {
                            background: rgba(251, 191, 36, 0.12);
                            border: 1px solid rgba(251, 191, 36, 0.2);
                        }

                        .trip-icon.denied {
                            background: rgba(251, 113, 133, 0.1);
                            border: 1px solid rgba(251, 113, 133, 0.2);
                        }

                        .trip-icon.checked {
                            background: rgba(34, 211, 238, 0.1);
                            border: 1px solid rgba(34, 211, 238, 0.2);
                        }

                        .trip-name {
                            font-weight: 700;
                            font-size: 0.95rem;
                            margin-bottom: 0.3rem;
                        }

                        .trip-meta {
                            font-size: 0.78rem;
                            color: var(--clr-muted);
                            line-height: 1.5;
                        }

                        .trip-status-pill {
                            padding: 0.3rem 0.8rem;
                            border-radius: 99px;
                            font-size: 0.67rem;
                            font-weight: 800;
                            letter-spacing: 0.06em;
                            text-transform: uppercase;
                            white-space: nowrap;
                            margin-top: 0.2rem;
                        }

                        .trip-status-pill.approved {
                            background: rgba(52, 211, 153, 0.12);
                            color: #34d399;
                            border: 1px solid rgba(52, 211, 153, 0.25);
                        }

                        .trip-status-pill.pending {
                            background: rgba(251, 191, 36, 0.12);
                            color: #fbbf24;
                            border: 1px solid rgba(251, 191, 36, 0.25);
                        }

                        .trip-status-pill.denied {
                            background: rgba(251, 113, 133, 0.1);
                            color: #fb7185;
                            border: 1px solid rgba(251, 113, 133, 0.2);
                        }

                        /* Passenger layout: catalog + sidebar */
                        .pax-main-grid {
                            display: grid;
                            grid-template-columns: 1fr 340px;
                            gap: 2rem;
                            margin-bottom: 2rem;
                        }

                        @media (max-width: 1100px) {
                            .pax-main-grid {
                                grid-template-columns: 1fr;
                            }
                        }

                        /* Membership card widget */
                        .membership-card {
                            background: linear-gradient(135deg, #1e1040, #0d1b3d, #0a2e25);
                            border: 1px solid rgba(129, 140, 248, 0.3);
                            border-radius: 24px;
                            padding: 2rem;
                            position: relative;
                            overflow: hidden;
                            margin-bottom: 1.5rem;
                        }

                        .membership-card::before {
                            content: '';
                            position: absolute;
                            top: -60px;
                            right: -60px;
                            width: 200px;
                            height: 200px;
                            background: radial-gradient(circle, rgba(99, 102, 241, 0.25) 0%, transparent 70%);
                            pointer-events: none;
                        }

                        .membership-chip {
                            display: inline-flex;
                            align-items: center;
                            gap: 0.4rem;
                            background: rgba(251, 191, 36, 0.15);
                            border: 1px solid rgba(251, 191, 36, 0.35);
                            color: #fbbf24;
                            padding: 0.3rem 0.85rem;
                            border-radius: 99px;
                            font-size: 0.68rem;
                            font-weight: 800;
                            text-transform: uppercase;
                            letter-spacing: 0.08em;
                            margin-bottom: 1.25rem;
                        }

                        .membership-name {
                            font-size: 1.3rem;
                            font-weight: 900;
                            letter-spacing: -0.03em;
                            margin-bottom: 0.3rem;
                        }

                        .membership-tagline {
                            font-size: 0.8rem;
                            color: var(--clr-muted);
                            margin-bottom: 1.5rem;
                        }

                        .membership-stats {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 1rem;
                        }

                        .membership-stat {
                            background: rgba(255, 255, 255, 0.04);
                            border: 1px solid rgba(255, 255, 255, 0.06);
                            border-radius: 14px;
                            padding: 1rem;
                        }

                        .membership-stat-val {
                            font-size: 1.5rem;
                            font-weight: 900;
                            letter-spacing: -0.03em;
                        }

                        .membership-stat-label {
                            font-size: 0.68rem;
                            color: var(--clr-muted);
                            text-transform: uppercase;
                            letter-spacing: 0.05em;
                            margin-top: 0.2rem;
                        }

                        /* Quick actions panel */
                        .quick-actions-panel {
                            background: rgba(15, 23, 42, 0.7);
                            border: 1px solid rgba(99, 130, 220, 0.2);
                            border-radius: 20px;
                            padding: 1.5rem;
                            margin-bottom: 1.5rem;
                            backdrop-filter: blur(12px);
                        }

                        .quick-actions-title {
                            font-size: 0.75rem;
                            font-weight: 700;
                            color: var(--clr-muted);
                            text-transform: uppercase;
                            letter-spacing: 0.08em;
                            margin-bottom: 1rem;
                        }

                        .quick-action-item {
                            display: flex;
                            align-items: center;
                            gap: 0.9rem;
                            padding: 0.85rem 0.75rem;
                            border-radius: 12px;
                            cursor: pointer;
                            transition: all 0.2s;
                            text-decoration: none;
                            color: var(--clr-text);
                            margin-bottom: 0.35rem;
                        }

                        .quick-action-item:hover {
                            background: rgba(99, 102, 241, 0.1);
                            transform: translateX(4px);
                        }

                        .quick-action-icon {
                            width: 36px;
                            height: 36px;
                            border-radius: 10px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 1rem;
                            flex-shrink: 0;
                        }

                        .quick-action-text {
                            font-size: 0.875rem;
                            font-weight: 600;
                            flex: 1;
                        }

                        .quick-action-arrow {
                            color: var(--clr-faint);
                            font-size: 0.85rem;
                        }

                        /* Notification / tips banner */
                        .pax-tip-banner {
                            background: linear-gradient(135deg, rgba(34, 211, 238, 0.08), rgba(99, 102, 241, 0.08));
                            border: 1px solid rgba(34, 211, 238, 0.2);
                            border-radius: 16px;
                            padding: 1.25rem 1.5rem;
                            display: flex;
                            align-items: flex-start;
                            gap: 1rem;
                            margin-bottom: 1.5rem;
                        }

                        .pax-tip-banner-icon {
                            font-size: 1.4rem;
                            flex-shrink: 0;
                            margin-top: 0.1rem;
                        }

                        .pax-tip-banner-title {
                            font-size: 0.85rem;
                            font-weight: 700;
                            color: var(--clr-cyan);
                            margin-bottom: 0.2rem;
                        }

                        .pax-tip-banner-body {
                            font-size: 0.78rem;
                            color: var(--clr-muted);
                            line-height: 1.5;
                        }
                    </style>
                </head>

                <body>
                    <div class="app-shell">
                        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

                            <div class="main-content">
                                <c:set var="pageTitle" value="Dashboard Overview" scope="request" />
                                <%@ include file="/WEB-INF/includes/topbar.jsp" %>

                                    <div class="page-content">
                                        <div class="dash-container">

                                            <!-- Global Header -->
                                            <div class="page-header">
                                                <div>
                                                    <h1
                                                        style="font-size: 2.25rem; font-weight: 800; letter-spacing: -0.04em; margin-bottom: 0.5rem;">
                                                        ${greeting}, ${currentUser.username}
                                                    </h1>
                                                    <p style="color: var(--clr-muted); font-size: 1.1rem;">
                                                        <c:choose>
                                                            <c:when test="${isPassenger}">Welcome back to your premium
                                                                travel hub.</c:when>
                                                            <c:otherwise>System operations are running smoothly across
                                                                all terminals.</c:otherwise>
                                                        </c:choose>
                                                    </p>
                                                </div>
                                            </div>

                                            <c:choose>
                                                <%--==========================================--%>
                                                    <%-- PASSENGER VIEW --%>
                                                        <%--==========================================--%>
                                                            <c:when test="${currentUser.role == 'PASSENGER'}">

                                                                <%-- ── PAX HERO STATS STRIP ── --%>
                                                                    <div class="pax-stats-strip">
                                                                        <div class="pax-stat">
                                                                            <div class="pax-stat-icon indigo">🎫</div>
                                                                            <div>
                                                                                <div class="pax-stat-val"
                                                                                    style="color: var(--clr-primary-lit);">
                                                                                    ${empty myRecentRequests ? 0 :
                                                                                    fn:length(myRecentRequests)}</div>
                                                                                <div class="pax-stat-label">Total
                                                                                    Bookings</div>
                                                                            </div>
                                                                        </div>
                                                                        <div class="pax-stat">
                                                                            <div class="pax-stat-icon emerald">✅</div>
                                                                            <div>
                                                                                <div class="pax-stat-val"
                                                                                    style="color: var(--clr-emerald);">
                                                                                    ${activeLounges}</div>
                                                                                <div class="pax-stat-label">Open Lounges
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                        <div class="pax-stat">
                                                                            <div class="pax-stat-icon amber">⭐</div>
                                                                            <div>
                                                                                <div class="pax-stat-val"
                                                                                    style="color: var(--clr-amber);">
                                                                                    Gold</div>
                                                                                <div class="pax-stat-label">Tier Status
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                        <div class="pax-stat">
                                                                            <div class="pax-stat-icon cyan">🏢</div>
                                                                            <div>
                                                                                <div class="pax-stat-val"
                                                                                    style="color: var(--clr-cyan);">
                                                                                    ${totalLounges}</div>
                                                                                <div class="pax-stat-label">Available
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>

                                                                    <%-- ── FEATURED LOUNGE HERO BANNER ── --%>
                                                                        <c:if test="${not empty allLounges}">
                                                                            <c:set var="feat"
                                                                                value="${allLounges[0]}" />
                                                                            <div class="featured-lounge-hero"
                                                                                onclick="openBookingModal('${feat.id}', '${feat.name}', 'Terminal ${feat.terminal}')">
                                                                                <div class="featured-hero-bg"></div>
                                                                                <div class="featured-hero-bg-pattern">
                                                                                </div>
                                                                                <div class="featured-hero-glow"></div>
                                                                                <div class="featured-hero-emoji">🛋
                                                                                </div>
                                                                                <div class="featured-hero-content">
                                                                                    <div class="featured-hero-tag">✦
                                                                                        Featured Lounge</div>
                                                                                    <div class="featured-hero-title">
                                                                                        ${feat.name}</div>
                                                                                    <div class="featured-hero-meta">
                                                                                        <span>🏢 Terminal
                                                                                            ${feat.terminal}</span>
                                                                                        <span>👥 ${feat.capacity} PAX
                                                                                            capacity</span>
                                                                                        <span>
                                                                                            <span
                                                                                                class="plc-dot open"></span>
                                                                                            ${feat.status}
                                                                                        </span>
                                                                                    </div>
                                                                                    <div class="featured-hero-actions">
                                                                                        <button class="btn-book-now"
                                                                                            onclick="event.stopPropagation(); openBookingModal('${feat.id}', '${feat.name}', 'Terminal ${feat.terminal}')">
                                                                                            Reserve Now →
                                                                                        </button>
                                                                                        <button class="btn-book-ghost"
                                                                                            onclick="event.stopPropagation(); location.href='${pageContext.request.contextPath}/lounges?id=${feat.id}'">
                                                                                            View Details
                                                                                        </button>
                                                                                    </div>
                                                                                </div>
                                                                            </div>
                                                                        </c:if>

                                                                        <%-- ── MAIN CONTENT + SIDEBAR GRID ── --%>
                                                                            <div class="pax-main-grid">

                                                                                <%-- LEFT: PREMIUM LOUNGE CARDS ── --%>
                                                                                    <div>
                                                                                        <div class="section-header">
                                                                                            <h2>All Premium Lounges</h2>
                                                                                            <a href="${pageContext.request.contextPath}/lounges"
                                                                                                class="btn btn-ghost btn-sm">Browse
                                                                                                All &rarr;</a>
                                                                                        </div>

                                                                                        <div
                                                                                            class="premium-catalog-grid">
                                                                                            <c:forEach var="l"
                                                                                                items="${allLounges}"
                                                                                                varStatus="s">
                                                                                                <c:set var="idx"
                                                                                                    value="${(s.index % 6) + 1}" />
                                                                                                <c:set var="occPct"
                                                                                                    value="${40 + (s.index * 13) % 55}" />
                                                                                                <c:set var="occClass"
                                                                                                    value="${occPct lt 50 ? 'low' : (occPct lt 75 ? 'medium' : 'high')}" />
                                                                                                <c:set var="glowClr"
                                                                                                    value="${idx == 1 ? '#6366f1' : (idx == 2 ? '#22d3ee' : (idx == 3 ? '#f59e0b' : (idx == 4 ? '#a78bfa' : (idx == 5 ? '#fb7185' : '#34d399'))))}" />
                                                                                                <c:set var="tierNames"
                                                                                                    value="Platinum,Gold,Business,Standard,Platinum,Gold" />
                                                                                                <c:set var="tierArr"
                                                                                                    value="${fn:split(tierNames, ',')}" />
                                                                                                <c:set var="tier"
                                                                                                    value="${tierArr[s.index % 6]}" />
                                                                                                <c:set var="emojis"
                                                                                                    value="🛋,🍸,✈️,🌐,🎭,🏊" />
                                                                                                <c:set var="emojiArr"
                                                                                                    value="${fn:split(emojis, ',')}" />

                                                                                                <div class="premium-lounge-card"
                                                                                                    onclick="location.href='${pageContext.request.contextPath}/lounges?id=${l.id}'">
                                                                                                    <div
                                                                                                        class="plc-visual">
                                                                                                        <div
                                                                                                            class="plc-visual-bg v${idx}">
                                                                                                        </div>
                                                                                                        <div class="plc-glow-orb"
                                                                                                            style="background: ${glowClr};">
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="plc-emoji-bg">
                                                                                                            🛋</div>
                                                                                                        <div
                                                                                                            class="plc-tier-badge ${fn:toLowerCase(tier)}">
                                                                                                            ${tier}
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="plc-status-dot">
                                                                                                            <span
                                                                                                                class="plc-dot ${l.active ? 'open' : 'closed'}"></span>
                                                                                                            ${l.active ?
                                                                                                            'OPEN' :
                                                                                                            'CLOSED'}
                                                                                                        </div>
                                                                                                    </div>

                                                                                                    <div
                                                                                                        class="plc-body">
                                                                                                        <div
                                                                                                            class="plc-name">
                                                                                                            ${l.name}
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="plc-location">
                                                                                                            📍 Terminal
                                                                                                            ${l.terminal}
                                                                                                            <c:if
                                                                                                                test="${not empty l.location}">
                                                                                                                &nbsp;·
                                                                                                                ${l.location}
                                                                                                            </c:if>
                                                                                                        </div>

                                                                                                        <%-- Amenity
                                                                                                            chips --%>
                                                                                                            <div
                                                                                                                class="plc-amenities">
                                                                                                                <span
                                                                                                                    class="plc-amenity-chip">🍽️
                                                                                                                    Fine
                                                                                                                    Dining</span>
                                                                                                                <span
                                                                                                                    class="plc-amenity-chip">📶
                                                                                                                    High-Speed
                                                                                                                    Wi-Fi</span>
                                                                                                                <span
                                                                                                                    class="plc-amenity-chip">🚿
                                                                                                                    Shower
                                                                                                                    Suites</span>
                                                                                                                <c:if
                                                                                                                    test="${s.index % 2 == 0}">
                                                                                                                    <span
                                                                                                                        class="plc-amenity-chip">💆
                                                                                                                        Spa</span>
                                                                                                                </c:if>
                                                                                                                <c:if
                                                                                                                    test="${s.index % 3 == 0}">
                                                                                                                    <span
                                                                                                                        class="plc-amenity-chip">🎮
                                                                                                                        Gaming</span>
                                                                                                                </c:if>
                                                                                                                <span
                                                                                                                    class="plc-amenity-chip">🔌
                                                                                                                    Power
                                                                                                                    Points</span>
                                                                                                            </div>

                                                                                                            <%-- Live
                                                                                                                occupancy
                                                                                                                bar --%>
                                                                                                                <div
                                                                                                                    class="plc-occupancy-row">
                                                                                                                    <div
                                                                                                                        class="plc-occupancy-label">
                                                                                                                        <span>Live
                                                                                                                            Occupancy</span>
                                                                                                                        <span
                                                                                                                            class="plc-occ-pct">${occPct}%</span>
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        class="plc-occ-bar">
                                                                                                                        <div class="plc-occ-fill ${occClass}"
                                                                                                                            style="width: ${occPct}%;">
                                                                                                                        </div>
                                                                                                                    </div>
                                                                                                                </div>

                                                                                                                <div
                                                                                                                    class="plc-footer">
                                                                                                                    <div
                                                                                                                        class="plc-info-chip">
                                                                                                                        👥
                                                                                                                        <strong>${l.capacity}</strong>&nbsp;PAX
                                                                                                                        max
                                                                                                                    </div>
                                                                                                                    <button
                                                                                                                        class="plc-book-btn"
                                                                                                                        onclick="event.stopPropagation(); openBookingModal('${l.id}', '${l.name}', 'Terminal ${l.terminal}')">
                                                                                                                        Book
                                                                                                                        Now
                                                                                                                    </button>
                                                                                                                </div>
                                                                                                    </div>
                                                                                                </div>
                                                                                            </c:forEach>

                                                                                            <c:if
                                                                                                test="${empty allLounges}">
                                                                                                <div class="empty-state"
                                                                                                    style="grid-column: 1/-1;">
                                                                                                    <div
                                                                                                        class="empty-state-icon">
                                                                                                        🏢</div>
                                                                                                    <p>No lounges are
                                                                                                        currently
                                                                                                        available.</p>
                                                                                                </div>
                                                                                            </c:if>
                                                                                        </div>
                                                                                    </div>

                                                                                    <%-- RIGHT SIDEBAR ── --%>
                                                                                        <div>
                                                                                            <%-- Membership card --%>
                                                                                                <div
                                                                                                    class="membership-card">
                                                                                                    <div
                                                                                                        class="membership-chip">
                                                                                                        ⭐ Gold Member
                                                                                                    </div>
                                                                                                    <div
                                                                                                        class="membership-name">
                                                                                                        ${currentUser.username}
                                                                                                    </div>
                                                                                                    <div
                                                                                                        class="membership-tagline">
                                                                                                        Premium Airport
                                                                                                        Lounge Access
                                                                                                    </div>
                                                                                                    <div
                                                                                                        class="membership-stats">
                                                                                                        <div
                                                                                                            class="membership-stat">
                                                                                                            <div class="membership-stat-val"
                                                                                                                style="color: var(--clr-emerald);">
                                                                                                                ${empty
                                                                                                                myRecentRequests
                                                                                                                ? 0 :
                                                                                                                fn:length(myRecentRequests)}
                                                                                                            </div>
                                                                                                            <div
                                                                                                                class="membership-stat-label">
                                                                                                                Visits
                                                                                                            </div>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="membership-stat">
                                                                                                            <div class="membership-stat-val"
                                                                                                                style="color: var(--clr-primary-lit);">
                                                                                                                ${totalLounges}
                                                                                                            </div>
                                                                                                            <div
                                                                                                                class="membership-stat-label">
                                                                                                                Lounges
                                                                                                            </div>
                                                                                                        </div>
                                                                                                    </div>
                                                                                                </div>

                                                                                                <%-- Tip banner --%>
                                                                                                    <div
                                                                                                        class="pax-tip-banner">
                                                                                                        <div
                                                                                                            class="pax-tip-banner-icon">
                                                                                                            💡</div>
                                                                                                        <div>
                                                                                                            <div
                                                                                                                class="pax-tip-banner-title">
                                                                                                                Pro Tip
                                                                                                            </div>
                                                                                                            <div
                                                                                                                class="pax-tip-banner-body">
                                                                                                                Book at
                                                                                                                least 2
                                                                                                                hours
                                                                                                                before
                                                                                                                departure
                                                                                                                for
                                                                                                                guaranteed
                                                                                                                seating
                                                                                                                at
                                                                                                                peak-hour
                                                                                                                lounges.
                                                                                                            </div>
                                                                                                        </div>
                                                                                                    </div>

                                                                                                    <%-- Quick actions
                                                                                                        --%>
                                                                                                        <div
                                                                                                            class="quick-actions-panel">
                                                                                                            <div
                                                                                                                class="quick-actions-title">
                                                                                                                Quick
                                                                                                                Actions
                                                                                                            </div>
                                                                                                            <a href="${pageContext.request.contextPath}/lounges"
                                                                                                                class="quick-action-item">
                                                                                                                <div class="quick-action-icon"
                                                                                                                    style="background: rgba(99,102,241,0.15);">
                                                                                                                    🏢
                                                                                                                </div>
                                                                                                                <span
                                                                                                                    class="quick-action-text">Browse
                                                                                                                    All
                                                                                                                    Lounges</span>
                                                                                                                <span
                                                                                                                    class="quick-action-arrow">›</span>
                                                                                                            </a>
                                                                                                            <a href="${pageContext.request.contextPath}/access-requests"
                                                                                                                class="quick-action-item">
                                                                                                                <div class="quick-action-icon"
                                                                                                                    style="background: rgba(52,211,153,0.12);">
                                                                                                                    📋
                                                                                                                </div>
                                                                                                                <span
                                                                                                                    class="quick-action-text">My
                                                                                                                    Reservations</span>
                                                                                                                <span
                                                                                                                    class="quick-action-arrow">›</span>
                                                                                                            </a>
                                                                                                            <a href="${pageContext.request.contextPath}/profile"
                                                                                                                class="quick-action-item">
                                                                                                                <div class="quick-action-icon"
                                                                                                                    style="background: rgba(251,191,36,0.12);">
                                                                                                                    👤
                                                                                                                </div>
                                                                                                                <span
                                                                                                                    class="quick-action-text">Update
                                                                                                                    Profile</span>
                                                                                                                <span
                                                                                                                    class="quick-action-arrow">›</span>
                                                                                                            </a>
                                                                                                            <a href="${pageContext.request.contextPath}/transactions"
                                                                                                                class="quick-action-item">
                                                                                                                <div class="quick-action-icon"
                                                                                                                    style="background: rgba(34,211,238,0.1);">
                                                                                                                    💳
                                                                                                                </div>
                                                                                                                <span
                                                                                                                    class="quick-action-text">Payment
                                                                                                                    History</span>
                                                                                                                <span
                                                                                                                    class="quick-action-arrow">›</span>
                                                                                                            </a>
                                                                                                        </div>

                                                                                                        <%-- Upcoming
                                                                                                            visits
                                                                                                            placeholder
                                                                                                            --%>
                                                                                                            <div
                                                                                                                class="glass-panel">
                                                                                                                <div
                                                                                                                    class="glass-panel-header">
                                                                                                                    <h2>📅
                                                                                                                        Upcoming
                                                                                                                        Visits
                                                                                                                    </h2>
                                                                                                                </div>
                                                                                                                <div class="glass-panel-body"
                                                                                                                    style="padding: 1.25rem;">
                                                                                                                    <c:choose>
                                                                                                                        <c:when
                                                                                                                            test="${empty myRecentRequests}">
                                                                                                                            <div class="empty-state"
                                                                                                                                style="padding: 2rem 1rem;">
                                                                                                                                <div
                                                                                                                                    class="empty-state-icon">
                                                                                                                                    ✈️
                                                                                                                                </div>
                                                                                                                                <p
                                                                                                                                    style="font-size: 0.8rem;">
                                                                                                                                    No
                                                                                                                                    upcoming
                                                                                                                                    visits
                                                                                                                                    scheduled.
                                                                                                                                </p>
                                                                                                                                <button
                                                                                                                                    class="btn btn-primary btn-sm"
                                                                                                                                    style="margin-top: 1rem;"
                                                                                                                                    onclick="location.href='${pageContext.request.contextPath}/lounges'">Book
                                                                                                                                    a
                                                                                                                                    Lounge</button>
                                                                                                                            </div>
                                                                                                                        </c:when>
                                                                                                                        <c:otherwise>
                                                                                                                            <c:forEach
                                                                                                                                var="r"
                                                                                                                                items="${myRecentRequests}"
                                                                                                                                end="2">
                                                                                                                                <div
                                                                                                                                    style="display: flex; align-items: center; gap: 0.85rem; padding: 0.85rem 0; border-bottom: 1px solid rgba(255,255,255,0.04);">
                                                                                                                                    <div
                                                                                                                                        style="width: 38px; height: 38px; border-radius: 12px; background: rgba(99,102,241,0.12); border: 1px solid rgba(99,102,241,0.2); display: flex; align-items: center; justify-content: center; font-size: 1rem;">
                                                                                                                                        🛋
                                                                                                                                    </div>
                                                                                                                                    <div
                                                                                                                                        style="flex: 1; min-width: 0;">
                                                                                                                                        <div
                                                                                                                                            style="font-weight: 700; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                                                                                                            ${r.loungeName}
                                                                                                                                        </div>
                                                                                                                                        <div
                                                                                                                                            style="font-size: 0.72rem; color: var(--clr-muted);">
                                                                                                                                            ${r.status}
                                                                                                                                        </div>
                                                                                                                                    </div>
                                                                                                                                    <span
                                                                                                                                        class="trip-status-pill ${fn:toLowerCase(r.status)}">${r.status}</span>
                                                                                                                                </div>
                                                                                                                            </c:forEach>
                                                                                                                        </c:otherwise>
                                                                                                                    </c:choose>
                                                                                                                </div>
                                                                                                            </div>
                                                                                        </div>
                                                                            </div>

                                                                            <%-- ── MY TRIPS TIMELINE ── --%>
                                                                                <div class="glass-panel">
                                                                                    <div class="glass-panel-header"
                                                                                        style="padding: 1.25rem 1.75rem;">
                                                                                        <h2>🗓 My Travel History</h2>
                                                                                        <a href="${pageContext.request.contextPath}/access-requests"
                                                                                            class="btn btn-ghost btn-sm">View
                                                                                            All</a>
                                                                                    </div>
                                                                                    <div class="glass-panel-body"
                                                                                        style="padding: 0 1.75rem 1rem;">
                                                                                        <c:choose>
                                                                                            <c:when
                                                                                                test="${empty myRecentRequests}">
                                                                                                <div class="empty-state"
                                                                                                    style="padding: 3.5rem 2rem;">
                                                                                                    <div
                                                                                                        class="empty-state-icon">
                                                                                                        🎫</div>
                                                                                                    <p>No lounge visits
                                                                                                        recorded yet.
                                                                                                        Start exploring!
                                                                                                    </p>
                                                                                                    <button
                                                                                                        class="btn btn-primary"
                                                                                                        style="margin-top: 1.25rem;"
                                                                                                        onclick="location.href='${pageContext.request.contextPath}/lounges'">
                                                                                                        Find a Lounge
                                                                                                    </button>
                                                                                                </div>
                                                                                            </c:when>
                                                                                            <c:otherwise>
                                                                                                <div
                                                                                                    class="trip-timeline">
                                                                                                    <c:forEach var="r"
                                                                                                        items="${myRecentRequests}">
                                                                                                        <c:set var="st"
                                                                                                            value="${fn:toLowerCase(r.status)}" />
                                                                                                        <div
                                                                                                            class="trip-item">
                                                                                                            <div
                                                                                                                class="trip-icon ${st}">
                                                                                                                <c:choose>
                                                                                                                    <c:when
                                                                                                                        test="${r.status == 'APPROVED'}">
                                                                                                                        ✅
                                                                                                                    </c:when>
                                                                                                                    <c:when
                                                                                                                        test="${r.status == 'PENDING'}">
                                                                                                                        ⏳
                                                                                                                    </c:when>
                                                                                                                    <c:otherwise>
                                                                                                                        ❌
                                                                                                                    </c:otherwise>
                                                                                                                </c:choose>
                                                                                                            </div>
                                                                                                            <div>
                                                                                                                <div
                                                                                                                    class="trip-name">
                                                                                                                    ${r.loungeName}
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="trip-meta">
                                                                                                                    Requested
                                                                                                                    ${r.requestTime}
                                                                                                                </div>
                                                                                                            </div>
                                                                                                            <div
                                                                                                                class="trip-status-pill ${st}">
                                                                                                                ${r.status}
                                                                                                            </div>
                                                                                                        </div>
                                                                                                    </c:forEach>
                                                                                                </div>
                                                                                            </c:otherwise>
                                                                                        </c:choose>
                                                                                    </div>
                                                                                </div>

                                                            </c:when>

                                                            <%--==========================================--%>
                                                                <%-- RECEPTION VIEW --%>
                                                                    <%--==========================================--%>
                                                                        <c:when
                                                                            test="${currentUser.role == 'RECEPTION'}">
                                                                            <div class="dash-grid-2">
                                                                                <div class="main-ops">
                                                                                    <div class="scan-module">
                                                                                        <h2
                                                                                            style="margin-bottom: 1rem; color: var(--clr-primary-lit); letter-spacing: -0.01em;">
                                                                                            ⚡ Digital Concierge</h2>
                                                                                        <p
                                                                                            style="color: var(--clr-muted); margin-bottom: 2rem;">
                                                                                            Scan boarding pass or enter
                                                                                            passenger ID for instant
                                                                                            check-in</p>
                                                                                        <input type="text"
                                                                                            class="scan-input"
                                                                                            placeholder="PASSENGER_ID_OR_SCAN"
                                                                                            autofocus>
                                                                                    </div>

                                                                                    <div class="glass-panel">
                                                                                        <div class="glass-panel-header">
                                                                                            <h2>Live Guest Roster</h2>
                                                                                            <span
                                                                                                class="badge badge-info">${fn:length(guestRoster)}
                                                                                                Active</span>
                                                                                        </div>
                                                                                        <div class="glass-panel-body"
                                                                                            style="padding: 0;">
                                                                                            <table class="table-v">
                                                                                                <thead>
                                                                                                    <tr>
                                                                                                        <th>Guest Name
                                                                                                        </th>
                                                                                                        <th>Entitlement
                                                                                                        </th>
                                                                                                        <th>Checked In
                                                                                                        </th>
                                                                                                        <th
                                                                                                            style="text-align: right;">
                                                                                                            Action</th>
                                                                                                    </tr>
                                                                                                </thead>
                                                                                                <tbody>
                                                                                                    <c:forEach var="g"
                                                                                                        items="${guestRoster}">
                                                                                                        <tr>
                                                                                                            <td>
                                                                                                                <div
                                                                                                                    style="font-weight: 700;">
                                                                                                                    ${g.passengerName}
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    style="font-size: 0.75rem; color: var(--clr-muted);">
                                                                                                                    ${g.loungeName}
                                                                                                                </div>
                                                                                                            </td>
                                                                                                            <td>
                                                                                                                <span
                                                                                                                    class="role-pill">
                                                                                                                    ${g.membershipType
                                                                                                                    !=
                                                                                                                    null
                                                                                                                    ?
                                                                                                                    g.membershipType
                                                                                                                    :
                                                                                                                    (g.providerName
                                                                                                                    !=
                                                                                                                    null
                                                                                                                    ?
                                                                                                                    g.providerName
                                                                                                                    :
                                                                                                                    'Standard')}
                                                                                                                </span>
                                                                                                            </td>
                                                                                                            <td>
                                                                                                                <fmt:formatDate
                                                                                                                    value="${g.checkedInAt}"
                                                                                                                    pattern="HH:mm" />
                                                                                                            </td>
                                                                                                            <td
                                                                                                                style="text-align: right;">
                                                                                                                <button
                                                                                                                    class="btn btn-ghost btn-sm"
                                                                                                                    style="color: var(--clr-rose)">Checkout</button>
                                                                                                            </td>
                                                                                                        </tr>
                                                                                                    </c:forEach>
                                                                                                    <c:if
                                                                                                        test="${empty guestRoster}">
                                                                                                        <tr>
                                                                                                            <td colspan="4"
                                                                                                                style="text-align: center; padding: 4rem; color: var(--clr-muted);">
                                                                                                                No
                                                                                                                guests
                                                                                                                currently
                                                                                                                in
                                                                                                                lounge.
                                                                                                            </td>
                                                                                                        </tr>
                                                                                                    </c:if>
                                                                                                </tbody>
                                                                                            </table>
                                                                                        </div>
                                                                                    </div>
                                                                                </div>

                                                                                <div class="side-ops">
                                                                                    <div
                                                                                        class="glass-panel gauge-wrapper">
                                                                                        <div class="gauge-svg">
                                                                                            <svg viewBox="0 0 100 50">
                                                                                                <path class="gauge-bg"
                                                                                                    d="M 10 50 A 40 40 0 0 1 90 50" />
                                                                                                <path
                                                                                                    class="gauge-progress"
                                                                                                    id="main-gauge"
                                                                                                    d="M 10 50 A 40 40 0 0 1 90 50" />
                                                                                            </svg>
                                                                                        </div>
                                                                                        <span
                                                                                            class="gauge-val-big">74%</span>
                                                                                        <div
                                                                                            style="font-size: 0.8rem; font-weight: 700; color: var(--clr-muted); text-transform: uppercase;">
                                                                                            Occupancy Level</div>
                                                                                        <div
                                                                                            style="margin-top: 1.5rem; font-weight: 700; color: var(--clr-primary-lit);">
                                                                                            ${primaryLounge.name}</div>
                                                                                        <div
                                                                                            style="font-size: 0.75rem; color: var(--clr-faint); margin-top: 0.5rem;">
                                                                                            Capacity:
                                                                                            ${primaryLounge.capacity}
                                                                                            PAX</div>
                                                                                    </div>

                                                                                    <div class="glass-panel"
                                                                                        style="margin-top: 1.5rem;">
                                                                                        <div class="glass-panel-header">
                                                                                            <h2>Queue Protocol</h2>
                                                                                        </div>
                                                                                        <div class="glass-panel-body"
                                                                                            style="padding: 0 1.5rem 1.5rem;">
                                                                                            <c:forEach var="p"
                                                                                                items="${pendingRequests}">
                                                                                                <div
                                                                                                    class="activity-item">
                                                                                                    <div
                                                                                                        class="icon-circle amber">
                                                                                                        ⏳</div>
                                                                                                    <div
                                                                                                        style="flex: 1">
                                                                                                        <div
                                                                                                            style="font-weight: 700; font-size: 0.85rem;">
                                                                                                            ${p.passengerName}
                                                                                                        </div>
                                                                                                        <div
                                                                                                            style="font-size: 0.75rem; color: var(--clr-muted);">
                                                                                                            ${p.membershipType
                                                                                                            != null ?
                                                                                                            p.membershipType
                                                                                                            :
                                                                                                            'Waitlist'}
                                                                                                        </div>
                                                                                                    </div>
                                                                                                    <button
                                                                                                        class="btn-text"
                                                                                                        onclick="location.href='${ctx}/access-requests'">&rarr;</button>
                                                                                                </div>
                                                                                            </c:forEach>
                                                                                        </div>
                                                                                    </div>
                                                                                </div>
                                                                            </div>
                                                                        </c:when>

                                                                        <%--==========================================--%>
                                                                            <%-- OPERATIONS MANAGER VIEW --%>
                                                                                <%--==========================================--%>
                                                                                    <c:when
                                                                                        test="${currentUser.role == 'OPERATIONS_MANAGER'}">
                                                                                        <div class="kpi-grid">
                                                                                            <div class="kpi-card">
                                                                                                <div class="kpi-label">
                                                                                                    Today's Traffic
                                                                                                </div>
                                                                                                <div class="kpi-value">
                                                                                                    1,420</div>
                                                                                                <div class="kpi-icon">👥
                                                                                                </div>
                                                                                                <div
                                                                                                    style="font-size: 0.7rem; color: var(--clr-emerald); font-weight: 700; margin-top: 0.5rem;">
                                                                                                    ↑ 14% vs avg</div>
                                                                                            </div>
                                                                                            <div class="kpi-card">
                                                                                                <div class="kpi-label">
                                                                                                    Avg. Service Time
                                                                                                </div>
                                                                                                <div class="kpi-value">
                                                                                                    ${avgStayDuration}m
                                                                                                </div>
                                                                                                <div class="kpi-icon">⏱
                                                                                                </div>
                                                                                            </div>
                                                                                            <div class="kpi-card">
                                                                                                <div class="kpi-label">
                                                                                                    Daily Revenue</div>
                                                                                                <div class="kpi-value">
                                                                                                    <fmt:formatNumber
                                                                                                        value="${totalRevenue}"
                                                                                                        type="currency"
                                                                                                        currencySymbol="$" />
                                                                                                </div>
                                                                                                <div class="kpi-icon">💰
                                                                                                </div>
                                                                                            </div>
                                                                                            <div class="kpi-card">
                                                                                                <div class="kpi-label">
                                                                                                    Waitlist Active
                                                                                                </div>
                                                                                                <div class="kpi-value">
                                                                                                    ${waitlistCount}
                                                                                                </div>
                                                                                                <div class="kpi-icon">⏳
                                                                                                </div>
                                                                                            </div>
                                                                                        </div>

                                                                                        <div class="dash-grid-2">
                                                                                            <div class="glass-panel">
                                                                                                <div
                                                                                                    class="glass-panel-header">
                                                                                                    <h2>Predictive
                                                                                                        Analytics</h2>
                                                                                                    <div
                                                                                                        class="badge badge-info">
                                                                                                        Next 12 Hours
                                                                                                    </div>
                                                                                                </div>
                                                                                                <div class="glass-panel-body"
                                                                                                    style="padding: 2rem;">
                                                                                                    <div
                                                                                                        class="chart-container">
                                                                                                        <c:forEach
                                                                                                            var="i"
                                                                                                            begin="1"
                                                                                                            end="12">
                                                                                                            <div class="forecast-bar"
                                                                                                                style="height: ${30 + (i*4)}%; opacity: ${0.3 + (i*0.05)}">
                                                                                                                <c:if
                                                                                                                    test="${i == 8}">
                                                                                                                    <div
                                                                                                                        style="position: absolute; top: -25px; left: 50%; transform: translateX(-50%); font-size: 10px; font-weight: 800; color: var(--clr-rose);">
                                                                                                                        PEAK
                                                                                                                    </div>
                                                                                                                </c:if>
                                                                                                            </div>
                                                                                                        </c:forEach>
                                                                                                    </div>
                                                                                                    <div
                                                                                                        style="display: flex; justify-content: space-between; margin-top: 1rem; font-size: 0.75rem; color: var(--clr-faint);">
                                                                                                        <span>08:00</span><span>12:00</span><span>16:00</span><span>20:00</span>
                                                                                                    </div>
                                                                                                </div>
                                                                                            </div>

                                                                                            <div class="glass-panel">
                                                                                                <div
                                                                                                    class="glass-panel-header">
                                                                                                    <h2>Critical Actions
                                                                                                    </h2>
                                                                                                </div>
                                                                                                <div class="glass-panel-body"
                                                                                                    style="padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem;">
                                                                                                    <button
                                                                                                        class="btn btn-primary"
                                                                                                        style="width: 100%">Run
                                                                                                        Daily
                                                                                                        Audit</button>
                                                                                                    <button
                                                                                                        class="btn btn-ghost"
                                                                                                        style="width: 100%; border: 1px solid var(--clr-border);">Inventory
                                                                                                        Report</button>
                                                                                                    <button
                                                                                                        class="btn btn-ghost"
                                                                                                        style="width: 100%; color: var(--clr-rose); border: 1px solid rgba(244,63,94,0.2);">Capacity
                                                                                                        Emergency</button>
                                                                                                </div>
                                                                                            </div>
                                                                                        </div>
                                                                                    </c:when>

                                                                                    <c:when
                                                                                        test="${currentUser.role == 'ADMIN'}">
                                                                                        <!-- Admin Top Performance HUD -->
                                                                                        <div class="kpi-grid"
                                                                                            style="grid-template-columns: repeat(4, 1fr);">
                                                                                            <div class="kpi-card">
                                                                                                <div class="kpi-label">
                                                                                                    Active Users</div>
                                                                                                <div class="kpi-value">
                                                                                                    ${totalUsers}</div>
                                                                                                <div class="kpi-icon">👤
                                                                                                </div>
                                                                                                <div
                                                                                                    style="font-size: 0.65rem; color: var(--clr-emerald); font-weight: 700;">
                                                                                                    System Wide</div>
                                                                                            </div>
                                                                                            <div class="kpi-card">
                                                                                                <div class="kpi-label">
                                                                                                    Total Revenue</div>
                                                                                                <div class="kpi-value">
                                                                                                    <fmt:formatNumber
                                                                                                        value="${totalRevenue}"
                                                                                                        type="currency"
                                                                                                        currencySymbol="$" />
                                                                                                </div>
                                                                                                <div class="kpi-icon">💸
                                                                                                </div>
                                                                                            </div>
                                                                                            <div class="kpi-card">
                                                                                                <div class="kpi-label">
                                                                                                    Network Load</div>
                                                                                                <div class="kpi-value">
                                                                                                    ${activeLounges}/${totalLounges}
                                                                                                </div>
                                                                                                <div class="kpi-icon">🏢
                                                                                                </div>
                                                                                                <div
                                                                                                    style="font-size: 0.65rem; color: var(--clr-muted);">
                                                                                                    Online Lounges</div>
                                                                                            </div>
                                                                                            <div class="kpi-card">
                                                                                                <div class="kpi-label">
                                                                                                    System Logs</div>
                                                                                                <div class="kpi-value">
                                                                                                    ${fn:length(systemLogs)}
                                                                                                </div>
                                                                                                <div class="kpi-icon">🛡
                                                                                                </div>
                                                                                                <div
                                                                                                    style="font-size: 0.65rem; color: var(--clr-amber);">
                                                                                                    Recent events</div>
                                                                                            </div>
                                                                                        </div>

                                                                                        <div class="dash-grid-2"
                                                                                            style="margin-top: 2rem;">
                                                                                            <!-- Left Column: Stats & Performance -->
                                                                                            <div class="admin-main">

                                                                                                <div class="glass-panel"
                                                                                                    style="margin-bottom: 2rem;">
                                                                                                    <div
                                                                                                        class="glass-panel-header">
                                                                                                        <h2>Lounge
                                                                                                            Performance
                                                                                                            HUD</h2>
                                                                                                        <div
                                                                                                            class="badge badge-info">
                                                                                                            Revenue
                                                                                                            Dist.</div>
                                                                                                    </div>
                                                                                                    <div class="glass-panel-body"
                                                                                                        style="padding: 1.5rem;">
                                                                                                        <c:forEach
                                                                                                            var="entry"
                                                                                                            items="${revenueByLounge}">
                                                                                                            <div
                                                                                                                style="margin-bottom: 1.25rem;">
                                                                                                                <div
                                                                                                                    style="display:flex; justify-content:space-between; margin-bottom:0.5rem; font-size:0.85rem;">
                                                                                                                    <span
                                                                                                                        style="font-weight:700;">${entry.key}</span>
                                                                                                                    <span
                                                                                                                        style="color:var(--clr-emerald); font-weight:800;">
                                                                                                                        <fmt:formatNumber
                                                                                                                            value="${entry.value}"
                                                                                                                            type="currency"
                                                                                                                            currencySymbol="$" />
                                                                                                                    </span>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    style="height:8px; background:rgba(255,255,255,0.03); border-radius:4px; overflow:hidden;">
                                                                                                                    <c:set
                                                                                                                        var="pct"
                                                                                                                        value="${(entry.value / totalRevenue) * 100}" />
                                                                                                                    <div
                                                                                                                        style="width:${pct}%; height:100%; background:var(--clr-primary); box-shadow: 0 0 10px var(--clr-primary);">
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                            </div>
                                                                                                        </c:forEach>
                                                                                                        <c:if
                                                                                                            test="${empty revenueByLounge}">
                                                                                                            <p
                                                                                                                style="text-align:center; color:var(--clr-faint); padding:2rem;">
                                                                                                                No
                                                                                                                revenue
                                                                                                                data
                                                                                                                recorded
                                                                                                                across
                                                                                                                lounges.
                                                                                                            </p>
                                                                                                        </c:if>
                                                                                                    </div>
                                                                                                </div>

                                                                                                <div
                                                                                                    class="glass-panel">
                                                                                                    <div
                                                                                                        class="glass-panel-header">
                                                                                                        <h2>🛫 Network
                                                                                                            Node
                                                                                                            Registry
                                                                                                        </h2>
                                                                                                    </div>
                                                                                                    <div class="glass-panel-body"
                                                                                                        style="padding: 1rem;">
                                                                                                        <c:forEach
                                                                                                            var="l"
                                                                                                            items="${allLounges}">
                                                                                                            <div class="admin-stat-row"
                                                                                                                style="cursor: pointer;"
                                                                                                                onclick="location.href='${ctx}/lounges?id=${l.id}'">
                                                                                                                <div
                                                                                                                    style="display: flex; align-items: center; gap: 1rem;">
                                                                                                                    <div
                                                                                                                        style="width: 10px; height: 10px; border-radius: 50%; background: ${l.active ? '#10b981' : '#f43f5e'}; box-shadow: 0 0 12px ${l.active ? '#10b981' : '#f43f5e'};">
                                                                                                                    </div>
                                                                                                                    <div>
                                                                                                                        <div
                                                                                                                            style="font-weight: 800; font-size: 1rem;">
                                                                                                                            ${l.name}
                                                                                                                        </div>
                                                                                                                        <div
                                                                                                                            style="font-size: 0.75rem; color: var(--clr-muted);">
                                                                                                                            Terminal
                                                                                                                            ${l.terminal}
                                                                                                                            &bull;
                                                                                                                            ${l.location}
                                                                                                                        </div>
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    style="text-align: right;">
                                                                                                                    <div
                                                                                                                        class="badge ${l.active ? 'badge-active' : 'badge-inactive'}">
                                                                                                                        ${l.active
                                                                                                                        ?
                                                                                                                        'ACTIVE'
                                                                                                                        :
                                                                                                                        'OFFLINE'}
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        style="font-size:0.65rem; color:var(--clr-faint); margin-top:0.25rem;">
                                                                                                                        Cap:
                                                                                                                        ${l.capacity}
                                                                                                                        PAX
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                            </div>
                                                                                                        </c:forEach>
                                                                                                    </div>
                                                                                                </div>
                                                                                            </div>

                                                                                            <!-- Right Column: Logs, Users & Actions -->
                                                                                            <div class="admin-side">
                                                                                                <div class="glass-panel"
                                                                                                    style="margin-bottom: 2rem; border: 1px solid rgba(99, 102, 241, 0.3); background: linear-gradient(135deg, rgba(30, 41, 59, 0.9), rgba(15, 23, 42, 0.9));">
                                                                                                    <div
                                                                                                        class="glass-panel-header">
                                                                                                        <h2
                                                                                                            style="color: var(--clr-primary-lit);">
                                                                                                            ⚡ Admin
                                                                                                            Terminal
                                                                                                        </h2>
                                                                                                    </div>
                                                                                                    <div class="glass-panel-body"
                                                                                                        style="padding: 1.5rem; display: flex; flex-direction: column; gap: 0.75rem;">
                                                                                                        <button
                                                                                                            class="btn btn-primary"
                                                                                                            style="width:100%; border-radius:12px;">
                                                                                                            <span
                                                                                                                style="margin-right:0.5rem;">👥</span>
                                                                                                            User
                                                                                                            Management
                                                                                                        </button>
                                                                                                        <button
                                                                                                            class="btn btn-ghost"
                                                                                                            style="width:100%; border:1px solid var(--clr-border); border-radius:12px; text-align:left;">
                                                                                                            <span
                                                                                                                style="margin-right:0.5rem;">🏗</span>
                                                                                                            Provisions
                                                                                                            New Lounge
                                                                                                        </button>
                                                                                                        <button
                                                                                                            class="btn btn-ghost"
                                                                                                            style="width:100%; border:1px solid var(--clr-border); border-radius:12px; text-align:left;">
                                                                                                            <span
                                                                                                                style="margin-right:0.5rem;">📂</span>
                                                                                                            Export Audit
                                                                                                            Trail
                                                                                                        </button>
                                                                                                        <button
                                                                                                            class="btn btn-ghost"
                                                                                                            style="width:100%; border:1px solid rgba(244,63,94,0.1); border-radius:12px; text-align:left; color:var(--clr-rose);">
                                                                                                            <span
                                                                                                                style="margin-right:0.5rem;">🔒</span>
                                                                                                            Security
                                                                                                            Sweep
                                                                                                        </button>
                                                                                                    </div>
                                                                                                </div>

                                                                                                <div class="glass-panel"
                                                                                                    style="margin-bottom: 2rem;">
                                                                                                    <div
                                                                                                        class="glass-panel-header">
                                                                                                        <h2>Recent
                                                                                                            Access</h2>
                                                                                                    </div>
                                                                                                    <div class="glass-panel-body"
                                                                                                        style="padding: 0 1.5rem 1.5rem;">
                                                                                                        <c:forEach
                                                                                                            var="tu"
                                                                                                            items="${recentUsers}">
                                                                                                            <div class="activity-item"
                                                                                                                style="padding: 0.75rem 0;">
                                                                                                                <div class="icon-circle"
                                                                                                                    style="background: rgba(255,255,255,0.03); font-size: 1rem;">
                                                                                                                    👤
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    style="flex: 1">
                                                                                                                    <div
                                                                                                                        style="font-weight: 700; font-size: 0.85rem;">
                                                                                                                        ${tu.username}
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        style="font-size: 0.7rem; color: var(--clr-primary-lit);">
                                                                                                                        ${tu.role}
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="status-dot ${tu.active ? 'green' : 'red'}">
                                                                                                                </div>
                                                                                                            </div>
                                                                                                        </c:forEach>
                                                                                                    </div>
                                                                                                </div>

                                                                                                <div
                                                                                                    class="glass-panel">
                                                                                                    <div
                                                                                                        class="glass-panel-header">
                                                                                                        <h2>📜 Audit
                                                                                                            Stream</h2>
                                                                                                    </div>
                                                                                                    <div class="glass-panel-body"
                                                                                                        style="padding: 0;">
                                                                                                        <div
                                                                                                            style="max-height: 480px; overflow-y: auto;">
                                                                                                            <c:forEach
                                                                                                                var="log"
                                                                                                                items="${systemLogs}">
                                                                                                                <div class="activity-item"
                                                                                                                    style="padding: 1rem 1.5rem; border-bottom: 1px solid rgba(255,255,255,0.02);">
                                                                                                                    <div
                                                                                                                        style="flex: 1">
                                                                                                                        <div
                                                                                                                            style="font-weight: 800; font-size: 0.8rem; color: #fff;">
                                                                                                                            ${log.actionType}
                                                                                                                        </div>
                                                                                                                        <div
                                                                                                                            style="font-size: 0.75rem; color: var(--clr-muted); margin-top: 0.25rem;">
                                                                                                                            ${log.description}
                                                                                                                        </div>
                                                                                                                        <div
                                                                                                                            style="font-size: 0.65rem; color: var(--clr-faint); margin-top: 0.5rem; display: flex; justify-content: space-between;">
                                                                                                                            <span
                                                                                                                                style="color:var(--clr-primary-lit)">${log.staffName}</span>
                                                                                                                            <fmt:formatDate
                                                                                                                                value="${log.createdAt}"
                                                                                                                                pattern="MMM dd, HH:mm" />
                                                                                                                        </div>
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                            </c:forEach>
                                                                                                        </div>
                                                                                                    </div>
                                                                                                </div>
                                                                                            </div>
                                                                                        </div>
                                                                                    </c:when>

                                                                                    <c:otherwise>
                                                                                        <div class="empty-state"
                                                                                            style="padding: 6rem;">
                                                                                            <div
                                                                                                style="font-size: 4rem; opacity: 0.2;">
                                                                                                🛡</div>
                                                                                            <h2
                                                                                                style="margin-top: 2rem;">
                                                                                                Custom View
                                                                                                Requested
                                                                                            </h2>
                                                                                            <p
                                                                                                style="color: var(--clr-muted);">
                                                                                                Your role
                                                                                                (${roleDisplayName})
                                                                                                is pending a
                                                                                                dashboard
                                                                                                template.
                                                                                            </p>
                                                                                            <button
                                                                                                class="btn btn-primary"
                                                                                                style="margin-top: 2rem;">Contact
                                                                                                System
                                                                                                Admin</button>
                                                                                        </div>
                                                                                    </c:otherwise>
                                            </c:choose>

                                        </div><!-- /dash-container -->
                                    </div><!-- /page-content -->
                            </div><!-- /main-content -->
                    </div><!-- /app-shell -->

                    <%-- ═══════════════════════════════════════════ BOOKING MODAL (Passenger Only)
                        ═══════════════════════════════════════════ --%>
                        <div class="booking-overlay" id="bookingOverlay" onclick="closeBookingModal(event)">
                            <div class="booking-modal" role="dialog" aria-modal="true" aria-labelledby="bModalTitle">
                                <button class="booking-modal-close" onclick="closeBookingModalDirect()"
                                    aria-label="Close">✕</button>

                                <div class="booking-modal-lounge-header">
                                    <div class="booking-modal-lounge-icon">🛋</div>
                                    <div>
                                        <div
                                            style="font-size: 0.7rem; font-weight: 700; color: var(--clr-primary-lit); text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 0.2rem;">
                                            Reserve Your Spot</div>
                                        <div id="bModalTitle"
                                            style="font-size: 1.2rem; font-weight: 900; letter-spacing: -0.02em;"></div>
                                        <div id="bModalSub"
                                            style="font-size: 0.8rem; color: var(--clr-muted); margin-top: 0.15rem;">
                                        </div>
                                    </div>
                                </div>

                                <form action="${pageContext.request.contextPath}/access-requests" method="post"
                                    onsubmit="return handleBookingSubmit(event)">
                                    <input type="hidden" name="action" value="request">
                                    <input type="hidden" name="loungeId" id="bLoungeId">

                                    <div class="booking-form-row">
                                        <div class="booking-form-group">
                                            <label class="booking-form-label" for="bArrival">Arrival Date</label>
                                            <input type="date" id="bArrival" name="arrivalDate"
                                                class="booking-form-input" required>
                                        </div>
                                        <div class="booking-form-group">
                                            <label class="booking-form-label" for="bTime">Arrival Time</label>
                                            <input type="time" id="bTime" name="arrivalTime" class="booking-form-input"
                                                required>
                                        </div>
                                    </div>

                                    <div class="booking-form-group">
                                        <label class="booking-form-label" for="bFlight">Flight Number</label>
                                        <input type="text" id="bFlight" name="flightNumber" class="booking-form-input"
                                            placeholder="e.g. AA 2041">
                                    </div>

                                    <div class="booking-form-group">
                                        <label class="booking-form-label" for="bGuests">Number of Guests</label>
                                        <select id="bGuests" name="guests" class="booking-form-input">
                                            <option value="1">1 Guest (just me)</option>
                                            <option value="2">2 Guests</option>
                                            <option value="3">3 Guests</option>
                                            <option value="4">4 Guests</option>
                                        </select>
                                    </div>

                                    <div class="booking-form-group">
                                        <label class="booking-form-label" for="bNotes">Special Requests</label>
                                        <input type="text" id="bNotes" name="notes" class="booking-form-input"
                                            placeholder="Dietary needs, accessibility, etc.">
                                    </div>

                                    <button type="submit" class="booking-submit">
                                        ✦ Confirm Reservation
                                    </button>
                                </form>
                            </div>
                        </div>

                        <script>
                            // ── Booking Modal ──────────────────────────────────
                            function openBookingModal(loungeId, loungeName, location) {
                                document.getElementById('bLoungeId').value = loungeId;
                                document.getElementById('bModalTitle').textContent = loungeName;
                                document.getElementById('bModalSub').textContent = location;
                                // Set default date to today
                                const today = new Date().toISOString().split('T')[0];
                                document.getElementById('bArrival').value = today;
                                document.getElementById('bookingOverlay').classList.add('active');
                                document.body.style.overflow = 'hidden';
                            }

                            function closeBookingModalDirect() {
                                document.getElementById('bookingOverlay').classList.remove('active');
                                document.body.style.overflow = '';
                            }

                            function closeBookingModal(e) {
                                if (e.target === document.getElementById('bookingOverlay')) {
                                    closeBookingModalDirect();
                                }
                            }

                            document.addEventListener('keydown', (e) => {
                                if (e.key === 'Escape') closeBookingModalDirect();
                            });

                            function handleBookingSubmit(e) {
                                const btn = e.target.querySelector('.booking-submit');
                                btn.textContent = '⏳ Sending Request…';
                                btn.disabled = true;
                                // Allow form to submit naturally; we just give visual feedback
                                return true;
                            }

                            // ── Gauge (Reception / Admin) ──────────────────────
                            window.addEventListener('load', () => {
                                const gauge = document.getElementById('main-gauge');
                                if (gauge) {
                                    setTimeout(() => { gauge.style.strokeDashoffset = '65.3'; }, 500);
                                }

                                // Animate occupancy bars
                                document.querySelectorAll('.plc-occ-fill').forEach(bar => {
                                    const target = bar.style.width;
                                    bar.style.width = '0';
                                    setTimeout(() => { bar.style.width = target; }, 200);
                                });
                            });

                            // ── Mobile sidebar helper ──────────────────────────
                            function toggleSidebar() {
                                document.getElementById('app-sidebar').classList.toggle('open');
                            }
                        </script>
                </body>

                </html>