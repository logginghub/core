package com.logginghub.utils;

public class Message {

    private String format;
    private Object[] params;

    public Message(String format, Object... params) {
        this.format = format;
        this.params = params;
    }

    @Override public String toString() {
        return StringUtils.format(format, params);
    }
}
