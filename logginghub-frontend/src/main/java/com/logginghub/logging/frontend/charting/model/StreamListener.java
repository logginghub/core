package com.logginghub.logging.frontend.charting.model;

public interface StreamListener<T> {
    void onNewItem(T t);
}
