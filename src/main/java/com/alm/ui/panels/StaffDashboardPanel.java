package com.alm.ui.panels;

import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Floor Operations Desk (Operational Manager Workflow)
 * Handles macro-logistics: Inventory, Facilities, Staffing SLAs, and Incidents.
 */
public class StaffDashboardPanel extends JPanel {

        public StaffDashboardPanel() {
                if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_LOUNGE_INVENTORY))
                        return;
                setBackground(ALMTheme.BG_DARK);
                setLayout(new BorderLayout());

                // Header
                JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
                header.setOpaque(false);
                JLabel title = new JLabel("\uD83D\uDCDC Floor Operations Desk & Logistics Control");
                title.setFont(ALMTheme.fontBold(24));
                title.setForeground(ALMTheme.TEXT_PRIMARY);
                header.add(title);

                JButton surgeBtn = makeBtn("\u26A0\uFE0F Activate Surge Protocol (Mass Delay)", ALMTheme.DANGER);
                surgeBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                                "Surge Protocol Activated!\n- Buffet switched to Ration Mode.\n- Walk-in sales suspended.\n- Off-duty staff alerted.",
                                "Incident Response", JOptionPane.WARNING_MESSAGE));

                JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
                rightHeader.setOpaque(false);
                rightHeader.add(surgeBtn);

                JPanel top = new JPanel(new BorderLayout());
                top.setOpaque(false);
                top.add(header, BorderLayout.WEST);
                top.add(rightHeader, BorderLayout.EAST);
                add(top, BorderLayout.NORTH);

                // Main Tabbed Interface
                JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
                tabs.setFont(ALMTheme.fontBold(14));
                tabs.setBackground(ALMTheme.BG_CARD);
                tabs.setForeground(ALMTheme.TEXT_PRIMARY);
                tabs.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

                tabs.addTab("Inventory & Supply Chain", buildInventoryTab());
                tabs.addTab("Facility & Turnaround", buildFacilityTab());
                tabs.addTab("Floor Staff & SLAs", buildSLATab());
                tabs.addTab("VIP Bespoke & Incidents", buildIncidentsTab());

                add(tabs, BorderLayout.CENTER);
        }

        // ── 1. Inventory & Supply Chain ──────────────────────────────────────────
        private JPanel buildInventoryTab() {
                JPanel p = new JPanel(new GridLayout(1, 3, 20, 20));
                p.setOpaque(false);
                p.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

                // Column 1: Automated Stock
                GCard c1 = new GCard(12);
                c1.setLayout(new BoxLayout(c1, BoxLayout.Y_AXIS));
                c1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c1.add(styledLabel("Live Depletion Monitoring", 16, ALMTheme.TEXT_PRIMARY));
                c1.add(Box.createVerticalStrut(20));

                c1.add(styledLabel("Premium Champagne", 12, ALMTheme.TEXT_SECONDARY));
                JProgressBar pb1 = new JProgressBar(0, 100);
                pb1.setValue(15);
                pb1.setForeground(ALMTheme.DANGER);
                pb1.setStringPainted(true);
                c1.add(pb1);
                c1.add(Box.createVerticalStrut(15));

                c1.add(styledLabel("Hot Buffet Trays", 12, ALMTheme.TEXT_SECONDARY));
                JProgressBar pb2 = new JProgressBar(0, 100);
                pb2.setValue(45);
                pb2.setForeground(ALMTheme.WARNING);
                pb2.setStringPainted(true);
                c1.add(pb2);
                c1.add(Box.createVerticalStrut(15));

                c1.add(styledLabel("Coffee Pods & Dairy", 12, ALMTheme.TEXT_SECONDARY));
                JProgressBar pb3 = new JProgressBar(0, 100);
                pb3.setValue(85);
                pb3.setForeground(ALMTheme.SUCCESS);
                pb3.setStringPainted(true);
                c1.add(pb3);

                // Column 2: Vendor Receiving
                GCard c2 = new GCard(12);
                c2.setLayout(new BorderLayout(0, 10));
                c2.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c2.add(styledLabel("Airside Vendor Logistics", 16, ALMTheme.TEXT_PRIMARY), BorderLayout.NORTH);

                JPanel form = new JPanel(new GridLayout(4, 1, 0, 10));
                form.setOpaque(false);
                form.add(styledLabel("Scan Delivery Manifest or manually enter:", 12, ALMTheme.TEXT_MUTED));
                JTextField itemF = makeField("Item ID / SKU");
                JTextField qtyF = makeField("Quantity Received");
                form.add(itemF);
                form.add(qtyF);

                JButton logInBtn = makeBtn("\uD83D\uDCE6 Log Delivery & Update Inventory", ALMTheme.ACCENT_CYAN);
                logInBtn.addActionListener(
                                e -> JOptionPane.showMessageDialog(this,
                                                "Master Inventory updated with incoming pallet."));
                form.add(logInBtn);
                c2.add(form, BorderLayout.CENTER);

                // Column 3: Wastage Tracking
                GCard c3 = new GCard(12);
                c3.setLayout(new BorderLayout(0, 10));
                c3.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c3.add(styledLabel("Post-Service Wastage Metrics", 16, ALMTheme.TEXT_PRIMARY), BorderLayout.NORTH);

                JPanel wForm = new JPanel(new GridLayout(4, 1, 0, 10));
                wForm.setOpaque(false);
                wForm.add(styledLabel("End of service window metric input:", 12, ALMTheme.TEXT_MUTED));
                JComboBox<String> wItem = new JComboBox<>(
                                new String[] { "Breakfast Buffet Leftovers", "Expired Dairy", "Spoiled Produce" });
                wItem.setBackground(ALMTheme.BG_INPUT);
                wItem.setForeground(ALMTheme.TEXT_PRIMARY);
                JTextField kgF = makeField("Amount in KG / Units");
                wForm.add(wItem);
                wForm.add(kgF);

                JButton wasteBtn = makeBtn("\u267B\uFE0F Record Wastage & Adjust Predictive Alg", ALMTheme.WARNING);
                wasteBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                                "Wastage logged. AI ordering adjusted downward for next cycle."));
                wForm.add(wasteBtn);
                c3.add(wForm, BorderLayout.CENTER);

                p.add(c1);
                p.add(c2);
                p.add(c3);
                return p;
        }

        // ── 2. Facility & Turnaround ─────────────────────────────────────────────
        private JPanel buildFacilityTab() {
                JPanel p = new JPanel(new BorderLayout(20, 20));
                p.setOpaque(false);
                p.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

                // Shower & Maintenance combined left panel
                JPanel left = new JPanel(new GridLayout(2, 1, 0, 20));
                left.setOpaque(false);
                left.setPreferredSize(new Dimension(500, 0));

                // Showers
                GCard c1 = new GCard(12);
                c1.setLayout(new BorderLayout());
                c1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c1.add(styledLabel("Shower Suite Turnaround", 16, ALMTheme.TEXT_PRIMARY), BorderLayout.NORTH);

                DefaultTableModel shModel = new DefaultTableModel(new String[] { "Suite", "Status", "Action" }, 0);
                shModel.addRow(new Object[] { "S1", "Occupied", "" });
                shModel.addRow(new Object[] { "S2", "Needs Cleaning", "" });
                shModel.addRow(new Object[] { "S3", "Clean & Ready", "" });
                JTable shTable = makeTable(shModel);
                shTable.getColumnModel().getColumn(2)
                                .setCellRenderer((t, v, s, f, r, c) -> makeBtn("Dispatch Cleaner",
                                                ALMTheme.ACCENT_CYAN));
                shTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
                        @Override
                        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
                                JButton b = makeBtn("Cleaning...", ALMTheme.ACCENT_CYAN);
                                b.addActionListener(e -> JOptionPane.showMessageDialog(null,
                                                "Cleaning crew dispatched. Once they tap 'Done' on their tablet, the next guest will be SMS notified."));
                                return b;
                        }
                });
                c1.add(new JScrollPane(shTable), BorderLayout.CENTER);

                // Maintenance
                GCard c2 = new GCard(12);
                c2.setLayout(new BorderLayout());
                c2.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c2.add(styledLabel("Urgent Maintenance Ticketing", 16, ALMTheme.TEXT_PRIMARY), BorderLayout.NORTH);
                JPanel mForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
                mForm.setOpaque(false);
                JComboBox<String> assets = new JComboBox<>(
                                new String[] { "Espresso Machine 02", "Wi-Fi Router 04", "Men's Restroom Sink" });
                assets.setBackground(ALMTheme.BG_INPUT);
                assets.setForeground(ALMTheme.TEXT_PRIMARY);
                JButton mBtn = makeBtn("Generate Ticket", ALMTheme.WARNING);
                mBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                                "Tier 1 Ticket dispatched to Airport Maintenance Corp. downtime timer started."));
                mForm.add(assets);
                mForm.add(mBtn);
                c2.add(mForm, BorderLayout.CENTER);

                left.add(c1);
                left.add(c2);

                // Right IoT
                GCard c3 = new GCard(12);
                c3.setLayout(new BoxLayout(c3, BoxLayout.Y_AXIS));
                c3.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
                c3.add(styledLabel("\uD83C\uDF21\uFE0F Smart IoT Environment Controls", 18, ALMTheme.TEXT_PRIMARY));
                c3.add(Box.createVerticalStrut(30));

                c3.add(styledLabel("HVAC Termperature Target: 72°F", 14, ALMTheme.TEXT_SECONDARY));
                JSlider temp = new JSlider(60, 80, 72);
                temp.setOpaque(false);
                temp.setForeground(Color.WHITE);
                c3.add(temp);
                c3.add(Box.createVerticalStrut(20));

                c3.add(styledLabel("Lighting Zone: Main Dining", 14, ALMTheme.TEXT_SECONDARY));
                JComboBox<String> lightCombo = new JComboBox<>(
                                new String[] { "Circadian Daylight (Default)", "Warm Evening", "Dim / Red-Eye Mode" });
                c3.add(lightCombo);
                c3.add(Box.createVerticalStrut(20));

                c3.add(styledLabel("Background Music Playlist", 14, ALMTheme.TEXT_SECONDARY));
                JComboBox<String> musicCombo = new JComboBox<>(
                                new String[] { "Lo-Fi Focus", "Classical Calm", "Vibrant Jazz" });
                c3.add(musicCombo);

                JButton iotBtn = makeBtn("Sync Environment to Lounge", ALMTheme.ACCENT_BLUE);
                iotBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                                "API payload sent to IoT hardware controllers. Space adapting."));
                c3.add(Box.createVerticalStrut(30));
                c3.add(iotBtn);

                p.add(left, BorderLayout.WEST);
                p.add(c3, BorderLayout.CENTER);
                return p;
        }

        // ── 3. Floor Staff & SLAs ────────────────────────────────────────────────
        private JPanel buildSLATab() {
                JPanel p = new JPanel(new BorderLayout(20, 20));
                p.setOpaque(false);
                p.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

                // Deploy + Briefing
                JPanel top = new JPanel(new GridLayout(1, 2, 20, 20));
                top.setOpaque(false);
                GCard c1 = new GCard(12);
                c1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c1.setLayout(new BoxLayout(c1, BoxLayout.Y_AXIS));
                c1.add(styledLabel("Dynamic Zone Deployment", 16, ALMTheme.TEXT_PRIMARY));
                c1.add(Box.createVerticalStrut(10));
                c1.add(styledLabel("Predictive algorithm shows a surge heading to Dining. Shift floor workers now.", 12,
                                ALMTheme.TEXT_MUTED));
                c1.add(Box.createVerticalStrut(15));

                JPanel p1 = new JPanel(new GridLayout(1, 3, 10, 0));
                p1.setOpaque(false);
                JComboBox<String> workers = new JComboBox<>(
                                new String[] { "3 Floor Staff", "5 Floor Staff", "All Hands" });
                JComboBox<String> zones = new JComboBox<>(
                                new String[] { "To: Dining Zone", "To: Business Center", "To: Showers" });
                JButton zbtn = makeBtn("Reassign", ALMTheme.SUCCESS);
                zbtn.addActionListener(
                                e -> JOptionPane.showMessageDialog(this,
                                                "Staff push notifications sent. Workers re-routing."));
                p1.add(workers);
                p1.add(zones);
                p1.add(zbtn);
                c1.add(p1);

                GCard c2 = new GCard(12);
                c2.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c2.setLayout(new BorderLayout());
                c2.add(styledLabel("Shift Briefings & Tablets", 16, ALMTheme.TEXT_PRIMARY), BorderLayout.NORTH);
                JButton bbtn = makeBtn("Generate Daily BOH Tablet Checklist \uD83D\uDCCB", ALMTheme.ACCENT_BLUE);
                bbtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                                "Checklist dispatched to Back of House (BOH) iPads. Requires digital sign-off."));
                c2.add(bbtn, BorderLayout.CENTER);

                top.add(c1);
                top.add(c2);
                p.add(top, BorderLayout.NORTH);

                // SLAs Table
                GCard c3 = new GCard(12);
                c3.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c3.setLayout(new BorderLayout());
                c3.add(styledLabel("Live Contractual SLA Compliance", 16, ALMTheme.TEXT_PRIMARY), BorderLayout.NORTH);

                DefaultTableModel slaModel = new DefaultTableModel(
                                new String[] { "KPI Metric", "Target Target", "Live Current", "Status" }, 0);
                slaModel.addRow(new Object[] { "Table Clear Time", "< 3 mins", "2m 10s", "MEETING" });
                slaModel.addRow(new Object[] { "Buffet Replenish", "< 5 mins", "6m 30s", "BREACHING" });
                slaModel.addRow(new Object[] { "Front Desk Wait", "< 2 mins", "45 secs", "MEETING" });
                JTable slaTable = makeTable(slaModel);
                c3.add(new JScrollPane(slaTable), BorderLayout.CENTER);

                p.add(c3, BorderLayout.CENTER);
                return p;
        }

        // ── 4. Incidents & VIPs ──────────────────────────────────────────────────
        private JPanel buildIncidentsTab() {
                JPanel p = new JPanel(new BorderLayout());
                p.setOpaque(false);
                p.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

                GCard c = new GCard(12);
                c.setLayout(new BorderLayout(0, 15));
                c.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                c.add(styledLabel("\u2605 VIP Bespoke Coordination", 20, ALMTheme.WARNING), BorderLayout.NORTH);

                DefaultTableModel vModel = new DefaultTableModel(
                                new String[] { "VIP Client Name", "Affiliation", "Location/ETA", "Bespoke Request",
                                                "Coordinate" },
                                0);
                vModel.addRow(
                                new Object[] { "Alexander Wright", "Emirates First", "In Lounge (Zone B)",
                                                "Prepare 2x Martini", "" });
                vModel.addRow(
                                new Object[] { "Sofia Lauren", "Gov Delegate", "Landing in 15m",
                                                "Reserve Private Cabana 01", "" });

                JTable vTable = makeTable(vModel);
                vTable.getColumnModel().getColumn(4)
                                .setCellRenderer((t, v, s, f, r, col) -> makeBtn("Done", ALMTheme.SUCCESS));
                vTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
                        @Override
                        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
                                JButton b = makeBtn("Mark Fulfilled", ALMTheme.SUCCESS);
                                b.addActionListener(e -> {
                                        stopCellEditing();
                                        JOptionPane.showMessageDialog(null,
                                                        "Task marked fulfilled! VIP preferences logged successfully.");
                                });
                                return b;
                        }
                });

                c.add(new JScrollPane(vTable), BorderLayout.CENTER);
                p.add(c, BorderLayout.CENTER);
                return p;
        }

        // ── Utilities ────────────────────────────────────────────────────────────
        private JLabel styledLabel(String text, int size, Color color) {
                JLabel l = new JLabel(text);
                l.setFont(ALMTheme.fontBold(size));
                l.setForeground(color);
                return l;
        }

        private JTextField makeField(String hint) {
                JTextField f = new JTextField();
                f.setBackground(ALMTheme.BG_INPUT);
                f.setForeground(ALMTheme.TEXT_PRIMARY);
                f.setCaretColor(ALMTheme.TEXT_PRIMARY);
                f.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ALMTheme.BORDER), hint, 0,
                                0,
                                ALMTheme.fontRegular(11), ALMTheme.TEXT_MUTED));
                return f;
        }

        private JButton makeBtn(String text, Color bg) {
                JButton b = new JButton(text);
                b.setBackground(bg);
                b.setForeground(Color.WHITE);
                b.setFont(ALMTheme.fontBold(13));
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return b;
        }

        private JTable makeTable(DefaultTableModel m) {
                JTable t = new JTable(m);
                t.setBackground(ALMTheme.BG_CARD);
                t.setForeground(ALMTheme.TEXT_PRIMARY);
                t.setGridColor(ALMTheme.BORDER);
                t.setRowHeight(40);
                t.setFont(ALMTheme.fontRegular(14));
                t.getTableHeader().setBackground(ALMTheme.BG_DEEPEST);
                t.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
                return t;
        }
}
