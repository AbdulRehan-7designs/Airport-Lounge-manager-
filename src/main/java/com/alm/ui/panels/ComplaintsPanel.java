package com.alm.ui.panels;

import com.alm.dao.UserDAO;
import com.alm.model.User;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Complaints & Guest Dispute Management.
 * Admins/Supervisors can assign, resolve and cancel complaints.
 */
public class ComplaintsPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> statusFilter;

    // Synthetic complaint model (extend DB later with a complaints table)
    private static final Object[][] DEMO_COMPLAINTS = {
            { "C-1024", "Sarah Khan", "WiFi connection issue", "10:45 AM", "OPEN", "Unassigned" },
            { "C-1025", "Jane Smith", "Food preference missing", "11:20 AM", "RESOLVED", "Reception 01" },
            { "C-1026", "Robert Brown", "Lost item: Umbrella", "12:15 PM", "IN_PROGRESS", "Supervisor A" },
            { "C-1027", "Amit Verma", "AC too cold in seating area", "01:00 PM", "OPEN", "Unassigned" },
            { "C-1028", "Priya Patel", "Billing discrepancy", "02:30 PM", "ESCALATED", "Manager" },
            { "C-1029", "Laura Garcia", "Restroom cleanliness", "03:10 PM", "OPEN", "Unassigned" },
    };

    public ComplaintsPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.HANDLE_GUEST_DISPUTE))
            return;
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        populateDemo();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("\uD83D\uDCE9 Complaints & Disputes");
        title.setFont(ALMTheme.fontBold(22));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        left.add(title);

        left.add(lblOf("Filter by Status:"));
        statusFilter = new JComboBox<>(new String[] { "All", "OPEN", "IN_PROGRESS", "ESCALATED", "RESOLVED" });
        statusFilter.setBackground(ALMTheme.BG_INPUT);
        statusFilter.setForeground(ALMTheme.TEXT_PRIMARY);
        statusFilter.setFont(ALMTheme.fontRegular(12));
        statusFilter.addActionListener(e -> applyFilter());
        left.add(statusFilter);
        hdr.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton addBtn = makeBtn("+ New Complaint", ALMTheme.ACCENT_BLUE);
        addBtn.addActionListener(e -> showNewComplaintDialog());
        right.add(addBtn);
        hdr.add(right, BorderLayout.EAST);

        return hdr;
    }

    // ── Main content ──────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Stats row
        panel.add(buildStatsRow(), BorderLayout.NORTH);

        // Table
        GCard card = new GCard(12);
        card.setLayout(new BorderLayout());

        String[] cols = { "ID", "Passenger", "Issue", "Time", "Status", "Assigned To", "Actions" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6;
            }
        };

        table = new JTable(tableModel);
        styleTable();

        // Action cell
        table.getColumnModel().getColumn(6).setCellRenderer((t, v, sel, foc, row, col) -> buildActionPanel(row));
        table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JPanel actionPanel;

            @Override
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int row, int col) {
                actionPanel = buildEditableActionPanel(row);
                return actionPanel;
            }

            @Override
            public Object getCellEditorValue() {
                return "";
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ALMTheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 80));
        row.add(statCard("OPEN", "3", ALMTheme.DANGER));
        row.add(statCard("IN PROGRESS", "1", ALMTheme.WARNING));
        row.add(statCard("ESCALATED", "1", new Color(0x8B, 0x5C, 0xF6)));
        row.add(statCard("RESOLVED", "1", ALMTheme.SUCCESS));
        return row;
    }

    private GCard statCard(String label, String count, Color accent) {
        GCard card = new GCard(10);
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel num = new JLabel(count);
        num.setFont(ALMTheme.fontBold(28));
        num.setForeground(accent);
        JLabel lbl = new JLabel(label);
        lbl.setFont(ALMTheme.fontRegular(11));
        lbl.setForeground(ALMTheme.TEXT_SECONDARY);
        JPanel tp = new JPanel(new BorderLayout());
        tp.setOpaque(false);
        tp.add(num, BorderLayout.NORTH);
        tp.add(lbl, BorderLayout.SOUTH);
        card.add(tp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildActionPanel(int row) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        p.setBackground(ALMTheme.BG_CARD);
        String status = row < tableModel.getRowCount() ? String.valueOf(tableModel.getValueAt(row, 4)) : "";
        if ("RESOLVED".equals(status)) {
            JButton b = miniBtn("Reopen", ALMTheme.WARNING);
            p.add(b);
        } else {
            p.add(miniBtn("Assign", ALMTheme.ACCENT_BLUE));
            p.add(miniBtn("Resolve \u2713", ALMTheme.SUCCESS));
            p.add(miniBtn("Escalate \u26A0", new Color(0x8B, 0x5C, 0xF6)));
        }
        return p;
    }

    private JPanel buildEditableActionPanel(int row) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        p.setBackground(ALMTheme.BG_CARD);
        String status = row < tableModel.getRowCount() ? String.valueOf(tableModel.getValueAt(row, 4)) : "";
        if ("RESOLVED".equals(status)) {
            JButton reopen = miniBtn("Reopen", ALMTheme.WARNING);
            reopen.addActionListener(e -> {
                tableModel.setValueAt("OPEN", row, 4);
                stopEditing();
            });
            p.add(reopen);
        } else {
            JButton assign = miniBtn("Assign", ALMTheme.ACCENT_BLUE);
            assign.addActionListener(e -> showAssignDialog(row));
            p.add(assign);

            JButton resolve = miniBtn("Resolve \u2713", ALMTheme.SUCCESS);
            resolve.addActionListener(e -> {
                tableModel.setValueAt("RESOLVED", row, 4);
                stopEditing();
                table.repaint();
            });
            p.add(resolve);

            JButton escalate = miniBtn("Escalate \u26A0", new Color(0x8B, 0x5C, 0xF6));
            escalate.addActionListener(e -> {
                tableModel.setValueAt("ESCALATED", row, 4);
                stopEditing();
                table.repaint();
            });
            p.add(escalate);
        }
        return p;
    }

    private void stopEditing() {
        if (table.isEditing())
            table.getCellEditor().stopCellEditing();
    }

    private void showAssignDialog(int row) {
        // Load staff users
        List<User> staff = userDAO.getAll();
        String[] names = staff.stream()
                .filter(u -> !"PASSENGER".equals(u.getRole()))
                .map(u -> u.getUsername() + " (" + u.getRole() + ")")
                .toArray(String[]::new);

        if (names.length == 0)
            names = new String[] { "Reception 01", "Supervisor A", "Manager B" };

        JComboBox<String> assignCombo = new JComboBox<>(names);
        JTextArea noteArea = new JTextArea(3, 20);
        noteArea.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));

        Object[] msg = { "Assign to:", assignCombo, "Add note:", new JScrollPane(noteArea) };
        int opt = JOptionPane.showConfirmDialog(this, msg, "Assign Complaint", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String assignee = (String) assignCombo.getSelectedItem();
            tableModel.setValueAt("IN_PROGRESS", row, 4);
            tableModel.setValueAt(assignee != null ? assignee.split(" ")[0] : "", row, 5);
            stopEditing();
            table.repaint();
        }
    }

    private void showNewComplaintDialog() {
        JTextField paxF = new JTextField();
        JTextArea issueA = new JTextArea(3, 20);
        issueA.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        JComboBox<String> priCombo = new JComboBox<>(new String[] { "LOW", "MEDIUM", "HIGH", "CRITICAL" });

        Object[] msg = { "Passenger Name:", paxF, "Issue Description:", new JScrollPane(issueA), "Priority:",
                priCombo };
        int opt = JOptionPane.showConfirmDialog(this, msg, "Log New Complaint", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION && !paxF.getText().trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            tableModel.addRow(new Object[] {
                    "C-" + (1030 + tableModel.getRowCount()),
                    paxF.getText().trim(),
                    issueA.getText().trim(),
                    sdf.format(new java.util.Date()),
                    "OPEN", "Unassigned", ""
            });
        }
    }

    // ── Styling ──────────────────────────────────────────────────────────────
    private void styleTable() {
        table.setBackground(ALMTheme.BG_CARD);
        table.setForeground(ALMTheme.TEXT_PRIMARY);
        table.setGridColor(ALMTheme.BORDER);
        table.setRowHeight(46);
        table.setFont(ALMTheme.fontRegular(13));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(ALMTheme.BG_CARD_HOVER);
        table.getTableHeader().setBackground(ALMTheme.BG_DEEPEST);
        table.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(ALMTheme.fontBold(12));

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(CENTER);
                switch (String.valueOf(v)) {
                    case "OPEN":
                        lbl.setForeground(ALMTheme.DANGER);
                        break;
                    case "IN_PROGRESS":
                        lbl.setForeground(ALMTheme.WARNING);
                        break;
                    case "ESCALATED":
                        lbl.setForeground(new Color(0x8B, 0x5C, 0xF6));
                        break;
                    case "RESOLVED":
                        lbl.setForeground(ALMTheme.SUCCESS);
                        break;
                    default:
                        lbl.setForeground(ALMTheme.TEXT_MUTED);
                }
                return lbl;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(230);
    }

    private void applyFilter() {
        String sel = (String) statusFilter.getSelectedItem();
        tableModel.setRowCount(0);
        for (Object[] row : DEMO_COMPLAINTS) {
            if ("All".equals(sel) || sel.equals(row[4])) {
                tableModel.addRow(new Object[] { row[0], row[1], row[2], row[3], row[4], row[5], "" });
            }
        }
    }

    private void populateDemo() {
        for (Object[] row : DEMO_COMPLAINTS) {
            tableModel.addRow(new Object[] { row[0], row[1], row[2], row[3], row[4], row[5], "" });
        }
    }

    private JButton miniBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(ALMTheme.fontBold(10));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(80, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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

    private JLabel lblOf(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ALMTheme.fontRegular(12));
        l.setForeground(ALMTheme.TEXT_SECONDARY);
        return l;
    }
}
