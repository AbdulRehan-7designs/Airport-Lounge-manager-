package com.alm.ui.panels;

import com.alm.dao.ReceptionServiceDAO;
import com.alm.dao.PassengerDAO;
import com.alm.ui.components.GButton;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Receptionist Services Hub — Tabbed panel providing:
 * 1. Active Visits (live view of checked-in passengers)
 * 2. Premium Services (add shower, pod, dining, etc.)
 * 3. Guest Management (add companion guests to visits)
 * 4. Waitlist (manage queue when lounge is full)
 * 5. Complaints (log issues for ops manager)
 * 6. Flight Info (upcoming departures)
 */
public class ReceptionServicesPanel extends JPanel {

    private final ReceptionServiceDAO svcDAO = new ReceptionServiceDAO();
    private final PassengerDAO paxDAO = new PassengerDAO();
    private final SimpleDateFormat dtFmt = new SimpleDateFormat("dd MMM HH:mm");

    // Tables
    private DefaultTableModel activeVisitsModel, servicesModel, guestsModel, waitlistModel, complaintsModel,
            flightsModel;

    public ReceptionServicesPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Color.WHITE);
        hdr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ALMTheme.BORDER),
                new EmptyBorder(14, 20, 14, 20)));
        JLabel title = new JLabel("Receptionist Services Hub");
        title.setFont(ALMTheme.fontBold(18));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Manage visits, services, guests, waitlist & complaints");
        sub.setFont(ALMTheme.fontRegular(11));
        sub.setForeground(ALMTheme.TEXT_MUTED);
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(sub);
        hdr.add(titlePanel, BorderLayout.WEST);

        GButton refreshAll = new GButton("\u21BB Refresh All", ALMTheme.ACCENT_BLUE);
        refreshAll.addActionListener(e -> refreshAllTabs());
        hdr.add(refreshAll, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(ALMTheme.fontBold(12));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Active Visits", buildActiveVisitsTab());
        tabs.addTab("Premium Services", buildServicesTab());
        tabs.addTab("Guest Management", buildGuestsTab());
        tabs.addTab("Waitlist", buildWaitlistTab());
        tabs.addTab("Complaints", buildComplaintsTab());
        tabs.addTab("Flight Info", buildFlightsTab());
        add(tabs, BorderLayout.CENTER);

        // Initial data load
        SwingUtilities.invokeLater(this::refreshAllTabs);
    }

    private int getCurrentLoungeId() {
        Integer id = com.alm.ui.core.SessionContext.getInstance().getCurrentLoungeId();
        return id != null ? id : 1;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 1: ACTIVE VISITS
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildActiveVisitsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = { "Visit ID", "Passenger", "Seat", "Access Type", "Class", "Membership", "Check-In Time" };
        activeVisitsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tbl = styledTable(activeVisitsModel);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(60);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        GButton refreshBtn = new GButton("\u21BB Refresh", ALMTheme.ACCENT_BLUE);
        refreshBtn.addActionListener(e -> loadActiveVisits());
        GButton changeSeatBtn = new GButton("Change Seat", ALMTheme.ACCENT_INDIGO);
        changeSeatBtn.addActionListener(e -> doChangeSeat(tbl));
        GButton checkoutBtn = new GButton("Check-Out Selected", ALMTheme.WARNING);
        checkoutBtn.addActionListener(e -> doCheckoutFromList(tbl));
        btns.add(refreshBtn);
        btns.add(changeSeatBtn);
        btns.add(checkoutBtn);

        p.add(btns, BorderLayout.NORTH);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return p;
    }

    private void loadActiveVisits() {
        activeVisitsModel.setRowCount(0);
        new SwingWorker<List<Map<String, Object>>, Void>() {
            protected List<Map<String, Object>> doInBackground() {
                return svcDAO.getActiveVisits(getCurrentLoungeId());
            }

            protected void done() {
                try {
                    for (Map<String, Object> v : get()) {
                        activeVisitsModel.addRow(new Object[] {
                                v.get("visit_id"), v.get("full_name"), v.get("assigned_seat"),
                                v.get("access_type"), v.get("ticket_class"),
                                v.get("membership_type") != null ? v.get("membership_type") : "—",
                                v.get("check_in_time") != null ? dtFmt.format(v.get("check_in_time")) : "—"
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void doChangeSeat(JTable tbl) {
        int row = tbl.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a visit first.");
            return;
        }
        int paxId = (int) activeVisitsModel.getValueAt(row, 0); // visit_id — we need passenger_id
        String paxName = (String) activeVisitsModel.getValueAt(row, 1);
        String currentSeat = (String) activeVisitsModel.getValueAt(row, 2);
        String newSeat = JOptionPane.showInputDialog(this,
                "Change seat for " + paxName + "\nCurrent: " + currentSeat + "\n\nEnter new seat (e.g. B12, E25):",
                "Change Seat", JOptionPane.PLAIN_MESSAGE);
        if (newSeat != null && !newSeat.trim().isEmpty()) {
            // We need passenger_id — search by name
            com.alm.model.Passenger p = paxDAO.findByFullName(paxName);
            if (p != null && svcDAO.changeSeat(p.getId(), newSeat.trim().toUpperCase())) {
                JOptionPane.showMessageDialog(this,
                        "Seat changed to " + newSeat.trim().toUpperCase() + " for " + paxName);
                loadActiveVisits();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to change seat.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doCheckoutFromList(JTable tbl) {
        int row = tbl.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a visit first.");
            return;
        }
        String paxName = (String) activeVisitsModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Check out " + paxName + "?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            com.alm.model.Passenger p = paxDAO.findByFullName(paxName);
            if (p != null && paxDAO.checkoutPassenger(p.getId())) {
                JOptionPane.showMessageDialog(this, paxName + " checked out successfully.");
                loadActiveVisits();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 2: PREMIUM SERVICES
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildServicesTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Top: service catalog
        String[] cols = { "Service", "Category", "Price", "Description" };
        servicesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable svcTbl = styledTable(servicesModel);

        // Bottom: add service to passenger
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addPanel.setOpaque(false);
        addPanel.setBorder(BorderFactory.createTitledBorder("Add Service to Checked-In Passenger"));
        JTextField paxField = new JTextField(15);
        paxField.setFont(ALMTheme.fontRegular(12));
        paxField.setToolTipText("Passenger name");
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        GButton addSvcBtn = new GButton("Add Service", ALMTheme.SUCCESS);
        addSvcBtn.addActionListener(e -> {
            int row = svcTbl.getSelectedRow();
            String paxName = paxField.getText().trim();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a service from the catalog.");
                return;
            }
            if (paxName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter passenger name.");
                return;
            }

            com.alm.model.Passenger pax = paxDAO.findByFullName(paxName);
            if (pax == null) {
                JOptionPane.showMessageDialog(this, "Passenger not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Integer visitId = svcDAO.getActiveVisitId(pax.getId());
            if (visitId == null) {
                JOptionPane.showMessageDialog(this, "No active visit for " + paxName, "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get service details from the model's hidden data
            List<Map<String, Object>> catalog = svcDAO.getServicesForLounge(getCurrentLoungeId());
            if (row >= catalog.size())
                return;
            Map<String, Object> svc = catalog.get(row);
            int qty = (int) qtySpinner.getValue();
            double price = ((java.math.BigDecimal) svc.get("price")).doubleValue() * qty;

            if (svcDAO.addServiceToVisit(visitId, (int) svc.get("service_id"), pax.getId(), qty, price)) {
                JOptionPane.showMessageDialog(this,
                        "Added: " + svc.get("service_name") + " x" + qty + " = ₹" + String.format("%.2f", price)
                                + "\nTo: " + paxName,
                        "\u2714 Service Added", JOptionPane.INFORMATION_MESSAGE);
                paxField.setText("");
            }
        });

        addPanel.add(new JLabel("Passenger:"));
        addPanel.add(paxField);
        addPanel.add(new JLabel("Qty:"));
        addPanel.add(qtySpinner);
        addPanel.add(addSvcBtn);

        GButton refreshBtn = new GButton("\u21BB Load Catalog", ALMTheme.ACCENT_BLUE);
        refreshBtn.addActionListener(e -> loadServiceCatalog());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(refreshBtn, BorderLayout.NORTH);
        top.add(new JScrollPane(svcTbl), BorderLayout.CENTER);

        p.add(top, BorderLayout.CENTER);
        p.add(addPanel, BorderLayout.SOUTH);
        return p;
    }

    private void loadServiceCatalog() {
        servicesModel.setRowCount(0);
        new SwingWorker<List<Map<String, Object>>, Void>() {
            protected List<Map<String, Object>> doInBackground() {
                return svcDAO.getServicesForLounge(getCurrentLoungeId());
            }

            protected void done() {
                try {
                    for (Map<String, Object> s : get()) {
                        servicesModel.addRow(new Object[] {
                                s.get("service_name"), s.get("category"),
                                "₹" + s.get("price"), s.get("description")
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 3: GUEST MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildGuestsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = { "Guest Name", "Document", "Fee", "Added At" };
        guestsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tbl = styledTable(guestsModel);

        // Add guest form
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createTitledBorder("Add Guest to Active Visit"));
        JTextField hostField = new JTextField(12);
        hostField.setToolTipText("Host passenger name");
        JTextField gNameField = new JTextField(12);
        gNameField.setToolTipText("Guest name");
        JTextField gDocField = new JTextField(10);
        gDocField.setToolTipText("ID/Passport (optional)");
        JSpinner feeSpinner = new JSpinner(new SpinnerNumberModel(20.00, 0.00, 500.00, 5.00));
        GButton addBtn = new GButton("Add Guest", ALMTheme.SUCCESS);
        addBtn.addActionListener(e -> {
            String host = hostField.getText().trim();
            String gName = gNameField.getText().trim();
            if (host.isEmpty() || gName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill host and guest name.");
                return;
            }
            com.alm.model.Passenger pax = paxDAO.findByFullName(host);
            if (pax == null) {
                JOptionPane.showMessageDialog(this, "Host not found.");
                return;
            }
            Integer visitId = svcDAO.getActiveVisitId(pax.getId());
            if (visitId == null) {
                JOptionPane.showMessageDialog(this, "No active visit for " + host);
                return;
            }
            double fee = (double) feeSpinner.getValue();
            if (svcDAO.addGuestToVisit(visitId, pax.getId(), gName, gDocField.getText().trim(), fee)) {
                JOptionPane.showMessageDialog(this,
                        "Guest " + gName + " added (Fee: ₹" + String.format("%.2f", fee) + ")");
                hostField.setText("");
                gNameField.setText("");
                gDocField.setText("");
                // Refresh with this visit
                loadGuestsForHost(visitId);
            }
        });

        form.add(new JLabel("Host:"));
        form.add(hostField);
        form.add(new JLabel("Guest:"));
        form.add(gNameField);
        form.add(new JLabel("Doc:"));
        form.add(gDocField);
        form.add(new JLabel("Fee ₹:"));
        form.add(feeSpinner);
        form.add(addBtn);

        p.add(form, BorderLayout.NORTH);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return p;
    }

    private void loadGuestsForHost(int visitId) {
        guestsModel.setRowCount(0);
        for (Map<String, Object> g : svcDAO.getGuestsForVisit(visitId)) {
            guestsModel.addRow(new Object[] {
                    g.get("guest_name"), g.get("guest_document"),
                    "₹" + g.get("fee"), g.get("added_at") != null ? dtFmt.format(g.get("added_at")) : "—"
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 4: WAITLIST
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildWaitlistTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = { "#", "Passenger", "Priority", "Requested At", "Status" };
        waitlistModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tbl = styledTable(waitlistModel);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        GButton refreshBtn = new GButton("\u21BB Refresh", ALMTheme.ACCENT_BLUE);
        refreshBtn.addActionListener(e -> loadWaitlist());
        GButton addBtn = new GButton("Add to Waitlist", ALMTheme.SUCCESS);
        addBtn.addActionListener(e -> doAddToWaitlist());
        GButton notifyBtn = new GButton("Notify Selected", ALMTheme.ACCENT_INDIGO);
        notifyBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0)
                return;
            int wlId = (int) waitlistModel.getValueAt(row, 0);
            svcDAO.updateWaitlistStatus(wlId, "NOTIFIED");
            JOptionPane.showMessageDialog(this, "Passenger notified!");
            loadWaitlist();
        });
        GButton seatBtn = new GButton("Mark Seated", ALMTheme.SUCCESS);
        seatBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0)
                return;
            int wlId = (int) waitlistModel.getValueAt(row, 0);
            svcDAO.updateWaitlistStatus(wlId, "SEATED");
            loadWaitlist();
        });

        btns.add(refreshBtn);
        btns.add(addBtn);
        btns.add(notifyBtn);
        btns.add(seatBtn);

        p.add(btns, BorderLayout.NORTH);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return p;
    }

    private void loadWaitlist() {
        waitlistModel.setRowCount(0);
        new SwingWorker<List<Map<String, Object>>, Void>() {
            protected List<Map<String, Object>> doInBackground() {
                return svcDAO.getWaitlist(getCurrentLoungeId());
            }

            protected void done() {
                try {
                    for (Map<String, Object> w : get()) {
                        waitlistModel.addRow(new Object[] {
                                w.get("waitlist_id"), w.get("full_name"), w.get("priority"),
                                w.get("requested_at") != null ? dtFmt.format(w.get("requested_at")) : "—",
                                w.get("status")
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void doAddToWaitlist() {
        String name = JOptionPane.showInputDialog(this, "Enter passenger name to add to waitlist:");
        if (name == null || name.trim().isEmpty())
            return;
        com.alm.model.Passenger p = paxDAO.findByFullName(name.trim());
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Passenger not found.");
            return;
        }
        String[] priorities = { "NORMAL", "HIGH", "VIP" };
        String priority = (String) JOptionPane.showInputDialog(this, "Select priority:", "Priority",
                JOptionPane.PLAIN_MESSAGE, null, priorities, "NORMAL");
        if (priority != null) {
            svcDAO.addToWaitlist(p.getId(), getCurrentLoungeId(), priority);
            JOptionPane.showMessageDialog(this, p.getFullName() + " added to waitlist (" + priority + ")");
            loadWaitlist();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 5: COMPLAINTS
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildComplaintsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = { "ID", "Passenger", "Category", "Description", "Severity", "Status", "Reported At" };
        complaintsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tbl = styledTable(complaintsModel);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(250);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        GButton refreshBtn = new GButton("\u21BB Refresh", ALMTheme.ACCENT_BLUE);
        refreshBtn.addActionListener(e -> loadComplaints());
        GButton logBtn = new GButton("Log Complaint", ALMTheme.DANGER);
        logBtn.addActionListener(e -> doLogComplaint());
        btns.add(refreshBtn);
        btns.add(logBtn);

        p.add(btns, BorderLayout.NORTH);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return p;
    }

    private void loadComplaints() {
        complaintsModel.setRowCount(0);
        new SwingWorker<List<Map<String, Object>>, Void>() {
            protected List<Map<String, Object>> doInBackground() {
                return svcDAO.getOpenComplaints(getCurrentLoungeId());
            }

            protected void done() {
                try {
                    for (Map<String, Object> c : get()) {
                        complaintsModel.addRow(new Object[] {
                                c.get("complaint_id"), c.get("full_name"), c.get("category"),
                                c.get("description"), c.get("severity"), c.get("status"),
                                c.get("reported_at") != null ? dtFmt.format(c.get("reported_at")) : "—"
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void doLogComplaint() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        form.setOpaque(false);
        JTextField nameField = new JTextField();
        String[] categories = { "FOOD", "WIFI", "SEAT", "STAFF", "CLEANLINESS", "OTHER" };
        JComboBox<String> catBox = new JComboBox<>(categories);
        String[] severities = { "LOW", "MEDIUM", "HIGH", "CRITICAL" };
        JComboBox<String> sevBox = new JComboBox<>(severities);
        sevBox.setSelectedItem("MEDIUM");
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);

        form.add(new JLabel("Passenger Name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(catBox);
        form.add(new JLabel("Severity:"));
        form.add(sevBox);
        form.add(new JLabel("Description:"));
        form.add(new JScrollPane(descArea));

        int result = JOptionPane.showConfirmDialog(this, form, "Log Complaint", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            com.alm.model.Passenger pax = name.isEmpty() ? null : paxDAO.findByFullName(name);
            int paxId = pax != null ? pax.getId() : 0;
            String reportedBy = com.alm.ui.core.SessionContext.getInstance().getCurrentUser() != null
                    ? com.alm.ui.core.SessionContext.getInstance().getCurrentUser().getUsername()
                    : "Reception";
            svcDAO.logComplaint(paxId, getCurrentLoungeId(), null,
                    (String) catBox.getSelectedItem(), descArea.getText().trim(),
                    (String) sevBox.getSelectedItem(), reportedBy);
            JOptionPane.showMessageDialog(this, "Complaint logged successfully.");
            loadComplaints();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 6: FLIGHT INFO
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildFlightsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = { "Flight #", "Airline", "Departure", "Origin", "Destination", "Gate", "Status" };
        flightsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tbl = styledTable(flightsModel);

        // Status column coloring
        tbl.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setFont(ALMTheme.fontBold(11));
                if (!s) {
                    String st = String.valueOf(v);
                    if (st.contains("DELAY"))
                        l.setForeground(ALMTheme.DANGER);
                    else if (st.contains("ON_TIME") || st.contains("SCHEDULED"))
                        l.setForeground(ALMTheme.SUCCESS);
                    else if (st.contains("BOARDING"))
                        l.setForeground(ALMTheme.WARNING);
                    else
                        l.setForeground(ALMTheme.TEXT_MUTED);
                }
                return l;
            }
        });

        GButton refreshBtn = new GButton("\u21BB Load Flights", ALMTheme.ACCENT_BLUE);
        refreshBtn.addActionListener(e -> loadFlights());
        p.add(refreshBtn, BorderLayout.NORTH);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return p;
    }

    private void loadFlights() {
        flightsModel.setRowCount(0);
        new SwingWorker<List<Map<String, Object>>, Void>() {
            protected List<Map<String, Object>> doInBackground() {
                return svcDAO.getUpcomingFlights(30);
            }

            protected void done() {
                try {
                    for (Map<String, Object> f : get()) {
                        flightsModel.addRow(new Object[] {
                                f.get("flight_number"), f.get("airline"),
                                f.get("departure_time") != null ? dtFmt.format(f.get("departure_time")) : "—",
                                f.get("origin"), f.get("destination"),
                                f.get("gate_number") != null ? f.get("gate_number") : "—",
                                f.get("status")
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════
    private void refreshAllTabs() {
        loadActiveVisits();
        loadServiceCatalog();
        loadWaitlist();
        loadComplaints();
        loadFlights();
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable tbl = new JTable(model);
        tbl.setFont(ALMTheme.fontRegular(11));
        tbl.setForeground(ALMTheme.TEXT_PRIMARY);
        tbl.setBackground(Color.WHITE);
        tbl.setRowHeight(32);
        tbl.setSelectionBackground(ALMTheme.ACCENT_BLUE_LIGHT);
        tbl.setSelectionForeground(ALMTheme.TEXT_PRIMARY);
        tbl.setGridColor(ALMTheme.BORDER_LIGHT);
        tbl.getTableHeader().setFont(ALMTheme.fontBold(11));
        tbl.getTableHeader().setBackground(ALMTheme.BG_DEEPEST);
        tbl.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return tbl;
    }
}
