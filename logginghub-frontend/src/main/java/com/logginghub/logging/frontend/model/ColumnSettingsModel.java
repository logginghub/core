package com.logginghub.logging.frontend.model;

import java.util.HashMap;
import java.util.Map;

public class ColumnSettingsModel {

    private boolean disableColumnFile = false;
    private Map<String, ColumnSettingModel> columnSettings = new HashMap<String, ColumnSettingModel>();

    public void setDisableColumnFile(boolean disableColumnFile) {
        this.disableColumnFile = disableColumnFile;
    }

    public boolean isDisableColumnFile() {
        return disableColumnFile;
    }

    public Map<String, ColumnSettingModel> getColumnSettings() {
        return columnSettings;
    }

    public ColumnSettingModel getSettingsForColumn(String column) {
        return columnSettings.get(column);
    }

    public final static class ColumnSettingModel {

        public int width;
        public int order;
        public String name;

        public ColumnSettingModel(String name, int width, int order) {
            this.name = name;
            this.width = width;
            this.order = order;
        }
    }
}
