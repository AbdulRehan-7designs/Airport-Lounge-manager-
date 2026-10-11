package com.alm.ui.shell;

import com.alm.ui.core.NavigationManager;
import com.alm.ui.core.SessionContext;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Persistent sidebar — clean white/light-gray design.
 * Logo at top, nav items in centre, user profile at bottom.
 */
public class SidebarPanel extends JPanel {

    private String activeItem = "Dashboard";

    protected SidebarPanel(Object[][] navItems) {
        setPreferredSize(new Dimension(ALMTheme.SIDEBAR_WIDTH, 0));
        setBackground(ALMTheme.BG_DEEPEST); // #F8F9FA
        setLayout(new BorderLayout());

        // ── Top: Logo / Branding ──────────────────────────────────────────────
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ALMTheme.BORDER));

        // Accent rectangle acting as a logo mark
        JLabel logoMark = new JLabel("\u2708") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ALMTheme.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(ALMTheme.fontBold(14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("\u2708", (getWidth() - fm.stringWidth("\u2708")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logoMark.setPreferredSize(new Dimension(30, 30));
        logoMark.setOpaque(false);

        JLabel logoText = new JLabel("LoungeOS");
        logoText.setFont(ALMTheme.fontBold(16));
        logoText.setForeground(ALMTheme.TEXT_PRIMARY);

        logoPanel.add(logoMark);
        logoPanel.add(logoText);

        // ── Centre: Nav Items ─────────────────────────────────────────────────
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 10, 10));

        for (Object[] item : navItems) {
            String label = (String) item[0];
            String icon = (String) item[1];
            String target = (String) item[2];
            NavItem btn = new NavItem(label, icon, target);
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(3));
        }

        // ── Bottom: User Profile ──────────────────────────────────────────────
        JPanel userPanel = createUserPanel();

        add(logoPanel, BorderLayout.NORTH);
        add(navPanel, BorderLayout.CENTER);
        add(userPanel, BorderLayout.SOUTH);

        // Sync active state on nav changes
        NavigationManager.getInstance().setOnViewChanged(() -> {
            String current = NavigationManager.getInstance().getCurrentView();
            setActiveItem(current);
        });
    }

    public void setActiveItem(String target) {
        this.activeItem = target;
        repaint();
        Component center = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof JPanel) {
            for (Component c : ((JPanel) center).getComponents()) {
                if (c instanceof NavItem)
                    c.repaint();
            }
        }
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ALMTheme.BORDER));
        panel.setPreferredSize(new Dimension(ALMTheme.SIDEBAR_WIDTH, 58));

        // Avatar circle drawn with Java2D
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ALMTheme.ACCENT_BLUE_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(ALMTheme.ACCENT_BLUE);
                g2.setFont(ALMTheme.fontBold(13));
                String initials = getInitials(SessionContext.getInstance().getUsername());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials,
                        (getWidth() - fm.stringWidth(initials)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            private String getInitials(String name) {
                if (name == null || name.isEmpty())
                    return "?";
                String[] parts = name.trim().split("\\s+");
                if (parts.length == 1)
                    return name.substring(0, Math.min(2, name.length())).toUpperCase();
                return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setOpaque(false);

        String username = SessionContext.getInstance().getUsername();
        JLabel name = new JLabel(username);
        name.setFont(ALMTheme.fontBold(12));
        name.setForeground(ALMTheme.TEXT_PRIMARY);

        JLabel arrow = new JLabel("\u25BE"); // ▾
        arrow.setForeground(ALMTheme.TEXT_MUTED);

        panel.add(avatar);
        panel.add(name);
        panel.add(arrow);
        return panel;
    }

    // ── Inner class: Single nav item ─────────────────────────────────────────
    private class NavItem extends JPanel {
        private final String label;
        private final String icon;
        private final String target;
        private boolean hovered = false;

        NavItem(String label, String icon, String target) {
            this.label = label;
            this.icon = icon;
            this.target = target;

            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setPreferredSize(new Dimension(ALMTheme.SIDEBAR_WIDTH - 20, 38));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    NavigationManager.getInstance().navigateTo(target);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        private boolean isActive() {
            return target.equalsIgnoreCase(activeItem);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth(), h = getHeight();

            if (isActive()) {
                // Active: filled light-blue pill background
                g2.setColor(ALMTheme.ACCENT_BLUE_LIGHT);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
                // Left accent bar
                g2.setColor(ALMTheme.ACCENT_BLUE);
                g2.fillRoundRect(0, 5, 3, h - 10, 3, 3);
            } else if (hovered) {
                // Hover: very light gray
                g2.setColor(new Color(0xE8, 0xEF, 0xFF));
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 8, 8));
            }

            // Icon — use a filled circle background for active, plain for others
            int iconX = 14, iconY = h / 2 + 5;
            g2.setFont(ALMTheme.fontRegular(13));
            g2.setColor(isActive() ? ALMTheme.ACCENT_BLUE : ALMTheme.TEXT_MUTED);
            g2.drawString(icon, iconX, iconY);

            // Label text
            g2.setFont(isActive() ? ALMTheme.fontBold(13) : ALMTheme.fontRegular(13));
            g2.setColor(isActive() ? ALMTheme.ACCENT_BLUE : ALMTheme.TEXT_SECONDARY);
            g2.drawString(label, 38, iconY);

            g2.dispose();
        }
    }
}
