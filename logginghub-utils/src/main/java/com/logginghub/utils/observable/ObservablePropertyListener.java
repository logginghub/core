package com.logginghub.utils.observable;

public interface ObservablePropertyListener<T> {
    void onPropertyChanged(T oldValue, T newValue);
}
