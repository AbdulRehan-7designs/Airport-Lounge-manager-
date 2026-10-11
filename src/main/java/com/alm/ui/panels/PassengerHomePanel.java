package com.alm.ui.panels;

import com.alm.ui.components.GCard;
import com.alm.ui.core.SessionContext;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import java.awt.*;

/**
 * End-To-End Passenger Workflow Panel
 * Demonstrates the 5 exact phases for a seamless lounge experience.
 */
public class PassengerHomePanel extends JPanel {

    private com.alm.model.Passenger currentPassenger;
    private com.alm.dao.PassengerDAO paxDAO = new com.alm.dao.PassengerDAO();
    private com.alm.dao.SessionTokenDAO tokenDAO = new com.alm.dao.SessionTokenDAO();
    private String activeToken;
    private javax.swing.Timer qrTimer;
    private javax.swing.Timer pollTimer;
    private String lastKnownStatus;

    public PassengerHomePanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_LOUNGE_AVAILABILITY))
            return;

        loadPassengerState();

        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 10, 0, 10);

        // Left Column (Access & Identity)
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0.35;
        g.weighty = 1.0;
        mainGrid.add(buildIdentityColumn(), g);

        // Right Column (Flight Context & Live Services)
        g.gridx = 1;
        g.gridy = 0;
        g.weightx = 0.65;
        g.weighty = 1.0;
        mainGrid.add(buildServicesColumn(), g);

        add(mainGrid, BorderLayout.CENTER);
    }

    private void loadPassengerState() {
        String name = SessionContext.getInstance().getUsername();
        if (name != null && !name.equals("Guest")) {
            currentPassenger = paxDAO.findByFullName(name);
        }

        // Strict Provisioning Requirement: If the airline booking process hasn't
        // created this profile via import, deny.
        if (currentPassenger == null) {
            currentPassenger = new com.alm.model.Passenger();
            currentPassenger.setId(-1);
            currentPassenger.setFullName(name != null ? name : "Unregistered");
            currentPassenger.setAccessStatus("NOT_ELIGIBLE");
            currentPassenger.setMembershipType("N/A");
            currentPassenger.setTicketClass("N/A");
        } else {
            // Force baseline values if they are missing but valid
            if (currentPassenger.getAccessStatus() == null) {
                currentPassenger.setAccessStatus("NOT_ELIGIBLE");
            }
            if (currentPassenger.getMembershipType() == null)
                currentPassenger.setMembershipType("Standard");
            if (currentPassenger.getTicketClass() == null)
                currentPassenger.setTicketClass("Economy");
        }
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);

        // Removed duplicate 'Welcome' title as the global shell already renders it!
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        hdr.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel clock = new JLabel();
        clock.setFont(ALMTheme.fontMono(14));
        clock.setForeground(ALMTheme.TEXT_MUTED);
        new javax.swing.Timer(1000,
                e -> clock.setText(new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()))).start();
        right.add(clock);

        JButton refreshBtn = makeBtn("↻ Sync Status", ALMTheme.BG_CARD);
        refreshBtn.addActionListener(e -> refreshUI());
        right.add(refreshBtn);

        hdr.add(right, BorderLayout.EAST);

        return hdr;
    }

    private void refreshUI() {
        loadPassengerState();
        removeAll();
        add(buildHeader(), BorderLayout.NORTH);

        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 10, 0, 10);

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0.35;
        g.weighty = 1.0;
        mainGrid.add(buildIdentityColumn(), g);

        g.gridx = 1;
        g.gridy = 0;
        g.weightx = 0.65;
        g.weighty = 1.0;
        mainGrid.add(buildServicesColumn(), g);

        add(mainGrid, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (currentPassenger != null) {
            lastKnownStatus = currentPassenger.getAccessStatus() + "_" + currentPassenger.getCheckedInLoungeId();
        }
        if (pollTimer == null) {
            pollTimer = new javax.swing.Timer(3000, e -> {
                String name = SessionContext.getInstance().getUsername();
                if (name != null && !name.equals("Guest")) {
                    com.alm.model.Passenger liveP = paxDAO.findByFullName(name);
                    if (liveP != null) {
                        String newStatus = liveP.getAccessStatus() + "_" + liveP.getCheckedInLoungeId();
                        if (lastKnownStatus != null && !lastKnownStatus.equals(newStatus)) {
                            refreshUI();
                            lastKnownStatus = newStatus;
                        } else if (lastKnownStatus == null) {
                            lastKnownStatus = newStatus;
                        }
                    }
                }
            });
        }
        pollTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (pollTimer != null)
            pollTimer.stop();
        if (qrTimer != null)
            qrTimer.stop();
    }

    // ── LEFT COLUMN: Access Status & QR ─────────────────────────────────────────
    private JPanel buildIdentityColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        // 1. Access Status Card
        GCard accessCard = new GCard(12);
        accessCard.setLayout(new BoxLayout(accessCard, BoxLayout.Y_AXIS));
        accessCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        accessCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        com.alm.service.AccessEvaluationService evalSvc = new com.alm.service.AccessEvaluationService();
        com.alm.service.AccessEvaluationService.EvaluationResult result = evalSvc.evaluateAccess(currentPassenger, 4);

        String status = currentPassenger.getAccessStatus();
        JLabel t = new JLabel(result.getAccessType().name().replace("_", " "));
        t.setFont(ALMTheme.fontBold(15)); // Reduced slightly to prevent truncation

        if ("ELIGIBLE".equals(status)) {
            t.setForeground(ALMTheme.SUCCESS);
        } else if ("CHECKED_IN".equals(status)) {
            t.setForeground(ALMTheme.ACCENT_CYAN);
        } else {
            t.setForeground(ALMTheme.DANGER);
        }

        accessCard.add(t);
        accessCard.add(Box.createVerticalStrut(15));

        accessCard.add(infoRow("Membership",
                currentPassenger.getMembershipType() != null ? currentPassenger.getMembershipType() : "None"));
        accessCard.add(Box.createVerticalStrut(5));

        String rules = result.getReason();
        if ("CHECKED_IN".equals(status)) {
            rules = "Visit Active - Access Validated";
        }
        accessCard.add(infoRow("Rule/Eval", "<html>" + rules + "</html>"));

        col.add(accessCard);
        col.add(Box.createVerticalStrut(20));

        // 2. Dynamic QR Code
        GCard qrCard = new GCard(12);
        qrCard.setLayout(new BoxLayout(qrCard, BoxLayout.Y_AXIS));
        qrCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel qt = new JLabel();
        qt.setFont(ALMTheme.fontBold(18));
        qt.setAlignmentX(Component.CENTER_ALIGNMENT);

        if ("CHECKED_IN".equals(status) || "ELIGIBLE".equals(status)) {
            qt.setText("ELIGIBLE".equals(status) ? "Your Identity Token" : "Visit Active (Scan to Check-Out)");
            if ("CHECKED_IN".equals(status))
                qt.setForeground(ALMTheme.SUCCESS);
            qrCard.add(qt);
            qrCard.add(Box.createVerticalStrut(15));

            JLabel qrImgLabel = new JLabel();
            qrImgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            qrCard.add(qrImgLabel);
            qrCard.add(Box.createVerticalStrut(10));

            String hintText = "CHECKED_IN".equals(status)
                    ? "Use this QR for: Service Auth, Check-out, Staff Help"
                    : "Scan at Reception to Check In & Out.";
            JLabel qHint = new JLabel(hintText, SwingConstants.CENTER);
            qHint.setFont(ALMTheme.fontRegular(12));
            qHint.setForeground(ALMTheme.TEXT_MUTED);
            qHint.setAlignmentX(Component.CENTER_ALIGNMENT);
            qrCard.add(qHint);

            JLabel qTimer = new JLabel("Generating Token...");
            qTimer.setFont(ALMTheme.fontMono(10));
            qTimer.setForeground(ALMTheme.WARNING);
            qTimer.setAlignmentX(Component.CENTER_ALIGNMENT);
            qrCard.add(Box.createVerticalStrut(5));
            qrCard.add(qTimer);
            qrCard.add(Box.createVerticalStrut(10));

            if (qrTimer != null)
                qrTimer.stop();
            int[] secondsLeft = { 60 };

            Runnable refreshQR = () -> {
                activeToken = tokenDAO.createTokenForPassenger(currentPassenger.getId(), 5);
                if (activeToken == null)
                    activeToken = "MOCK-QR-FAILSAFE";
                try {
                    com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
                    com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(activeToken,
                            com.google.zxing.BarcodeFormat.QR_CODE, 220, 220);
                    java.awt.image.BufferedImage img = com.google.zxing.client.j2se.MatrixToImageWriter
                            .toBufferedImage(bitMatrix);
                    qrImgLabel.setIcon(new ImageIcon(img));
                } catch (Exception ex) {
                    qrImgLabel.setText("QR Error");
                }
                secondsLeft[0] = 60;
                qTimer.setText("Refreshing in " + secondsLeft[0] + "s...");
            };

            refreshQR.run();

            qrTimer = new javax.swing.Timer(1000, e -> {
                secondsLeft[0]--;
                if (secondsLeft[0] <= 0)
                    refreshQR.run();
                else
                    qTimer.setText("Refreshing in " + secondsLeft[0] + "s...");
            });
            qrTimer.start();

            if ("CHECKED_IN".equals(status)) {
                qrCard.add(Box.createVerticalStrut(15));
                JButton outBtn = makeBtn("\uD83D\uDEAA Check-Out Now", ALMTheme.DANGER);
                outBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                outBtn.addActionListener(e -> {
                    paxDAO.checkoutPassenger(currentPassenger.getId());
                    loadPassengerState();
                    Component[] comps = getParent().getComponents();
                    for (Component c : comps) {
                        if (c instanceof JButton && ((JButton) c).getText().contains("Sync Status")) {
                            ((JButton) c).doClick();
                        }
                    }
                });
                qrCard.add(outBtn);
            }
        } else {
            qt.setText("Access Denied");
            qt.setForeground(ALMTheme.DANGER);
            qrCard.add(qt);
        }

        col.add(qrCard);
        return col;
    }

    // ── RIGHT COLUMN: Flight Context & Live Services ────────────────────────────
    private JPanel buildServicesColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        // 1. Live Context (Flight & Lounge Status)
        GCard contextCard = new GCard(12);
        contextCard.setLayout(new BorderLayout());
        contextCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contextCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel pTop = new JPanel(new BorderLayout());
        pTop.setOpaque(false);
        JLabel lt = new JLabel("Live Visit Context");
        lt.setFont(ALMTheme.fontBold(18));
        pTop.add(lt, BorderLayout.WEST);

        JPanel liveTag = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        liveTag.setOpaque(false);
        boolean isCheckedIn = "CHECKED_IN".equals(currentPassenger.getAccessStatus());
        JLabel pulse = new JLabel("\u25CF " + (isCheckedIn ? "CHECKED IN" : "NOT CHECKED IN"));
        pulse.setFont(ALMTheme.fontBold(12));
        pulse.setForeground(isCheckedIn ? ALMTheme.SUCCESS : ALMTheme.TEXT_MUTED);
        liveTag.add(pulse);
        pTop.add(liveTag, BorderLayout.EAST);
        contextCard.add(pTop, BorderLayout.NORTH);

        JPanel cGrid = new JPanel(new GridLayout(3, 2, 20, 15));
        cGrid.setOpaque(false);
        cGrid.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        com.alm.dao.FlightDAO flightDAO = new com.alm.dao.FlightDAO();
        com.alm.model.Flight flight = null;
        if (currentPassenger.getFlightId() != null) {
            flight = flightDAO.findById(currentPassenger.getFlightId());
        }

        String fn = "Awaiting Sync...";
        String fTime = "TBD";
        String gateInfo = "";

        if (flight != null) {
            String route = (flight.getOrigin() != null && flight.getDestination() != null)
                    ? (flight.getOrigin() + " \u279C " + flight.getDestination())
                    : "";
            fn = "Flight " + flight.getFlightNumber() + (route.isEmpty() ? "" : " (" + route + ")");

            if (flight.getScheduledDeparture() != null) {
                fTime = new java.text.SimpleDateFormat("HH:mm").format(flight.getScheduledDeparture());
                if (flight.getDelayMinutes() > 0) {
                    fTime += " (+" + flight.getDelayMinutes() + "m)";
                }
            } else if (currentPassenger.getFlightTime() != null) {
                fTime = new java.text.SimpleDateFormat("HH:mm").format(currentPassenger.getFlightTime());
            }

            if (flight.getGate() != null && !flight.getGate().isEmpty() && !flight.getGate().equals("null")) {
                gateInfo = " \u2022 Gate " + flight.getGate();
            } else if (flight.getTerminal() != null && !flight.getTerminal().isEmpty()
                    && !flight.getTerminal().equals("null")) {
                gateInfo = " \u2022 T" + flight.getTerminal();
            }
        } else if (currentPassenger.getFlightId() != null) {
            fn = "Flight " + currentPassenger.getFlightId();
            if (currentPassenger.getFlightTime() != null) {
                fTime = new java.text.SimpleDateFormat("HH:mm").format(currentPassenger.getFlightTime());
            }
        }

        cGrid.add(infoBox("Flight Route", fn));
        cGrid.add(infoBox("Boarding Info", fTime + gateInfo));

        String seat = currentPassenger.getAssignedSeat() != null && isCheckedIn ? currentPassenger.getAssignedSeat()
                : "Assigned at Reception";
        String cls = currentPassenger.getTicketClass() != null ? currentPassenger.getTicketClass() : "Economy";
        cGrid.add(infoBox("Seat Allocation", seat + " \u2022 " + cls + " Zone"));

        String lInfo = "Unknown Context";
        com.alm.model.Lounge l = null;
        if (isCheckedIn && currentPassenger.getCheckedInLoungeId() != null) {
            l = new com.alm.dao.LoungeDAO().getById(currentPassenger.getCheckedInLoungeId());
            if (l != null)
                lInfo = l.getName() + " - T" + l.getTerminal();
        } else {
            lInfo = "Scan QR at Front Desk";
        }
        cGrid.add(infoBox("Lounge Location", lInfo));

        if (l != null) {
            final int actLoungeId = l.getId();
            long occ = paxDAO.getAllPassengers().stream()
                    .filter(px -> px.getCheckedInLoungeId() != null && px.getCheckedInLoungeId() == actLoungeId
                            && "CHECKED_IN".equals(px.getAccessStatus()))
                    .count();
            cGrid.add(infoBox("Lounge Capacity", occ + " / " + l.getCapacity() + " Occupied"));
        } else {
            cGrid.add(infoBox("Lounge Capacity", "N/A"));
        }

        if (isCheckedIn && currentPassenger.getCheckInTime() != null) {
            JLabel durLbl = new JLabel();
            durLbl.setFont(ALMTheme.fontBold(14));
            durLbl.setForeground(ALMTheme.TEXT_PRIMARY);
            javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
                long diffMins = (System.currentTimeMillis() - currentPassenger.getCheckInTime().getTime()) / 60000;
                long hours = diffMins / 60;
                long mins = diffMins % 60;
                durLbl.setText(hours + "h " + mins + "m elapsed");
            });
            t.start();
            t.setInitialDelay(0); // Trigger instantly
            JPanel durPanel = new JPanel();
            durPanel.setLayout(new BoxLayout(durPanel, BoxLayout.Y_AXIS));
            durPanel.setOpaque(false);
            JLabel titleLbl = new JLabel("Visit Duration");
            titleLbl.setFont(ALMTheme.fontRegular(11));
            titleLbl.setForeground(ALMTheme.TEXT_MUTED);
            durPanel.add(titleLbl);
            durPanel.add(Box.createVerticalStrut(2));
            durPanel.add(durLbl);
            cGrid.add(durPanel);
        } else {
            cGrid.add(infoBox("Visit Duration", "Not Started"));
        }
        contextCard.add(cGrid, BorderLayout.CENTER);

        col.add(contextCard);
        col.add(Box.createVerticalStrut(20));

        // 2. Service Request & Tracking
        GCard serviceCard = new GCard(12);
        serviceCard.setLayout(new BoxLayout(serviceCard, BoxLayout.Y_AXIS));
        serviceCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel st = new JLabel("My Requests & Assistant");
        st.setFont(ALMTheme.fontBold(18));
        serviceCard.add(st);
        serviceCard.add(Box.createVerticalStrut(15));

        JPanel btnGrp = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btnGrp.setOpaque(false);
        btnGrp.setAlignmentX(LEFT_ALIGNMENT);

        JButton wifiBtn = makeBtn("\uD83D\uDCF6 Show Network Info", ALMTheme.ACCENT_CYAN);
        wifiBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Network: LoungeNet_Fast\nPassword: PremiumFlyer2026\nType: WPA3 Encrypted", "Wi-Fi Identity",
                JOptionPane.INFORMATION_MESSAGE));

        JButton foodBtn = makeBtn("\uD83C\uDF7D Request A La Carte", ALMTheme.ACCENT_PURPLE);
        foodBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Order received! Serving staff has been notified of seat C4."));

        btnGrp.add(wifiBtn);
        btnGrp.add(foodBtn);
        serviceCard.add(btnGrp);
        serviceCard.add(Box.createVerticalStrut(20));

        // Improved Request Tracking
        JPanel reqPanel = new JPanel();
        reqPanel.setLayout(new BoxLayout(reqPanel, BoxLayout.Y_AXIS));
        reqPanel.setOpaque(false);
        reqPanel.setAlignmentX(LEFT_ALIGNMENT);

        reqPanel.add(
                buildRequestItem("Shower Suite", "In Progress", "ETA: 5 min | Assigned: John", ALMTheme.ACCENT_CYAN));
        reqPanel.add(Box.createVerticalStrut(10));
        reqPanel.add(buildRequestItem("Luggage Storage", "Completed", "Drop-off stored at Bay B", ALMTheme.SUCCESS));

        serviceCard.add(reqPanel);

        com.alm.service.AccessEvaluationService evalSvc = new com.alm.service.AccessEvaluationService();
        com.alm.service.AccessEvaluationService.EvaluationResult result = evalSvc.evaluateAccess(currentPassenger, 4);

        if (result.getAccessType() == com.alm.service.AccessEvaluationService.AccessType.PAID) {
            serviceCard.add(Box.createVerticalStrut(20));
            serviceCard.add(buildChargesCard());
        }

        col.add(serviceCard);
        return col;
    }

    private JPanel buildRequestItem(String title, String status, String details, Color statusColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, statusColor));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel t = new JLabel(title);
        t.setFont(ALMTheme.fontBold(13));
        t.setForeground(ALMTheme.TEXT_PRIMARY);
        left.add(t);

        JLabel d = new JLabel(details);
        d.setFont(ALMTheme.fontRegular(11));
        d.setForeground(ALMTheme.TEXT_MUTED);
        left.add(d);

        p.add(left, BorderLayout.CENTER);

        JLabel s = new JLabel("\u25CF " + status);
        s.setFont(ALMTheme.fontBold(12));
        s.setForeground(statusColor);
        p.add(s, BorderLayout.EAST);

        return p;
    }

    private JPanel buildChargesCard() {
        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.Y_AXIS));
        bp.setOpaque(false);
        bp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ALMTheme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(15, 0, 0, 0)));

        JLabel bt = new JLabel("Current Charges (Pay-Per-Use)");
        bt.setFont(ALMTheme.fontBold(14));
        bt.setForeground(ALMTheme.TEXT_PRIMARY);
        bp.add(bt);
        bp.add(Box.createVerticalStrut(10));

        bp.add(infoRow("Entry Fee", "\u20B91500"));
        bp.add(infoRow("Shower Suite", "\u20B9300"));
        bp.add(Box.createVerticalStrut(5));

        JPanel tot = infoRow("Total Due", "\u20B91800");
        for (Component c : tot.getComponents()) {
            if (c instanceof JLabel) {
                ((JLabel) c).setFont(ALMTheme.fontBold(14));
            }
        }
        bp.add(tot);

        return bp;
    }

    private JPanel infoRow(String k, String v) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lblK = new JLabel(k + ": ");
        lblK.setFont(ALMTheme.fontRegular(13));
        lblK.setForeground(ALMTheme.TEXT_MUTED);
        JLabel lblV = new JLabel(v);
        lblV.setFont(ALMTheme.fontBold(13));
        lblV.setForeground(ALMTheme.TEXT_PRIMARY);
        p.add(lblK, BorderLayout.WEST);
        p.add(lblV, BorderLayout.CENTER);
        return p;
    }

    private JPanel infoBox(String title, String val) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(ALMTheme.fontRegular(11));
        t.setForeground(ALMTheme.TEXT_MUTED);
        JLabel v = new JLabel(val);
        v.setFont(ALMTheme.fontBold(14));
        v.setForeground(ALMTheme.TEXT_PRIMARY);
        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(v);
        return p;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(ALMTheme.fontBold(13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
