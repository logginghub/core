package com.logginghub.utils.container;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract base class that implements observable pattern for things that can change.
 * 
 * @author James
 * 
 * @param <T>
 */
public class Changeable<T> {
    private List<ChangeListener<T>> m_listeners = new CopyOnWriteArrayList<ChangeListener<T>>();

    public void addChangeListener(ChangeListener<T> listener) {
        m_listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener<T> listener) {
        m_listeners.remove(listener);
    }

    protected void fireChanged(T t) {
        for (ChangeListener<T> listener : m_listeners) {
            listener.onChanged(t);
        }
    }
}
