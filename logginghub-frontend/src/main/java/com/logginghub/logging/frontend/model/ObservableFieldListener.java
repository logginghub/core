package com.logginghub.logging.frontend.model;

public interface ObservableFieldListener<T> {
    void onChanged(T oldValue, T newValue);
}
