package com.alm.ui.panels;

import com.alm.dao.AccessProviderDAO;
import com.alm.dao.AccessRequestDAO;
import com.alm.dao.LoungeDAO;
import com.alm.dao.PassengerDAO;
import com.alm.model.AccessRequest;
import com.alm.model.Lounge;
import com.alm.model.Passenger;
import com.alm.model.AccessProvider;
import com.alm.service.AccessFlowEngine;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Enhanced Access Control Panel - handles check-in requests and SmartFlow
 * engine.
 */
public class AccessControlPanel extends JPanel {

    private final AccessRequestDAO requestDAO = new AccessRequestDAO();
    private final LoungeDAO loungeDAO = new LoungeDAO();
    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final AccessProviderDAO providerDAO = new AccessProviderDAO();

    private DefaultTableModel tableModel;
    private JTable table;
    private JCheckBox showAllCheck;

    public AccessControlPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_ACCESS_REQUESTS))
            return;
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("\uD83D\uDD12 Access Control (SmartFlow)");
        title.setFont(ALMTheme.fontBold(24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        left.add(title);

        left.add(Box.createHorizontalStrut(20));
        showAllCheck = new JCheckBox("Show History (All)");
        showAllCheck.setForeground(ALMTheme.TEXT_SECONDARY);
        showAllCheck.setBackground(ALMTheme.BG_DARK);
        showAllCheck.setFocusPainted(false);
        showAllCheck.addActionListener(e -> loadData());
        left.add(showAllCheck);

        hdr.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton refreshBtn = makeBtn("↻ Refresh", ALMTheme.BG_CARD);
        refreshBtn.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        refreshBtn.setForeground(ALMTheme.TEXT_PRIMARY);
        refreshBtn.addActionListener(e -> loadData());
        right.add(refreshBtn);

        JButton addBtn = makeBtn("+ Check-in Passenger", ALMTheme.ACCENT_BLUE);
        addBtn.addActionListener(e -> showNewRequestDialog());
        right.add(addBtn);

        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    private GCard buildContent() {
        GCard card = new GCard(12);
        card.setLayout(new BorderLayout());

        String[] cols = { "ID", "Passenger", "Lounge", "Provider", "Reason", "Status", "Actions" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6;
            }
        };

        table = new JTable(tableModel);
        styleTable();

        table.getColumnModel().getColumn(6).setCellRenderer((t, val, s, f, row, col) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
            p.setOpaque(true);
            p.setBackground(s ? ALMTheme.BG_CARD_HOVER : ALMTheme.BG_CARD);
            String status = String.valueOf(tableModel.getValueAt(row, 5));
            if ("PENDING".equals(status)) {
                p.add(miniBtn("Approve", ALMTheme.SUCCESS));
                p.add(miniBtn("Deny", ALMTheme.DANGER));
            } else {
                JLabel l = new JLabel("Resolved");
                l.setForeground(ALMTheme.TEXT_MUTED);
                l.setFont(ALMTheme.fontRegular(11));
                p.add(l);
            }
            return p;
        });

        table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object v, boolean s, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
                p.setBackground(ALMTheme.BG_CARD);
                String status = String.valueOf(tableModel.getValueAt(row, 5));
                if ("PENDING".equals(status)) {
                    JButton appBtn = miniBtn("Approve", ALMTheme.SUCCESS);
                    appBtn.addActionListener(e -> {
                        stopCellEditing();
                        decideRequest(row, "APPROVED");
                    });
                    p.add(appBtn);

                    JButton denBtn = miniBtn("Deny", ALMTheme.DANGER);
                    denBtn.addActionListener(e -> {
                        stopCellEditing();
                        decideRequest(row, "DENIED");
                    });
                    p.add(denBtn);
                }
                return p;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ALMTheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

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

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(CENTER);
                String stat = String.valueOf(v);
                if ("APPROVED".equals(stat))
                    lbl.setForeground(ALMTheme.SUCCESS);
                else if ("DENIED".equals(stat))
                    lbl.setForeground(ALMTheme.DANGER);
                else
                    lbl.setForeground(ALMTheme.WARNING);
                return lbl;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        new SwingWorker<List<AccessRequest>, Void>() {
            @Override
            protected List<AccessRequest> doInBackground() {
                return showAllCheck.isSelected() ? requestDAO.getAll() : requestDAO.getPending();
            }

            @Override
            protected void done() {
                try {
                    for (AccessRequest r : get()) {
                        tableModel.addRow(new Object[] {
                                r.getId(), r.getPassengerName(), r.getLoungeName(),
                                r.getProviderName() != null ? r.getProviderName() : "WALK-IN",
                                r.getDecisionReason(), r.getStatus(), ""
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void decideRequest(int row, String newStatus) {
        int id = (int) tableModel.getValueAt(row, 0);
        String actionName = "APPROVED".equals(newStatus) ? "Approve" : "Deny";
        String reason = JOptionPane.showInputDialog(this, "Notes / Reason for " + actionName + ":");
        if (reason == null)
            return;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return requestDAO.decide(id, newStatus, reason);
            }

            @Override
            protected void done() {
                try {
                    if (get())
                        loadData();
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void showNewRequestDialog() {
        JComboBox<Passenger> passCombo = new JComboBox<>();
        passCombo.setRenderer(makeComboRenderer(true));

        JComboBox<Lounge> loungeCombo = new JComboBox<>();
        loungeCombo.setRenderer(makeComboRenderer(false));

        JComboBox<AccessProvider> provCombo = new JComboBox<>();
        provCombo.setRenderer(makeComboRenderer(false));

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                passengerDAO.getAllPassengers().forEach(passCombo::addItem);
                loungeDAO.getAllLounges().forEach(loungeCombo::addItem);
                providerDAO.getAll().forEach(provCombo::addItem);
                return null;
            }
        }.execute();

        JTextField ticketF = new JTextField();
        JTextField eliteF = new JTextField();

        Object[] msg = {
                "Passenger:", passCombo,
                "Lounge:", loungeCombo,
                "Provider (Optional):", provCombo,
                "Ticket Class (e.g. First, Business):", ticketF,
                "Elite Status (e.g. GOLD):", eliteF
        };

        if (JOptionPane.showConfirmDialog(this, msg, "New SmartFlow Access Request",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            AccessRequest r = new AccessRequest();
            if (passCombo.getSelectedItem() != null)
                r.setPassengerId(((Passenger) passCombo.getSelectedItem()).getId());
            if (loungeCombo.getSelectedItem() != null)
                r.setLoungeId(((Lounge) loungeCombo.getSelectedItem()).getId());
            if (provCombo.getSelectedItem() != null)
                r.setProviderId(((AccessProvider) provCombo.getSelectedItem()).getId());

            r.setTicketClass(ticketF.getText().trim());
            r.setEliteStatus(eliteF.getText().trim());

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    AccessFlowEngine engine = new AccessFlowEngine();
                    AccessFlowEngine.Decision dec = engine.evaluate(r);
                    r.setStatus(dec.status);
                    r.setDecisionReason(dec.reason);
                    return requestDAO.create(r);
                }

                @Override
                protected void done() {
                    loadData();
                }
            }.execute();
        }
    }

    private DefaultListCellRenderer makeComboRenderer(boolean isPassenger) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object val, int idx, boolean isS,
                    boolean hasF) {
                super.getListCellRendererComponent(list, val, idx, isS, hasF);
                if (val instanceof Passenger)
                    setText(((Passenger) val).getFullName());
                if (val instanceof Lounge)
                    setText(((Lounge) val).getName());
                if (val instanceof AccessProvider)
                    setText(((AccessProvider) val).getProviderName());
                return this;
            }
        };
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

    private JButton miniBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(ALMTheme.fontBold(11));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(80, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
