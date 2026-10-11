package com.alm.ui.panels;

import com.alm.dao.PassengerDAO;
import com.alm.model.Passenger;
import com.alm.ui.components.GButton;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern passenger management panel.
 * Replaces passenger-list.jsp.
 */
public class PassengerListPanel extends JPanel {

    private final PassengerDAO passengerDAO = new PassengerDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    public PassengerListPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.MANAGE_PASSENGERS))
            return;
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());

        setupUI();
    }

    private void setupUI() {
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Passenger Directory");
        title.setFont(ALMTheme.fontBold(24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        GButton addBtn = new GButton("+ Add Passenger", ALMTheme.ACCENT_BLUE);
        addBtn.addActionListener(e -> showAddPassengerDialog());
        header.add(addBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- Table ---
        String[] columns = { "ID", "Full Name", "Passport #", "Nationality", "Phone", "Email", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        scrollPane.getViewport().setBackground(ALMTheme.BG_DARK);

        GCard tableCard = new GCard(14);
        tableCard.setLayout(new BorderLayout());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        loadData();
    }

    private void styleTable() {
        table.setBackground(ALMTheme.BG_CARD);
        table.setForeground(ALMTheme.TEXT_PRIMARY);
        table.setGridColor(ALMTheme.BORDER);
        table.setRowHeight(40);
        table.setFont(ALMTheme.fontRegular(14));
        table.setSelectionBackground(new Color(59, 130, 246, 40));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.getTableHeader().setBackground(ALMTheme.BG_INPUT);
        table.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(ALMTheme.fontBold(12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ALMTheme.BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        new SwingWorker<List<Passenger>, Void>() {
            @Override
            protected List<Passenger> doInBackground() {
                return passengerDAO.getAllPassengers();
            }

            @Override
            protected void done() {
                try {
                    List<Passenger> list = get();
                    for (Passenger p : list) {
                        tableModel.addRow(new Object[] {
                                p.getId(),
                                p.getFullName(),
                                p.getPassportNumber(),
                                p.getNationality(),
                                p.getPhone(),
                                p.getEmail(),
                                p.isActive() ? "ACTIVE" : "INACTIVE"
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void showAddPassengerDialog() {
        JTextField nameF = new JTextField();
        JTextField passF = new JTextField();
        JTextField natF = new JTextField();
        JTextField phoneF = new JTextField();
        JTextField emailF = new JTextField();

        Object[] message = {
                "Full Name:", nameF,
                "Passport Number:", passF,
                "Nationality:", natF,
                "Phone:", phoneF,
                "Email:", emailF
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Register New Passenger",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameF.getText().trim();
            String passport = passF.getText().trim();
            if (name.isEmpty() || passport.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Passport are required.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Passenger p = new Passenger();
            p.setFullName(name);
            p.setPassportNumber(passport);
            p.setNationality(natF.getText().trim());
            p.setPhone(phoneF.getText().trim());
            p.setEmail(emailF.getText().trim());
            p.setActive(true);

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return passengerDAO.addPassenger(p);
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
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            label.setHorizontalAlignment(JLabel.CENTER);
            if ("ACTIVE".equals(value))
                label.setForeground(ALMTheme.SUCCESS);
            else
                label.setForeground(ALMTheme.DANGER);
            return label;
        }
    }
}
