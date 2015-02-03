package com.logginghub.logging.frontend.brainscan;

import com.logginghub.utils.observable.ObservableListListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ThreadGroupingEditorPanel extends JPanel {

    private List<ThreadGroupModel> models = new ArrayList<ThreadGroupModel>();
    private AbstractTableModel tableModel;
    private ThreadGroupingModel model;

    public ThreadGroupingEditorPanel() {
        setLayout(new MigLayout("", "[]", "[][]"));

        tableModel = new AbstractTableModel() {
            @Override public int getRowCount() {
                return models.size();
            }

            @Override public int getColumnCount() {
                return 4;
            }

            @Override public String getColumnName(int column) {
                return new String[] { "Enabled", "Name", "Matcher", "Regex or wildcard" }[column];
            }

            @Override public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                    case 3:
                        return Boolean.class;
                    case 1:
                    case 2:
                        return String.class;
                }
                return null;                 
            }
            
            @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
            
            @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                ThreadGroupModel threadGroupModel = models.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        threadGroupModel.getEnabled().set((Boolean)aValue);
                        break;
                    case 1:
                        threadGroupModel.getName().set((String)aValue);
                        break;
                    case 2:
                        threadGroupModel.getMatcher().set((String)aValue);
                        break;
                    case 3:
                        threadGroupModel.getRegex().set((Boolean)aValue);
                        break;
                }
                 
            }
            
            @Override public Object getValueAt(int rowIndex, int columnIndex) {
                ThreadGroupModel threadGroupModel = models.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return threadGroupModel.getEnabled().get();
                    case 1:
                        return threadGroupModel.getName().get();
                    case 2:
                        return threadGroupModel.getMatcher().get();
                    case 3:
                        return threadGroupModel.getRegex().get();
                }
                return null;
            }
        };

        JButton btnNewButton = new JButton("Create group");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createGroup();
            }
        });
        add(btnNewButton, "flowx,cell 0 0");

        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), "cell 0 1");

        JButton btnNewButton_1 = new JButton("Remove selected");
        add(btnNewButton_1, "cell 0 0");

    }

    protected void createGroup() {
        ThreadGroupModel model = new ThreadGroupModel();
        model.getName().set("New group");
        model.getMatcher().set("*");
        model.getRegex().set(false);
        model.getEnabled().set(false);
        this.model.getGroups().add(model);
    }

    public void bind(ThreadGroupingModel model) {
        this.model = model;
        model.getGroups().addListenerAndNotifyExisting(new ObservableListListener<ThreadGroupModel>() {
            @Override public void onRemoved(ThreadGroupModel t, int index) {}

            @Override public void onCleared() {}

            @Override public void onAdded(final ThreadGroupModel t) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        models.add(t);
                        int lastRow = models.size() - 1;
                        tableModel.fireTableRowsInserted(lastRow, lastRow);
                    }

                });
            }
        });
    }

}
