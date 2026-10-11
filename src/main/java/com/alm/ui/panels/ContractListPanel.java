package com.alm.ui.panels;

import com.alm.dao.AccessProviderDAO;
import com.alm.model.AccessProvider;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Contracts & Partner Agreements Panel.
 * Admin view for managing airline, credit card, and aggregator contracts.
 */
public class ContractListPanel extends JPanel {

    private final AccessProviderDAO providerDAO = new AccessProviderDAO();
    private DefaultTableModel tableModel;
    private JTable table;

    public ContractListPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.MANAGE_ACCESS_PROVIDERS))
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
        JLabel title = new JLabel("\uD83D\uDCC4 Partner Agreements & Contracts");
        title.setFont(ALMTheme.fontBold(24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        left.add(title);
        hdr.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton refreshBtn = makeBtn("↻ Refresh", ALMTheme.BG_CARD);
        refreshBtn.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        refreshBtn.setForeground(ALMTheme.TEXT_PRIMARY);
        refreshBtn.addActionListener(e -> loadData());
        right.add(refreshBtn);

        JButton addBtn = makeBtn("+ Add Contract", ALMTheme.ACCENT_BLUE);
        addBtn.addActionListener(e -> showAddDialog());
        right.add(addBtn);

        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    private GCard buildContent() {
        GCard card = new GCard(12);
        card.setLayout(new BorderLayout());

        String[] cols = { "ID", "Partner Name", "Type", "Status", "Actions" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 4;
            }
        };

        table = new JTable(tableModel);
        styleTable();

        table.getColumnModel().getColumn(4).setCellRenderer((t, v, s, f, row, col) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            p.setOpaque(true);
            p.setBackground(s ? ALMTheme.BG_CARD_HOVER : ALMTheme.BG_CARD);
            p.add(miniBtn("Edit", ALMTheme.WARNING));
            p.add(miniBtn("Terminate", ALMTheme.DANGER));
            return p;
        });

        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object v, boolean s, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
                p.setBackground(ALMTheme.BG_CARD);
                JButton editBtn = miniBtn("Edit", ALMTheme.WARNING);
                editBtn.addActionListener(e -> {
                    stopCellEditing();
                    showEditDialog(row);
                });
                p.add(editBtn);

                JButton termBtn = miniBtn("Terminate", ALMTheme.DANGER);
                termBtn.addActionListener(e -> {
                    int opt = JOptionPane.showConfirmDialog(ContractListPanel.this,
                            "Are you sure you want to terminate this contract?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (opt == JOptionPane.YES_OPTION) {
                        try {
                            AccessProvider provider = providerDAO
                                    .getById(Integer.parseInt(tableModel.getValueAt(row, 0).toString()));
                            if (provider != null) {
                                provider.setActive(false);
                                providerDAO.update(provider);
                            }
                        } catch (Exception ex) {
                        }
                        tableModel.setValueAt("INACTIVE", row, 3);
                        stopCellEditing();
                        table.repaint();
                    }
                });
                p.add(termBtn);
                return p;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ALMTheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void loadData() {
        new SwingWorker<List<AccessProvider>, Void>() {
            @Override
            protected List<AccessProvider> doInBackground() {
                return providerDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (AccessProvider p : get()) {
                        tableModel.addRow(new Object[] {
                                p.getId(), p.getProviderName(), p.getProviderType(),
                                p.isActive() ? "ACTIVE" : "INACTIVE", ""
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void showAddDialog() {
        JTextField nameF = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(
                new String[] { "AIRLINE", "CREDIT_CARD", "AGGREGATOR", "CORPORATE" });
        JCheckBox activeCheck = new JCheckBox("Active", true);

        Object[] msg = {
                "Partner Name:", nameF,
                "Provider Type:", typeCombo,
                "Status:", activeCheck
        };

        if (JOptionPane.showConfirmDialog(this, msg, "Add Contract",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String name = nameF.getText().trim();
            if (name.isEmpty())
                return;
            AccessProvider p = new AccessProvider();
            p.setProviderName(name);
            p.setProviderType((String) typeCombo.getSelectedItem());
            p.setActive(activeCheck.isSelected());

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return providerDAO.insert(p);
                }

                @Override
                protected void done() {
                    loadData();
                }
            }.execute();
        }
    }

    private void showEditDialog(int row) {
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        JTextField nameF = new JTextField(tableModel.getValueAt(row, 1).toString());

        if (JOptionPane.showConfirmDialog(this,
                new Object[] { "New Partner Name:", nameF }, "Edit Contract",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        AccessProvider p = providerDAO.getById(id);
                        if (p != null) {
                            p.setProviderName(nameF.getText().trim());
                            return providerDAO.update(p);
                        }
                    } catch (Exception ex) {
                    }
                    return false;
                }

                @Override
                protected void done() {
                    loadData();
                }
            }.execute();
        }
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

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(CENTER);
                lbl.setForeground("ACTIVE".equals(v) ? ALMTheme.SUCCESS : ALMTheme.DANGER);
                return lbl;
            }
        });

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setFont(ALMTheme.fontBold(12));
                String type = String.valueOf(v);
                if ("CREDIT_CARD".equals(type))
                    lbl.setForeground(new Color(0x60, 0xA5, 0xFA));
                else if ("AIRLINE".equals(type))
                    lbl.setForeground(new Color(0xA7, 0x8B, 0xFA));
                else if ("AGGREGATOR".equals(type))
                    lbl.setForeground(new Color(0x34, 0xD3, 0x99));
                else
                    lbl.setForeground(ALMTheme.WARNING);
                return lbl;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
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
