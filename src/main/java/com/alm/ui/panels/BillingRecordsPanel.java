package com.alm.ui.panels;

import com.alm.dao.TransactionDAO;
import com.alm.model.Transaction;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Billing Records Panel — matches the reference screenshot.
 * Left/Center: Filterable billing table with refresh + export
 * Right: Revenue summary cards + Payment method pie chart
 */
public class BillingRecordsPanel extends JPanel {

    private final TransactionDAO txDAO = new TransactionDAO();
    private DefaultTableModel tableModel;

    // Summary labels
    private JLabel totalRevLabel;
    private JLabel pendingLabel;
    private JLabel completedLabel;

    // Filter controls
    private JComboBox<String> methodFilter;
    private JTextField fromDateField;
    private JTextField toDateField;

    // Pie chart data
    private int cardCount = 4, cashCount = 2, paypalCount = 1;

    public BillingRecordsPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_TRANSACTION_HISTORY))
            return;
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());

        add(makeTitleBar(), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(12, 0));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        main.add(buildTableSection(), BorderLayout.CENTER);
        main.add(buildSidePanel(), BorderLayout.EAST);

        add(main, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        loadData(null);
    }

    private JPanel makeTitleBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        JLabel lbl = new JLabel("Billing Records");
        lbl.setFont(ALMTheme.fontBold(22));
        lbl.setForeground(ALMTheme.TEXT_PRIMARY);
        p.add(lbl);
        return p;
    }

    // ── Left/Center: Table Section ────────────────────────────────────────────
    private GCard buildTableSection() {
        GCard card = new GCard(12);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Title + filter row
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);

        JLabel title = new JLabel("Billing Records");
        title.setFont(ALMTheme.fontBold(15));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        top.add(title, BorderLayout.NORTH);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);

        filterRow.add(makeLabel("Filter by Payment:"));
        methodFilter = new JComboBox<>(new String[] { "All", "CARD", "CASH", "UPI" });
        methodFilter.setBackground(ALMTheme.BG_INPUT);
        methodFilter.setForeground(ALMTheme.TEXT_PRIMARY);
        methodFilter.setFont(ALMTheme.fontRegular(12));
        methodFilter.setPreferredSize(new Dimension(100, 26));
        filterRow.add(methodFilter);

        filterRow.add(makeLabel("Filter by Date:"));
        fromDateField = makeDateField("2024-04-01");
        filterRow.add(fromDateField);
        filterRow.add(makeLabel("To:"));
        toDateField = makeDateField("2024-04-30");
        filterRow.add(toDateField);

        JButton filterBtn = makeBtn("Filter", ALMTheme.ACCENT_BLUE);
        filterBtn.addActionListener(e -> loadData((String) methodFilter.getSelectedItem()));
        filterRow.add(filterBtn);

        top.add(filterRow, BorderLayout.CENTER);

        // Refresh / Export row
        JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolRow.setOpaque(false);
        JButton refreshBtn = makeBtn("Refresh Table", ALMTheme.BG_CARD);
        refreshBtn.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        refreshBtn.setForeground(ALMTheme.TEXT_PRIMARY);
        refreshBtn.addActionListener(e -> loadData(null));
        toolRow.add(refreshBtn);

        JButton exportBtn = makeBtn("Export Data", ALMTheme.BG_CARD);
        exportBtn.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        exportBtn.setForeground(ALMTheme.TEXT_PRIMARY);
        exportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Export to CSV coming soon.", "Export",
                JOptionPane.INFORMATION_MESSAGE));
        toolRow.add(exportBtn);
        top.add(toolRow, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);

        // Table
        String[] cols = { "Bill ID", "Passenger Name", "Amount", "Payment Status", "Payment Method", "Invoice Date",
                "Action" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6;
            }
        };

        JTable table = new JTable(tableModel);
        styleTable(table);

        table.getColumnModel().getColumn(6).setCellRenderer((t, v, s, f, row, col) -> {
            JButton btn = makeBtn("View Bill", ALMTheme.ACCENT_BLUE);
            btn.setFont(ALMTheme.fontBold(11));
            return btn;
        });

        table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int row, int col) {
                JButton btn = makeBtn("View Bill", ALMTheme.ACCENT_BLUE);
                btn.addActionListener(e -> {
                    Object id = tableModel.getValueAt(row, 0);
                    Object name = tableModel.getValueAt(row, 1);
                    Object amount = tableModel.getValueAt(row, 2);
                    JOptionPane.showMessageDialog(null,
                            "Bill #" + id + "\nPassenger: " + name + "\nAmount: " + amount,
                            "Bill Details", JOptionPane.INFORMATION_MESSAGE);
                    stopCellEditing();
                });
                return btn;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ALMTheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void styleTable(JTable table) {
        table.setBackground(ALMTheme.BG_CARD);
        table.setForeground(ALMTheme.TEXT_PRIMARY);
        table.setGridColor(ALMTheme.BORDER);
        table.setRowHeight(38);
        table.setFont(ALMTheme.fontRegular(13));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(ALMTheme.BG_CARD_HOVER);
        table.getTableHeader().setBackground(ALMTheme.BG_DEEPEST);
        table.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(ALMTheme.fontBold(12));

        // Status column renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(CENTER);
                String status = v == null ? "" : v.toString();
                switch (status) {
                    case "SUCCESS":
                    case "Paid":
                        lbl.setForeground(ALMTheme.SUCCESS);
                        break;
                    case "INITIATED":
                    case "Pending":
                        lbl.setForeground(ALMTheme.WARNING);
                        break;
                    case "FAILED":
                        lbl.setForeground(ALMTheme.DANGER);
                        break;
                    default:
                        lbl.setForeground(ALMTheme.ACCENT_BLUE);
                }
                return lbl;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
    }

    // ── Right: Summary + Pie chart ────────────────────────────────────────────
    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setOpaque(false);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(240, 0));

        side.add(makeSummaryCard("\uD83D\uDCB0", "₹445.00", "Total Revenue", new Color(0x60, 0xA5, 0xFF, 60)));
        side.add(Box.createVerticalStrut(10));
        side.add(makeSummaryCard("\u26A0", "2", "Pending Payments", new Color(0xFF, 0xCC, 0x60, 60)));
        side.add(Box.createVerticalStrut(10));
        side.add(makeSummaryCard("\u2705", "6", "Payments Completed", new Color(0x60, 0xFF, 0xA0, 60)));
        side.add(Box.createVerticalStrut(10));
        side.add(buildPieCard());

        return side;
    }

    private GCard makeSummaryCard(String icon, String value, String sub, Color bg) {
        GCard card = new GCard(12) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(ALMTheme.fontRegular(24));
        card.add(iconLbl, BorderLayout.WEST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(ALMTheme.fontBold(22));
        valLbl.setForeground(ALMTheme.TEXT_PRIMARY);

        if (sub.contains("Revenue")) {
            totalRevLabel = valLbl;
        } else if (sub.contains("Pending")) {
            pendingLabel = valLbl;
        } else if (sub.contains("Completed")) {
            completedLabel = valLbl;
        }

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(ALMTheme.fontRegular(12));
        subLbl.setForeground(ALMTheme.TEXT_SECONDARY);

        JPanel txt = new JPanel(new BorderLayout());
        txt.setOpaque(false);
        txt.add(valLbl, BorderLayout.NORTH);
        txt.add(subLbl, BorderLayout.SOUTH);
        card.add(txt, BorderLayout.CENTER);

        return card;
    }

    private GCard buildPieCard() {
        GCard card = new GCard(12);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 240));

        JLabel title = new JLabel("Payment Methods");
        title.setFont(ALMTheme.fontBold(13));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        // Pie panel
        JPanel pie = new PieChart();
        pie.setOpaque(false);
        pie.setPreferredSize(new Dimension(0, 130));
        card.add(pie, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.add(legendItem(ALMTheme.ACCENT_BLUE, "Credit Card (" + cardCount + ")"));
        legend.add(legendItem(ALMTheme.WARNING, "Cash (" + cashCount + ")"));
        legend.add(legendItem(ALMTheme.SUCCESS, "PayPal (" + paypalCount + ")"));
        card.add(legend, BorderLayout.SOUTH);

        return card;
    }

    private JPanel legendItem(Color c, String label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setOpaque(false);
        JLabel dot = new JLabel("\u25A0");
        dot.setFont(ALMTheme.fontRegular(12));
        dot.setForeground(c);
        JLabel lbl = new JLabel(label);
        lbl.setFont(ALMTheme.fontRegular(11));
        lbl.setForeground(ALMTheme.TEXT_SECONDARY);
        row.add(dot);
        row.add(lbl);
        return row;
    }

    // ── Status bar ─────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(ALMTheme.BG_DEEPEST);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tabs.setOpaque(false);
        tabs.add(statusTab("Passenger Dashboard", false));
        tabs.add(statusTab("Service Requests", false));
        tabs.add(statusTab("Billing Records", true));
        bar.add(tabs, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.setOpaque(false);
        JLabel dot = new JLabel("\u25CF");
        dot.setForeground(ALMTheme.SUCCESS);
        dot.setFont(ALMTheme.fontRegular(14));
        JLabel db = new JLabel("Connected to SQL Database");
        db.setFont(ALMTheme.fontRegular(12));
        db.setForeground(ALMTheme.TEXT_SECONDARY);
        right.add(db);
        right.add(dot);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JButton statusTab(String label, boolean active) {
        JButton btn = new JButton(label);
        btn.setFont(ALMTheme.fontRegular(12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBackground(active ? ALMTheme.ACCENT_BLUE : ALMTheme.BG_CARD);
        btn.setForeground(active ? Color.WHITE : ALMTheme.TEXT_SECONDARY);
        return btn;
    }

    // ── Data Loading ──────────────────────────────────────────────────────────
    private void loadData(String methodFilterStr) {
        new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() {
                return txDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    List<Transaction> list = get();
                    BigDecimal total = BigDecimal.ZERO;
                    int pending = 0, completed = 0;
                    cardCount = 0;
                    cashCount = 0;
                    paypalCount = 0;

                    SimpleDateFormat sdf = new SimpleDateFormat("d-M-yy");
                    for (Transaction tx : list) {
                        String method = tx.getPaymentMethod();
                        String status = tx.getPaymentStatus();
                        if ("All".equals(methodFilterStr) || methodFilterStr == null || method == null
                                || methodFilterStr.equalsIgnoreCase(method)) {
                            String displayStatus = "SUCCESS".equals(status) ? "Paid"
                                    : ("INITIATED".equals(status) ? "Pending" : status);
                            String dateStr = tx.getProcessedAt() != null ? sdf.format(tx.getProcessedAt()) : "-";
                            tableModel.addRow(new Object[] { tx.getId(), "Passenger #" + tx.getRequestId(),
                                    "₹" + tx.getAmount(), displayStatus, method, dateStr, "View" });
                        }
                        if ("SUCCESS".equals(status)) {
                            total = total.add(tx.getAmount());
                            completed++;
                        } else if ("INITIATED".equals(status))
                            pending++;
                        if (method != null) {
                            switch (method.toUpperCase()) {
                                case "CARD":
                                    cardCount++;
                                    break;
                                case "CASH":
                                    cashCount++;
                                    break;
                                case "UPI":
                                    paypalCount++;
                                    break;
                            }
                        }
                    }

                    // Demo fallback
                    if (list.isEmpty()) {
                        tableModel.addRow(
                                new Object[] { 3001, "Sarah Khan", "₹30.00", "Paid", "Credit Card", "4-8-24", "View" });
                        tableModel.addRow(
                                new Object[] { 3002, "John Williams", "₹75.00", "Paid", "Cash", "4-8-24", "View" });
                        tableModel.addRow(new Object[] { 3005, "Lisa Wong", "₹25.00", "Pending", "Credit Card",
                                "4-8-24", "View" });
                        tableModel.addRow(
                                new Object[] { 3004, "Laura Garcia", "₹55.00", "Paid", "Cash", "4-8-24", "View" });
                        tableModel.addRow(new Object[] { 3003, "Michael Brown", "₹65.00", "Paid", "Credit Card",
                                "4-8-24", "View" });
                        tableModel.addRow(new Object[] { 3008, "Priya Patel", "₹90.00", "Pending", "Credit Card",
                                "4-8-24", "View" });
                        tableModel.addRow(
                                new Object[] { 3007, "Abdul Rehman", "₹55.00", "PayPal", "PayPal", "4-8-24", "View" });
                        total = new BigDecimal("445");
                        pending = 2;
                        completed = 6;
                        cardCount = 4;
                        cashCount = 2;
                        paypalCount = 1;
                    }

                    BigDecimal finalTotal = total;
                    int fp = pending, fc = completed;
                    SwingUtilities.invokeLater(() -> {
                        if (totalRevLabel != null)
                            totalRevLabel.setText("₹" + String.format("%.2f", finalTotal));
                        if (pendingLabel != null)
                            pendingLabel.setText(String.valueOf(fp));
                        if (completedLabel != null)
                            completedLabel.setText(String.valueOf(fc));
                        repaint();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ALMTheme.fontRegular(12));
        l.setForeground(ALMTheme.TEXT_SECONDARY);
        return l;
    }

    private JTextField makeDateField(String placeholder) {
        JTextField f = new JTextField(placeholder, 9);
        f.setFont(ALMTheme.fontRegular(12));
        f.setForeground(ALMTheme.TEXT_PRIMARY);
        f.setBackground(ALMTheme.BG_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ALMTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        return f;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(ALMTheme.fontBold(12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Pie Chart Component ────────────────────────────────────────────────────
    private class PieChart extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = cardCount + cashCount + paypalCount;
            if (total == 0)
                total = 1;

            int diameter = Math.min(getWidth() - 20, getHeight() - 10);
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            Color[] colors = { ALMTheme.ACCENT_BLUE, ALMTheme.WARNING, ALMTheme.SUCCESS };
            int[] counts = { cardCount, cashCount, paypalCount };

            int startAngle = 0;
            for (int i = 0; i < counts.length; i++) {
                int arcAngle = (int) Math.round(360.0 * counts[i] / total);
                g2.setColor(colors[i]);
                g2.fillArc(x, y, diameter, diameter, startAngle, arcAngle);
                startAngle += arcAngle;
            }

            // Center hole (donut style)
            g2.setColor(ALMTheme.BG_CARD);
            int hole = diameter / 3;
            g2.fillOval(x + (diameter - hole) / 2, y + (diameter - hole) / 2, hole, hole);

            g2.dispose();
        }
    }
}
