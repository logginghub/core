package com.logginghub.logging.frontend.patterniseddataview;

import java.util.Arrays;
import java.util.Date;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.swingutils.table.ExtensibleTableModel;
import com.logginghub.utils.DateFormatFactory;

public class PatternisedDataViewTableModel extends ExtensibleTableModel<PatternisedLogEvent> {

    private static final int COLUMN_TIME = 0;
    private static final int COLUMN_PATTERN_ID = 1;
    private static final int COLUMN_VARIABLES = 2;
    private static final int COLUMN_PID = 3;
    private static final int COLUMN_SOURCE_APPLICATION = 4;
    private static final int COLUMN_SOURCE_HOST = 5;

    private String[] columnNames = new String[] { "Time", "PatternID", "Variables", "PID", "Source", "Host"};

    @Override public String[] getColumnNames() {
        return columnNames;
    }

    @Override public Object extractValue(PatternisedLogEvent item, int columnIndex) {

        Object value = "???";
        switch (columnIndex) {

            case COLUMN_TIME:
                value = DateFormatFactory.getTimeWithMillis(DateFormatFactory.local).format(new Date(item.getTime()));
                break;
            case COLUMN_PID:
                value = item.getPid();
                break;

            case COLUMN_PATTERN_ID:
                value = item.getPatternID();
                break;

            case COLUMN_VARIABLES:
                value = Arrays.toString(item.getVariables());
                break;
                
            case COLUMN_SOURCE_APPLICATION:
                value = item.getSourceApplication();
                break;
                
            case COLUMN_SOURCE_HOST:
                value = item.getSourceHost();
                break;

        }

        return value;
    }

}
