package com.logginghub.utils.observable;

import com.logginghub.utils.filter.Filter;

import java.util.List;

@SuppressWarnings("unchecked") public class ObservableFilteredList<T> extends ObservableList<T> {

    public ObservableFilteredList(ObservableList<T> source, List<T> decoratee, final Filter<T> filter) {
        super(decoratee);

        source.addListenerAndNotifyExisting(new ObservableListListener<T>() {
            public void onAdded(T t) {
                if (filter.passes(t)) {
                    add(t);
                }
            }

            public void onRemoved(T t, int index) {
                remove(t);
            }

            public void onCleared() {
                clear();
            }
        });

    }
}
