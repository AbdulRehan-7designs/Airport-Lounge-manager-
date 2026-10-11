package com.alm.ui.core;

import com.alm.config.Permission;
import com.alm.ui.theme.ALMTheme;
import javax.swing.*;
import java.awt.*;

public class PermissionUtil {
    public static boolean verify(JPanel panel, Permission requiredPermission) {
        if (requiredPermission != null && !SessionContext.getInstance().hasPermission(requiredPermission)) {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            JLabel errorLabel = new JLabel("\u26A0 Access Denied: Insufficient Privileges", SwingConstants.CENTER);
            errorLabel.setFont(ALMTheme.fontBold(24));
            // Using TEXT_MUTED or similar as an error color because TEXT_ERROR might not be
            // defined
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.CENTER);
            panel.setBackground(ALMTheme.BG_DARK);
            panel.revalidate();
            panel.repaint();
            return false;
        }
        return true;
    }
}
