package com.alm.ui.shell;

import com.alm.ui.core.SessionContext;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Top bar — clean white design.
 * Left: Welcome message. Right: icons, date/clock, user badge.
 */
public class TopbarPanel extends JPanel {

    private JLabel clockLabel;
    private JLabel welcomeLabel;

    public TopbarPanel() {
        setPreferredSize(new Dimension(0, ALMTheme.TOPBAR_HEIGHT));
        setBackground(ALMTheme.BG_CARD); // white
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ALMTheme.BORDER));
        setLayout(new BorderLayout());

        // ── Left: Welcome ─────────────────────────────────────────────────────
        welcomeLabel = new JLabel("  Welcome, " + SessionContext.getInstance().getUsername() + "!");
        welcomeLabel.setFont(ALMTheme.fontBold(17));
        welcomeLabel.setForeground(ALMTheme.TEXT_PRIMARY);
        add(welcomeLabel, BorderLayout.WEST);

        // ── Right: Icons + Date + User ────────────────────────────────────────
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        rightPanel.setOpaque(false);

        // Flat icon buttons
        JLabel bellIcon = makeIconBtn(ALMTheme.ICON_BELL);
        JLabel calIcon = makeIconBtn(ALMTheme.ICON_CALENDAR);
        JLabel gearIcon = makeIconBtn(ALMTheme.ICON_SETTINGS);

        // Date label
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        JLabel dateLabel = new JLabel(sdf.format(new Date()));
        dateLabel.setFont(ALMTheme.fontRegular(12));
        dateLabel.setForeground(ALMTheme.TEXT_MUTED);

        // Vertical divider
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 22));
        sep.setForeground(ALMTheme.BORDER);

        // Live clock
        clockLabel = new JLabel();
        clockLabel.setFont(ALMTheme.fontMono(12));
        clockLabel.setForeground(ALMTheme.ACCENT_CYAN);

        // Role badge pill
        String role = SessionContext.getInstance().isLoggedIn()
                ? SessionContext.getInstance().getCurrentUser().getRole()
                : "Admin";
        JLabel userBadge = new JLabel(" " + role + " \u25BE ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ALMTheme.ACCENT_BLUE_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        userBadge.setFont(ALMTheme.fontBold(12));
        userBadge.setForeground(ALMTheme.ACCENT_BLUE);
        userBadge.setOpaque(false);
        userBadge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userBadge.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        // Dropdown logout menu
        JPopupMenu userMenu = new JPopupMenu();
        userMenu.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        JMenuItem logoutItem = new JMenuItem(ALMTheme.ICON_LOGOUT + "  Logout");
        logoutItem.setFont(ALMTheme.fontRegular(13));
        logoutItem.setForeground(ALMTheme.DANGER);
        logoutItem.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof com.alm.ui.core.MainAppWindow) {
                ((com.alm.ui.core.MainAppWindow) win).logout();
            }
        });
        userMenu.add(logoutItem);

        userBadge.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                userMenu.show(userBadge, 0, userBadge.getHeight());
            }
        });

        rightPanel.add(bellIcon);
        rightPanel.add(calIcon);
        rightPanel.add(gearIcon);
        rightPanel.add(Box.createHorizontalStrut(4));
        rightPanel.add(sep);
        rightPanel.add(Box.createHorizontalStrut(4));
        rightPanel.add(dateLabel);
        rightPanel.add(clockLabel);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(userBadge);

        add(rightPanel, BorderLayout.EAST);

        startClock();
    }

    private JLabel makeIconBtn(String icon) {
        JLabel l = new JLabel(icon) {
            private boolean hovered = false;

            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ALMTheme.BORDER_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        l.setFont(ALMTheme.fontRegular(16));
        l.setForeground(ALMTheme.TEXT_SECONDARY);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return l;
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
            clockLabel.setText(tf.format(new Date()));
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    /** Update welcome message after login. */
    public void refreshUser() {
        welcomeLabel.setText("  Welcome, " + SessionContext.getInstance().getUsername() + "!");
    }
}
