package com.alm.ui.panels;

import com.alm.dao.UserDAO;
import com.alm.model.User;
import com.alm.service.AuthService;
import com.alm.ui.components.GCard;
import com.alm.ui.theme.ALMTheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Staff Management Panel (Admin only).
 * Lists all users, allows role change, account activation/deactivation,
 * and creates new staff accounts.
 */
public class StaffManagementPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> roleFilter;

    private static final String[] ALL_ROLES = {
            "ADMIN", "OPERATIONS_MANAGER", "SUPERVISOR", "RECEPTION", "AUDITOR", "PASSENGER"
    };

    public StaffManagementPanel() {
        if (!com.alm.ui.core.PermissionUtil.verify(this, com.alm.config.Permission.MANAGE_STAFF))
            return;
        setBackground(ALMTheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadData();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel title = new JLabel("\uD83D\uDC65 Staff & User Management");
        title.setFont(ALMTheme.fontBold(22));
        title.setForeground(ALMTheme.TEXT_PRIMARY);
        left.add(title);

        left.add(new JLabel("Filter:") {
            {
                setForeground(ALMTheme.TEXT_SECONDARY);
                setFont(ALMTheme.fontRegular(12));
            }
        });
        roleFilter = new JComboBox<>(new String[] { "All Roles", "ADMIN", "OPERATIONS_MANAGER", "SUPERVISOR",
                "RECEPTION", "AUDITOR", "PASSENGER" });
        roleFilter.setBackground(ALMTheme.BG_INPUT);
        roleFilter.setForeground(ALMTheme.TEXT_PRIMARY);
        roleFilter.setFont(ALMTheme.fontRegular(12));
        roleFilter.addActionListener(e -> loadData());
        left.add(roleFilter);

        hdr.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton refreshBtn = makeBtn("↻ Refresh", ALMTheme.BG_CARD);
        refreshBtn.setBorder(BorderFactory.createLineBorder(ALMTheme.BORDER));
        refreshBtn.setForeground(ALMTheme.TEXT_PRIMARY);
        refreshBtn.addActionListener(e -> loadData());
        right.add(refreshBtn);

        JButton addBtn = makeBtn("+ Add User", ALMTheme.ACCENT_BLUE);
        addBtn.addActionListener(e -> showAddUserDialog());
        right.add(addBtn);

        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    // ── Table card ─────────────────────────────────────────────────────────────
    private GCard buildTableCard() {
        GCard card = new GCard(12);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        String[] cols = { "ID", "Username", "Email", "Role", "Status", "Change Role", "Toggle Active" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5 || c == 6;
            }
        };

        table = new JTable(tableModel);
        styleTable();

        // "Change Role" dropdown editor
        JComboBox<String> roleEditor = new JComboBox<>(ALL_ROLES);
        roleEditor.setBackground(ALMTheme.BG_INPUT);
        roleEditor.setForeground(ALMTheme.TEXT_PRIMARY);
        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(roleEditor) {
            @Override
            public boolean stopCellEditing() {
                int row = table.getSelectedRow();
                String newRole = (String) roleEditor.getSelectedItem();
                Object idObj = tableModel.getValueAt(row, 0);
                if (idObj != null && newRole != null) {
                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() {
                            return userDAO.updateRole(Long.parseLong(idObj.toString()), newRole);
                        }

                        @Override
                        protected void done() {
                            try {
                                if (get()) {
                                    tableModel.setValueAt(newRole, row, 3);
                                    tableModel.setValueAt(newRole, row, 5);
                                    JOptionPane.showMessageDialog(StaffManagementPanel.this,
                                            "Role updated to " + newRole, "Role Changed",
                                            JOptionPane.INFORMATION_MESSAGE);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.execute();
                }
                return super.stopCellEditing();
            }
        });

        // "Toggle Active" button renderer
        table.getColumnModel().getColumn(6).setCellRenderer((t, v, sel, foc, row, col) -> {
            Object status = tableModel.getValueAt(row, 4);
            boolean active = "ACTIVE".equals(status);
            JButton btn = makeBtn(active ? "Deactivate" : "Activate", active ? ALMTheme.DANGER : ALMTheme.SUCCESS);
            btn.setFont(ALMTheme.fontBold(11));
            return btn;
        });

        table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private int editRow;

            @Override
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int row, int col) {
                editRow = row;
                Object status = tableModel.getValueAt(row, 4);
                boolean active = "ACTIVE".equals(status);
                JButton btn = makeBtn(active ? "Deactivate" : "Activate", active ? ALMTheme.DANGER : ALMTheme.SUCCESS);
                btn.addActionListener(e -> {
                    Object idObj = tableModel.getValueAt(editRow, 0);
                    boolean newStatus = !"ACTIVE".equals(tableModel.getValueAt(editRow, 4));
                    if (idObj != null) {
                        userDAO.updateActiveStatus(Long.parseLong(idObj.toString()), newStatus);
                        tableModel.setValueAt(newStatus ? "ACTIVE" : "INACTIVE", editRow, 4);
                        table.repaint();
                    }
                    stopCellEditing();
                });
                return btn;
            }
        });

        // Double-click to see user detail
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.columnAtPoint(e.getPoint()) < 5) {
                    int row = table.rowAtPoint(e.getPoint());
                    showUserDetailDialog(row);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ALMTheme.BG_DARK);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void styleTable() {
        table.setBackground(ALMTheme.BG_CARD);
        table.setForeground(ALMTheme.TEXT_PRIMARY);
        table.setGridColor(ALMTheme.BORDER);
        table.setRowHeight(42);
        table.setFont(ALMTheme.fontRegular(13));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(ALMTheme.BG_CARD_HOVER);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(ALMTheme.BG_DEEPEST);
        table.getTableHeader().setForeground(ALMTheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(ALMTheme.fontBold(12));

        // Status coloring
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(CENTER);
                lbl.setForeground("ACTIVE".equals(v) ? ALMTheme.SUCCESS : ALMTheme.DANGER);
                return lbl;
            }
        });

        // Role badge coloring
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String role = v == null ? "" : v.toString();
                switch (role) {
                    case "ADMIN":
                        lbl.setForeground(ALMTheme.DANGER);
                        break;
                    case "OPERATIONS_MANAGER":
                        lbl.setForeground(ALMTheme.WARNING);
                        break;
                    case "SUPERVISOR":
                        lbl.setForeground(ALMTheme.ACCENT_CYAN);
                        break;
                    case "RECEPTION":
                        lbl.setForeground(ALMTheme.SUCCESS);
                        break;
                    case "AUDITOR":
                        lbl.setForeground(ALMTheme.ACCENT_PURPLE);
                        break;
                    case "PASSENGER":
                        lbl.setForeground(ALMTheme.TEXT_SECONDARY);
                        break;
                    default:
                        lbl.setForeground(ALMTheme.TEXT_PRIMARY);
                }
                return lbl;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
    }

    // ── Data Loading ──────────────────────────────────────────────────────────
    private void loadData() {
        String selectedRole = (String) (roleFilter != null ? roleFilter.getSelectedItem() : "All Roles");
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() {
                return userDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    List<User> users = get();
                    for (User u : users) {
                        if (!"All Roles".equals(selectedRole) && !selectedRole.equals(u.getRole()))
                            continue;
                        tableModel.addRow(new Object[] {
                                u.getId(), u.getUsername(), u.getEmail() == null ? "-" : u.getEmail(),
                                u.getRole(), u.isActive() ? "ACTIVE" : "INACTIVE",
                                u.getRole(), // editable column pre-filled with current role
                                ""
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────
    private void showAddUserDialog() {
        JTextField usernameF = new JTextField();
        JTextField emailF = new JTextField();
        JPasswordField passF = new JPasswordField();
        JComboBox<String> roleCombo = new JComboBox<>(ALL_ROLES);

        Object[] fields = {
                "Username:", usernameF,
                "Email:", emailF,
                "Password:", passF,
                "Role:", roleCombo
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Add New User", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION)
            return;

        String username = usernameF.getText().trim();
        String email = emailF.getText().trim();
        String password = new String(passF.getPassword()).trim();
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password are required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hashed = new AuthService().hashPassword(password);
        User u = new User(0, username, email, hashed, role, true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return userDAO.createUser(u);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(StaffManagementPanel.this, "User created successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(StaffManagementPanel.this, "Failed. Username may already exist.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void showUserDetailDialog(int row) {
        if (row < 0)
            return;
        String id = String.valueOf(tableModel.getValueAt(row, 0));
        String username = String.valueOf(tableModel.getValueAt(row, 1));
        String email = String.valueOf(tableModel.getValueAt(row, 2));
        String role = String.valueOf(tableModel.getValueAt(row, 3));
        String status = String.valueOf(tableModel.getValueAt(row, 4));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ALMTheme.BG_CARD);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        String[][] info = { { "User ID:", id }, { "Username:", username }, { "Email:", email }, { "Role:", role },
                { "Status:", status } };
        for (String[] row2 : info) {
            g.gridx = 0;
            g.weightx = 0.3;
            JLabel k = new JLabel(row2[0]);
            k.setFont(ALMTheme.fontBold(12));
            k.setForeground(ALMTheme.TEXT_SECONDARY);
            panel.add(k, g);
            g.gridx = 1;
            g.weightx = 0.7;
            g.gridy++;
            JLabel v = new JLabel(row2[1]);
            v.setFont(ALMTheme.fontRegular(12));
            v.setForeground(ALMTheme.TEXT_PRIMARY);
            panel.add(v, g);
        }

        JOptionPane.showMessageDialog(this, panel, "User Details — " + username, JOptionPane.PLAIN_MESSAGE);
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
}
