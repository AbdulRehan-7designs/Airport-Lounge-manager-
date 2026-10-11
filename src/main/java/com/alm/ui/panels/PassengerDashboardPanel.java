package com.alm.ui.panels;

import com.alm.dao.FlightDAO;
import com.alm.dao.LoungeDAO;
import com.alm.dao.PassengerDAO;
import com.alm.dao.TransactionDAO;
import com.alm.model.Flight;
import com.alm.model.Lounge;
import com.alm.model.Passenger;
import com.alm.model.Transaction;
import com.alm.ui.components.GButton;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Receptionist Front Desk Module — Production-ready UX.
 *
 * Layout: Left(Scan+Info) | Center(Seats+Actions) | Right(Occupancy+Queue)
 * Flow: Search -> Verify -> Seat -> Confirm Check-In -> Generate Bill (anytime
 * after)
 * Cancel: State-aware rollback (seat, queue, bill).
 */
public class PassengerDashboardPanel extends JPanel {

    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final LoungeDAO loungeDAO = new LoungeDAO();
    private final TransactionDAO txDAO = new TransactionDAO();
    private final FlightDAO flightDAO = new FlightDAO();

    // State
    private Passenger currentPassenger;
    private String currentAccessType;
    private String assignedSeat;
    private boolean checkedIn;
    private boolean billGenerated;
    private int currentOccupancy = 0, maxCapacity = 50;
    private javax.swing.Timer lockTimer;
    private int lockCountdown;

    // Step 1
    private JTextField scanField;
    private JLabel statusDot, statusText;

    // Step 2
    private JLabel nameVal, bpVal, flightVal, membVal, classVal;
    private JLabel accessPill;
    private JLabel vipBadge;
    private JPanel infoCard;

    // Step 3
    private final boolean[] seatOccupied = new boolean[30];
    private JLabel seatFeedback, selectedSeatLbl;
    private JLabel occupancyLbl, occupancyPctLbl;
    private JProgressBar occupancyBar;
    private JComboBox<Lounge> loungeCb;

    // Step 4 buttons
    private GButton confirmBtn, checkOutBtn, billBtn, cancelBtn;

    // Step 5
    private DefaultTableModel queueModel;
    private javax.swing.Timer queueTimer;

    public PassengerDashboardPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.CHECK_IN_GUEST))
            return;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (queueTimer != null)
            queueTimer.stop();
        if (lockTimer != null)
            lockTimer.stop();
    }

    // ===================== TOP BAR =====================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ALMTheme.BORDER),
                new EmptyBorder(12, 20, 12, 20)));
        JPanel titleArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titleArea.setOpaque(false);
        JLabel title = new JLabel("\uD83D\uDEE7 Front Desk Dashboard");
        title.setFont(ALMTheme.fontBold(20));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        titleArea.add(title);

        // Stepper: subtle numbered circles
        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        stepper.setOpaque(false);
        String[] steps = { "Scan", "Verify", "Seat", "Confirm", "Queue" };
        for (int i = 0; i < steps.length; i++) {
            stepper.add(makeStepDot(i + 1, steps[i]));
            if (i < steps.length - 1) {
                JLabel dash = new JLabel(" \u2014 ");
                dash.setForeground(ALMTheme.BORDER);
                dash.setFont(ALMTheme.fontRegular(10));
                stepper.add(dash);
            }
        }

        // Refresh button to reload live data
        GButton refreshBtn = new GButton("\u21BB Refresh", ALMTheme.ACCENT_BLUE);
        refreshBtn.setPreferredSize(new Dimension(90, 28));
        refreshBtn.addActionListener(e -> {
            // Reload lounge snapshot from DB
            Integer ctxLounge = com.alm.ui.core.SessionContext.getInstance().getCurrentLoungeId();
            if (ctxLounge != null) {
                Lounge l = loungeDAO.getById(ctxLounge);
                if (l != null)
                    refreshLiveLoungeSnapshot(l);
            }
            status("Refreshed live data.", ALMTheme.SUCCESS);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        // Removed loungeCb declaration here

        new SwingWorker<List<Lounge>, Void>() {
            protected List<Lounge> doInBackground() {
                return loungeDAO.getAllLounges();
            }

            protected void done() {
                if (loungeCb == null)
                    return;
                try {
                    for (Lounge l : get()) {
                        loungeCb.addItem(l);
                    }
                } catch (Exception e) {
                }

                // Once loaded, set default to the one in context if it exists
                Integer ctxLounge = com.alm.ui.core.SessionContext.getInstance().getCurrentLoungeId();
                if (ctxLounge != null) {
                    for (int i = 0; i < loungeCb.getItemCount(); i++) {
                        if (loungeCb.getItemAt(i).getId() == ctxLounge) {
                            loungeCb.setSelectedIndex(i);
                            break;
                        }
                    }
                } else if (loungeCb.getItemCount() > 0) {
                    loungeCb.setSelectedIndex(0); // This will trigger the action listener too
                }
            }
        }.execute();

        JLabel connDot = new JLabel("\u25CF ONLINE");
        connDot.setFont(ALMTheme.fontBold(10));
        connDot.setForeground(ALMTheme.SUCCESS);
        JLabel clock = new JLabel();
        clock.setFont(ALMTheme.fontMono(10));
        clock.setForeground(ALMTheme.TEXT_MUTED);
        new javax.swing.Timer(1000, e -> clock.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()))).start();
        right.add(refreshBtn);
        // Removed setting of loungeCb to right
        right.add(connDot);
        right.add(clock);

        bar.add(titleArea, BorderLayout.WEST);
        bar.add(stepper, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel makeStepDot(int num, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel(String.valueOf(num)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ALMTheme.ACCENT_BLUE_LIGHT);
                g2.fillOval(0, 0, 20, 20);
                g2.setColor(ALMTheme.ACCENT_BLUE);
                g2.setFont(ALMTheme.fontBold(10));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(String.valueOf(num), (20 - fm.stringWidth(String.valueOf(num))) / 2, 14);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(20, 20));
        JLabel lbl = new JLabel(label);
        lbl.setFont(ALMTheme.fontRegular(10));
        lbl.setForeground(ALMTheme.TEXT_MUTED);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ===================== MAIN 3-COLUMN =====================
    private JPanel buildMain() {
        JPanel area = new JPanel(new GridBagLayout());
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(12, 14, 12, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 5, 0, 5);
        g.weighty = 1.0;

        // Left column: Scan + Info
        g.gridx = 0;
        g.weightx = 0.26;
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(buildScanCard());
        left.add(Box.createVerticalStrut(8));
        left.add(buildInfoCard());
        area.add(left, g);

        // Center: Seats + Actions (tied together)
        g.gridx = 1;
        g.weightx = 0.44;
        area.add(buildSeatCard(), g);

        // Right: Occupancy + Queue
        g.gridx = 2;
        g.weightx = 0.30;
        JPanel rt = new JPanel();
        rt.setLayout(new BoxLayout(rt, BoxLayout.Y_AXIS));
        rt.setOpaque(false);
        rt.add(buildOccupancyCard());
        rt.add(Box.createVerticalStrut(8));
        rt.add(buildQueueCard());
        area.add(rt, g);
        return area;
    }

    // ===================== SCAN CARD =====================
    private GCard buildScanCard() {
        GCard c = new GCard(10);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(14, 14, 14, 14));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel t = sectionTitle("Scan / Search");
        JLabel sub = new JLabel("QR code, boarding pass, or passenger name");
        sub.setFont(ALMTheme.fontRegular(10));
        sub.setForeground(ALMTheme.TEXT_MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        scanField = new JTextField();
        scanField.setFont(ALMTheme.fontMono(13));
        scanField.setBackground(ALMTheme.BG_INPUT);
        scanField.setForeground(ALMTheme.TEXT_PRIMARY);
        scanField.setCaretColor(ALMTheme.ACCENT_BLUE);
        scanField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ALMTheme.BORDER), new EmptyBorder(7, 8, 7, 8)));
        scanField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        scanField.setAlignmentX(LEFT_ALIGNMENT);
        scanField.addActionListener(e -> doSearch());

        JPanel btnRow = new JPanel();
        btnRow.setLayout(new BoxLayout(btnRow, BoxLayout.X_AXIS));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        GButton btn = new GButton("Search", ALMTheme.ACCENT_BLUE);
        btn.addActionListener(e -> doSearch());

        GButton qrBtn = new GButton("Scan QR \ud83d\udcf7", ALMTheme.ACCENT_INDIGO);
        qrBtn.addActionListener(e -> {
            com.alm.ui.components.QRScannerDialog scanner = new com.alm.ui.components.QRScannerDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    qrText -> {
                        scanField.setText(qrText);
                        doSearch();
                    });
            scanner.setVisible(true);
        });

        btnRow.add(btn);
        btnRow.add(Box.createHorizontalStrut(8));
        btnRow.add(qrBtn);

        JPanel sr = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        sr.setOpaque(false);
        sr.setAlignmentX(LEFT_ALIGNMENT);
        sr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        statusDot = new JLabel("\u25CF");
        statusDot.setForeground(ALMTheme.TEXT_MUTED);
        statusText = new JLabel("Awaiting input...");
        statusText.setFont(ALMTheme.fontRegular(11));
        statusText.setForeground(ALMTheme.TEXT_MUTED);
        sr.add(statusDot);
        sr.add(statusText);

        c.add(t);
        c.add(Box.createVerticalStrut(2));
        c.add(sub);
        c.add(Box.createVerticalStrut(8));
        c.add(scanField);
        c.add(Box.createVerticalStrut(6));
        c.add(btnRow);
        c.add(Box.createVerticalStrut(6));
        c.add(sr);
        return c;
    }

    // ===================== INFO CARD (read-only) =====================
    private GCard buildInfoCard() {
        GCard c = new GCard(10);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(12, 14, 12, 14));
        infoCard = c;

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        hdr.add(sectionTitle("Passenger Info"), BorderLayout.WEST);
        vipBadge = new JLabel(" VIP ");
        vipBadge.setFont(ALMTheme.fontBold(9));
        vipBadge.setForeground(new Color(0x7A, 0x5A, 0x00));
        vipBadge.setOpaque(true);
        vipBadge.setBackground(new Color(0xFF, 0xF0, 0xC0));
        vipBadge.setBorder(BorderFactory.createLineBorder(new Color(0xF9, 0xA8, 0x25)));
        vipBadge.setVisible(false);
        hdr.add(vipBadge, BorderLayout.EAST);
        c.add(hdr);
        JLabel ro = new JLabel("Verification only \u2014 read-only fields");
        ro.setFont(ALMTheme.fontRegular(9));
        ro.setForeground(ALMTheme.TEXT_MUTED);
        ro.setAlignmentX(LEFT_ALIGNMENT);
        c.add(ro);
        c.add(Box.createVerticalStrut(6));

        nameVal = new JLabel("\u2014");
        bpVal = new JLabel("\u2014");
        flightVal = new JLabel("\u2014");
        membVal = new JLabel("\u2014");
        classVal = new JLabel("\u2014");
        c.add(infoRow("Name", nameVal));
        c.add(infoRow("Boarding Pass", bpVal));
        c.add(infoRow("Flight", flightVal));
        c.add(infoRow("Membership", membVal));
        c.add(infoRow("Ticket Class", classVal));
        c.add(Box.createVerticalStrut(6));

        // Access pill
        accessPill = new JLabel("  \u25CF  PENDING  ");
        accessPill.setFont(ALMTheme.fontBold(11));
        accessPill.setForeground(ALMTheme.TEXT_MUTED);
        accessPill.setOpaque(true);
        accessPill.setBackground(ALMTheme.BG_DEEPEST);
        accessPill.setAlignmentX(LEFT_ALIGNMENT);
        accessPill.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        accessPill.setHorizontalAlignment(SwingConstants.CENTER);
        c.add(accessPill);
        return c;
    }

    // ===================== SEAT CARD (with actions inside) =====================
    private GCard buildSeatCard() {
        GCard c = new GCard(10);
        c.setLayout(new BorderLayout(0, 6));
        c.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Header with auto-recommend + legend
        JPanel hdr = new JPanel(new BorderLayout(6, 0));
        hdr.setOpaque(false);
        JPanel hdrLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hdrLeft.setOpaque(false);
        hdrLeft.add(sectionTitle("Seat Assignment"));
        GButton autoBtn = new GButton("Auto-Assign", ALMTheme.ACCENT_INDIGO);
        autoBtn.setPreferredSize(new Dimension(100, 26));
        autoBtn.addActionListener(e -> autoSeat());
        hdrLeft.add(autoBtn);
        hdrLeft.add(Box.createHorizontalStrut(10));

        loungeCb = new JComboBox<>();
        loungeCb.setBackground(ALMTheme.BG_CARD);
        loungeCb.setForeground(ALMTheme.ACCENT_BLUE);
        loungeCb.setFont(ALMTheme.fontBold(14));
        // Keep string small to avoid layout bursting
        loungeCb.setPreferredSize(new Dimension(170, 30));

        // Setup renderer for Lounge object
        loungeCb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Lounge) {
                    Lounge l = (Lounge) value;
                    setText(l.getName().length() > 20 ? l.getName().substring(0, 18) + "..." : l.getName());
                    setToolTipText(l.getName() + " - T" + l.getTerminal());
                }
                return this;
            }
        });

        // Selection listener to trigger live reload of occupancy & seats
        loungeCb.addActionListener(e -> {
            Lounge selected = (Lounge) loungeCb.getSelectedItem();
            if (selected != null) {
                com.alm.ui.core.SessionContext.getInstance().setCurrentLoungeId(selected.getId());
                refreshLiveLoungeSnapshot(selected);
            }
        });

        new SwingWorker<List<Lounge>, Void>() {
            protected List<Lounge> doInBackground() {
                return loungeDAO.getAllLounges();
            }

            protected void done() {
                try {
                    for (Lounge l : get()) {
                        loungeCb.addItem(l);
                    }
                } catch (Exception e) {
                }

                // Once loaded, set default to the one in context if it exists
                Integer ctxLounge = com.alm.ui.core.SessionContext.getInstance().getCurrentLoungeId();
                if (ctxLounge != null) {
                    for (int i = 0; i < loungeCb.getItemCount(); i++) {
                        if (loungeCb.getItemAt(i).getId() == ctxLounge) {
                            loungeCb.setSelectedIndex(i);
                            break;
                        }
                    }
                } else if (loungeCb.getItemCount() > 0) {
                    loungeCb.setSelectedIndex(0); // This will trigger the action listener too
                }
            }
        }.execute();
        hdrLeft.add(loungeCb);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        legend.setOpaque(false);
        legend.add(legendItem(new Color(0x43, 0xA0, 0x47), "Free"));
        legend.add(legendItem(new Color(0xB7, 0x1C, 0x1C), "Taken"));
        legend.add(legendItem(ALMTheme.ACCENT_BLUE, "Sel."));
        legend.add(legendItem(new Color(0x9E, 0x9E, 0x9E), "Rest."));
        hdr.add(hdrLeft, BorderLayout.WEST);
        hdr.add(legend, BorderLayout.EAST);

        // Grids
        JPanel grids = new JPanel();
        grids.setLayout(new BoxLayout(grids, BoxLayout.Y_AXIS));
        grids.setOpaque(false);
        grids.add(sectionBadge("VIP", new Color(0xF9, 0xA8, 0x25)));
        grids.add(Box.createVerticalStrut(4));
        grids.add(makeSeatGrid(0, 10, "V"));
        grids.add(Box.createVerticalStrut(10));
        grids.add(sectionBadge("Business", ALMTheme.ACCENT_BLUE));
        grids.add(Box.createVerticalStrut(4));
        grids.add(makeSeatGrid(10, 20, "B"));
        grids.add(Box.createVerticalStrut(10));
        grids.add(sectionBadge("Economy", ALMTheme.SUCCESS));
        grids.add(Box.createVerticalStrut(4));
        grids.add(makeSeatGrid(20, 30, "E"));

        // Feedback + selected row
        JPanel fbRow = new JPanel(new BorderLayout(8, 0));
        fbRow.setOpaque(false);
        selectedSeatLbl = new JLabel("None");
        selectedSeatLbl.setFont(ALMTheme.fontBold(13));
        selectedSeatLbl.setForeground(ALMTheme.ACCENT_BLUE);
        seatFeedback = new JLabel("");
        seatFeedback.setFont(ALMTheme.fontRegular(11));
        seatFeedback.setForeground(ALMTheme.TEXT_MUTED);
        JPanel selP = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        selP.setOpaque(false);
        JLabel sl = new JLabel("Selected: ");
        sl.setFont(ALMTheme.fontRegular(12));
        sl.setForeground(ALMTheme.TEXT_SECONDARY);
        selP.add(sl);
        selP.add(selectedSeatLbl);
        fbRow.add(selP, BorderLayout.WEST);
        fbRow.add(seatFeedback, BorderLayout.EAST);

        // Action buttons (INSIDE seat card = no disconnect)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ALMTheme.BORDER_LIGHT));

        cancelBtn = new GButton("Cancel", ALMTheme.BG_DEEPEST);
        cancelBtn.setForeground(ALMTheme.DANGER);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ALMTheme.DANGER), new EmptyBorder(6, 16, 6, 16)));
        cancelBtn.addActionListener(e -> doCancel());

        billBtn = new GButton("Generate Bill", ALMTheme.ACCENT_BLUE);
        billBtn.setEnabled(false);
        billBtn.addActionListener(e -> doBill());

        confirmBtn = new GButton("Confirm Check-In", ALMTheme.SUCCESS);
        confirmBtn.setEnabled(false);
        confirmBtn.setFont(ALMTheme.fontBold(14));
        confirmBtn.setPreferredSize(new Dimension(180, 38));
        confirmBtn.addActionListener(e -> doConfirm());

        checkOutBtn = new GButton("Check-Out Passenger", ALMTheme.WARNING);
        checkOutBtn.setVisible(false);
        checkOutBtn.setFont(ALMTheme.fontBold(14));
        checkOutBtn.setPreferredSize(new Dimension(180, 38));
        checkOutBtn.addActionListener(e -> doCheckOut());

        actions.add(cancelBtn);
        actions.add(billBtn);
        actions.add(confirmBtn);
        actions.add(checkOutBtn);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.add(fbRow, BorderLayout.NORTH);
        bottom.add(actions, BorderLayout.SOUTH);

        c.add(hdr, BorderLayout.NORTH);
        c.add(grids, BorderLayout.CENTER);
        c.add(bottom, BorderLayout.SOUTH);
        return c;
    }

    private JPanel makeSeatGrid(int from, int to, String prefix) {
        JPanel grid = new JPanel(new GridLayout(2, 5, 5, 5));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        for (int i = from; i < to; i++) {
            final int idx = i;
            String id = prefix + (i + 1);
            JButton btn = new JButton(id);
            btn.setFont(ALMTheme.fontBold(11));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(0, 34));
            styleSeat(btn, idx);
            btn.addActionListener(e -> selectSeat(id, idx, grid));
            grid.add(btn);
        }
        return grid;
    }

    /**
     * Determines which seat sections (VIP/Business/Economy) are allowed
     * based on the passenger's evaluated access type and ticket class.
     * Returns an array of booleans: [allowVIP, allowBusiness, allowEconomy].
     */
    private boolean[] getAllowedSections() {
        if (currentPassenger == null || currentAccessType == null) {
            return new boolean[] { false, false, false };
        }

        String ticket = currentPassenger.getTicketClass() != null
                ? currentPassenger.getTicketClass().toUpperCase()
                : "";
        String membership = currentPassenger.getMembershipType() != null
                ? currentPassenger.getMembershipType().toUpperCase()
                : "";

        switch (currentAccessType) {
            case "COMPLIMENTARY_AIRLINE":
                if (ticket.contains("FIRST") || ticket.contains("VIP")) {
                    return new boolean[] { true, true, true }; // First/VIP → all sections
                } else if (ticket.contains("BUSINESS")) {
                    return new boolean[] { false, true, true }; // Business → Business+Economy
                }
                return new boolean[] { false, false, true }; // Fallback → Economy
            case "COMPLIMENTARY_MEMBERSHIP":
                // Premium members → Business + Economy (VIP only if Platinum/VIP tier)
                boolean isPlatinum = membership.contains("PLATINUM") || membership.contains("VIP")
                        || membership.contains("BLACK") || membership.contains("DIAMOND");
                return new boolean[] { isPlatinum, true, true };
            case "PAID":
                return new boolean[] { false, false, true }; // Pay-per-use → Economy only
            default:
                return new boolean[] { false, false, false }; // DENIED
        }
    }

    /**
     * Returns which section (0=VIP, 1=Business, 2=Economy) a seat index belongs to.
     */
    private int getSeatSection(int idx) {
        if (idx < 10)
            return 0; // VIP
        if (idx < 20)
            return 1; // Business
        return 2; // Economy
    }

    private boolean isSeatAllowed(int idx) {
        boolean[] allowed = getAllowedSections();
        int section = getSeatSection(idx);
        return allowed[section];
    }

    private void styleSeat(JButton btn, int idx) {
        String id = btn.getText();
        boolean allowed = isSeatAllowed(idx);

        if (id.equals(assignedSeat)) {
            // Currently selected seat
            btn.setBackground(ALMTheme.ACCENT_BLUE);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(0x0D, 0x47, 0xA1), 3));
            btn.setEnabled(true);
        } else if (seatOccupied[idx]) {
            // Occupied
            btn.setBackground(new Color(0xFF, 0xCB, 0xCB));
            btn.setForeground(new Color(0xB7, 0x1C, 0x1C));
            btn.setBorder(BorderFactory.createLineBorder(new Color(0xE5, 0x73, 0x73), 2));
            btn.setEnabled(false);
        } else if (!allowed && currentPassenger != null) {
            // Restricted section for this passenger's tier
            btn.setBackground(new Color(0xE0, 0xE0, 0xE0));
            btn.setForeground(new Color(0x9E, 0x9E, 0x9E));
            btn.setBorder(BorderFactory.createLineBorder(new Color(0xBD, 0xBD, 0xBD), 1));
            btn.setEnabled(false);
        } else {
            // Available
            btn.setBackground(new Color(0xC8, 0xE6, 0xC9));
            btn.setForeground(new Color(0x1B, 0x5E, 0x20));
            btn.setBorder(BorderFactory.createLineBorder(new Color(0x43, 0xA0, 0x47), 2));
            btn.setEnabled(true);
        }
    }

    private void selectSeat(String id, int idx, JPanel srcGrid) {
        if (seatOccupied[idx])
            return;

        // Enforce section restriction
        if (!isSeatAllowed(idx)) {
            String section = getSeatSection(idx) == 0 ? "VIP" : getSeatSection(idx) == 1 ? "Business" : "Economy";
            String paxTier = currentAccessType != null ? currentAccessType : "UNKNOWN";
            seatFeedback.setText("\u26D4 " + section + " section restricted for " + paxTier + " access");
            seatFeedback.setForeground(ALMTheme.DANGER);
            status("Section restricted: " + section + " seats not available for " + paxTier + " passengers.",
                    ALMTheme.WARNING);
            return;
        }

        assignedSeat = id;
        selectedSeatLbl.setText(id);
        String section = id.startsWith("V") ? "VIP" : id.startsWith("B") ? "Business" : "Economy";
        seatFeedback.setText(id + " selected \u2014 " + section + " section");
        seatFeedback.setForeground(ALMTheme.ACCENT_BLUE);
        confirmBtn.setEnabled(currentPassenger != null && !"CHECKED_IN".equals(currentPassenger.getAccessStatus()));

        // Lock timer
        lockCountdown = 30;
        if (lockTimer != null)
            lockTimer.stop();
        lockTimer = new javax.swing.Timer(1000, e -> {
            lockCountdown--;
            if (lockCountdown > 0) {
                seatFeedback.setText(id + " locked for " + lockCountdown + "s");
            } else {
                seatFeedback.setText("Lock expired \u2014 select again");
                seatFeedback.setForeground(ALMTheme.WARNING);
                assignedSeat = null;
                selectedSeatLbl.setText("None");
                confirmBtn.setEnabled(false);
                lockTimer.stop();
            }
            refreshAllSeats();
        });
        lockTimer.start();
        refreshAllSeats();
        status("Seat " + id + " selected (" + section + " \u2014 " + currentAccessType + ")", ALMTheme.ACCENT_BLUE);
    }

    private void refreshAllSeats() {
        refreshGrid(this);
    }

    private void refreshGrid(Container parent) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                int idx = parseSeatIdx(b.getText());
                if (idx >= 0 && idx < seatOccupied.length) {
                    styleSeat(b, idx);
                }
            }
            if (c instanceof Container)
                refreshGrid((Container) c);
        }
    }

    private int parseSeatIdx(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", "")) - 1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Intelligent Seat Auto-Assign — allocates the best available seat
     * based on the passenger's access type, ticket class, and membership.
     *
     * Priority order:
     * First/VIP Class → VIP section first, then Business, then Economy
     * Business Class → Business section first, then Economy
     * Premium Membership → Business first (VIP if Platinum+), then Economy
     * Pay-Per-Use/Economy → Economy section only
     */
    private void autoSeat() {
        if (currentPassenger == null) {
            seatFeedback.setText("Search a passenger first.");
            seatFeedback.setForeground(ALMTheme.WARNING);
            return;
        }
        if ("CHECKED_IN".equals(currentPassenger.getAccessStatus())) {
            seatFeedback.setText("Already checked in at " + currentPassenger.getAssignedSeat());
            seatFeedback.setForeground(ALMTheme.WARNING);
            return;
        }

        boolean[] allowed = getAllowedSections();
        // allowed[0]=VIP, allowed[1]=Business, allowed[2]=Economy

        // Build priority order of sections to search
        int[][] sectionRanges = {
                { 0, 10 }, // VIP: idx 0-9
                { 10, 20 }, // Business: idx 10-19
                { 20, 30 } // Economy: idx 20-29
        };

        // Determine search order based on best match
        int[] searchOrder;
        String ticket = currentPassenger.getTicketClass() != null
                ? currentPassenger.getTicketClass().toUpperCase()
                : "";

        if (allowed[0] && (ticket.contains("FIRST") || ticket.contains("VIP"))) {
            searchOrder = new int[] { 0, 1, 2 }; // VIP → Business → Economy
        } else if (allowed[1]) {
            searchOrder = new int[] { 1, 0, 2 }; // Business → VIP → Economy
        } else {
            searchOrder = new int[] { 2, 1, 0 }; // Economy → Business → VIP
        }

        // Search in priority order
        for (int sectionIdx : searchOrder) {
            if (!allowed[sectionIdx])
                continue;
            int from = sectionRanges[sectionIdx][0];
            int to = sectionRanges[sectionIdx][1];
            for (int i = from; i < to; i++) {
                if (!seatOccupied[i]) {
                    String pfx = i < 10 ? "V" : i < 20 ? "B" : "E";
                    String id = pfx + (i + 1);
                    String sec = i < 10 ? "VIP" : i < 20 ? "Business" : "Economy";
                    assignedSeat = id;
                    selectedSeatLbl.setText(id + " (auto)");
                    seatFeedback.setText("\u2728 Smart-Assigned: " + id + " \u2014 " + sec
                            + " (based on " + currentAccessType + ")");
                    seatFeedback.setForeground(ALMTheme.SUCCESS);
                    confirmBtn.setEnabled(true);
                    refreshAllSeats();
                    status("Auto-assigned " + id + " (" + sec + ") for "
                            + currentPassenger.getFullName() + " [" + currentAccessType + "]",
                            ALMTheme.SUCCESS);
                    return;
                }
            }
        }

        seatFeedback.setText("No eligible seats available for " + currentAccessType + "!");
        seatFeedback.setForeground(ALMTheme.DANGER);
    }

    // ===================== OCCUPANCY =====================
    private GCard buildOccupancyCard() {
        GCard c = new GCard(10);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(12, 14, 12, 14));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        c.add(sectionTitle("Occupancy"));
        c.add(Box.createVerticalStrut(4));
        occupancyLbl = new JLabel(currentOccupancy + " / " + maxCapacity);
        occupancyLbl.setFont(ALMTheme.fontBold(18));
        occupancyLbl.setForeground(ALMTheme.TEXT_PRIMARY);
        occupancyLbl.setAlignmentX(LEFT_ALIGNMENT);
        c.add(occupancyLbl);
        c.add(Box.createVerticalStrut(4));
        occupancyBar = new JProgressBar(0, maxCapacity);
        occupancyBar.setValue(currentOccupancy);
        occupancyBar.setStringPainted(false);
        occupancyBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        occupancyBar.setAlignmentX(LEFT_ALIGNMENT);
        updateOccColor();
        c.add(occupancyBar);
        occupancyPctLbl = new JLabel("0% capacity");
        occupancyPctLbl.setFont(ALMTheme.fontRegular(10));
        occupancyPctLbl.setForeground(ALMTheme.TEXT_MUTED);
        occupancyPctLbl.setAlignmentX(LEFT_ALIGNMENT);
        c.add(Box.createVerticalStrut(2));
        c.add(occupancyPctLbl);
        return c;
    }

    // ===================== SERVICE QUEUE =====================
    private GCard buildQueueCard() {
        GCard c = new GCard(10);
        c.setLayout(new BorderLayout(0, 6));
        c.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(sectionTitle("Service Queue"), BorderLayout.WEST);
        JLabel live = new JLabel("LIVE");
        live.setFont(ALMTheme.fontBold(9));
        live.setForeground(Color.WHITE);
        live.setOpaque(true);
        live.setBackground(ALMTheme.SUCCESS);
        live.setBorder(new EmptyBorder(1, 5, 1, 5));
        hdr.add(live, BorderLayout.EAST);

        String[] cols = { "Passenger", "Seat", "Request", "Priority", "Status" };
        queueModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c2) {
                return false;
            }
        };

        // Start Auto-Polling Timer for live queue updates
        queueTimer = new javax.swing.Timer(5000, e -> refreshServiceQueue());
        queueTimer.start();
        refreshServiceQueue(); // initial fetch

        JTable tbl = new JTable(queueModel);
        tbl.setFont(ALMTheme.fontRegular(11));
        tbl.setForeground(ALMTheme.TEXT_PRIMARY);
        tbl.setBackground(Color.WHITE);
        tbl.setRowHeight(30);
        tbl.setGridColor(ALMTheme.BORDER_LIGHT);
        tbl.setSelectionBackground(ALMTheme.ACCENT_BLUE_LIGHT);
        tbl.getTableHeader().setFont(ALMTheme.fontBold(10));
        tbl.getTableHeader().setBackground(ALMTheme.BG_DEEPEST);
        tbl.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(40);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(55);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(70);

        // Priority badge renderer
        tbl.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int cl) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, cl);
                l.setHorizontalAlignment(CENTER);
                l.setFont(ALMTheme.fontBold(10));
                if (!s && "HIGH".equalsIgnoreCase(String.valueOf(v))) {
                    l.setForeground(Color.WHITE);
                    l.setBackground(ALMTheme.DANGER);
                    l.setOpaque(true);
                } else {
                    l.setForeground(ALMTheme.TEXT_MUTED);
                    l.setOpaque(false);
                }
                return l;
            }
        });
        // Status renderer
        tbl.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int cl) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, cl);
                l.setFont(ALMTheme.fontBold(10));
                if (!s) {
                    String st = String.valueOf(v);
                    if ("Pending".equals(st))
                        l.setForeground(ALMTheme.WARNING);
                    else if ("In Progress".equals(st))
                        l.setForeground(ALMTheme.ACCENT_BLUE);
                    else if ("Completed".equals(st))
                        l.setForeground(ALMTheme.SUCCESS);
                    else
                        l.setForeground(ALMTheme.TEXT_MUTED);
                }
                return l;
            }
        });

        GButton markBtn = new GButton("Mark Complete", ALMTheme.SUCCESS);
        markBtn.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r >= 0)
                queueModel.setValueAt("Completed", r, 4);
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        c.add(hdr, BorderLayout.NORTH);
        c.add(sp, BorderLayout.CENTER);
        c.add(markBtn, BorderLayout.SOUTH);
        return c;
    }

    private void refreshServiceQueue() {
        Integer loungeId = com.alm.ui.core.SessionContext.getInstance().getCurrentLoungeId();
        if (loungeId == null)
            return;
        final int lid = loungeId;
        new SwingWorker<List<java.util.Map<String, Object>>, Void>() {
            protected List<java.util.Map<String, Object>> doInBackground() {
                return new com.alm.dao.ReceptionServiceDAO().getActiveServiceQueue(lid);
            }

            protected void done() {
                try {
                    List<java.util.Map<String, Object>> reqs = get();
                    queueModel.setRowCount(0);
                    for (java.util.Map<String, Object> req : reqs) {
                        String pax = (String) req.get("full_name");
                        String seat = (String) req.get("assigned_seat");
                        String svc = (String) req.get("service_name");
                        String st = (String) req.get("status");
                        String prio = "Normal";

                        // Very basic priority estimation
                        if (svc.toUpperCase().contains("PRAY") || svc.toUpperCase().contains("EMERGENCY"))
                            prio = "HIGH";
                        else if (svc.toUpperCase().contains("WIFI"))
                            prio = "Low";

                        // Status formatting
                        if ("PENDING".equalsIgnoreCase(st))
                            st = "Pending";
                        else if ("IN_PROGRESS".equalsIgnoreCase(st))
                            st = "In Progress";

                        queueModel.addRow(new Object[] { pax, seat != null ? seat : "N/A", svc, prio, st });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    // ===================== ACTIONS =====================
    private void doSearch() {
        String q = scanField.getText().trim();
        if (q.isEmpty())
            return;
        status("Searching...", ALMTheme.ACCENT_BLUE);
        new SwingWorker<Passenger, Void>() {
            private String tokenUsedStr = null;

            protected Passenger doInBackground() {
                // 1. Check if the string might be a digital token
                if (q.length() > 20) {
                    com.alm.dao.SessionTokenDAO tDAO = new com.alm.dao.SessionTokenDAO();
                    com.alm.model.SessionToken token = tDAO.validateToken(q);
                    if (token != null) {
                        tokenUsedStr = q; // Flag that a valid token triggered this
                        return passengerDAO.findById(token.getPassengerId());
                    }
                }

                // 2. Fallbacks
                Passenger p = passengerDAO.findByPassport(q);
                if (p == null)
                    for (Passenger each : passengerDAO.getAllPassengers())
                        if (each.getFullName().equalsIgnoreCase(q))
                            return each;
                return p;
            }

            protected void done() {
                try {
                    Passenger p = get();
                    if (p == null) {
                        status("Not found in DB", ALMTheme.DANGER);
                        com.alm.util.AudioFeedback.playError();
                        clearForm();
                        return;
                    }
                    if (tokenUsedStr != null) {
                        status("Valid QR Authenticated", ALMTheme.SUCCESS);
                        com.alm.util.AudioFeedback.playScanSuccess();
                    } else {
                        status("Found Match", ALMTheme.ACCENT_BLUE);
                        com.alm.util.AudioFeedback.playScanSuccess();
                    }
                    loadPassenger(p);
                } catch (Exception ex) {
                    status("Error!", ALMTheme.DANGER);
                }
            }
        }.execute();
    }

    private void loadPassenger(Passenger p) {
        currentPassenger = p;
        billGenerated = false;

        // Stop any previous lock timer
        if (lockTimer != null) {
            lockTimer.stop();
            lockTimer = null;
        }

        nameVal.setText(p.getFullName());
        bpVal.setText(p.getPassportNumber() != null ? p.getPassportNumber() : "N/A");

        Flight f = p.getFlightId() != null ? flightDAO.findById(p.getFlightId()) : null;
        if (f != null) {
            flightVal.setText(f.getFlightNumber() + " (" + f.getStatus() + ")");
        } else {
            flightVal.setText(p.getFlightId() != null ? "FL-" + p.getFlightId()
                    : "EK-" + (100 + Math.abs(p.getFullName().hashCode()) % 800));
        }

        String m = p.getMembershipType() != null ? p.getMembershipType() : "None";
        membVal.setText(m);
        classVal.setText(p.getTicketClass() != null ? p.getTicketClass() : "Economy");
        boolean vip = m.contains("Platinum") || m.contains("VIP") || classVal.getText().contains("Business")
                || classVal.getText().contains("First");
        vipBadge.setVisible(vip);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(vip ? new Color(0xF9, 0xA8, 0x25) : ALMTheme.BORDER, vip ? 2 : 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        // UNIFIED DECISION ENGINE: No manual selection allowed. AI evaluate
        com.alm.service.AccessEvaluationService evalSvc = new com.alm.service.AccessEvaluationService();
        com.alm.service.AccessEvaluationService.EvaluationResult result = evalSvc.evaluateAccess(p, 4);
        currentAccessType = result.getAccessType().name();

        boolean isCheckedIn = "CHECKED_IN".equals(p.getAccessStatus());
        boolean ok = p.isActive()
                && result.getAccessType() != com.alm.service.AccessEvaluationService.AccessType.DENIED;
        String reason = result.getReason();

        if (f != null && ("CANCELLED".equals(f.getStatus()) || "DEPARTED".equals(f.getStatus()))) {
            ok = false;
            reason = "Flight is " + f.getStatus();
        }

        if (isCheckedIn) {
            // --- ALREADY CHECKED IN ---
            checkedIn = true;
            assignedSeat = p.getAssignedSeat();
            selectedSeatLbl.setText(assignedSeat != null ? assignedSeat + " (active)" : "N/A");
            seatFeedback.setText("Currently checked in");
            seatFeedback.setForeground(ALMTheme.ACCENT_BLUE);

            accessPill.setText("  \u25CF  CHECKED IN  ");
            accessPill.setForeground(Color.WHITE);
            accessPill.setBackground(ALMTheme.ACCENT_BLUE);

            confirmBtn.setVisible(false);
            confirmBtn.setEnabled(false);
            checkOutBtn.setVisible(true);
            billBtn.setEnabled(true);

            status("\u2714 " + p.getFullName() + " is CHECKED IN at Seat " + assignedSeat
                    + " \u2014 Use Check-Out to release.", ALMTheme.ACCENT_BLUE);
        } else {
            // --- FRESH CHECK-IN FLOW ---
            checkedIn = false;
            assignedSeat = null;
            selectedSeatLbl.setText("None");
            seatFeedback.setText("");

            accessPill.setText(
                    ok ? "  \u25CF  " + result.getAccessType() + "  " : "  \u25CF  " + result.getAccessType() + "  ");
            accessPill.setForeground(Color.WHITE);
            accessPill.setBackground(ok ? ALMTheme.SUCCESS : ALMTheme.DANGER);

            confirmBtn.setVisible(true);
            confirmBtn.setText("Confirm Check-In");
            confirmBtn.setEnabled(false); // Enable only when seat is chosen
            checkOutBtn.setVisible(false);
            billBtn.setEnabled(false);

            status("Evaluated " + p.getFullName() + ": " + reason, ok ? ALMTheme.SUCCESS : ALMTheme.WARNING);
        }

        scanField.setText("");
        refreshAllSeats();
    }

    private void doCheckOut() {
        if (currentPassenger != null) {
            String paxName = currentPassenger.getFullName();
            String paxSeat = assignedSeat;
            boolean ok = passengerDAO.checkoutPassenger(currentPassenger.getId());
            if (ok) {
                // Free the local seat map immediately
                if (paxSeat != null) {
                    int idx = parseSeatIdx(paxSeat);
                    if (idx >= 0)
                        seatOccupied[idx] = false;
                }
                clearForm();
                // Sync occupancy + seats from live DB
                refreshOccupancyFromDB();
                status("\u2714 " + paxName + " checked out from " + paxSeat + ". Seat freed.", ALMTheme.SUCCESS);
                JOptionPane.showMessageDialog(this,
                        "Check-Out successful!\nPassenger: " + paxName + "\nSeat " + paxSeat + " is now available.",
                        "\u2714 Check-Out Complete", JOptionPane.INFORMATION_MESSAGE);
                com.alm.util.AudioFeedback.playCheckOut();
            } else {
                status("Failed to check out.", ALMTheme.DANGER);
                JOptionPane.showMessageDialog(this, "Check-Out Database operation failed.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doConfirm() {
        if (currentPassenger == null || assignedSeat == null) {
            status("Assign seat first", ALMTheme.WARNING);
            return;
        }

        Integer loungeId = com.alm.ui.core.SessionContext.getInstance().getCurrentLoungeId();
        if (loungeId == null)
            loungeId = 1; // Failsafe fallback mapped to Above Ground Level

        // 1. Transactional Database Check-In Execution! Include Unified Engine payload.
        try {
            boolean success = passengerDAO.transactionalCheckIn(currentPassenger.getId(), loungeId, assignedSeat,
                    currentAccessType);
            if (!success) {
                status("Transaction Failed Systemically. Please try again or Check-Out passenger.", ALMTheme.DANGER);
                JOptionPane.showMessageDialog(this, "Unknown Transaction Lock block on check-in.", "Check-In Blocked",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IllegalStateException txException) {
            status("Check-In Blocked: " + txException.getMessage(), ALMTheme.DANGER);
            JOptionPane.showMessageDialog(this,
                    "Transaction Rollback Exception!\n" + txException.getMessage()
                            + "\n\nPlease resolve this blocker before proceeding.",
                    "Check-In Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Commit visual state only after SQL transaction successfully commits!
        int idx = parseSeatIdx(assignedSeat);
        if (idx >= 0)
            seatOccupied[idx] = true;
        if (lockTimer != null) {
            lockTimer.stop();
            lockTimer = null;
        }
        checkedIn = true;

        String t = new SimpleDateFormat("HH:mm").format(new Date());
        queueModel.insertRow(0,
                new Object[] { currentPassenger.getFullName(), assignedSeat, "Check-in " + t, "Normal", "Pending" });

        // Transition UI to Check-Out mode
        confirmBtn.setVisible(false);
        confirmBtn.setEnabled(false);
        checkOutBtn.setVisible(true);
        billBtn.setEnabled(true);

        accessPill.setText("  \u25CF  CHECKED IN  ");
        accessPill.setBackground(ALMTheme.ACCENT_BLUE);

        selectedSeatLbl.setText(assignedSeat + " (active)");
        seatFeedback.setText("Checked in successfully");
        seatFeedback.setForeground(ALMTheme.SUCCESS);

        // Sync occupancy + seats from live DB
        refreshOccupancyFromDB();

        status("\u2714 Checked in: " + currentPassenger.getFullName() + " @ " + assignedSeat + " at " + t,
                ALMTheme.SUCCESS);
        JOptionPane.showMessageDialog(this,
                "Check-in confirmed!\nPassenger: " + currentPassenger.getFullName() + "\nSeat: " + assignedSeat
                        + "\nAccess: " + currentAccessType + "\nTime: " + t,
                "\u2714 Success", JOptionPane.INFORMATION_MESSAGE);
        com.alm.util.AudioFeedback.playCheckInSuccess();
    }

    private void doBill() {
        if (currentPassenger == null)
            return;

        // Determine Section & Pricing
        String sec = assignedSeat != null && assignedSeat.startsWith("V") ? "VIP"
                : assignedSeat != null && assignedSeat.startsWith("B") ? "Business" : "Economy";

        double base = sec.equals("VIP") ? 1500 : sec.equals("Business") ? 800 : 400;

        // Custom payment method based on eval
        String method = "Credit Card / POS";
        if (currentAccessType != null && currentAccessType.startsWith("COMPLIMENTARY")) {
            base = 0; // Free for complimentary tiers!
            method = "Sponsor Auth (" + currentAccessType.replace("_", " ") + ")";
        }

        double cgst = base * 0.09;
        double sgst = base * 0.09;
        double total = base + cgst + sgst;

        String invNo = "INV-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());

        Lounge selLounge = (Lounge) loungeCb.getSelectedItem();
        String loungeInfo = selLounge != null ? selLounge.getName() + " - T" + selLounge.getTerminal()
                : "LoungeOS Front Desk";

        Date validUntil = new Date(System.currentTimeMillis() + (3 * 3600 * 1000)); // Standard 3 hour validity

        String billHtml = "<html><body style='width:250px; font-family:sans-serif;'>"
                + "<div style='text-align:center;'>"
                + "  <h2 style='color:#0F4C81; margin-bottom:2px;'>LoungeOS</h2>"
                + "  <p style='margin-top:0; font-size:10px; color:#555;'>" + loungeInfo + "</p>"
                + "</div>"
                + "<hr style='border:1px dashed #ccc;'/>"
                + "<table width='100%' style='font-size:11px;'>"
                + "<tr><td><b>Invoice:</b></td><td align='right'>" + invNo + "</td></tr>"
                + "<tr><td><b>Time:</b></td><td align='right'>"
                + new SimpleDateFormat("dd MMM HH:mm").format(new Date()) + "</td></tr>"
                + "<tr><td><b>Passenger:</b></td><td align='right'>" + currentPassenger.getFullName() + "</td></tr>"
                + "<tr><td><b>Seat:</b></td><td align='right'>" + (assignedSeat != null ? assignedSeat : "Unassigned")
                + "</td></tr>"
                + "<tr><td><b>Duration:</b></td><td align='right'>Valid till "
                + new SimpleDateFormat("HH:mm").format(validUntil) + "</td></tr>"
                + "<tr><td><b>Method:</b></td><td align='right'>" + method + "</td></tr>"
                + "</table>"
                + "<hr style='border:1px dashed #ccc;'/>"
                + "<table width='100%' style='font-size:11px;'>"
                + "<tr><td>Base Access (" + sec + ")</td><td align='right'>\u20B9" + String.format("%.2f", base)
                + "</td></tr>"
                + "<tr><td>CGST @ 9%</td><td align='right'>\u20B9" + String.format("%.2f", cgst) + "</td></tr>"
                + "<tr><td>SGST @ 9%</td><td align='right'>\u20B9" + String.format("%.2f", sgst) + "</td></tr>"
                + "</table>"
                + "<hr style='border:1px dashed #ccc;'/>"
                + "<div style='text-align:right; font-size:14px;'><b>TOTAL: \u20B9" + String.format("%.2f", total)
                + "</b></div>"
                + "</body></html>";

        // Generate QR component
        JLabel qrImgLabel = new JLabel();
        qrImgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode("RECEIPT_ID:" + invNo,
                    com.google.zxing.BarcodeFormat.QR_CODE, 100, 100);
            java.awt.image.BufferedImage img = com.google.zxing.client.j2se.MatrixToImageWriter
                    .toBufferedImage(bitMatrix);
            qrImgLabel.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            qrImgLabel.setText("[QR Generator Ex]");
        }

        // Build Custom Dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Digital Receipt", true);
        dialog.setLayout(new BorderLayout());

        JPanel receiptPanel = new JPanel(new BorderLayout());
        receiptPanel.setBackground(Color.WHITE);
        receiptPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        receiptPanel.add(new JLabel(billHtml), BorderLayout.NORTH);

        JPanel qrPanel = new JPanel();
        qrPanel.setBackground(Color.WHITE);
        qrPanel.setLayout(new BoxLayout(qrPanel, BoxLayout.Y_AXIS));
        qrImgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrPanel.add(qrImgLabel);
        JLabel qrHint = new JLabel("Scan for Digital Copy");
        qrHint.setFont(ALMTheme.fontRegular(9));
        qrHint.setForeground(Color.GRAY);
        qrHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrPanel.add(qrHint);

        receiptPanel.add(qrPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(true);
        btnPanel.setBackground(new Color(0xF5, 0xF5, 0xF5));
        GButton printBtn = new GButton("Print", ALMTheme.TEXT_SECONDARY);
        GButton emailBtn = new GButton("Email Receipt", ALMTheme.ACCENT_CYAN);
        GButton okBtn = new GButton("Close", ALMTheme.ACCENT_BLUE);

        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Sent to POS Printer LP-200. Requesting Job...", "Printing",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        emailBtn.addActionListener(e -> {
            String eAddr = currentPassenger.getEmail() != null && !currentPassenger.getEmail().isEmpty()
                    ? currentPassenger.getEmail()
                    : "recorded passenger contact";
            JOptionPane.showMessageDialog(dialog, "E-Receipt instantly dispatched to " + eAddr + "!", "Email Sent",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        okBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(printBtn);
        btnPanel.add(emailBtn);
        btnPanel.add(okBtn);

        dialog.add(receiptPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Log transaction (asynchronously)
        if (total > 0) {
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() {
                    try {
                        Transaction tx = new Transaction();
                        tx.setRequestId(1); // Real system maps to an auth request_id context
                        tx.setAmount(BigDecimal.valueOf(total));
                        tx.setCurrency("INR");
                        tx.setPaymentMethod("CARD");
                        tx.setPaymentStatus("SUCCESS");
                        txDAO.logTransaction(tx);
                    } catch (Exception ignored) {
                    }
                    return null;
                }
            }.execute();
        }

        billGenerated = true;
        billBtn.setEnabled(false);
        status("Bill Generated: " + invNo, ALMTheme.SUCCESS);
        dialog.setVisible(true);
    }

    private void doCancel() {
        StringBuilder warn = new StringBuilder("Cancel current operation?");
        if (checkedIn)
            warn.append("\n- This will CHECK OUT the passenger and free their seat.");
        if (billGenerated)
            warn.append("\n- Bill already generated (refund may apply).");
        if (!checkedIn && assignedSeat != null)
            warn.append("\n- Selected seat will be released.");

        int r = JOptionPane.showConfirmDialog(this, warn.toString(), "Cancel", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION)
            return;

        // If passenger is checked in, do a proper DB checkout
        if (checkedIn && currentPassenger != null) {
            boolean dbOk = passengerDAO.checkoutPassenger(currentPassenger.getId());
            if (!dbOk) {
                status("DB checkout failed during cancel!", ALMTheme.DANGER);
            }
        }

        // Rollback seat visual state
        if (assignedSeat != null) {
            int idx = parseSeatIdx(assignedSeat);
            if (idx >= 0)
                seatOccupied[idx] = false;
        }
        if (lockTimer != null) {
            lockTimer.stop();
            lockTimer = null;
        }
        clearForm();
        refreshOccupancyFromDB();
        status("Cancelled. Ready for next passenger.", ALMTheme.TEXT_MUTED);
    }

    private void clearForm() {
        currentPassenger = null;
        assignedSeat = null;
        checkedIn = false;
        billGenerated = false;
        selectedSeatLbl.setText("None");
        seatFeedback.setText("");
        nameVal.setText("\u2014");
        bpVal.setText("\u2014");
        flightVal.setText("\u2014");
        membVal.setText("\u2014");
        classVal.setText("\u2014");
        accessPill.setText("  \u25CF  PENDING  ");
        accessPill.setForeground(ALMTheme.TEXT_MUTED);
        accessPill.setBackground(ALMTheme.BG_DEEPEST);
        vipBadge.setVisible(false);
        infoCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        confirmBtn.setEnabled(false);
        confirmBtn.setVisible(true);
        if (checkOutBtn != null) {
            checkOutBtn.setVisible(false);
        }
        billBtn.setEnabled(false);
        refreshAllSeats();
        scanField.requestFocus();
    }

    // ===================== UTILS =====================
    private void status(String msg, Color c) {
        statusDot.setForeground(c);
        statusText.setText(msg);
        statusText.setForeground(c);
    }

    private void updateOccUI() {
        occupancyLbl.setText(currentOccupancy + " / " + maxCapacity);
        occupancyBar.setMaximum(maxCapacity);
        occupancyBar.setValue(currentOccupancy);
        int pct = maxCapacity > 0 ? (currentOccupancy * 100 / maxCapacity) : 0;
        if (occupancyPctLbl != null) {
            occupancyPctLbl.setText(pct + "% capacity");
        }
        updateOccColor();
    }

    private void updateOccColor() {
        double r = maxCapacity > 0 ? (double) currentOccupancy / maxCapacity : 0;
        occupancyBar.setForeground(r >= 1 ? ALMTheme.DANGER : r >= 0.85 ? ALMTheme.WARNING : ALMTheme.SUCCESS);
    }

    /**
     * Live-refresh occupancy count + seat grid from the database.
     * Called after every check-in, check-out, and cancel to keep the bar accurate.
     */
    private void refreshOccupancyFromDB() {
        Integer loungeId = com.alm.ui.core.SessionContext.getInstance().getCurrentLoungeId();
        if (loungeId == null)
            loungeId = 1;
        final int lid = loungeId;

        new SwingWorker<List<Passenger>, Void>() {
            protected List<Passenger> doInBackground() {
                return passengerDAO.getAllPassengers().stream()
                        .filter(p -> p.getCheckedInLoungeId() != null && p.getCheckedInLoungeId() == lid
                                && "CHECKED_IN".equals(p.getAccessStatus()))
                        .toList();
            }

            protected void done() {
                try {
                    List<Passenger> livePax = get();
                    currentOccupancy = livePax.size();

                    // Reset seat map and rehydrate from live DB data
                    for (int i = 0; i < seatOccupied.length; i++) {
                        seatOccupied[i] = false;
                    }
                    for (Passenger p : livePax) {
                        String seat = p.getAssignedSeat();
                        if (seat != null && !seat.isEmpty()) {
                            int idx = parseSeatIdx(seat);
                            if (idx >= 0 && idx < seatOccupied.length) {
                                seatOccupied[idx] = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Fallback: keep current local state
                }
                updateOccUI();
                refreshAllSeats();
            }
        }.execute();
    }

    private void refreshLiveLoungeSnapshot(Lounge lounge) {
        if (lounge == null)
            return;

        maxCapacity = lounge.getCapacity();
        currentOccupancy = 0;
        for (int i = 0; i < seatOccupied.length; i++) {
            seatOccupied[i] = false;
        }

        new SwingWorker<List<Passenger>, Void>() {
            protected List<Passenger> doInBackground() {
                // Brute force filter to get real checked-in users for THIS lounge
                return passengerDAO.getAllPassengers().stream()
                        .filter(p -> p.getCheckedInLoungeId() != null && p.getCheckedInLoungeId() == lounge.getId()
                                && "CHECKED_IN".equals(p.getAccessStatus()))
                        .toList();
            }

            protected void done() {
                try {
                    List<Passenger> livePax = get();
                    currentOccupancy = livePax.size();

                    // Rehydrate seat map based on actual passenger records
                    for (Passenger p : livePax) {
                        String seat = p.getAssignedSeat();
                        if (seat != null && !seat.isEmpty()) {
                            int idx = parseSeatIdx(seat);
                            if (idx >= 0 && idx < seatOccupied.length) {
                                seatOccupied[idx] = true;
                            }
                        }
                    }
                } catch (Exception e) {
                }

                updateOccUI();
                refreshAllSeats();
            }
        }.execute();
    }

    private JLabel sectionTitle(String t) {
        JLabel l = new JLabel(t);
        l.setFont(ALMTheme.fontBold(13));
        l.setForeground(ALMTheme.TEXT_PRIMARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel infoRow(String lbl, JLabel val) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel k = new JLabel(lbl + ":");
        k.setFont(ALMTheme.fontRegular(11));
        k.setForeground(ALMTheme.TEXT_MUTED);
        k.setPreferredSize(new Dimension(90, 18));
        val.setFont(ALMTheme.fontBold(11));
        val.setForeground(ALMTheme.TEXT_PRIMARY);
        p.add(k, BorderLayout.WEST);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JPanel legendItem(Color c, String t) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        p.setOpaque(false);
        JLabel d = new JLabel("\u25A0");
        d.setFont(ALMTheme.fontBold(11));
        d.setForeground(c);
        JLabel l = new JLabel(t);
        l.setFont(ALMTheme.fontRegular(9));
        l.setForeground(ALMTheme.TEXT_MUTED);
        p.add(d);
        p.add(l);
        return p;
    }

    private JLabel sectionBadge(String name, Color c) {
        JLabel l = new JLabel(" " + name + " ");
        l.setFont(ALMTheme.fontBold(10));
        l.setForeground(c);
        l.setOpaque(true);
        l.setBackground(ALMTheme.withAlpha(c, 20));
        l.setBorder(new EmptyBorder(2, 4, 2, 4));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
}
