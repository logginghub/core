package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.logging.Logger;

public class StreamResultItem {

    private String path;
    private String result;
    private long time;
    private boolean isNumeric;

    public StreamResultItem() {}

    public StreamResultItem(long time, String path, String result, boolean isNumeric) {
        super();
        this.time = time;
        this.path = path;
        this.result = result;
        this.isNumeric = isNumeric;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public String getPath() {
        return path;
    }

    public String getResult() {
        return result;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override public String toString() {
        return "StreamResultItem [time=" + Logger.toDateString(time) + ", path=" + path + ", result=" + result + ", isNumeric=" + isNumeric + "]";
    }

    public long getTime() {
        return time;
    }

}
