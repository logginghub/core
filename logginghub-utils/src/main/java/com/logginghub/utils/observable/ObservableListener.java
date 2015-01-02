package com.logginghub.utils.observable;

public interface ObservableListener {
    void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged);
}
