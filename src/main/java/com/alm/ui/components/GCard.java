package com.alm.ui.components;

import com.alm.ui.theme.ALMTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A clean white card panel with rounded corners and a subtle border/shadow.
 * Replaces dark glassmorphic cards with a light, professional appearance.
 */
public class GCard extends JPanel {

    private int radius;
    private Color bgColor;
    private Color borderColor;
    private boolean showBorder;

    public GCard() {
        this(ALMTheme.CARD_RADIUS);
    }

    public GCard(int radius) {
        this.radius = radius;
        this.bgColor = ALMTheme.BG_CARD; // white
        this.borderColor = ALMTheme.BORDER; // #E0E0E0
        this.showBorder = true;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    public void setCardBackground(Color c) {
        this.bgColor = c;
        repaint();
    }

    public void setCardBorder(Color c) {
        this.borderColor = c;
        repaint();
    }

    public void setShowBorder(boolean b) {
        this.showBorder = b;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - 2;
        int h = getHeight() - 2;

        // Soft drop-shadow (offset 1px, no blur via layered fill)
        g2.setColor(new Color(0, 0, 0, 12));
        g2.fill(new RoundRectangle2D.Double(2, 3, w, h, radius, radius));

        // Card fill
        RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, radius, radius);
        g2.setColor(bgColor);
        g2.fill(shape);

        // Border
        if (showBorder) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(shape);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
