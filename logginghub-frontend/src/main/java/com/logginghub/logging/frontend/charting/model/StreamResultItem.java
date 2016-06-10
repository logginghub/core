package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.logging.Logger;

public class StreamResultItem {

    private String path;
    private String groupBy;
    private String result;
    private long time;
    private boolean isNumeric;
    private String label;

    public StreamResultItem() {
    }

    public StreamResultItem(long time, String label, String path, String groupBy, String result, boolean isNumeric) {
        super();
        this.label = label;
        this.time = time;
        this.path = path;
        this.groupBy = groupBy;
        this.result = result;
        this.isNumeric = isNumeric;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getTime() {
        return time;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    @Override
    public String toString() {
        return "StreamResultItem [time=" +
               Logger.toDateString(time) +
               ", label=" +
               label +
               ", path=" +
               path +
               ", result=" +
               result +
               ", isNumeric=" +
               isNumeric +
               "]";
    }

}
