package com.logginghub.logging.frontend.charting.swing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.charting.model.BatchedArraryListTableModel;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;

public class SimpleEventMessageTable extends JTable implements LogEventListener {
    private static final long serialVersionUID = 1L;

    // private List<LogEvent> events = new ArrayList<LogEvent>();

    private static final Logger logger = Logger.getLoggerFor(SimpleEventMessageTable.class);

    private BatchedArraryListTableModel<LogEvent> model = new BatchedArraryListTableModel<LogEvent>() {
        @Override
        public String[] getColumnNames() {
            return new String[] { "Event Message" };
        }

        @Override
        public Object extractValue(LogEvent item, int columnIndex) {
            return item.getMessage();
        }
    };
    private EventSource logEventSelectedEvent = new EventSource("EventSelected");

    public SimpleEventMessageTable() {
        setModel(model);
        setName("SimpleEventMessageTable");

        model.setMaximumRows(1000);

        JTableHeader tableHeader = getTableHeader();
        TableCellRenderer defaultRenderer = tableHeader.getDefaultRenderer();
        DefaultTableCellRenderer defaultTableCellRenderer = (DefaultTableCellRenderer) defaultRenderer;
        defaultTableCellRenderer.setHorizontalAlignment(JLabel.LEFT);

        model.setSorter(new Comparator<LogEvent>() {
            @Override
            public int compare(LogEvent o1, LogEvent o2) {
                return CompareUtils.start().add(o2.getOriginTime(), o1.getOriginTime())
                        .add(o2.getSequenceNumber(), o1.getSequenceNumber()).compare();
            }
        });

        // TODO : figure out a better way to do this:
        getColumn("Event Message").setMinWidth(10000);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();

                StringBuilder output = new StringBuilder();

                output.append("Event for indexes " + firstIndex + " - " + lastIndex + "; isAdjusting is " + isAdjusting
                        + "; selected indexes:");

                List<LogEvent> selected = new ArrayList<LogEvent>();
                if (lsm.isSelectionEmpty()) {
                    output.append(" <none>");
                } else {

                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    for (int i = minIndex; i <= maxIndex; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            output.append(" " + i);

                            LogEvent event = model.getItemAtRow(i);
                            selected.add(event);
                        }
                    }
                }

                logEventSelectedEvent.fireEvent(selected);
                logger.debug("{}", output);
            }

        });
    }

    public EventSource getLogEventSelectedEvent() {
        return logEventSelectedEvent;
    }

    @Override
    public void onNewLogEvent(final LogEvent event) {
        model.addToBatch(event);
    }

    public void bind(ObservableList<LogEvent> notPatternised) {
        notPatternised.addListenerAndNotifyCurrent(new ObservableListListener<LogEvent>() {
            @Override public void onRemoved(LogEvent t, int index) {
                model.remove(t);
            }

            @Override public void onCleared() {
            }

            @Override public void onAdded(LogEvent t) {
                model.addToBatch(t);
            }
        });
    }

    public BatchedArraryListTableModel<LogEvent> getModel() {
        return model;
    }

    public void setFilter(final String newValue) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                model.setFilter(new Filter<LogEvent>() {
                    public boolean passes(LogEvent t) {
                        return t.getMessage().startsWith(newValue);
                    }
                });
            }
        });
    }

}
