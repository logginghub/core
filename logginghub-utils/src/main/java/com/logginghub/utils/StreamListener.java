package com.logginghub.utils;

public interface StreamListener<T> {
    void onNewItem(T t);
}
