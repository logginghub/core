package com.logginghub.utils;

public class SystemTimeProvider implements TimeProvider {
    public long getTime() {
        return System.currentTimeMillis();
    }
}
