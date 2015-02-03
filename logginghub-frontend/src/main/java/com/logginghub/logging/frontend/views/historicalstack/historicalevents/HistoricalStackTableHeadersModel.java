package com.logginghub.logging.frontend.views.historicalstack.historicalevents;

import javax.swing.table.AbstractTableModel;

/**
 * Created by james on 03/02/15.
 */
public class HistoricalStackTableHeadersModel extends AbstractTableModel {

    private final HistoricalStackUnderlyingModel underlyingModel;

    public HistoricalStackTableHeadersModel(HistoricalStackUnderlyingModel underlyingModel) {this.underlyingModel = underlyingModel;}

    @Override public int getRowCount() {
        return underlyingModel.getRows().size();
    }

    @Override public int getColumnCount() {
        return 4;
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
        return super.getColumnClass(columnIndex);
    }

    @Override public String getColumnName(int column) {
        String name = "?";
        switch (column) {
            case 0:
                name = "Environment";
                break;
            case 1:
                name = "Host";
                break;
            case 2:
                name = "Type";
                break;
            case 3:
                name = "Instance";
                break;
        }

        return name;
    }

    @Override public Object getValueAt(int rowIndex, int column) {

        HistoricalStackTableRow row = underlyingModel.getRows().get(rowIndex);

        Object value = "?";

        switch (column) {
            case 0:
                value = row.getEnvironment();
                break;
            case 1:
                value = row.getHost();
                break;
            case 2:
                value = row.getType();
                break;
            case 3:
                value = row.getInstance();
                break;
        }
        return value;
    }
}
