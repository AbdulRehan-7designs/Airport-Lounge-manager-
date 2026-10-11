package com.alm.ui.components;

import com.alm.ui.theme.ALMTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A styled button with a smooth hover effect and rounded corners.
 * Works in light mode — default accent is blue (#1976D2).
 */
public class GButton extends JButton {

    private Color normalBg;
    private Color hoverBg;
    private Color currentBg;
    private int radius = 8;

    /** Primary blue button. */
    public GButton(String text) {
        this(text, ALMTheme.ACCENT_BLUE);
    }

    /** Custom background color button. */
    public GButton(String text, Color accentColor) {
        super(text);
        this.normalBg = accentColor;
        // Darken for hover instead of brighten (better on light themes)
        this.hoverBg = accentColor.darker();
        this.currentBg = normalBg;

        setForeground(Color.WHITE);
        setFont(ALMTheme.fontBold(13));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                currentBg = hoverBg;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                currentBg = normalBg;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                currentBg = hoverBg.darker();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentBg = hoverBg;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow hint — subtle 1px bottom-right shadow
        g2.setColor(new Color(0, 0, 0, 18));
        g2.fill(new RoundRectangle2D.Double(1, 1, getWidth() - 1, getHeight() - 1, radius, radius));

        // Main fill
        g2.setColor(currentBg);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));

        // Subtle top highlight
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, (getHeight() - 1) / 2.0, radius, radius));

        // Centered label
        FontMetrics fm = g2.getFontMetrics(getFont());
        g2.setFont(getFont());
        g2.setColor(getForeground());
        int tx = (getWidth() - fm.stringWidth(getText())) / 2;
        int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), tx, ty);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 36, fm.getHeight() + 14);
    }
}
