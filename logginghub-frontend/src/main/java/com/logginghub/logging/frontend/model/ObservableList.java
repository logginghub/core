package com.logginghub.logging.frontend.model;

import java.util.List;

public interface ObservableList<T> extends List<T> {

    void addListListener(ObservableListListener<T> listener);
    void addListListenerAndNotifyExisting(ObservableListListener<T> listener);
    void removeListListener(ObservableListListener<T> listener);
    T getFirst(Matcher<T> matcher);
}
