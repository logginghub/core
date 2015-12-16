package com.logginghub.logging.frontend.model;

import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTableModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the configuration and model attributes needed for the event display table.
 */
public class EventTableColumnModel {

    private Map<Integer, String> columnNameMappings = new HashMap<Integer, String>();

    public EventTableColumnModel() {
        // Add the default column name mappings

        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_TIME, "Time");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_SOURCE, "Source");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_HOST, "Host");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_LEVEL, "Level");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_THREAD, "Thread");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_CLASS_METHOD, "Method");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_MESSAGE, "Message");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_DIAGNOSTIC_CONTEXT, "DC");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_LOCKED, "Locked");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_PID, "PID");
        columnNameMappings.put(DetailedLogEventTableModel.COLUMN_CHANNEL, "Channel");
    }

    public String getColumnName(int columnIndex) {
        return columnNameMappings.get(columnIndex);
    }

    public Map<Integer, String> getColumnNameMappings() {
        return columnNameMappings;
    }
}
