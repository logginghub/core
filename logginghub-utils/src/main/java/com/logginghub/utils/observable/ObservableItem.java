package com.logginghub.utils.observable;

public interface ObservableItem {
    void setParent(ObservableItemContainer parent);
    void addListener(ObservablePropertyListener observablePropertyListener);
    ObservableItem duplicate();
}
