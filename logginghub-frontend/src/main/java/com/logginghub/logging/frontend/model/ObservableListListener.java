package com.logginghub.logging.frontend.model;

public interface ObservableListListener<T> {
    void onItemAdded(T t);
    void onItemRemoved(T t);
}
