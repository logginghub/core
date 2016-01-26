package com.logginghub.utils.observable;

public abstract class ObservableListAdaptor<T> implements ObservableListListener<T> {
    public void onAdded(T t) {}
    public void onRemoved(T t, int index) {}
    public void onCleared() {}
}
