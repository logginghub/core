package com.logginghub.logging.frontend.instanceview;

import com.logginghub.swingutils.table.ExtensibleTableModel;
import com.logginghub.utils.TimeUtils;

public class InstanceViewTableModel extends ExtensibleTableModel<InstanceInfo> {

    private static final int COLUMN_NAME = 0;
    private static final int COLUMN_PID = 1;
    private static final int COLUMN_HOST = 2;
    private static final int COLUMN_IP = 3;
    private static final int COLUMN_LOCAL_PORT = 4;
    private static final int COLUMN_PING = 5;
    private static final int COLUMN_LAST_RESPONSE = 6;

    private String[] columnNames = new String[] { "Name", "PID", "Host", "IP", "Local Port", "Ping", "Last Seen" };

    @Override public String[] getColumnNames() {
        return columnNames;
    }

    @Override public Object extractValue(InstanceInfo item, int columnIndex) {

        Object value = "???";
        switch (columnIndex) {
            case COLUMN_PID:
                value = item.getPid();
                break;
            case COLUMN_LOCAL_PORT:
                value = item.getLocalPort();
                break;
            case COLUMN_PING:
                value = item.getDelay();
                break;
            case COLUMN_HOST:
                value = item.getHost();
                break;
            case COLUMN_IP:
                value = item.getIp();
                break;
            case COLUMN_NAME:
                value = item.getName();
                break;
            case COLUMN_LAST_RESPONSE:
                value = TimeUtils.formatIntervalMilliseconds(System.currentTimeMillis() - item.getLastResponse());
                break;
        }

        return value;
    }

}
