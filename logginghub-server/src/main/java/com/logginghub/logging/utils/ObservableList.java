package com.logginghub.logging.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableList<T> implements List<T> {

    private List<T> decoratee;
    private CopyOnWriteArrayList<ObservableListListener<T>> listeners = new CopyOnWriteArrayList<ObservableListListener<T>>();

    public ObservableList() {
        this.decoratee = new ArrayList<T>();
    }
    
    public ObservableList(List<T> decoratee) {
        this.decoratee = decoratee;
    }

    public void addListener(ObservableListListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ObservableListListener<T> listener) {
        listeners.remove(listener);
    }

    public void addListenerAndNotifyCurrent(ObservableListListener<T> listener) {
        addListener(listener);
        for (T t : decoratee) {
            listener.onAdded(t);
        }
    }

    @Override public int size() {
        return decoratee.size();
    }

    @Override public boolean isEmpty() {
        return decoratee.isEmpty();

    }

    @Override public boolean contains(Object o) {
        return decoratee.contains(o);
    }

    @Override public Iterator<T> iterator() {
        return decoratee.iterator();
    }

    @Override public Object[] toArray() {
        return decoratee.toArray();
    }

    @Override public <T> T[] toArray(T[] a) {
        return decoratee.toArray(a);
    }

    @Override public boolean add(T e) {
        boolean add = decoratee.add(e);
        notifyAdded(e);
        return add;
    }

    @SuppressWarnings("unchecked") @Override public boolean remove(Object o) {
        boolean removed = decoratee.remove(o);
        if(removed) {
            notifyRemoved((T)o);
        }
        return removed;
    }

    @Override public boolean containsAll(Collection<?> c) {
        return decoratee.containsAll(c);
    }

    @Override public boolean addAll(Collection<? extends T> c) {
        boolean added = decoratee.addAll(c);
        for (T t : c) {
            notifyAdded(t);
        }
        return added;
    }

    @Override public boolean addAll(int index, Collection<? extends T> c) {
        // TODO : implement the removal stuff for this
        return decoratee.addAll(index, c);
    }

    @Override public boolean removeAll(Collection<?> c) {
        // TODO : implement the removal stuff for this
        boolean removed = decoratee.removeAll(c);
        return removed;
    }

    @Override public boolean retainAll(Collection<?> c) {
        // TODO : implement the removal stuff for this!
        return decoratee.retainAll(c);
    }

    @Override public void clear() {
        
        List<T> existing = new ArrayList<T>(decoratee);
        
        decoratee.clear();
        
        for (T t : existing) {
            notifyRemoved(t);
        }
    }

    @Override public T get(int index) {
        return decoratee.get(index);
    }

    @Override public T set(int index, T element) {
        T previous = decoratee.set(index, element);
        
        if(previous != null) {
            notifyRemoved(previous);
        }
        
        notifyAdded(element);
        
        return previous;
    }

    @Override public void add(int index, T element) {
        decoratee.add(index, element);
        notifyAdded(element);
    }

    @Override public T remove(int index) {
        T removed = decoratee.remove(index);
        notifyRemoved(removed);
        return removed;
    }

    @Override public int indexOf(Object o) {
        return decoratee.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
        return decoratee.lastIndexOf(o);
    }

    @Override public ListIterator<T> listIterator() {
        return decoratee.listIterator();
    }

    @Override public ListIterator<T> listIterator(int index) {
        return decoratee.listIterator(index);
    }

    @Override public List<T> subList(int fromIndex, int toIndex) {
        return decoratee.subList(fromIndex, toIndex);
    }

    private void notifyRemoved(T t) {
        for (ObservableListListener<T> listener : listeners) {
            listener.onRemoved(t);
        }
    }

    private void notifyAdded(T t) {
        for (ObservableListListener<T> listener : listeners) {
            listener.onAdded(t);
        }
    }

}
