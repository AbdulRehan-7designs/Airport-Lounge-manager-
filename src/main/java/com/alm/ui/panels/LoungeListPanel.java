package com.alm.ui.panels;

import com.alm.dao.LoungeDAO;
import com.alm.model.Lounge;
import com.alm.ui.components.GCard;
import com.alm.ui.core.NavigationManager;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel to list all lounges in a modern table.
 * Replaces lounge-list.jsp.
 */
public class LoungeListPanel extends JPanel {

    private final LoungeDAO loungeDAO = new LoungeDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    public LoungeListPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_LOUNGE_AVAILABILITY))
            return;
        setupUI();
    }

    private void setupUI() {
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Lounge Directory");
        title.setFont(ALMTheme.fontBold(24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Search bar (simplified)
        JTextField searchField = new JTextField(15);
        searchField.setBackground(ALMTheme.BG_INPUT);
        searchField.setForeground(ALMTheme.TEXT_PRIMARY);
        searchField.setCaretColor(ALMTheme.TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ALMTheme.BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchWrap.setOpaque(false);
        searchWrap.add(new JLabel("\uD83D\uDD0D ")); // magnifying glass
        searchWrap.add(searchField);
        header.add(searchWrap, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- Table ---
        String[] columns = { "ID", "Name", "Location", "Terminal", "Capacity", "Status" };
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

        // --- Table Click Listener ---
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int id = (int) tableModel.getValueAt(row, 0);
                        // Navigate to detail
                        openLoungeDetail(id);
                    }
                }
            }
        });

        // Initial Load
        loadData();
    }

    private void styleTable() {
        table.setBackground(ALMTheme.BG_CARD);
        table.setForeground(ALMTheme.TEXT_PRIMARY);
        table.setGridColor(ALMTheme.BORDER);
        table.setRowHeight(40);
        table.setFont(ALMTheme.fontRegular(14));
        table.setSelectionBackground(new Color(59, 130, 246, 40));
        table.setSelectionForeground(ALMTheme.TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.getTableHeader().setBackground(ALMTheme.BG_INPUT);
        table.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(ALMTheme.fontBold(12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ALMTheme.BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        SwingWorker<List<Lounge>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Lounge> doInBackground() {
                return loungeDAO.getAllLounges();
            }

            @Override
            protected void done() {
                try {
                    List<Lounge> lounges = get();
                    for (Lounge l : lounges) {
                        tableModel.addRow(new Object[] {
                                l.getId(),
                                l.getName(),
                                l.getLocation(),
                                l.getTerminal(),
                                l.getCapacity(),
                                l.getStatus()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void openLoungeDetail(int id) {
        LoungeDetailPanel detail = (LoungeDetailPanel) NavigationManager.getInstance().getPanel("lounge-detail");
        if (detail != null) {
            detail.loadLounge(id);
            NavigationManager.getInstance().navigateTo("lounge-detail");
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            label.setHorizontalAlignment(JLabel.CENTER);
            String status = (String) value;
            if ("ACTIVE".equalsIgnoreCase(status)) {
                label.setForeground(ALMTheme.SUCCESS);
            } else if ("CLOSED".equalsIgnoreCase(status)) {
                label.setForeground(ALMTheme.DANGER);
            } else {
                label.setForeground(ALMTheme.WARNING);
            }
            return label;
        }
    }
}
