package com.alm.ui.auth;

import com.alm.model.User;
import com.alm.ui.core.SessionContext;
import com.alm.ui.components.GButton;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Premium Login Screen — clean white/blue design.
 * Left: Animated light brand panel with floating shapes.
 * Right: Clean white form with professional field styling.
 */
public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private GButton loginBtn;
    private Runnable onLoginSuccess;

    public LoginPanel(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(new HeroPanel(), BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
    }

    private JPanel buildRightPanel() {
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setPreferredSize(new Dimension(480, 0));
        rightContainer.setBackground(Color.WHITE);
        rightContainer.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ALMTheme.BORDER));

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 44, 20, 44));

        // ── Branding ──────────────────────────────────────────────────────────
        // Logo mark — blue rounded square + text
        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoMark = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ALMTheme.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(ALMTheme.fontBold(18));
                g2.drawString("\u2708", 6, 24);
                g2.dispose();
            }
        };
        logoMark.setPreferredSize(new Dimension(36, 36));
        logoMark.setOpaque(false);

        JLabel brand = new JLabel("LoungeOS");
        brand.setFont(ALMTheme.fontBold(26));
        brand.setForeground(ALMTheme.TEXT_PRIMARY);

        brandRow.add(logoMark);
        brandRow.add(brand);

        JLabel sub = new JLabel("Enterprise Airport Lounge Management");
        sub.setFont(ALMTheme.fontRegular(13));
        sub.setForeground(ALMTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcome = new JLabel("Sign In to Continue");
        welcome.setFont(ALMTheme.fontBold(20));
        welcome.setForeground(ALMTheme.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Fields ────────────────────────────────────────────────────────────
        JLabel userLabel = makeFieldLabel("Username / Employee ID");
        usernameField = createField(false);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = makeFieldLabel("Password");
        passwordField = (JPasswordField) createField(true);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(ALMTheme.fontBold(12));
        errorLabel.setForeground(ALMTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginBtn = new GButton("Sign In", ALMTheme.ACCENT_BLUE);
        loginBtn.setFont(ALMTheme.fontBold(15));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    attemptLogin();
            }
        };
        usernameField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        JLabel forgot = new JLabel("Forgot credentials? Contact System Admin.");
        forgot.setFont(ALMTheme.fontRegular(12));
        forgot.setForeground(ALMTheme.TEXT_MUTED);
        forgot.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Assembly ──────────────────────────────────────────────────────────
        card.add(brandRow);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(40));

        card.add(welcome);
        card.add(Box.createVerticalStrut(24));

        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));

        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));

        card.add(errorLabel);
        card.add(Box.createVerticalStrut(10));

        card.add(loginBtn);
        card.add(Box.createVerticalStrut(36));
        card.add(forgot);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 16, 0, 16);
        rightContainer.add(card, gbc);

        return rightContainer;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ALMTheme.fontBold(12));
        l.setForeground(ALMTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField createField(boolean isPassword) {
        JTextField f = isPassword ? new JPasswordField() : new JTextField();
        f.setFont(ALMTheme.fontRegular(15));
        f.setForeground(ALMTheme.TEXT_PRIMARY);
        f.setCaretColor(ALMTheme.ACCENT_BLUE);
        f.setBackground(ALMTheme.BG_INPUT); // #F8F9FA
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ALMTheme.BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return f;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Authenticating...");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                try {
                    return new com.alm.service.AuthService().login(username, password);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    User u = get();
                    if (u != null) {
                        SessionContext.getInstance().login(u);
                        onLoginSuccess.run();
                    } else {
                        errorLabel.setText("Invalid credentials. Try: admin / password");
                    }
                } catch (Exception e) {
                    errorLabel.setText("Backend connection failed.");
                }
                loginBtn.setEnabled(true);
                loginBtn.setText("Sign In");
            }
        }.execute();
    }

    // ── Hero Panel — animated light abstract art ─────────────────────────────
    private static class HeroPanel extends JPanel {
        private float time = 0;

        public HeroPanel() {
            Timer timer = new Timer(30, e -> {
                time += 0.012f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Light gradient background
            GradientPaint bg = new GradientPaint(0, 0, new Color(0xE3, 0xF2, 0xFD),
                    w, h, new Color(0xF3, 0xE5, 0xF5));
            g2.setPaint(bg);
            g2.fillRect(0, 0, w, h);

            // Animated soft blobs
            drawBlob(g2, w / 2.0 + Math.sin(time) * 140, h / 2.0 + Math.cos(time) * 130, 340,
                    new Color(0x19, 0x76, 0xD2, 50)); // Blue blob
            drawBlob(g2, w * 0.75 + Math.cos(time * 0.7) * 170, h * 0.25 + Math.sin(time * 0.7) * 150, 420,
                    new Color(0x7B, 0x1F, 0xA2, 38)); // Purple blob
            drawBlob(g2, w * 0.2 + Math.sin(time * 1.1) * 110, h * 0.75 + Math.cos(time * 1.1) * 100, 300,
                    new Color(0x00, 0x97, 0xA7, 40)); // Cyan blob

            // Frosted overlay
            g2.setColor(new Color(255, 255, 255, 80));
            g2.fillRect(0, 0, w, h);

            // Connecting dot network
            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(new Color(0x19, 0x76, 0xD2, 25));
            int nodes = 18;
            double[] nx = new double[nodes], ny = new double[nodes];
            for (int i = 0; i < nodes; i++) {
                nx[i] = w / 2.0 + Math.sin(time + i * 1.4) * w * 0.75;
                ny[i] = h / 2.0 + Math.cos(time + i * 0.9) * h * 0.75;
                g2.fillOval((int) nx[i] - 3, (int) ny[i] - 3, 6, 6);
                if (i > 0)
                    g2.drawLine((int) nx[i], (int) ny[i], (int) nx[i - 1], (int) ny[i - 1]);
            }
            if (nodes > 0)
                g2.drawLine((int) nx[0], (int) ny[0], (int) nx[nodes - 1], (int) ny[nodes - 1]);

            // Headline text
            g2.setFont(new Font("Segoe UI", Font.BOLD, 44));
            g2.setColor(new Color(0x21, 0x21, 0x21, 210));
            String headline = "Welcome to the Future";
            int sx = 60, sy = h - 140;
            g2.drawString(headline, sx, sy);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            g2.setColor(new Color(0x61, 0x61, 0x61));
            g2.drawString("Streamlined operations. Uncompromised luxury.", sx, sy + 38);

            g2.dispose();
        }

        private void drawBlob(Graphics2D g2, double cx, double cy, double r, Color color) {
            RadialGradientPaint paint = new RadialGradientPaint(
                    new Point((int) cx, (int) cy), (float) r,
                    new float[] { 0f, 1f },
                    new Color[] { color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0) });
            g2.setPaint(paint);
            g2.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        }
    }
}
