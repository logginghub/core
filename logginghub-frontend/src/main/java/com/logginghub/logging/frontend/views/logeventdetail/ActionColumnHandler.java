package com.logginghub.logging.frontend.views.logeventdetail;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Provides JTable support for action button in the log event detail tables.
 */
public class ActionColumnHandler {

    public class ButtonRenderer extends JButton implements TableCellRenderer {

        private final String label;

        public ButtonRenderer(String label) {
            this.label = label;
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText(label);
            return this;
        }
    }

    interface ButtonClickedListener {
        void onClicked(int row, int column);
    }

    public class ButtonEditor extends DefaultCellEditor {
        private final ButtonClickedListener clickListener;
        protected JButton button;

        private String label;

        private boolean isPushed;
        private int column;
        private int row;

        public ButtonEditor(final ButtonClickedListener clickListener) {
            super(new JCheckBox());
            this.clickListener = clickListener;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            this.row = row;
            this.column = column;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                clickListener.onClicked(row, column);
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }


}
