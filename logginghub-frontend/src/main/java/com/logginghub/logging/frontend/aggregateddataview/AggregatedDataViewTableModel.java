package com.logginghub.logging.frontend.aggregateddataview;

import java.text.NumberFormat;
import java.util.Date;

import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.swingutils.table.ExtensibleTableModel;
import com.logginghub.utils.DateFormatFactory;

public class AggregatedDataViewTableModel extends ExtensibleTableModel<AggregatedLogEvent> {

    private static final int COLUMN_TIME = 0;
    private static final int COLUMN_AGGREGATION_ID = 1;
    private static final int COLUMN_SERIES_KEY = 2;
    private static final int COLUMN_VALUE = 3;

    private String[] columnNames = new String[] { "Time", "AggregationID", "Key", "Value" };

    private static NumberFormat nf = NumberFormat.getInstance();

    @Override public String[] getColumnNames() {
        return columnNames;
    }

    @Override public Object extractValue(AggregatedLogEvent item, int columnIndex) {

        Object value = "???";
        switch (columnIndex) {

            case COLUMN_SERIES_KEY:
                value = item.getSeriesKey();
                break;
            case COLUMN_TIME:
                value = DateFormatFactory.getTimeWithMillis(DateFormatFactory.local).format(new Date(item.getTime()));
                break;
            case COLUMN_AGGREGATION_ID:
                value = item.getAggregationID();
                break;
            case COLUMN_VALUE:
                value = nf.format(item.getValue());
                break;
        }

        return value;
    }

}
