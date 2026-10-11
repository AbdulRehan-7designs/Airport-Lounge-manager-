package com.alm.ui.panels;

import com.alm.model.Passenger;
import com.alm.model.User;
import com.alm.dao.PassengerDAO;
import com.alm.ui.components.GCard;
import com.alm.ui.core.SessionContext;
import com.alm.ui.theme.ALMTheme;
import com.alm.util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Visit History Panel for Passengers.
 */
public class VisitHistoryPanel extends JPanel {

    private DefaultTableModel tableModel;

    public VisitHistoryPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_OWN_VISIT_HISTORY))
            return;
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());

        setupUI();
        loadVisits();
    }

    private void setupUI() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GCard card = new GCard(20);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("\uD83D\uDCCB My Visit History", JLabel.LEFT);
        title.setFont(ALMTheme.fontBold(24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(ALMTheme.ACCENT_BLUE);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(ALMTheme.fontBold(13));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.addActionListener(e -> loadVisits());
        header.add(refreshBtn, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        String[] columns = { "Visit ID", "Lounge", "Check-In", "Check-Out", "Status", "Action" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // allow clicking button
            }
        };

        JTable table = new JTable(tableModel);
        table.setBackground(ALMTheme.BG_CARD);
        table.setForeground(ALMTheme.TEXT_PRIMARY);
        table.setRowHeight(45);
        table.setFont(ALMTheme.fontRegular(14));
        table.setShowVerticalLines(false);
        table.setGridColor(ALMTheme.BORDER);
        table.setSelectionBackground(ALMTheme.BG_CARD_HOVER);

        table.getTableHeader().setBackground(ALMTheme.BG_DEEPEST);
        table.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(ALMTheme.fontBold(13));

        // Status renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(CENTER);
                String status = v == null ? "" : v.toString();
                if ("ACTIVE".equals(status)) {
                    lbl.setForeground(ALMTheme.SUCCESS);
                    lbl.setFont(ALMTheme.fontBold(14));
                } else {
                    lbl.setForeground(ALMTheme.TEXT_MUTED);
                }
                return lbl;
            }
        });

        // View Bill Button Renderer and Editor
        table.getColumnModel().getColumn(5).setCellRenderer((t, v, s, f, row, col) -> {
            JButton btn = new JButton("View Bill");
            btn.setBackground(ALMTheme.BG_INPUT);
            btn.setForeground(Color.WHITE);
            btn.setFont(ALMTheme.fontBold(12));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            return btn;
        });

        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int row, int col) {
                JButton btn = new JButton("View Bill");
                btn.setBackground(ALMTheme.BG_INPUT);
                btn.setForeground(Color.WHITE);
                btn.addActionListener(e -> {
                    String visitIdStr = tableModel.getValueAt(row, 0).toString();
                    String lounge = tableModel.getValueAt(row, 1).toString();
                    String in = tableModel.getValueAt(row, 2).toString();
                    String out = tableModel.getValueAt(row, 3).toString();

                    String message = "=== VISIT BILL ===\n" +
                            "Visit ID: " + visitIdStr + "\n" +
                            "Lounge: " + lounge + "\n" +
                            "Check-In: " + in + "\n" +
                            "Check-Out: " + out + "\n\n" +
                            "Base Fee: ₹0.00\n" +
                            "Services: ₹0.00\n" +
                            "-----------------\n" +
                            "TOTAL DUE: ₹0.00\n" +
                            "(Covered by Membership)";

                    JTextArea ta = new JTextArea(message);
                    ta.setEditable(false);
                    ta.setFont(ALMTheme.fontRegular(14));
                    ta.setBackground(ALMTheme.BG_CARD);
                    ta.setForeground(ALMTheme.TEXT_PRIMARY);

                    JOptionPane.showMessageDialog(VisitHistoryPanel.this, new JScrollPane(ta), "Bill Detail",
                            JOptionPane.INFORMATION_MESSAGE);
                    stopCellEditing();
                });
                return btn;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ALMTheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);

        center.add(card, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void loadVisits() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> data = new ArrayList<>();
                User u = SessionContext.getInstance().getCurrentUser();
                if (u == null)
                    return data;

                Passenger p = new PassengerDAO().findByUserId(u.getId());
                if (p == null)
                    return data;

                String sql = "SELECT v.visit_id, l.name, v.check_in_time, v.check_out_time " +
                        "FROM visits v " +
                        "JOIN lounges l ON v.lounge_id = l.lounge_id " +
                        "WHERE v.passenger_id = ? " +
                        "ORDER BY v.check_in_time DESC";

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");

                try (Connection conn = DBConnection.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, p.getId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int vid = rs.getInt("visit_id");
                            String lname = rs.getString("name");
                            java.sql.Timestamp inTime = rs.getTimestamp("check_in_time");
                            java.sql.Timestamp outTime = rs.getTimestamp("check_out_time");

                            String inStr = inTime != null ? sdf.format(inTime) : "-";
                            String outStr = outTime != null ? sdf.format(outTime) : "ONGOING";
                            String status = outTime == null ? "ACTIVE" : "COMPLETED";

                            data.add(new Object[] { "#" + vid, lname, inStr, outStr, status, "View Bill" });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return data;
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    List<Object[]> rows = get();
                    for (Object[] r : rows) {
                        tableModel.addRow(r);
                    }
                    if (rows.isEmpty()) {
                        // Fallback message just for feedback
                        tableModel.addRow(new Object[] { "-", "No visit history found", "-", "-", "-", "" });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
