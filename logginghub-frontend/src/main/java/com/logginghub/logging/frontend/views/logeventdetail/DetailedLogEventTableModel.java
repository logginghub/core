package com.logginghub.logging.frontend.views.logeventdetail;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.filters.CompositeAndFilter;
import com.logginghub.logging.filters.MessageContainsFilter;
import com.logginghub.logging.frontend.Utils;
import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.frontend.images.Icons.IconIdentifier;
import com.logginghub.logging.frontend.model.LogEventContainer;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.Is;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.Pair;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

public class DetailedLogEventTableModel extends DefaultTableModel implements LogEventListener {
    
    private static final Logger logger = Logger.getLoggerFor(DetailedLogEventTableModel.class);
    private static final long serialVersionUID = 1L;

    private Object eventLock = new Object();

    public static final int COLUMN_TIME = 0;
    public static final int COLUMN_SOURCE = 1;
    public static final int COLUMN_HOST = 2;
    public static final int COLUMN_LEVEL = 3;
    public static final int COLUMN_THREAD = 4;
    public static final int COLUMN_CLASS_METHOD = 5;
    public static final int COLUMN_MESSAGE = 6;
    public static final int COLUMN_DIAGNOSTIC_CONTEXT = 7;
    public static final int COLUMN_LOCKED = 8;
    public static final int COLUMN_PID = 9;
    public static final int COLUMN_CHANNEL = 10;

    public static final int NUMBER_OF_COLUMNS = 11;

    private static final String BLANK = "";
    private static final String[] columnNames = new String[] { "Time", "Source", "Host", "Level", "Thread", "Method", "Message", "DC", "", "PID", "Channel" };

    private CompositeAndFilter filters = new CompositeAndFilter();

    private LogEventContainerController eventController;

    private boolean isPlaying = true;
//    private IntegerStat gets;

    public DetailedLogEventTableModel(LogEventContainerController eventController) {
        
//        StatBundle bundle = new StatBundle();
//        gets = bundle.createIncremental("gets");
//        bundle.startPerSecond(logger);
        
        this.eventController = eventController;
    }

    @Override public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override public int getColumnCount() {
        return NUMBER_OF_COLUMNS;
    }

    @Override public int getRowCount() {
        int rowCount = 0;

        // gah - the super class ctor calls get row count :/
        if (eventLock != null) {
            synchronized (eventLock) {
                rowCount = eventController.getLiveEventsThatPassFilter().size();
            }
        }

        return rowCount;
    }

    public LogEvent getLogEventAtRow(int rowIndex) {
//        gets.increment();
//        Is.swingEventThread();
        synchronized (eventLock) {
            return eventController.getLiveEventsThatPassFilter().get(rowIndex);
        }
    }

    public int getVisibleIndexForEvent(LogEvent event) {
//        Is.swingEventThread();
        synchronized (eventLock) {
            int index = eventController.getLiveEventsThatPassFilter().indexOf(event);
            return index;
        }
    }

    @Override public Object getValueAt(int row, int column) {
        LogEvent logEvent;
//        Is.swingEventThread();
        synchronized (eventLock) {
            logEvent = eventController.getLiveEventsThatPassFilter().get(row);
        }

        if (logEvent == null) {
            throw new RuntimeException("The row returned from the visible events collection was null. Not sure how this is possible. Index was " + row + " row count was " + getRowCount());
        }

        Object value;

        switch (column) {
            case COLUMN_LOCKED: {
                if (logEvent instanceof DefaultLogEvent) {
                    DefaultLogEvent defaultLogEvent = (DefaultLogEvent) logEvent;
                    Metadata metadata = defaultLogEvent.getMetadata();
                    if (metadata.containsKey("locked")) {
                        if (metadata.getBoolean("locked")) {
                            return Icons.get(IconIdentifier.Locked);
                        }
                        else {
                            return Icons.get(IconIdentifier.Unlocked);
                        }
                    }
                    else {
                        value = "";
                    }
                }
                else {
                    value = "";
                }
                break;
            }
            case COLUMN_CLASS_METHOD: {
                
                String sourceClassName = logEvent.getSourceClassName();
                String sourceMethodName = logEvent.getSourceMethodName();
                if(sourceClassName != null && sourceMethodName != null) {
                    value = sourceClassName + "." + sourceMethodName;    
                }else if(sourceClassName != null ){
                    value = sourceClassName;
                }else if(sourceMethodName != null) {
                    value = sourceMethodName;
                }else {
                    value = "[Not captured]";
                }
                break;
            }
            case COLUMN_SOURCE: {
                value = logEvent.getSourceApplication();
                break;
            }
            case COLUMN_HOST: {
                value = logEvent.getSourceHost();
                break;
            }
            case COLUMN_TIME: {
                value = Utils.formatTime(logEvent.getOriginTime());
                break;
            }
            case COLUMN_THREAD: {
                value = logEvent.getThreadName();
                break;
            }
            case COLUMN_MESSAGE: {
                value = logEvent.getMessage();
                break;
            }
            case COLUMN_LEVEL: {
                value = logEvent.getLevelDescription();
                break;
            }
            case COLUMN_DIAGNOSTIC_CONTEXT: {
                value = formatDiagnosticContext(logEvent.getFormattedObject());
                break;
            }
            case COLUMN_PID: {
                value = logEvent.getPid();
                break;
            }
            case COLUMN_CHANNEL: {
                value = logEvent.getChannel();
                break;
            }
            default: {
                value = "???";
            }
        }

        return value;
    }

    private String formatDiagnosticContext(String[] formattedObject) {

        String formatted;

        if (formattedObject != null && formattedObject.length > 0) {
            StringBuilder context = new StringBuilder();
            for (int i = 0; i < formattedObject.length; i++) {
                context.append("[").append(formattedObject[i]).append("]");
            }
            formatted = context.toString();
        }
        else {
            formatted = BLANK;
        }

        return formatted;
    }

    @Override public boolean isCellEditable(int arg0, int arg1) {
        return false;
    }

    @Override public void setValueAt(Object arg0, int arg1, int arg2) {

    }

    public void addFilter(Filter<LogEvent> filter, LogEvent currentSelection) {
        filters.addFilter(filter);
        // int newSelectionPosition =
        refilter(currentSelection);
        // return newSelectionPosition;
    }

    public void refreshFilters(LogEvent currentSelection) {
        // int refilter =
        refilter(currentSelection);
        // return refilter;
    }

    public void removeFilter(Filter<LogEvent> filter, LogEvent currentSelection) {
        filters.removeFilter(filter);
        // int newSelectionPosition = refilter(currentSelection);
        refilter(currentSelection);
        // return newSelectionPosition;
    }

    public void clear() {
        fireTableDataChanged();
    }

    private void refilter(final LogEvent currentSelection) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                synchronized (eventLock) {
                    eventController.refilter(filters);
                    eventController.getLiveEventsThatPassFilter().indexOf(currentSelection);
                }
                fireTableDataChanged();
            }
        });
        // return newSelectionPosition;
    }

    // //////////////////////////////////////////////////////////////////
    // LogEventListener implementations
    // //////////////////////////////////////////////////////////////////

    public void onNewLogEvent(LogEventContainer newEvents) {
        int startAddedRow;
        int endAddedRow;
        int removedRows = 0;

        Is.swingEventThread();

        synchronized (eventLock) {

            startAddedRow = eventController.getLiveEventsThatPassFilter().size();
            endAddedRow = startAddedRow;

            for (LogEvent event : newEvents) {
                Pair<Boolean, Boolean> state = eventController.add(event, filters, isPlaying);
                boolean visible = state.getA();
                boolean removedVisible = state.getB();

                if (visible) {
                    endAddedRow++;
                }

                if (removedVisible) {
                    removedRows++;
                }
            }
        }

        if (isPlaying) {

            if (removedRows > 0) {
//                Out.out("Rows delete from {} to {}", 0, removedRows -1);
                fireTableRowsDeleted(0, removedRows - 1);
            }

            // We've just got rid of some, so we need to offset the added rows back
            startAddedRow -= removedRows;
            endAddedRow -= removedRows;
//            Out.out("Rows inserted from {} to {}", startAddedRow, endAddedRow);
            fireTableRowsInserted(startAddedRow, endAddedRow);
        }
    }

    public void onNewLogEvent(LogEvent event) {
        if (event == null) {
            throw new RuntimeException("Please dont add null events");
        }

        int rowCount = -1;
        boolean removedVisible;

        Is.swingEventThread();

        synchronized (eventLock) {

            // TODO : this is leaky and nasty, maybe we should house the filters
            // and the playing state in the controller?
            Pair<Boolean, Boolean> pair = eventController.add(event, filters, isPlaying);
            boolean isVisible = pair.getA();
            removedVisible = pair.getB();

            if (isVisible) {
                rowCount = eventController.getLiveEventsThatPassFilter().size();
            }
        }
    }

    public int getAllEventsSize() {
        synchronized (eventLock) {
            return eventController.getLiveEventsSize();
        }
    }

    public int findFirstTime(long time) {
        int found = -1;
        synchronized (eventLock) {
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                LogEvent next = eventController.getLiveEventsThatPassFilter().get(i);
                long entryTime = next.getOriginTime();
                logger.finest("Comparing entryTime '{}' vs search time '{}'", Logger.toDateString(entryTime), Logger.toDateString(time));
                if(entryTime >= time) {
                    found = i;
                    break;
                }
            }
        }

        return found; 
    }
    
    /**
     * Search the visible rows for the next row that matches the filter.
     * 
     * @param selectedRow
     * @param filter
     * @return
     */
    public int findNextEvent(int selectedRow, MessageContainsFilter filter) {
        int found = -1;
        synchronized (eventLock) {
            for (int i = selectedRow; i < getRowCount(); i++) {
                LogEvent next = eventController.getLiveEventsThatPassFilter().get(i);
                if (filter.passes(next)) {
                    found = i;
                    break;
                }
            }
        }

        return found;
    }

    public int findPreviousEvent(int selectedRow, MessageContainsFilter filter) {
        int found = -1;
        synchronized (eventLock) {
            for (int i = selectedRow; i > 0; i--) {
                LogEvent next = eventController.getLiveEventsThatPassFilter().get(i);
                if (filter.passes(next)) {
                    found = i;
                    break;
                }
            }
        }

        return found;
    }

    public CompositeAndFilter getFilters() {
        return filters;
    }

    public void play() {
        this.isPlaying = true;
        synchronized (eventLock) {
            eventController.play();
        }
    }

    public void pause() {
        this.isPlaying = false;
    }

    @Override public void removeRow(int rowIndex) {
        Is.swingEventThread();

        synchronized (eventLock) {
            eventController.removeLiveEvent(rowIndex);
        }
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    
}
