package com.logginghub.logging.frontend.charting;

import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.charting.model.ChartSeriesFilterModel;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.Stream;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.logging.frontend.charting.model.TableChartModel;
import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.Color;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableChartView extends MigPanel {

    private static final Logger logger = Logger.getLoggerFor(TableChartView.class);
    private static final long serialVersionUID = 1L;
    private TableChartViewTableModel tableModel;
    private JScrollPane scroller;
    private TitledBorder titledBorder;

    public TableChartView() {
        super("fill", "[fill, grow]", "[fill, grow]");
        setBorder(BorderFactory.createLineBorder(Color.lightGray));
        setBackground(Color.white);
    }

    //    public void addMarker(final long currentTimeMillis, final String string) {
    //        SwingUtilities.invokeLater(new Runnable() {
    //            public void run() {
    //                chart.addMarker(currentTimeMillis, string);
    //            }
    //        });
    //    }

    public void bind(final NewChartingController controller, TableChartModel chartModel) {

        this.tableModel = new TableChartViewTableModel();

        JTable table = new JTable(tableModel);

        this.titledBorder = BorderFactory.createTitledBorder("Table");
        this.scroller = new JScrollPane(table);

        scroller.setBorder(titledBorder);

        chartModel.getTitle().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, String newValue) {
                titledBorder.setTitle(newValue);
            }
        });

        chartModel.getResets().addListener(new ObservablePropertyListener<Integer>() {
            @Override
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                clearChartData();
            }

        });

        ObservableList<ChartSeriesModel> matcherModels = chartModel.getMatcherModels();
        matcherModels.addListenerAndNotifyCurrent(new ObservableListListener<ChartSeriesModel>() {

            StreamListener<ChunkedResult> listener = new StreamListener<ChunkedResult>() {
                @Override
                public void onNewItem(final ChunkedResult t) {
                    // TODO: consider batching
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            tableModel.onNewChunkedResult(t);
                        }
                    });

                }
            };

            @Override
            public void onAdded(ChartSeriesModel matcherModel) {
                Stream<ChunkedResult> stream = controller.getResultStreamFor(matcherModel);
                stream.addListener(listener);

                matcherModel.getFilters().addListener(new ObservableListListener<ChartSeriesFilterModel>() {
                    @Override
                    public void onAdded(ChartSeriesFilterModel t) {
                        clearChartData();
                    }

                    @Override
                    public void onRemoved(ChartSeriesFilterModel t, int index) {
                        clearChartData();
                    }

                    @Override
                    public void onCleared() {
                        clearChartData();
                    }


                });

            }

            @Override
            public void onRemoved(ChartSeriesModel t, int index) {
                logger.info("Unbinding chart series model '{}' from chart", t);
                Stream<ChunkedResult> stream = controller.getResultStreamFor(t);
                stream.removeListener(listener);
                clearChartData();
            }

            @Override
            public void onCleared() {
            }

        });

        add(scroller);

    }

    public void clearChartData() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableModel.clear();
            }
        });
    }


    public void saveChartData(final File folder) {

    }

    public void saveChartImages(final File folder) {

    }

    private static class TableChartViewTableModel extends AbstractTableModel {

        private List<String> columns = new ArrayList<String>();
        private List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        private Set<String> columnSet = new HashSet<String>();

        private Map<String, Integer> rowLookup = new HashMap<String, Integer>();

        private Object lock = new Object();

        public TableChartViewTableModel() {
            //            rows.add(new HashMap<String, String>());
        }

        public void clear() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    rows.clear();
                    columns.clear();
                    columnSet.clear();
                    rowLookup.clear();

                    //            rows.add(new HashMap<String, String>());
                    fireTableStructureChanged();

                }
            });
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            Map<String, String> valueMap = rows.get(rowIndex);

            String column = columns.get(columnIndex);

            String value = valueMap.get(column);

            return value;
        }

        public void onNewChunkedResult(final ChunkedResult t) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    String source = t.getSource();
                    String label = t.getLabel() + " (" + t.getMode() + ")";
                    String groupBy = t.getGroupBy();

                    Map<String, String> row;
                    Integer rowIndex = rowLookup.get(source);
                    if (rowIndex == null) {
                        rowIndex = rows.size();
                        rowLookup.put(source, rowIndex);
                        row = new HashMap<String, String>();
                        rows.add(row);
                        fireTableStructureChanged();
                    } else {
                        row = rows.get(rowIndex);
                    }

                    if (!columnSet.contains(groupBy)) {
                        columns.add(groupBy);
                        columnSet.add(groupBy);
                        fireTableStructureChanged();
                    }

                    if (!columnSet.contains(label)) {
                        columns.add(label);
                        columnSet.add(label);
                        fireTableStructureChanged();
                    }

                    // TODO : work out the row index based on some field?

                    NumberFormat instance = NumberFormat.getInstance();

                    row.put(label, instance.format(t.getValue()));
                    row.put(groupBy, source);

                    fireTableRowsUpdated(rowIndex, rowIndex);

                }
            });

        }
    }
}
