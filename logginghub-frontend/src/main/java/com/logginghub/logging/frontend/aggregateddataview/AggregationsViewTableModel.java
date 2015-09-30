package com.logginghub.logging.frontend.aggregateddataview;

import java.text.NumberFormat;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.frontend.aggregateddataview.AggregationsViewTable.PatternComboWrapper;
import com.logginghub.logging.frontend.services.PatternManagementService;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.swingutils.table.ExtensibleTableModel;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;

public class AggregationsViewTableModel extends ExtensibleTableModel<Aggregation> {

    public static final int COLUMN_AGGREGATION_ID = 0;
    public static final int COLUMN_PATTERN_ID = 1;
    public static final int COLUMN_CAPTURE_LABEL_INDEX = 2;
    public static final int COLUMN_TYPE = 3;
    public static final int COLUMN_INTERVAL = 4;
    public static final int COLUMN_GROUP_BY = 5;

    private String[] columnNames = new String[] { "AggregationID", "Pattern ID", "Capture Label Index", "Type", "Interval", "Group by" };

    private static NumberFormat nf = NumberFormat.getInstance();
    private PatternManagementService patternService;

    public AggregationsViewTableModel(PatternManagementService patternService) {
        this.patternService = patternService;
    }

    @Override public String[] getColumnNames() {
        return columnNames;
    }

    @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        Aggregation itemAtRow = getItemAtRow(rowIndex);

        switch (columnIndex) {
            case COLUMN_PATTERN_ID:
                itemAtRow.setPatternID(((PatternComboWrapper) aValue).getPattern().getPatternId());
                break;
            case COLUMN_CAPTURE_LABEL_INDEX:
                itemAtRow.setCaptureLabelIndex((Integer) aValue);
                break;
            case COLUMN_TYPE:
                itemAtRow.setType(AggregationType.valueOf((String) aValue));
                break;
            case COLUMN_INTERVAL:
                itemAtRow.setInterval(TimeUtils.parseInterval((String) aValue));
                break;
            case COLUMN_GROUP_BY:
                itemAtRow.setGroupBy((String) aValue);
                break;
        }
    }

    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {

        boolean editable = false;

        switch (columnIndex) {
            case COLUMN_AGGREGATION_ID:
                editable = false;
                break;
            case COLUMN_PATTERN_ID:
                editable = true;
                break;
            case COLUMN_CAPTURE_LABEL_INDEX:
                editable = true;
                break;
            case COLUMN_TYPE:
                editable = true;
                break;
            case COLUMN_INTERVAL:
                editable = true;
                break;
            case COLUMN_GROUP_BY:
                editable = true;
                break;
        }

        return editable;
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
        Class<?> clazz = null;

        switch (columnIndex) {
            case COLUMN_AGGREGATION_ID:
                clazz = Integer.class;
                break;
            case COLUMN_PATTERN_ID:
                clazz = String.class;
                break;
            case COLUMN_CAPTURE_LABEL_INDEX:
                clazz = Integer.class;
                break;
            case COLUMN_TYPE:
                clazz = String.class;
                break;
            case COLUMN_INTERVAL:
                clazz = String.class;
                break;
            case COLUMN_GROUP_BY:
                clazz = String.class;
                break;
        }

        return clazz;

    }

    @Override public Object extractValue(Aggregation item, int columnIndex) {

        Object value = "???";
        switch (columnIndex) {

            case COLUMN_AGGREGATION_ID:
                value = item.getAggregationID();
                break;
            case COLUMN_PATTERN_ID:
                Pattern pattern = patternService.getPatternByID(item.getPatternID());
                if(pattern != null) {
                value = StringUtils.format("[{}] {}", item.getPatternID(), pattern.getName());
                }else{
                    value = "Pattern...";
                }
                break;
            case COLUMN_CAPTURE_LABEL_INDEX:
                value = item.getCaptureLabelIndex();
                break;
            case COLUMN_TYPE:
                value = item.getType().name();
                break;
            case COLUMN_INTERVAL:
                value = TimeUtils.formatIntervalMilliseconds(item.getInterval());
                break;
            case COLUMN_GROUP_BY:
                value = item.getGroupBy();
                break;
        }

        return value;
    }
}
