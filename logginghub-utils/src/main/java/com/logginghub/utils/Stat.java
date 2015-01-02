package com.logginghub.utils;

public interface Stat {
    boolean hasChanged();
    int getValue();
    String getName();
    void reset();
}
