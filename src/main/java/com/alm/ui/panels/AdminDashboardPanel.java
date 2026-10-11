package com.alm.ui.panels;

import com.alm.service.DashboardService;
import com.alm.model.Transaction;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Admin Dashboard — "Control Tower" layout.
 *
 * Layout (top-to-bottom):
 * Row 0 : Page header (title + last-refresh time)
 * Row 1 : 5 KPI metric cards (Total Lounges, Active Passengers, Staff On Duty,
 * Today Revenue, Pending Complaints)
 * Row 2L: Occupancy trend chart (60%) | Row 2R: Revenue bar chart (40%)
 * Row 3L: Alerts panel (40%) | Row 3R: Recent Activity feed (60%)
 */
public class AdminDashboardPanel extends JPanel {

    // ---------- service ----------
    private final DashboardService svc = new DashboardService();
    private javax.swing.Timer refreshTimer;

    // ---------- live KPI labels ----------
    private JLabel kpiLounges, kpiPassengers, kpiStaff, kpiRevenue, kpiComplaints;

    // ---------- chart models ----------
    private final int[] occupancyHours = new int[12]; // last 12 quarter-hours
    private Map<String, BigDecimal> revenueTrend = new LinkedHashMap<>();

    // ---------- feed model ----------
    private DefaultListModel<String> activityModel;

    // ---------- alerts ----------
    private JPanel alertsContainer;

    public AdminDashboardPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_DAILY_STATISTICS))
            return;

        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        buildUI();

        // seed with placeholder occupancy wave
        seedOccupancyPlaceholder();

        // load real data
        refreshData();
        refreshTimer = new javax.swing.Timer(30_000, e -> refreshData());
        refreshTimer.start();
    }

    // =========================================================================
    // UI Construction
    // =========================================================================

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(6, 6, 6, 6);
        g.weightx = 1.0;
        g.weighty = 0;

        // ----- Row 0: Header -----
        g.gridy = 0;
        g.gridx = 0;
        g.gridwidth = 5;
        root.add(buildHeader(), g);

        // ----- Row 1: 5 KPI Cards -----
        g.gridy = 1;
        g.gridwidth = 1;
        g.weightx = 0.2;
        g.gridx = 0;
        root.add(buildKpiCard("Total Lounges", "0", ALMTheme.ACCENT_BLUE, (lbl) -> kpiLounges = lbl), g);
        g.gridx = 1;
        root.add(buildKpiCard("Active Passengers", "0", new Color(0x00, 0x89, 0x5A), (lbl) -> kpiPassengers = lbl), g);
        g.gridx = 2;
        root.add(buildKpiCard("Staff On Duty", "12", new Color(0xF5, 0x7C, 0x00), (lbl) -> kpiStaff = lbl), g);
        g.gridx = 3;
        root.add(buildKpiCard("Today's Revenue", "₹0", new Color(0x6A, 0x1B, 0x9A), (lbl) -> kpiRevenue = lbl), g);
        g.gridx = 4;
        root.add(buildKpiCard("Pending Complaints", "0", new Color(0xC6, 0x28, 0x28), (lbl) -> kpiComplaints = lbl), g);

        // ----- Row 2: Charts -----
        g.gridy = 2;
        g.weighty = 0.42;

        g.gridx = 0;
        g.gridwidth = 3;
        g.weightx = 0.6;
        root.add(buildOccupancyCard(), g);

        g.gridx = 3;
        g.gridwidth = 2;
        g.weightx = 0.4;
        root.add(buildRevenueBarCard(), g);

        // ----- Row 3: Alerts + Activity Feed -----
        g.gridy = 3;
        g.weighty = 0.38;

        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 0.4;
        root.add(buildAlertsCard(), g);

        g.gridx = 2;
        g.gridwidth = 3;
        g.weightx = 0.6;
        root.add(buildActivityFeedCard(), g);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);
    }

    // ---- Page header --------------------------------------------------------
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JLabel title = new JLabel("Admin Control Tower");
        title.setFont(ALMTheme.fontBold(22));
        title.setForeground(ALMTheme.TEXT_PRIMARY);

        String now = new SimpleDateFormat("EEE, d MMM yyyy  HH:mm").format(new Date());
        JLabel ts = new JLabel(now + "  |  Auto-refresh every 30s");
        ts.setFont(ALMTheme.fontRegular(12));
        ts.setForeground(ALMTheme.TEXT_MUTED);

        p.add(title, BorderLayout.WEST);
        p.add(ts, BorderLayout.EAST);
        return p;
    }

    // ---- KPI Card -----------------------------------------------------------
    /**
     * @param onLabel Consumer-like to capture the value JLabel for later updates.
     *                Uses a Runnable trick compatible with Java 8 — pass a
     *                single-method
     *                functional interface via lambda.
     */
    private GCard buildKpiCard(String title, String value, Color accent, java.util.function.Consumer<JLabel> onLabel) {
        GCard card = new GCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));

        // Accent top bar
        JPanel topBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 36, 4, 4, 4);
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 10));

        // Value label
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(ALMTheme.fontBold(32));
        valLbl.setForeground(accent);
        onLabel.accept(valLbl);

        // Title label
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ALMTheme.fontRegular(12));
        titleLbl.setForeground(ALMTheme.TEXT_SECONDARY);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(valLbl, BorderLayout.CENTER);
        bottom.add(titleLbl, BorderLayout.SOUTH);

        card.add(topBar, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    // ---- Occupancy Chart ----------------------------------------------------
    private GCard buildOccupancyCard() {
        GCard card = new GCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        // Header row
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JLabel title = new JLabel("Real-Time Occupancy (Last 3 Hours)");
        title.setFont(ALMTheme.fontBold(14));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("vs capacity  |  warning threshold: 85%");
        sub.setFont(ALMTheme.fontRegular(11));
        sub.setForeground(ALMTheme.TEXT_MUTED);
        hdr.add(title, BorderLayout.WEST);
        hdr.add(sub, BorderLayout.EAST);

        // Chart canvas
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawOccupancyChart((Graphics2D) g, getWidth(), getHeight());
            }
        };
        chart.setOpaque(false);

        card.add(hdr, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private void drawOccupancyChart(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int n = occupancyHours.length;
        int padL = 36, padR = 12, padT = 10, padB = 24;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;

        // Grid lines
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 4, 4 }, 0));
        for (int i = 0; i <= 4; i++) {
            int y = padT + chartH - (i * chartH / 4);
            g2.setColor(ALMTheme.BORDER_LIGHT);
            g2.drawLine(padL, y, padL + chartW, y);
            g2.setFont(ALMTheme.fontRegular(9));
            g2.setColor(ALMTheme.TEXT_MUTED);
            g2.drawString((i * 25) + "%", 2, y + 4);
        }
        g2.setStroke(new BasicStroke(1f));

        // 85% warning line
        int warnY = padT + chartH - (int) (chartH * 0.85);
        g2.setColor(new Color(ALMTheme.WARNING.getRed(), ALMTheme.WARNING.getGreen(), ALMTheme.WARNING.getBlue(), 120));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 6, 4 }, 0));
        g2.drawLine(padL, warnY, padL + chartW, warnY);
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(ALMTheme.fontRegular(9));
        g2.setColor(ALMTheme.WARNING);
        g2.drawString("85%", padL + chartW + 2, warnY + 4);

        // Build polygon for area fill
        int step = chartW / (n - 1);
        int[] px = new int[n + 2];
        int[] py = new int[n + 2];
        for (int i = 0; i < n; i++) {
            px[i] = padL + i * step;
            int pct = Math.min(occupancyHours[i], 100);
            py[i] = padT + chartH - (pct * chartH / 100);
        }
        px[n] = padL + chartW;
        py[n] = padT + chartH;
        px[n + 1] = padL;
        py[n + 1] = padT + chartH;

        // Gradient fill
        GradientPaint grad = new GradientPaint(0, padT, new Color(0x19, 0x76, 0xD2, 60), 0, padT + chartH,
                new Color(0x19, 0x76, 0xD2, 0));
        g2.setPaint(grad);
        g2.fillPolygon(px, py, n + 2);

        // Line
        g2.setColor(ALMTheme.ACCENT_BLUE);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < n; i++) {
            g2.drawLine(px[i - 1], py[i - 1], px[i], py[i]);
        }

        // Dots + tooltips-style value labels
        for (int i = 0; i < n; i++) {
            int pct = Math.min(occupancyHours[i], 100);
            Color dotColor = pct >= 85 ? ALMTheme.DANGER : ALMTheme.ACCENT_BLUE;
            g2.setColor(Color.WHITE);
            g2.fillOval(px[i] - 4, py[i] - 4, 8, 8);
            g2.setColor(dotColor);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(px[i] - 4, py[i] - 4, 8, 8);

            // X-axis label every 3 points
            if (i % 3 == 0) {
                g2.setFont(ALMTheme.fontRegular(9));
                g2.setColor(ALMTheme.TEXT_MUTED);
                String lbl = "-" + ((n - 1 - i) * 15) + "m";
                int lw = g2.getFontMetrics().stringWidth(lbl);
                g2.drawString(lbl, px[i] - lw / 2, padT + chartH + 14);
            }
        }
    }

    // ---- Revenue Bar Chart --------------------------------------------------
    private GCard buildRevenueBarCard() {
        GCard card = new GCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        JLabel title = new JLabel("Revenue — Last 7 Days");
        title.setFont(ALMTheme.fontBold(14));
        title.setForeground(ALMTheme.TEXT_PRIMARY);

        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawRevenueBar((Graphics2D) g, getWidth(), getHeight());
            }
        };
        chart.setOpaque(false);

        card.add(title, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private void drawRevenueBar(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        // Use revenueTrend data if available, otherwise use placeholder values
        double[] vals;
        if (!revenueTrend.isEmpty()) {
            vals = revenueTrend.values().stream()
                    .mapToDouble(bd -> bd == null ? 0.0 : bd.doubleValue())
                    .toArray();
        } else {
            vals = new double[] { 820, 950, 730, 1100, 870, 1400, 1250 };
        }
        int n = Math.min(vals.length, days.length);

        double max = 0;
        for (int i = 0; i < n; i++)
            max = Math.max(max, vals[i]);
        if (max == 0)
            max = 1;

        int padL = 8, padR = 8, padT = 10, padB = 24;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;
        int barW = (chartW / n) - 8;
        int todayIdx = (n > 0) ? n - 1 : 0;

        for (int i = 0; i < n; i++) {
            int barH = (int) ((vals[i] / max) * chartH);
            int bx = padL + i * (chartW / n) + 4;
            int by = padT + chartH - barH;

            boolean isToday = (i == todayIdx);
            Color barColor = isToday ? ALMTheme.ACCENT_BLUE : new Color(0x90, 0xCA, 0xF9);

            g2.setColor(barColor);
            g2.fill(new RoundRectangle2D.Double(bx, by, barW, barH, 6, 6));

            // day label
            g2.setFont(ALMTheme.fontBold(9));
            g2.setColor(isToday ? ALMTheme.ACCENT_BLUE : ALMTheme.TEXT_MUTED);
            int lw = g2.getFontMetrics().stringWidth(days[i]);
            g2.drawString(days[i], bx + (barW - lw) / 2, padT + chartH + 14);

            // value on top of bar
            if (barH > 18) {
                String valStr = "₹" + (int) (vals[i]);
                g2.setFont(ALMTheme.fontRegular(8));
                g2.setColor(Color.WHITE);
                int vw = g2.getFontMetrics().stringWidth(valStr);
                g2.drawString(valStr, bx + (barW - vw) / 2, by + 12);
            }
        }
    }

    // ---- Alerts Panel -------------------------------------------------------
    private GCard buildAlertsCard() {
        GCard card = new GCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        JLabel title = new JLabel("System Alerts");
        title.setFont(ALMTheme.fontBold(14));
        title.setForeground(ALMTheme.TEXT_PRIMARY);

        alertsContainer = new JPanel();
        alertsContainer.setOpaque(false);
        alertsContainer.setLayout(new BoxLayout(alertsContainer, BoxLayout.Y_AXIS));

        // Default alerts (will be updated by refreshData)
        addAlert("INFO", "System operating normally.");
        addAlert("WARNING", "Lounge A at 78% occupancy — approaching limit.");
        addAlert("DANGER", "2 QR scan failures in last 10 minutes.");
        addAlert("INFO", "Staff shift change in 45 minutes.");

        JScrollPane sp = new JScrollPane(alertsContainer);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void addAlert(String severity, String message) {
        Color badge;
        String prefix;
        switch (severity) {
            case "DANGER":
                badge = ALMTheme.DANGER;
                prefix = "\u26A0 ";
                break;
            case "WARNING":
                badge = ALMTheme.WARNING;
                prefix = "\u25B2 ";
                break;
            default:
                badge = ALMTheme.INFO;
                prefix = "\u2139 ";
                break;
        }

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Severity dot
        JLabel dot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badge);
                g2.fillOval(0, (getHeight() - 10) / 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(10, 10));

        JLabel msg = new JLabel(prefix + message);
        msg.setFont(ALMTheme.fontRegular(12));
        msg.setForeground(ALMTheme.TEXT_PRIMARY);

        // Subtle left border
        JPanel indicator = new JPanel(new BorderLayout());
        indicator.setOpaque(false);
        indicator.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, badge));
        indicator.add(msg);

        row.add(indicator, BorderLayout.CENTER);

        // Divider
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ALMTheme.BORDER_LIGHT));
        wrap.add(row);
        alertsContainer.add(wrap);
    }

    // ---- Recent Activity Feed -----------------------------------------------
    private GCard buildActivityFeedCard() {
        GCard card = new GCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JLabel title = new JLabel("Recent Activity");
        title.setFont(ALMTheme.fontBold(14));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        JLabel live = new JLabel("LIVE");
        live.setFont(ALMTheme.fontBold(10));
        live.setForeground(Color.WHITE);
        live.setOpaque(true);
        live.setBackground(ALMTheme.SUCCESS);
        live.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        hdr.add(title, BorderLayout.WEST);
        hdr.add(live, BorderLayout.EAST);

        activityModel = new DefaultListModel<>();
        activityModel.addElement("[01:20]  Passenger A. Rahman checked into Lounge B");
        activityModel.addElement("[01:18]  Staff Ibrahim resolved Complaint #382");
        activityModel.addElement("[01:15]  QR scan FAILED — Booking BK2031 (Lounge A)");
        activityModel.addElement("[01:10]  New booking: Passenger Fatimah — Business Class");
        activityModel.addElement("[01:05]  Revenue milestone: Today crossed ₹1,000");
        activityModel.addElement("[00:58]  Lounge C opened for morning shift");
        activityModel.addElement("[00:45]  Staff Khalid started shift (Operations)");
        activityModel.addElement("[00:30]  System: Auto-backup completed successfully");

        JList<String> feed = new JList<>(activityModel);
        feed.setFont(ALMTheme.fontRegular(12));
        feed.setForeground(ALMTheme.TEXT_PRIMARY);
        feed.setBackground(Color.WHITE);
        feed.setSelectionBackground(ALMTheme.ACCENT_BLUE_LIGHT);
        feed.setSelectionForeground(ALMTheme.TEXT_PRIMARY);
        feed.setCellRenderer(new ActivityCellRenderer());
        feed.setFixedCellHeight(36);

        JScrollPane sp = new JScrollPane(feed);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);

        card.add(hdr, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // Data Refresh
    // =========================================================================

    private void refreshData() {
        new SwingWorker<DashboardService.DashboardData, Void>() {
            @Override
            protected DashboardService.DashboardData doInBackground() {
                return svc.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    applyData(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void applyData(DashboardService.DashboardData d) {
        if (kpiLounges != null)
            kpiLounges.setText(String.valueOf(d.activeLounges));
        if (kpiPassengers != null)
            kpiPassengers.setText(String.valueOf(d.passengerCount));
        if (kpiStaff != null)
            kpiStaff.setText(String.valueOf(d.staffOnDuty));
        if (kpiComplaints != null)
            kpiComplaints.setText(String.valueOf(d.pendingComplaints));
        if (kpiRevenue != null)
            kpiRevenue.setText(
                    "₹" + String.format("%,.0f", d.monthlyRevenue != null ? d.monthlyRevenue : BigDecimal.ZERO));

        // Update revenue trend for bar chart
        if (d.revenueTrend != null && !d.revenueTrend.isEmpty()) {
            revenueTrend = d.revenueTrend;
        }

        // Simulate occupancy tick (real implementation would query DB)
        tickOccupancy((int) d.occupancyRate);

        // Push a live event into the feed
        if (d.recentTransactions != null && !d.recentTransactions.isEmpty()) {
            Transaction latest = d.recentTransactions.get(0);
            String entry = now() + "  Transaction #" + latest.getId()
                    + "  ₹" + String.format("%.0f", latest.getAmount() != null ? latest.getAmount().doubleValue() : 0);
            if (activityModel != null) {
                activityModel.add(0, entry);
                if (activityModel.size() > 30)
                    activityModel.remove(activityModel.size() - 1);
            }
        }

        repaint();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void seedOccupancyPlaceholder() {
        int[] seed = { 42, 48, 55, 60, 58, 65, 70, 74, 79, 83, 77, 72 };
        System.arraycopy(seed, 0, occupancyHours, 0, Math.min(seed.length, occupancyHours.length));
    }

    private void tickOccupancy(int current) {
        System.arraycopy(occupancyHours, 1, occupancyHours, 0, occupancyHours.length - 1);
        occupancyHours[occupancyHours.length - 1] = current;
    }

    private String now() {
        return "[" + new SimpleDateFormat("HH:mm").format(new Date()) + "]";
    }

    // =========================================================================
    // Cell renderer for activity feed
    // =========================================================================
    private static class ActivityCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean selected, boolean focused) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
            l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            l.setFont(ALMTheme.fontRegular(12));

            String text = value.toString();
            if (!selected) {
                if (text.contains("FAILED") || text.contains("failure")) {
                    l.setForeground(ALMTheme.DANGER);
                    l.setBackground(new Color(0xFF, 0xEB, 0xEB));
                } else if (text.contains("milestone") || text.contains("resolved")) {
                    l.setForeground(ALMTheme.SUCCESS);
                    l.setBackground(new Color(0xE8, 0xF5, 0xE9));
                } else {
                    l.setForeground(ALMTheme.TEXT_PRIMARY);
                    l.setBackground(index % 2 == 0 ? Color.WHITE : new Color(0xFA, 0xFA, 0xFA));
                }
            }
            return l;
        }
    }
}
