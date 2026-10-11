package com.alm.ui.panels;

import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Advanced Settings Panel.
 * Allows configuration of appearance, system defaults, and developer advanced
 * options.
 */
public class SettingsPanel extends JPanel {

    public SettingsPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("\u2699 System Settings");
        title.setFont(ALMTheme.fontBold(24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        header.add(title);

        JButton saveBtn = makeBtn("\uD83D\uDCBE Save Changes", ALMTheme.SUCCESS);
        saveBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings saved successfully", "Saved",
                JOptionPane.INFORMATION_MESSAGE));
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(saveBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        top.add(header, BorderLayout.WEST);
        top.add(rightHeader, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // Content
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        grid.add(buildAppearanceCard());
        grid.add(buildNotificationCard());

        // Hide System/Advanced configs from Passengers
        boolean isPassenger = true;
        if (com.alm.ui.core.SessionContext.getInstance().getCurrentUser() != null) {
            String role = com.alm.ui.core.SessionContext.getInstance().getCurrentUser().getRole();
            isPassenger = com.alm.config.Role.isPassenger(role);
        }

        if (!isPassenger) {
            grid.add(buildSystemCard());
            grid.add(buildAdvancedCard());
        }

        add(grid, BorderLayout.CENTER);
    }

    private GCard buildAppearanceCard() {
        GCard card = new GCard(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.add(cardTitle("\uD83C\uDFA8 Appearance"));
        card.add(Box.createVerticalStrut(16));

        card.add(makeToggle("Enable Dark Mode", true));
        card.add(Box.createVerticalStrut(8));
        card.add(makeToggle("Compact Table Rows", false));
        card.add(Box.createVerticalStrut(8));

        JPanel fontP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fontP.setOpaque(false);
        JLabel l = new JLabel("UI Scale: ");
        l.setFont(ALMTheme.fontRegular(14));
        l.setForeground(ALMTheme.TEXT_PRIMARY);
        JComboBox<String> scale = new JComboBox<>(new String[] { "100% (Default)", "125%", "150%" });
        scale.setBackground(ALMTheme.BG_INPUT);
        scale.setForeground(ALMTheme.TEXT_PRIMARY);
        fontP.add(l);
        fontP.add(scale);
        card.add(fontP);

        return card;
    }

    private GCard buildNotificationCard() {
        GCard card = new GCard(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.add(cardTitle("\uD83D\uDD14 Notifications"));
        card.add(Box.createVerticalStrut(16));

        card.add(makeToggle("Play sound on new Complaint", true));
        card.add(Box.createVerticalStrut(8));
        card.add(makeToggle("Email alerts for Escalations", true));
        card.add(Box.createVerticalStrut(8));
        card.add(makeToggle("Show desktop notifications", false));

        return card;
    }

    private GCard buildSystemCard() {
        GCard card = new GCard(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.add(cardTitle("\uD83D\uDEE0 Automation & Defaults"));
        card.add(Box.createVerticalStrut(16));

        card.add(makeToggle("Auto-assign Walk-in lounges", true));
        card.add(Box.createVerticalStrut(8));

        JPanel timeP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timeP.setOpaque(false);
        JLabel l = new JLabel("Auto-logout after (mins): ");
        l.setFont(ALMTheme.fontRegular(14));
        l.setForeground(ALMTheme.TEXT_PRIMARY);
        JTextField tf = new JTextField("30", 4);
        tf.setBackground(ALMTheme.BG_INPUT);
        tf.setForeground(ALMTheme.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        timeP.add(l);
        timeP.add(tf);
        card.add(timeP);

        return card;
    }

    private GCard buildAdvancedCard() {
        GCard card = new GCard(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.add(cardTitle("\uD83D\uDE80 Advanced Tasks (Admin)"));
        card.add(Box.createVerticalStrut(16));

        JButton syncBtn = makeBtn("\uD83D\uDD04 Force DB Sync", ALMTheme.ACCENT_BLUE);
        syncBtn.addActionListener(
                e -> JOptionPane.showMessageDialog(this, "Master Database Synchronization started in background."));
        card.add(syncBtn);
        card.add(Box.createVerticalStrut(10));

        JButton logsBtn = makeBtn("\uD83D\uDCC4 Export System Logs", ALMTheme.WARNING);
        logsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Logs exported to /logs/system_dump.csv"));
        card.add(logsBtn);
        card.add(Box.createVerticalStrut(10));

        JButton clearBtn = makeBtn("\uD83D\uDDD1 Clear Cache", ALMTheme.DANGER);
        clearBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Application cache flushed successfully."));
        card.add(clearBtn);

        return card;
    }

    private JLabel cardTitle(String title) {
        JLabel l = new JLabel(title);
        l.setFont(ALMTheme.fontBold(18));
        l.setForeground(ALMTheme.ACCENT_BLUE);
        return l;
    }

    private JCheckBox makeToggle(String label, boolean selected) {
        JCheckBox cb = new JCheckBox(label, selected);
        cb.setFont(ALMTheme.fontRegular(14));
        cb.setForeground(ALMTheme.TEXT_PRIMARY);
        cb.setOpaque(false);
        return cb;
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
