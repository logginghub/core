package com.logginghub.logging.frontend.views.logeventdetail;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.filters.CompositeAndFilter;
import com.logginghub.logging.filters.MessageContainsFilter;
import com.logginghub.logging.frontend.Utils;
import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.frontend.images.Icons.IconIdentifier;
import com.logginghub.logging.frontend.model.EnvironmentController;
import com.logginghub.logging.frontend.model.EventTableColumnModel;
import com.logginghub.logging.frontend.model.LevelNamesModel;
import com.logginghub.logging.frontend.model.LogEventContainer;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.Is;
import com.logginghub.utils.ObjectUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.Pair;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Map;

public class DetailedLogEventTableModel extends DefaultTableModel implements LogEventListener {

    public static final int COLUMN_CHANNEL = 10;
    public static final int COLUMN_CLASS_METHOD = 5;
    public static final int COLUMN_DIAGNOSTIC_CONTEXT = 7;
    public static final int COLUMN_HOST = 2;
    public static final int COLUMN_LEVEL = 3;
    public static final int COLUMN_LOCKED = 8;
    public static final int COLUMN_MESSAGE = 6;
    public static final int COLUMN_PID = 9;
    public static final int COLUMN_SOURCE = 1;
    public static final int COLUMN_THREAD = 4;
    public static final int COLUMN_TIME = 0;
    public static final int NUMBER_OF_COLUMNS = 11;
    private static final Logger logger = Logger.getLoggerFor(DetailedLogEventTableModel.class);
    private static final long serialVersionUID = 1L;
    private static final String BLANK = "";
    //    private final EventTableColumnModel eventTableColumnModel;
    private final LevelNamesModel levelNamesModel;
    private Object eventLock = new Object();
    private CompositeAndFilter filters = new CompositeAndFilter();
    private LogEventContainerController eventController;
    private boolean[] isColumnEditable = new boolean[100];

    private boolean isPlaying = true;

    //    private Map<Integer, String> metadataColumns = new HashMap<Integer, String>();
    //    private Map<Integer, String> metadataColumnNames = new HashMap<Integer, String>();

    private ColumnTarget[] visibleColumns = new ColumnTarget[NUMBER_OF_COLUMNS];
    private EnvironmentController environmentController;

    public DetailedLogEventTableModel(EventTableColumnModel eventTableColumnModel,
                                      LevelNamesModel levelNamesModel,
                                      LogEventContainerController eventController) {
        //        this.eventTableColumnModel = eventTableColumnModel;
        this.levelNamesModel = levelNamesModel;
        this.eventController = eventController;

        visibleColumns[0] = new ColumnTarget("Time", COLUMN_TIME, null, ColumnTarget.Renderer.Normal);
        visibleColumns[1] = new ColumnTarget("Source", COLUMN_SOURCE, null, ColumnTarget.Renderer.Normal);
        visibleColumns[2] = new ColumnTarget("Host", COLUMN_HOST, null, ColumnTarget.Renderer.Normal);
        visibleColumns[3] = new ColumnTarget("Level", COLUMN_LEVEL, null, ColumnTarget.Renderer.Normal);
        visibleColumns[4] = new ColumnTarget("Thread", COLUMN_THREAD, null, ColumnTarget.Renderer.Normal);
        visibleColumns[5] = new ColumnTarget("Method", COLUMN_CLASS_METHOD, null, ColumnTarget.Renderer.Normal);
        visibleColumns[6] = new ColumnTarget("Message", COLUMN_MESSAGE, null, ColumnTarget.Renderer.Normal);
        visibleColumns[7] = new ColumnTarget("DC", COLUMN_DIAGNOSTIC_CONTEXT, null, ColumnTarget.Renderer.Normal);
        visibleColumns[8] = new ColumnTarget("Locked", COLUMN_LOCKED, null, ColumnTarget.Renderer.Normal);
        visibleColumns[9] = new ColumnTarget("PID", COLUMN_PID, null, ColumnTarget.Renderer.Normal);
        visibleColumns[10] = new ColumnTarget("Channel", COLUMN_CHANNEL, null, ColumnTarget.Renderer.Normal);
    }

    public void addFilter(Filter<LogEvent> filter, LogEvent currentSelection) {
        filters.addFilter(filter);
        refilter(currentSelection);
    }

    public EnvironmentController getEnvironmentController() {
        return environmentController;
    }

    private void refilter(final LogEvent currentSelection) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (eventLock) {
                    Stopwatch refiltering = Stopwatch.start("Refiltering");
                    Out.out("Filters : {}", ObjectUtils.recursiveDump(filters));
                    eventController.refilter(filters);
                    logger.info(refiltering);
                    eventController.getLiveEventsThatPassFilter().indexOf(currentSelection);
                }
                fireTableDataChanged();
            }
        });
    }

    public void fireTableDataChanged() {
        super.fireTableDataChanged();
    }

    public void fireTableStructureChanged() {
        super.fireTableStructureChanged();
    }



    public void addMetadataColumn(int index, String metadataKey, String name, ColumnTarget.Renderer renderer) {

        //        ColumnTarget[] newVisibleColumns;
        //        if(visibleColumns.length == 0) {
        //            newVisibleColumns = new ColumnTarget[1];
        //            newVisibleColumns[0] = new ColumnTarget(name, -1, metadataKey);
        //        }else {
        //
        //            int newLength = visibleColumns.length + 1;
        //            newVisibleColumns = new ColumnTarget[newLength];
        //
        //            // Copy the first chunk up to the insertion point
        //            System.arraycopy(visibleColumns, 0, newVisibleColumns, 0, index);
        //
        //            // Add the new item
        //            newVisibleColumns[index] = new ColumnTarget(name, -1, metadataKey);
        //
        //            // Copy the second chunk passed the removed item to the end
        //            System.arraycopy(visibleColumns, index, newVisibleColumns, index + 1, visibleColumns.length - index);
        //        }
        //
        //        // Switch over the arrays
        //        visibleColumns = newVisibleColumns;
        //
        //        // Tell the views the model has changed
        //        fireTableStructureChanged();

        int newLength = visibleColumns.length + 1;
        ColumnTarget[] newVisibleColumns = new ColumnTarget[newLength];

        // Copy the first chunk up to the insertion point
        System.arraycopy(visibleColumns, 0, newVisibleColumns, 0, index);

        // Add the new item
        newVisibleColumns[index] = new ColumnTarget(name, -1, metadataKey, renderer);

        // Copy the second chunk passed the removed item to the end
        System.arraycopy(visibleColumns, index, newVisibleColumns, index + 1, visibleColumns.length - index);


        // Switch over the arrays
        visibleColumns = newVisibleColumns;

        // Tell the views the model has changed
        fireTableStructureChanged();
    }

    public void clear() {
        fireTableDataChanged();
    }

    public int findFirstTime(long time) {
        int found = -1;
        synchronized (eventLock) {
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                LogEvent next = eventController.getLiveEventsThatPassFilter().get(i);
                long entryTime = next.getOriginTime();
                logger.finest("Comparing entryTime '{}' vs search time '{}'", Logger.toDateString(entryTime), Logger.toDateString(time));
                if (entryTime >= time) {
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

    public int getAllEventsSize() {
        synchronized (eventLock) {
            return eventController.getLiveEventsSize();
        }
    }

    public CompositeAndFilter getFilters() {
        return filters;
    }

    public LogEvent getLogEventAtRow(int rowIndex) {
        synchronized (eventLock) {
            return eventController.getLiveEventsThatPassFilter().get(rowIndex);
        }
    }

    public int getVisibleIndexForEvent(LogEvent event) {
        synchronized (eventLock) {
            int index = eventController.getLiveEventsThatPassFilter().indexOf(event);
            return index;
        }
    }

    public boolean isColumnVisible(String column) {
        return getColumnIndex(column) != -1;
    }

    public int getColumnIndex(String column) {
        int index = -1;
        for (int i = 0; i < visibleColumns.length; i++) {
            ColumnTarget visibleColumn = visibleColumns[i];
            if (visibleColumn.columnName.equalsIgnoreCase(column)) {
                index = i;
                break;
            }
        }
        return index;
    }

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
                fireTableRowsDeleted(0, removedRows - 1);
            }

            // We've just got rid of some, so we need to offset the added rows back
            startAddedRow -= removedRows;
            endAddedRow -= removedRows;
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

    public void pause() {
        this.isPlaying = false;
    }

    public void play() {
        this.isPlaying = true;
        synchronized (eventLock) {
            eventController.play();
        }
    }

    public void refreshFilters(LogEvent currentSelection) {
        refilter(currentSelection);
    }

    public void removeColumn(String column) {

        int columnIndex = getColumnIndex(column);

        if (columnIndex != -1) {

            int currentLength = visibleColumns.length;
            int newLength = currentLength - 1;
            ColumnTarget[] newVisibleColumns = new ColumnTarget[newLength];

            // Copy the first chunk up to the cut off point
            System.arraycopy(visibleColumns, 0, newVisibleColumns, 0, columnIndex);

            // Copy the second chunk passed the removed item to the end
            System.arraycopy(visibleColumns, columnIndex + 1, newVisibleColumns, columnIndex, currentLength - (columnIndex + 1));

            // Switch over the arrays
            visibleColumns = newVisibleColumns;
        }

        // Tell the views the model has changed
        fireTableStructureChanged();
    }

    // //////////////////////////////////////////////////////////////////
    // LogEventListener implementations
    // //////////////////////////////////////////////////////////////////

    public void removeFilter(Filter<LogEvent> filter, LogEvent currentSelection) {
        filters.removeFilter(filter);
        refilter(currentSelection);
    }

    @Override
    public void removeRow(int rowIndex) {
        Is.swingEventThread();

        synchronized (eventLock) {
            eventController.removeLiveEvent(rowIndex);
        }
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    @Override
    public int getRowCount() {
        int rowCount = 0;

        // gah - the super class ctor calls get row count :/
        if (eventLock != null) {
            synchronized (eventLock) {
                rowCount = eventController.getLiveEventsThatPassFilter().size();
            }
        }

        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return visibleColumns.length; // NUMBER_OF_COLUMNS + metadataColumns.size();
    }

    @Override
    public String getColumnName(int column) {

        return visibleColumns[column].columnName;


        //        String name = eventTableColumnModel.getColumnNameMappings().get(column);
        //        if (name == null) {
        //            name = metadataColumnNames.get(column);
        //        }
        //
        //        return name;
    }

    @Override
    public boolean isCellEditable(int row, int columns) {
        return isColumnEditable(columns);
    }

    private boolean isColumnEditable(int column) {
        return isColumnEditable[column];
    }

    public void setColumnEditable(int column, boolean isEditable) {
        isColumnEditable[column] = isEditable;
    }

    //    @Override
    //    public Object getValueAt(int row, int column) {
    //        LogEvent logEvent;
    //        synchronized (eventLock) {
    //            logEvent = eventController.getLiveEventsThatPassFilter().get(row);
    //        }
    //
    //        if (logEvent == null) {
    //            throw new RuntimeException("The row returned from the visible events collection was null. Not sure how this is possible. Index was " +
    //                                       row +
    //                                       " row count was " +
    //                                       getRowCount());
    //        }
    //
    //        Object value;
    //
    //        switch (column) {
    //            case COLUMN_LOCKED: {
    //                if (logEvent instanceof DefaultLogEvent) {
    //                    DefaultLogEvent defaultLogEvent = (DefaultLogEvent) logEvent;
    //                    Map<String, String> metadata = defaultLogEvent.getMetadata();
    //                    if (metadata.containsKey("locked")) {
    //                        if (metadata.get("locked").equalsIgnoreCase("true")) {
    //                            return Icons.get(IconIdentifier.Locked);
    //                        } else {
    //                            return Icons.get(IconIdentifier.Unlocked);
    //                        }
    //                    } else {
    //                        value = "";
    //                    }
    //                } else {
    //                    value = "";
    //                }
    //                break;
    //            }
    //            case COLUMN_CLASS_METHOD: {
    //
    //                String sourceClassName = logEvent.getSourceClassName();
    //                String sourceMethodName = logEvent.getSourceMethodName();
    //                if (sourceClassName != null && sourceMethodName != null) {
    //                    value = sourceClassName + "." + sourceMethodName;
    //                } else if (sourceClassName != null) {
    //                    value = sourceClassName;
    //                } else if (sourceMethodName != null) {
    //                    value = sourceMethodName;
    //                } else {
    //                    value = "[Not captured]";
    //                }
    //                break;
    //            }
    //            case COLUMN_SOURCE: {
    //                value = logEvent.getSourceApplication();
    //                break;
    //            }
    //            case COLUMN_HOST: {
    //                value = logEvent.getSourceHost();
    //                break;
    //            }
    //            case COLUMN_TIME: {
    //                value = Utils.formatTime(logEvent.getOriginTime());
    //                break;
    //            }
    //            case COLUMN_THREAD: {
    //                value = logEvent.getThreadName();
    //                break;
    //            }
    //            case COLUMN_MESSAGE: {
    //                value = logEvent.getMessage();
    //                break;
    //            }
    //            case COLUMN_LEVEL: {
    //                value = levelNamesModel.getLevelName(logEvent.getLevel());
    //                break;
    //            }
    //            case COLUMN_DIAGNOSTIC_CONTEXT: {
    //                value = formatDiagnosticContext(logEvent.getFormattedObject());
    //                break;
    //            }
    //            case COLUMN_PID: {
    //                value = logEvent.getPid();
    //                break;
    //            }
    //            case COLUMN_CHANNEL: {
    //                value = logEvent.getChannel();
    //                break;
    //            }
    //            default: {
    //                logger.fine("Custom column id {}", column);
    //                String metadatakey = metadataColumns.get(column);
    //                if (metadatakey != null && logEvent.getMetadata() != null) {
    //                    value = logEvent.getMetadata().get(metadatakey);
    //                    if (value == null) {
    //                        value = "";
    //                    }
    //                } else {
    //                    value = "???";
    //                }
    //            }
    //        }
    //
    //        return value;
    //    }

    @Override
    public Object getValueAt(int row, int column) {
        LogEvent logEvent;
        synchronized (eventLock) {
            logEvent = eventController.getLiveEventsThatPassFilter().get(row);
        }

        if (logEvent == null) {
            throw new RuntimeException("The row returned from the visible events collection was null. Not sure how this is possible. Index was " +
                                       row +
                                       " row count was " +
                                       getRowCount());
        }

        Object value;

        ColumnTarget visibleColumn = visibleColumns[column];
        if (visibleColumn.metadata == null) {
            value = extractEventField(logEvent, visibleColumn.eventFieldIndex);
        } else {
            logger.fine("Custom column id {}", column);
            String metadatakey = visibleColumn.metadata;
            Map<String, String> metadata = logEvent.getMetadata();
            if (metadatakey != null && metadata != null) {
                value = metadata.get(metadatakey);

                if (value == null) {
                    value = "";
                }else {
                    if (visibleColumn.renderer == ColumnTarget.Renderer.Date) {
                        value = Logger.toLocalDateString(Long.parseLong(value.toString())).toString();
                    }
                }

            } else {
                value = "???";
            }
        }

        return value;
    }

    private Object extractEventField(LogEvent logEvent, int eventFieldIndex) {

        Object value;

        switch (eventFieldIndex) {
            case COLUMN_LOCKED: {
                if (logEvent instanceof DefaultLogEvent) {
                    DefaultLogEvent defaultLogEvent = (DefaultLogEvent) logEvent;
                    Map<String, String> metadata = defaultLogEvent.getMetadata();
                    if (metadata.containsKey("locked")) {
                        if (metadata.get("locked").equalsIgnoreCase("true")) {
                            return Icons.get(IconIdentifier.Locked);
                        } else {
                            return Icons.get(IconIdentifier.Unlocked);
                        }
                    } else {
                        value = "";
                    }
                } else {
                    value = "";
                }
                break;
            }
            case COLUMN_CLASS_METHOD: {

                String sourceClassName = logEvent.getSourceClassName();
                String sourceMethodName = logEvent.getSourceMethodName();
                if (sourceClassName != null && sourceMethodName != null) {
                    value = sourceClassName + "." + sourceMethodName;
                } else if (sourceClassName != null) {
                    value = sourceClassName;
                } else if (sourceMethodName != null) {
                    value = sourceMethodName;
                } else {
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
                value = levelNamesModel.getLevelName(logEvent.getLevel());
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
        } else {
            formatted = BLANK;
        }

        return formatted;
    }

    public void setEnvironmentController(EnvironmentController environmentController) {
        this.environmentController = environmentController;
    }

    @Override
    public void setValueAt(Object item, int row, int column) {

    }

    public final static class ColumnTarget {
        int eventFieldIndex;
        String metadata;
        String columnName;
        Renderer renderer = Renderer.Normal;

        public ColumnTarget(String columnName, int eventFieldIndex, String metadata, Renderer renderer) {
            this.columnName = columnName;
            this.eventFieldIndex = eventFieldIndex;
            this.metadata = metadata;
            this.renderer = renderer;
        }

        public enum Renderer {
            Normal, Date, Action
        }
    }


}
