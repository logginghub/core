package com.logginghub.logging.frontend.components;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.frontend.images.Icons.IconIdentifier;

public class QuickFilterHistoryTableRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;
    private Icon trashIcon = Icons.get(IconIdentifier.Delete);

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (column == 1) {
            String status = (String) value;

            label.setText("");
            label.setIcon(trashIcon);
        }else{
            label.setText(value.toString());
            label.setIcon(null);
        }
        return label;
    }
}