package com.logginghub.utils.observable;

import java.util.List;

import com.logginghub.utils.filter.Filter;

@SuppressWarnings("unchecked") public class ObservableFilteredList<T> extends ObservableList<T> {

    public ObservableFilteredList(ObservableList<T> source, List<T> decoratee, final Filter<T> filter) {
        super(decoratee);

        source.addListenerAndNotifyExisting(new ObservableListListener<T>() {
            public void onAdded(T t) {
                if (filter.passes(t)) {
                    add(t);
                }
            }

            public void onRemoved(T t) {
                remove(t);
            }

            public void onCleared() {
                clear();
            }
        });

    }
}
