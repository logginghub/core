package com.logginghub.utils.observable;

public interface ObservableListListener<T> {
    void onAdded(T t);
    void onRemoved(T t, int index);
    void onCleared();
}
