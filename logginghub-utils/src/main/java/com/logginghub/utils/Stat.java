package com.logginghub.utils;

public interface Stat<ReturnType> {
    boolean hasChanged();
    ReturnType getValue();
    String getName();
    void reset();
}
