package com.alm.ui.panels;

import com.alm.dao.FlightDAO;
import com.alm.model.Flight;
import com.alm.service.AviationstackConfig;
import com.alm.service.FlightDataCache;
import com.alm.service.FlightSyncService;
import com.alm.ui.theme.ALMTheme;
import com.alm.ui.components.GCard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Enterprise Flight Sync Center — Real-time API management panel.
 *
 * Shows:
 * - API sync status (source, quota, cache age)
 * - KPI cards (last sync, records, crowd estimate, quota)
 * - Flight table with terminal/gate/delay columns
 * - Configurable auto-sync intervals
 * - CSV fallback upload
 */
public class SyncCenterPanel extends JPanel {

    private final FlightSyncService syncService = new FlightSyncService();
    private final FlightDAO flightDAO = new FlightDAO();
    private final FlightDataCache cache = FlightDataCache.getInstance();

    private JLabel lastSyncLabel;
    private JLabel recordsUpdatedLabel;
    private JLabel estimatedCrowdLabel;
    private JLabel quotaLabel;
    private JLabel syncSourceLabel;
    private JLabel cacheAgeLabel;
    private JLabel statusBarLabel;
    private DefaultTableModel tableModel;

    public SyncCenterPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ALMTheme.BG_DEEP);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildDataArea(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        refreshTableData();
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);

        JLabel title = new JLabel("Enterprise Flight Sync Center");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);

        String apiStatus = AviationstackConfig.isApiConfigured()
                ? "● Aviationstack API Connected"
                : "○ No API Key — Using Mock Data";
        JLabel subtitle = new JLabel(apiStatus);
        subtitle.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitle.setForeground(AviationstackConfig.isApiConfigured()
                ? ALMTheme.SUCCESS
                : ALMTheme.TEXT_MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);

        h.add(left, BorderLayout.WEST);
        return h;
    }

    private JPanel buildDataArea() {
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setOpaque(false);

        // Top Dashboard Cards — 2 rows of KPIs
        JPanel KPIPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        KPIPanel.setOpaque(false);

        lastSyncLabel = new JLabel("Never");
        recordsUpdatedLabel = new JLabel("0");
        estimatedCrowdLabel = new JLabel("0");
        quotaLabel = new JLabel(String.valueOf(cache.getRemainingQuota()));
        syncSourceLabel = new JLabel("None");

        KPIPanel.add(buildKpiCard("Last API Sync", lastSyncLabel, ALMTheme.ACCENT_BLUE));
        KPIPanel.add(buildKpiCard("Sync Source", syncSourceLabel, new Color(139, 92, 246)));
        KPIPanel.add(buildKpiCard("Records Updated", recordsUpdatedLabel, ALMTheme.SUCCESS));
        KPIPanel.add(buildKpiCard("Est. 2hr Arrivals", estimatedCrowdLabel, ALMTheme.WARNING));
        KPIPanel.add(buildKpiCard("API Quota Left", quotaLabel,
                cache.isQuotaLow() ? ALMTheme.DANGER : ALMTheme.ACCENT_BLUE));

        main.add(KPIPanel, BorderLayout.NORTH);

        // Flights Table — now with Terminal, Gate, Delay columns
        String[] cols = { "Flight No", "Status", "Airline", "Route",
                "Terminal/Gate", "Departure", "Delay", "Load (Calc)" };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));

        // Color-code the delay column
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                if (val != null) {
                    String text = val.toString();
                    if (text.contains("delayed")) {
                        c.setForeground(ALMTheme.DANGER);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(ALMTheme.SUCCESS);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel tableCont = new GCard(20);
        tableCont.setLayout(new BorderLayout());

        // Cache age indicator
        cacheAgeLabel = new JLabel("  Cache: " + cache.getFlightCacheAge());
        cacheAgeLabel.setFont(new Font("Inter", Font.ITALIC, 11));
        cacheAgeLabel.setForeground(ALMTheme.TEXT_MUTED);

        JPanel tableHeaderRow = new JPanel(new BorderLayout());
        tableHeaderRow.setOpaque(false);
        JLabel tTitle = new JLabel("  Local DB Flights Cache (Next 8Hrs)");
        tTitle.setFont(new Font("Inter", Font.BOLD, 14));
        tTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        tableHeaderRow.add(tTitle, BorderLayout.WEST);
        tableHeaderRow.add(cacheAgeLabel, BorderLayout.EAST);

        tableCont.add(tableHeaderRow, BorderLayout.NORTH);
        tableCont.add(scrollPane, BorderLayout.CENTER);

        main.add(tableCont, BorderLayout.CENTER);

        // Sidebar Actions
        JPanel actions = new GCard(20);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setBorder(new EmptyBorder(20, 20, 20, 20));
        actions.setPreferredSize(new Dimension(260, 0));

        JLabel actLabel = new JLabel("Sync Controls");
        actLabel.setFont(new Font("Inter", Font.BOLD, 15));
        actions.add(actLabel);
        actions.add(Box.createVerticalStrut(20));

        // Sync Now button
        JButton syncBtn = new JButton("⟳ Force API Sync Now");
        syncBtn.setBackground(ALMTheme.ACCENT_BLUE);
        syncBtn.setForeground(Color.WHITE);
        syncBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        syncBtn.addActionListener(e -> {
            syncBtn.setEnabled(false);
            syncBtn.setText("Syncing...");
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    syncService.syncNow();
                    return null;
                }

                @Override
                protected void done() {
                    updateKPIs();
                    refreshTableData();
                    syncBtn.setEnabled(true);
                    syncBtn.setText("⟳ Force API Sync Now");
                }
            }.execute();
        });
        actions.add(syncBtn);

        actions.add(Box.createVerticalStrut(8));

        // Smart Sync button (only syncs if cache is stale)
        JButton smartSyncBtn = new JButton("Smart Sync (if stale)");
        smartSyncBtn.setBackground(new Color(16, 185, 129));
        smartSyncBtn.setForeground(Color.WHITE);
        smartSyncBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        smartSyncBtn.addActionListener(e -> {
            new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() {
                    return syncService.smartSync();
                }

                @Override
                protected void done() {
                    try {
                        int result = get();
                        updateKPIs();
                        if (result > 0)
                            refreshTableData();
                        else
                            JOptionPane.showMessageDialog(SyncCenterPanel.this,
                                    "Cache is still fresh (" + cache.getFlightCacheAge() + "). No sync needed.",
                                    "Smart Sync", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ignored) {
                    }
                }
            }.execute();
        });
        actions.add(smartSyncBtn);

        actions.add(Box.createVerticalStrut(20));

        // CSV Upload
        JButton csvBtn = new JButton("⬆ Upload CSV (Fallback)");
        csvBtn.setBackground(ALMTheme.BG_DEEPEST);
        csvBtn.setForeground(ALMTheme.TEXT_MUTED);
        csvBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        csvBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Flight Schedule CSV");
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToUpload = fileChooser.getSelectedFile();
                if (!fileToUpload.getName().toLowerCase().endsWith(".csv")) {
                    JOptionPane.showMessageDialog(this, "Please select an actual .csv file", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    int updated = syncService.syncFromCSV(fileToUpload);
                    updateKPIs();
                    refreshTableData();
                    JOptionPane.showMessageDialog(this,
                            "CSV Processed successfully.\nUpdated/Upserted: " + updated + " records.");
                }
            }
        });
        actions.add(csvBtn);

        actions.add(Box.createVerticalStrut(20));

        // Auto-Sync with configurable interval
        JPanel autoSyncRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        autoSyncRow.setOpaque(false);
        autoSyncRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JCheckBox autoUpdateCheck = new JCheckBox("Auto-Sync every");
        autoUpdateCheck.setOpaque(false);
        autoUpdateCheck.setForeground(ALMTheme.TEXT_PRIMARY);

        JComboBox<String> intervalCombo = new JComboBox<>(
                new String[] { "5 min", "15 min", "30 min", "1 hour", "24 hour" });
        intervalCombo.setSelectedIndex(2); // default 30 min
        int[] intervalMs = { 5 * 60_000, 15 * 60_000, 30 * 60_000, 60 * 60_000, 24 * 60 * 60_000 };

        autoSyncRow.add(autoUpdateCheck);
        autoSyncRow.add(intervalCombo);

        // Use smart sync for auto — saves quota
        Timer syncTimer = new Timer(intervalMs[2], e -> {
            syncService.smartSync();
            SwingUtilities.invokeLater(() -> {
                updateKPIs();
                refreshTableData();
            });
        });

        autoUpdateCheck.addActionListener(e -> {
            if (autoUpdateCheck.isSelected()) {
                syncTimer.setDelay(intervalMs[intervalCombo.getSelectedIndex()]);
                syncTimer.start();
            } else {
                syncTimer.stop();
            }
        });
        intervalCombo.addActionListener(e -> {
            if (syncTimer.isRunning()) {
                syncTimer.setDelay(intervalMs[intervalCombo.getSelectedIndex()]);
            }
        });

        actions.add(autoSyncRow);

        // API configuration hint
        if (!AviationstackConfig.isApiConfigured()) {
            actions.add(Box.createVerticalStrut(20));
            JLabel hint = new JLabel("<html><b>Tip:</b> Set your API key in<br>"
                    + "<code>api.properties</code> to<br>sync real flight data.</html>");
            hint.setFont(new Font("Inter", Font.PLAIN, 11));
            hint.setForeground(ALMTheme.WARNING);
            actions.add(hint);
        }

        main.add(actions, BorderLayout.EAST);

        return main;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(5, 0, 0, 0));

        statusBarLabel = new JLabel(syncService.getStatusSummary());
        statusBarLabel.setFont(new Font("Inter", Font.PLAIN, 11));
        statusBarLabel.setForeground(ALMTheme.TEXT_MUTED);

        bar.add(statusBarLabel, BorderLayout.WEST);
        return bar;
    }

    private JPanel buildKpiCard(String title, JLabel valueDisplay, Color accent) {
        GCard card = new GCard(15);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Inter", Font.BOLD, 11));
        tLbl.setForeground(ALMTheme.TEXT_MUTED);

        valueDisplay.setFont(new Font("Inter", Font.BOLD, 24));
        valueDisplay.setForeground(accent);

        card.add(tLbl, BorderLayout.NORTH);
        card.add(valueDisplay, BorderLayout.CENTER);
        return card;
    }

    private void updateKPIs() {
        if (syncService.getLastSyncTime() > 0) {
            lastSyncLabel.setText(
                    new SimpleDateFormat("HH:mm:ss").format(new Date(syncService.getLastSyncTime())));
        }
        recordsUpdatedLabel.setText(String.valueOf(syncService.getLastRecordsUpdatedCount()));
        syncSourceLabel.setText(syncService.getLastSyncSource());
        quotaLabel.setText(String.valueOf(cache.getRemainingQuota()));
        cacheAgeLabel.setText("  Cache: " + cache.getFlightCacheAge());
        statusBarLabel.setText(syncService.getStatusSummary());

        // Color quota indicator
        quotaLabel.setForeground(cache.isQuotaLow() ? ALMTheme.DANGER : ALMTheme.ACCENT_BLUE);
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        List<Flight> flights = flightDAO.getUpcomingFlights(8);

        int totalLoungeCrowd = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        for (Flight f : flights) {
            totalLoungeCrowd += f.getEstimatedLoungePassengers();

            tableModel.addRow(new Object[] {
                    f.getFlightNumber(),
                    f.getStatus(),
                    f.getAirline(),
                    f.getOrigin() + " → " + f.getDestination(),
                    f.getTerminalGateDisplay(),
                    sdf.format(f.getScheduledDeparture()),
                    f.getDelayDisplay(),
                    "~" + f.getEstimatedLoungePassengers() + " pax"
            });
        }
        estimatedCrowdLabel.setText(String.valueOf(totalLoungeCrowd));
        cacheAgeLabel.setText("  Cache: " + cache.getFlightCacheAge());
    }
}
