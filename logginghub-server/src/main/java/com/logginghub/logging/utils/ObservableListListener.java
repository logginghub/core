package com.logginghub.logging.utils;

public interface ObservableListListener<T> {
    void onAdded(T t);
    void onRemoved(T t);
}
