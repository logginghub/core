package com.logginghub.logging.frontend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableArrayList<T> extends ArrayList<T> implements ObservableList<T> {
    private static final long serialVersionUID = 1L;
    private List<ObservableListListener<T>> listeners = new CopyOnWriteArrayList<ObservableListListener<T>>();

    @Override public void addListListener(ObservableListListener<T> listener) {
        listeners.add(listener);
    }

    @Override public void addListListenerAndNotifyExisting(ObservableListListener<T> listener) {
        addListListener(listener);
        for (T t : this) {
            listener.onItemAdded(t);
        }
    }

    @Override public void removeListListener(ObservableListListener<T> listener) {
        listeners.remove(listener);
    }

    public void add(int index, T element) {
        super.add(index, element);
        fireItemAdded(element);
    }

    public boolean add(T e) {
        boolean b = super.add(e);
        fireItemAdded(e);
        return b;
    };

    @Override public T remove(int index) {
        T removed = super.remove(index);
        fireItemRemoved(removed);
        return removed;
    }

    @SuppressWarnings("unchecked") @Override public boolean remove(Object o) {
        boolean b = super.remove(o);
        fireItemRemoved((T) o);
        return b;
    }

    private void fireItemAdded(T element) {
        for (ObservableListListener<T> observableListListener : listeners) {
            observableListListener.onItemAdded(element);
        }
    };

    private void fireItemRemoved(T element) {
        for (ObservableListListener<T> observableListListener : listeners) {
            observableListListener.onItemRemoved(element);
        }
    };

    public T getFirst(Matcher<T> matcher) {
        T first = null;
        for (T t : this) {
            if (matcher.matches(t)) {
                first = t;
                break;
            }
        }

        return first;
    }

}
