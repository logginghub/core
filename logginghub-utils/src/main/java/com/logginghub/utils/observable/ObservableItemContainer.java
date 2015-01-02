package com.logginghub.utils.observable;


public interface ObservableItemContainer {
    
    void onChildAdded(ObservableItem item);
    void onChildRemoved(ObservableItem item);
    void onChildChanged(ObservableItem item);
    
    void addListener(ObservableListener listener);
    void removeListener(ObservableListener listener);
}
