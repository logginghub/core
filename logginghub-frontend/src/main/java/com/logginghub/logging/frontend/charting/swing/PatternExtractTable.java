package com.logginghub.logging.frontend.charting.swing;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.charting.model.BatchedArraryListTableModel;
import com.logginghub.logging.frontend.charting.model.PatternisedDataModel;
import com.logginghub.logging.frontend.charting.model.PatternisedDataSeriesModel;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.ObservableListListener;

public class PatternExtractTable extends JPanel {

    private JTable table;
    private String[] columnNames;

    private BatchedArraryListTableModel<PatternisedDataModel> tableModel = new BatchedArraryListTableModel<PatternisedDataModel>() {
        private static final long serialVersionUID = 1L;

        @Override public Object extractValue(PatternisedDataModel item, int columnIndex) {
            if (columnIndex < item.getVariables().length) {
                return item.getVariables()[columnIndex];
            }
            else {
                return "?";
            }

        }

        @Override public String[] getColumnNames() {
            return columnNames;
        }
    };

    private static final long serialVersionUID = 1L;

    public PatternExtractTable() {
        setLayout(new MigLayout("fill", "[grow,fill]", "[grow,fill]"));
        table = new JTable(tableModel) {
            private static final long serialVersionUID = 1L;

            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                JComponent c = (JComponent) super.prepareRenderer(renderer, rowIndex, vColIndex);

                int selectedColumn = table.getSelectedColumn();
                if (selectedColumn == vColIndex) {
                    c.setBackground(Color.cyan);
                }
                else {
                    c.setBackground(Color.white);
                }

                return c;
            }
        };
        table.setName("Pattern extract table");

        tableModel.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
            }
        });

        add(new JScrollPane(table));
    }

    public void bind(PatternisedDataSeriesModel patternisedModel) {

        patternisedModel.getPatternised().addListenerAndNotifyExisting(new ObservableListListener<PatternisedDataModel>() {

            @Override public void onAdded(PatternisedDataModel t) {
                tableModel.addToBatch(t);
            }

            @Override public void onCleared() {
                tableModel.clear();
            }

            @Override public void onRemoved(PatternisedDataModel t) {
                tableModel.remove(t);
            }
        });

    }

    public void setLabels(final List<String> labels) {
        columnNames = StringUtils.toArray(labels);
        tableModel.fireTableStructureChanged();
    }

    public void setColumnSelection() {

    }

    public String getSelectedColumName() {
        int selectedColumn = table.getSelectedColumn();
        if (selectedColumn == -1) {
            return null;
        }
        else {
            return table.getColumnName(selectedColumn);
        }
    }

}