package com.logginghub.utils.observable;

public interface ObservableCollectionListener<T> {
    void onChange(ObservableProperty<T> object);
}
