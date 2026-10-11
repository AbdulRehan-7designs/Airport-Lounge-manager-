package com.alm.ui.panels;

import com.alm.dao.AirportDAO;
import com.alm.dao.LoungeDAO;
import com.alm.model.Airport;
import com.alm.model.Lounge;
import com.alm.ui.components.GCard;
import com.alm.ui.core.SessionContext;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LocationManagementPanel extends JPanel {

    private final AirportDAO airportDAO = new AirportDAO();
    private final LoungeDAO loungeDAO = new LoungeDAO();

    private JComboBox<ComboItem> filterAirportCombo;
    private JComboBox<ComboItem> filterLoungeCombo;
    private JPanel detailsPanel;

    public LocationManagementPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.MANAGE_LOUNGES))
            return;

        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Lounge & Airport Management");
        title.setFont(ALMTheme.fontBold(24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Center Content Group
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 20, 20));
        contentGrid.setOpaque(false);

        contentGrid.add(buildAddFormsCard());
        contentGrid.add(buildSyncCard());

        add(contentGrid, BorderLayout.CENTER);
    }

    private GCard buildAddFormsCard() {
        GCard card = new GCard(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title1 = new JLabel("✈ Add New Airport");
        title1.setFont(ALMTheme.fontBold(18));
        title1.setForeground(ALMTheme.ACCENT_CYAN);
        card.add(title1);
        card.add(Box.createVerticalStrut(10));

        JTextField apName = makeField();
        JTextField apCode = makeField();
        JTextField apCity = makeField();
        JTextField apCountry = makeField();

        card.add(makeRow("Airport Name:", apName));
        card.add(makeRow("IATA Code:", apCode));
        card.add(makeRow("City:", apCity));
        card.add(makeRow("Country:", apCountry));

        JButton addApBtn = makeBtn("Create Airport", ALMTheme.SUCCESS);
        addApBtn.addActionListener(e -> {
            Airport a = new Airport(0, apName.getText(), apCode.getText(), apCity.getText(), apCountry.getText(), true,
                    null);
            if (airportDAO.create(a)) {
                JOptionPane.showMessageDialog(this, "Airport Created!");
                refreshCombos();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create Airport.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(makeRow("", addApBtn));

        card.add(Box.createVerticalStrut(30));
        JLabel title2 = new JLabel("🛋 Add New Lounge");
        title2.setFont(ALMTheme.fontBold(18));
        title2.setForeground(ALMTheme.ACCENT_CYAN);
        card.add(title2);
        card.add(Box.createVerticalStrut(10));

        filterAirportCombo = new JComboBox<>();
        filterAirportCombo.setBackground(ALMTheme.BG_INPUT);
        filterAirportCombo.setForeground(ALMTheme.TEXT_PRIMARY);

        JTextField lgName = makeField();
        JTextField lgTerm = makeField();
        JTextField lgCap = makeField();

        card.add(makeRow("Select Airport:", filterAirportCombo));
        card.add(makeRow("Lounge Name:", lgName));
        card.add(makeRow("Terminal:", lgTerm));
        card.add(makeRow("Capacity:", lgCap));

        JButton addLgBtn = makeBtn("Create Lounge", ALMTheme.SUCCESS);
        addLgBtn.addActionListener(e -> {
            ComboItem item = (ComboItem) filterAirportCombo.getSelectedItem();
            if (item == null)
                return;
            try {
                Lounge l = new Lounge(0, item.id, lgName.getText(), lgTerm.getText(), Integer.parseInt(lgCap.getText()),
                        "ACTIVE");
                l.setActive(true);
                if (loungeDAO.create(l)) {
                    JOptionPane.showMessageDialog(this, "Lounge Created!");
                    refreshCombos();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create Lounge.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Capacity must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(makeRow("", addLgBtn));

        return card;
    }

    private GCard buildSyncCard() {
        GCard card = new GCard(14);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        JLabel title = new JLabel("🔄 Sync Active Lounge Details");
        title.setFont(ALMTheme.fontBold(18));
        title.setForeground(ALMTheme.ACCENT_CYAN);
        top.add(title);
        top.add(Box.createVerticalStrut(10));

        filterLoungeCombo = new JComboBox<>();
        filterLoungeCombo.setBackground(ALMTheme.BG_INPUT);
        filterLoungeCombo.setForeground(ALMTheme.TEXT_PRIMARY);
        filterLoungeCombo.addActionListener(e -> updateLoungeDetails());

        JPanel row = makeRow("Select Lounge Context:", filterLoungeCombo);
        top.add(row);

        detailsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        top.add(detailsPanel);

        JButton syncBtn = makeBtn("Switch Context & Sync", ALMTheme.ACCENT_BLUE);
        syncBtn.addActionListener(e -> {
            ComboItem item = (ComboItem) filterLoungeCombo.getSelectedItem();
            if (item != null) {
                SessionContext.getInstance().setCurrentLoungeId(item.id);
                JOptionPane.showMessageDialog(this, "Session context active lounge switched to: " + item.name
                        + "\nDetails synced globally within SessionContext.");
            }
        });
        top.add(makeRow("", syncBtn));

        card.add(top, BorderLayout.NORTH);

        refreshCombos();
        return card;
    }

    private void updateLoungeDetails() {
        ComboItem item = (ComboItem) filterLoungeCombo.getSelectedItem();
        detailsPanel.removeAll();
        if (item != null) {
            Lounge l = loungeDAO.getById(item.id);
            if (l != null) {
                addDetail("Name", l.getName());
                addDetail("Location/Airport ID", l.getLocation() + " (ap=" + l.getAirportId() + ")");
                addDetail("Terminal", l.getTerminal());
                addDetail("Capacity", String.valueOf(l.getCapacity()));
                addDetail("Status", l.getStatus() + (l.isActive() ? "" : " (INACTIVE)"));
            }
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void addDetail(String key, String val) {
        JLabel k = new JLabel(key + ": ");
        k.setFont(ALMTheme.fontBold(14));
        k.setForeground(ALMTheme.TEXT_SECONDARY);
        JLabel v = new JLabel(val);
        v.setFont(ALMTheme.fontRegular(14));
        v.setForeground(ALMTheme.TEXT_PRIMARY);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(k);
        p.add(v);
        detailsPanel.add(p);
    }

    private void refreshCombos() {
        filterAirportCombo.removeAllItems();
        List<Airport> airports = airportDAO.getAll();
        for (Airport a : airports) {
            filterAirportCombo.addItem(new ComboItem(a.getId(), a.getIataCode() + " - " + a.getName()));
        }

        filterLoungeCombo.removeAllItems();
        List<Lounge> lounges = loungeDAO.getAllLounges();
        for (Lounge l : lounges) {
            filterLoungeCombo.addItem(new ComboItem(l.getId(), l.getName() + " (" + l.getTerminal() + ")"));
        }
    }

    private JTextField makeField() {
        JTextField tf = new JTextField(15);
        tf.setBackground(ALMTheme.BG_INPUT);
        tf.setForeground(ALMTheme.TEXT_PRIMARY);
        tf.setCaretColor(ALMTheme.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ALMTheme.BORDER),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return tf;
    }

    private JPanel makeRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        if (!label.isEmpty()) {
            JLabel l = new JLabel(label);
            l.setFont(ALMTheme.fontRegular(14));
            l.setForeground(ALMTheme.TEXT_SECONDARY);
            l.setPreferredSize(new Dimension(140, 30));
            p.add(l, BorderLayout.WEST);
        }
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return p;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(ALMTheme.fontBold(14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static class ComboItem {
        int id;
        String name;

        public ComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
