package com.alm.ui.panels;

import com.alm.dao.TransactionDAO;
import com.alm.model.Transaction;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Revenue and Transactions panel.
 * Replaces TransactionServlet + reports logic.
 */
public class RevenuePanel extends JPanel {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private JLabel totalRevenueLabel;
    private JTable table;
    private DefaultTableModel tableModel;

    public RevenuePanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.VIEW_FINANCIAL_REPORTS))
            return;
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Revenue & Financials");
        title.setFont(ALMTheme.fontBold(24));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // --- Content ---
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(false);

        // Summary Card
        GCard summaryCard = new GCard();
        summaryCard.setPreferredSize(new Dimension(0, 100));
        summaryCard.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 20));

        JLabel revTitle = new JLabel("Total Lifetime Revenue:");
        revTitle.setFont(ALMTheme.fontRegular(16));
        revTitle.setForeground(ALMTheme.TEXT_SECONDARY);
        summaryCard.add(revTitle);

        totalRevenueLabel = new JLabel("₹0.00");
        totalRevenueLabel.setFont(ALMTheme.fontBold(32));
        totalRevenueLabel.setForeground(ALMTheme.ACCENT_CYAN);
        summaryCard.add(totalRevenueLabel);

        content.add(summaryCard, BorderLayout.NORTH);

        // Transactions Table
        String[] columns = { "ID", "Request ID", "Amount", "Method", "Status", "Date" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ALMTheme.BG_DARK);

        GCard tableCard = new GCard(14);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
                "Recent Transactions", 0, 0, ALMTheme.fontBold(14), ALMTheme.TEXT_PRIMARY));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        loadData();
    }

    private void styleTable() {
        table.setBackground(ALMTheme.BG_CARD);
        table.setForeground(ALMTheme.TEXT_PRIMARY);
        table.setGridColor(ALMTheme.BORDER);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(ALMTheme.BG_INPUT);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            private BigDecimal total;
            private List<Transaction> transactions;

            @Override
            protected Void doInBackground() {
                total = transactionDAO.getTotalRevenue();
                transactions = transactionDAO.getAll();
                return null;
            }

            @Override
            protected void done() {
                totalRevenueLabel.setText("₹" + String.format("%,.2f", total));
                tableModel.setRowCount(0);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                for (Transaction t : transactions) {
                    tableModel.addRow(new Object[] {
                            t.getId(),
                            t.getRequestId(),
                            "₹" + t.getAmount(),
                            t.getPaymentMethod(),
                            t.getPaymentStatus(),
                            sdf.format(t.getProcessedAt())
                    });
                }
            }
        }.execute();
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            label.setHorizontalAlignment(JLabel.CENTER);
            String status = (String) value;
            if ("SUCCESS".equalsIgnoreCase(status))
                label.setForeground(ALMTheme.SUCCESS);
            else if ("FAILED".equalsIgnoreCase(status))
                label.setForeground(ALMTheme.DANGER);
            else
                label.setForeground(ALMTheme.WARNING);
            return label;
        }
    }
}
