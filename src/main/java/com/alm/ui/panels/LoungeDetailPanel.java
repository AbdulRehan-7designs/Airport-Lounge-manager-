package com.alm.ui.panels;

import com.alm.dao.LoungeAmenityDAO;
import com.alm.dao.LoungeDAO;
import com.alm.model.Lounge;
import com.alm.model.LoungeAmenity;
import com.alm.ui.components.GButton;
import com.alm.ui.components.GCard;
import com.alm.ui.core.NavigationManager;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Detailed view of a lounge including its amenities.
 * Replaces lounge-detail.jsp.
 */
public class LoungeDetailPanel extends JPanel {

    private final LoungeDAO loungeDAO = new LoungeDAO();
    private final LoungeAmenityDAO amenityDAO = new LoungeAmenityDAO();

    private Lounge currentLounge;
    private JLabel nameLabel, subLabel, capacityLabel, statusLabel;
    private DefaultTableModel amenityModel;
    private JTable amenityTable;

    public LoungeDetailPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_LOUNGE_AVAILABILITY))
            return;
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Top: Header & Back Button ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        GButton backBtn = new GButton("\u2190 Back", ALMTheme.BG_CARD);
        backBtn.addActionListener(e -> NavigationManager.getInstance().navigateTo("lounges"));
        header.add(backBtn, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        nameLabel = new JLabel("Lounge Name");
        nameLabel.setFont(ALMTheme.fontBold(24));
        nameLabel.setForeground(ALMTheme.TEXT_PRIMARY);
        subLabel = new JLabel("Location • Terminal");
        subLabel.setFont(ALMTheme.fontRegular(14));
        subLabel.setForeground(ALMTheme.TEXT_SECONDARY);
        titlePanel.add(nameLabel);
        titlePanel.add(subLabel);
        header.add(titlePanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // --- Content Area ---
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // -- Info Card --
        GCard infoCard = new GCard();
        infoCard.setLayout(new GridLayout(2, 2, 20, 10));
        infoCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        infoCard.add(createLabel("Capacity"));
        capacityLabel = createValue("0");
        infoCard.add(capacityLabel);

        infoCard.add(createLabel("Status"));
        statusLabel = createValue("UNKNOWN");
        infoCard.add(statusLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        content.add(infoCard, gbc);

        // -- Amenities Card --
        GCard amenitiesCard = new GCard();
        amenitiesCard.setLayout(new BorderLayout());
        amenitiesCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel amTitle = new JLabel("Amenities & Inventory");
        amTitle.setFont(ALMTheme.fontBold(18));
        amTitle.setForeground(ALMTheme.TEXT_PRIMARY);
        amenitiesCard.add(amTitle, BorderLayout.NORTH);

        String[] amCols = { "ID", "Item Name", "Total", "Available" };
        amenityModel = new DefaultTableModel(amCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        amenityTable = new JTable(amenityModel);
        styleAmenityTable();
        amenitiesCard.add(new JScrollPane(amenityTable), BorderLayout.CENTER);

        // Add Amenity Button
        GButton addAmBtn = new GButton("+ Add Amenity", ALMTheme.ACCENT_BLUE);
        addAmBtn.addActionListener(e -> showAddAmenityDialog());
        JPanel bWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bWrap.setOpaque(false);
        bWrap.add(addAmBtn);
        amenitiesCard.add(bWrap, BorderLayout.SOUTH);

        gbc.gridy = 1;
        gbc.weighty = 0.8;
        content.add(amenitiesCard, gbc);

        add(content, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ALMTheme.fontRegular(14));
        l.setForeground(ALMTheme.TEXT_SECONDARY);
        return l;
    }

    private JLabel createValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ALMTheme.fontBold(16));
        l.setForeground(ALMTheme.TEXT_PRIMARY);
        return l;
    }

    private void styleAmenityTable() {
        amenityTable.setBackground(ALMTheme.BG_CARD);
        amenityTable.setForeground(ALMTheme.TEXT_PRIMARY);
        amenityTable.setGridColor(ALMTheme.BORDER);
        amenityTable.setRowHeight(35);
        amenityTable.getTableHeader().setBackground(ALMTheme.BG_INPUT);
        amenityTable.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        amenityTable.setShowVerticalLines(false);
    }

    public void loadLounge(int id) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<LoungeAmenity> amenities;

            @Override
            protected Void doInBackground() {
                currentLounge = loungeDAO.getById(id);
                amenities = amenityDAO.getByLoungeId(id);
                return null;
            }

            @Override
            protected void done() {
                if (currentLounge != null) {
                    nameLabel.setText(currentLounge.getName());
                    subLabel.setText(currentLounge.getLocation() + " \u2022 Terminal " + currentLounge.getTerminal());
                    capacityLabel.setText(String.valueOf(currentLounge.getCapacity()));
                    statusLabel.setText(currentLounge.getStatus());
                    statusLabel.setForeground(
                            "ACTIVE".equalsIgnoreCase(currentLounge.getStatus()) ? ALMTheme.SUCCESS : ALMTheme.DANGER);

                    amenityModel.setRowCount(0);
                    for (LoungeAmenity a : amenities) {
                        amenityModel.addRow(
                                new Object[] { a.getId(), a.getItemName(), a.getTotalCount(), a.getAvailableCount() });
                    }
                }
            }
        };
        worker.execute();
    }

    private void showAddAmenityDialog() {
        if (currentLounge == null)
            return;

        JTextField nameField = new JTextField();
        JTextField totalField = new JTextField("0");
        JTextField availField = new JTextField("0");

        Object[] message = {
                "Item Name:", nameField,
                "Total Count:", totalField,
                "Available Count:", availField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Amenity", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            try {
                int total = Integer.parseInt(totalField.getText());
                int avail = Integer.parseInt(availField.getText());
                if (!name.isEmpty()) {
                    LoungeAmenity a = new LoungeAmenity();
                    a.setLoungeId(currentLounge.getId());
                    a.setItemName(name);
                    a.setTotalCount(total);
                    a.setAvailableCount(avail);

                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() {
                            return amenityDAO.add(a);
                        }

                        @Override
                        protected void done() {
                            try {
                                if (get())
                                    loadLounge(currentLounge.getId());
                            } catch (Exception ignored) {
                            }
                        }
                    }.execute();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid numbers provided.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
