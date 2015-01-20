package com.logginghub.logging.frontend.regexbuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.filter.Filter;

public class SimpleEventTableModel extends AbstractTableModel implements LogEventListener {

    private static final long serialVersionUID = 1L;
    private List<LogEvent> events = new ArrayList<LogEvent>();
    private Filter<LogEvent> filter;

    public enum Column {
        SourceHost,
        SourceApplication,
        Message
    }

    @Override public int getRowCount() {
        synchronized (events) {
            return events.size();
        }
    }

    @Override public int getColumnCount() {
        return Column.values().length;

    }

    @Override public String getColumnName(int columnIndex) {
        Column column = Column.values()[columnIndex];
        return column.toString();
    }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {

        Object value;

        LogEvent event = getRowAt(rowIndex);
        Column column = Column.values()[columnIndex];

        switch (column) {
            case Message:
                value = event.getMessage();
                break;
            case SourceApplication:
                value = event.getSourceApplication();
                break;
            case SourceHost:
                value = event.getSourceHost();
                break;
            default:
                throw new RuntimeException(String.format("Unsupported column %s", column));
        }

        return value;
    }

    @Override public void onNewLogEvent(final LogEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                addItem(event);
                limitRows();
            }
        });
    }

    private void addItem(LogEvent event) {
        int index;
        synchronized (events) {
            index = events.size();
            events.add(event);
        }
        fireTableRowsInserted(index, index);
    }

    private void limitRows() {
        synchronized (events) {
            if (events.size() > 1000) {
                events.remove(0);
            }
        }

        fireTableRowsDeleted(0, 0);
    }

    public LogEvent getRowAt(int row) {
        synchronized (events) {
            return events.get(row);
        }
    }

    public void setFilter(Filter<LogEvent> filter) {
        this.filter = filter;
        updateFilter();
    }

    public void updateFilter() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                synchronized (events) {
                    Iterator<LogEvent> iterator = events.iterator();
                    while (iterator.hasNext()) {
                        if (filter.passes(iterator.next())) {

                        }
                        else {
                            iterator.remove();
                        }
                    }
                }

                fireTableDataChanged();
            }
        });
    }

}
