package com.logginghub.utils.observable;

import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableList<T> implements List<T>, ObservableItem, ObservableItemContainer {

    private List<T> decoratee;
    private ObservableItemContainer parent;
    private Class<T> contentClass;
    private String name;

    private CopyOnWriteArrayList<ObservableListListener<T>> listListeners = null;
    private CopyOnWriteArrayList<ObservableListener> containerListeners;
    private CopyOnWriteArrayList<ObservablePropertyListener<T>> itemListeners;

    public ObservableList(List<T> decoratee) {
        this.decoratee = decoratee;
        this.parent = null;
    }

    public ObservableList(List<T> decoratee, Observable parent) {
        this.decoratee = decoratee;
        this.parent = parent;
    }

    public ObservableList(Class<T> contentClass, List<T> decoratee) {
        this.contentClass = contentClass;
        this.decoratee = decoratee;
        this.parent = null;
    }

    // public ObservableList<T> copy() {
    // ObservableList<T> copy = new ObservableList<T>(new ArrayList<T>());
    // for (T t : decoratee) {
    // if (t instanceof ObservableItem) {
    // ObservableItem observableItem = (ObservableItem) t;
    // copy.decoratee.add((T) observableItem.copy());
    // }
    // else {
    // copy.decoratee.add(t);
    // }
    // }
    // return copy;
    // }

    public void setContentClass(Class<T> contentClass) {
        this.contentClass = contentClass;
    }

    public ObservableList(Class<T> contentClass, List<T> decoratee, Observable parent) {
        this.decoratee = decoratee;
        this.parent = parent;
        this.contentClass = contentClass;
    }

    public Class<T> getContentClass() {
        return contentClass;
    }

    public void add(int index, T element) {
        synchronized (decoratee) {
            decoratee.add(index, element);
        }

        if (element instanceof ObservableItem) {
            ObservableItem observableItem = (ObservableItem) element;
            observableItem.setParent(this);
        }

        if (listListeners != null) {
            for (ObservableListListener<T> observableListListener : listListeners) {
                observableListListener.onAdded(element);
            }
        }

        notifyParent();
    }

    public void setParent(ObservableItemContainer parent) {
        this.parent = parent;
    }

    private void notifyParent() {
        if (parent != null) {
            parent.onChildChanged(this);
        }
    }

    public boolean add(T element) {
        boolean added;
        synchronized (decoratee) {
            added = decoratee.add(element);
        }

        if (element instanceof ObservableItem) {
            ObservableItem observableItem = (ObservableItem) element;
            observableItem.setParent(this);
        }

        if (listListeners != null) {
            for (ObservableListListener<T> observableListListener : listListeners) {
                observableListListener.onAdded(element);
            }
        }

        notifyParent();

        return added;
    }

    public boolean addAll(Collection c) {
        
        for (Object object : c) {
            add((T) object);
        }
        
        return true;
    }        

    public boolean addAll(int index, Collection c) {
        throw new NotImplementedException();
    }

    public synchronized void addListener(ObservableListListener<T> listener) {
        if (listListeners == null) {
            listListeners = new CopyOnWriteArrayList<ObservableListListener<T>>();
        }
        listListeners.add(listener);
    }

    public void addListenerAndNotifyExisting(ObservableListListener<T> listener) {
        addListener(listener);
        synchronized (decoratee) {
            for (T item : decoratee) {
                listener.onAdded(item);
            }
        }

    }

    public void clear() {
        synchronized (decoratee) {
            if (listListeners != null) {

                for(int i = 0; i < decoratee.size(); i++) {
                    T item = decoratee.get(i);

                    for (ObservableListListener<T> observableListListener : listListeners) {
                        observableListListener.onRemoved(item, i);
                    }
                }
            }

            decoratee.clear();
        }

        if (listListeners != null) {
            notifyCleared();
        }
    }

    private void notifyCleared() {
        for (final ObservableListListener<T> observableListListener : listListeners) {
            observableListListener.onCleared();
        }
    }

    public void clearQuietly() {
        synchronized (decoratee) {
            decoratee.clear();
        }

        if (listListeners != null) {
            for (ObservableListListener<T> observableListListener : listListeners) {
                observableListListener.onCleared();
            }
        }
    }

    public boolean contains(Object o) {
        synchronized (decoratee) {
            return decoratee.contains(o);
        }
    }

    public boolean containsAll(Collection c) {
        synchronized (decoratee) {
            return decoratee.containsAll(c);
        }
    }

    public T get(int index) {
        synchronized (decoratee) {
            return decoratee.get(index);
        }
    }

    public int indexOf(Object o) {
        synchronized (decoratee) {
            return decoratee.indexOf(o);
        }
    }

    public boolean isEmpty() {
        synchronized (decoratee) {
            return decoratee.isEmpty();
        }
    }

    public Iterator iterator() {
        synchronized (decoratee) {
            return decoratee.iterator();
        }
    }

    public int lastIndexOf(Object o) {
        synchronized (decoratee) {
            return decoratee.lastIndexOf(o);
        }
    }

    public ListIterator<T> listIterator() {
        synchronized (decoratee) {
            return decoratee.listIterator();
        }
    }

    public ListIterator<T> listIterator(int index) {
        synchronized (decoratee) {
            return decoratee.listIterator(index);
        }
    }

    public T remove(int index) {
        T removed;
        synchronized (decoratee) {
            removed = decoratee.remove(index);
        }

        if (listListeners != null) {
            // Do a reverse loop through the listeners - this ensures symmetry when binding on add
            // and unbind on remove.
            for (int i = size() - 1; i > 0; i--) {
                ObservableListListener<T> observableListListener = listListeners.get(i);
                observableListListener.onRemoved(removed, index);
            }
        }

        notifyParent();

        return removed;
    }

    public boolean remove(Object o) {

        boolean removed;
        int index;
        synchronized (decoratee) {
            index = decoratee.indexOf(o);
            removed = decoratee.remove(o);
        }

        if (listListeners != null) {
            // Do a reverse loop through the listeners - this ensures symmetry when binding on add
            // and unbind on remove.
            int size = listListeners.size();
            for (int i = size - 1; i >= 0; i--) {
                ObservableListListener<T> observableListListener = listListeners.get(i);
                observableListListener.onRemoved((T) o, index);
            }
        }

        notifyParent();

        return removed;
    }

    public boolean removeAll(Collection c) {
        synchronized (decoratee) {
            return decoratee.removeAll(c);
        }
    }

    public synchronized void removeListener(ObservableListListener<T> listener) {
        if (listListeners != null) {
            listListeners.remove(listener);
        }
    }

    public boolean retainAll(Collection c) {
        synchronized (decoratee) {
            return decoratee.retainAll(c);
        }
    }

    public T set(int index, T element) {
        synchronized (decoratee) {
            return decoratee.set(index, element);
        }
    }

    public int size() {
        synchronized (decoratee) {
            return decoratee.size();
        }
    }

    public List<T> subList(int fromIndex, int toIndex) {
        synchronized (decoratee) {
            return decoratee.subList(fromIndex, toIndex);
        }
    }

    public Object[] toArray() {
        synchronized (decoratee) {
            return decoratee.toArray();
        }
    }

    public Object[] toArray(Object[] a) {
        synchronized (decoratee) {
            return decoratee.toArray(a);
        }
    }

    public String toString() {
        synchronized (decoratee) {
            return decoratee.toString();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addListener(ObservablePropertyListener observablePropertyListener) {
        if (itemListeners == null) {
            itemListeners = new CopyOnWriteArrayList<ObservablePropertyListener<T>>();
        }
        itemListeners.add(observablePropertyListener);
    }

    public String getName() {
        return name;
    }

    public void setParent(Observable parent) {
        if (this.parent != null) {
            this.parent.onChildRemoved(this);
        }
        this.parent = parent;

        parent.onChildAdded(this);
    }

    public void onChildAdded(ObservableItem item) {}

    public void onChildRemoved(ObservableItem item) {}

    public void onChildChanged(ObservableItem item) {
        notifyParent();
        if (containerListeners != null) {
            for (ObservableListener observableListener : containerListeners) {
                observableListener.onChanged(this, item);
            }
        }
    }

    public synchronized void addListener(ObservableListener listener) {
        if (containerListeners == null) {
            containerListeners = new CopyOnWriteArrayList<ObservableListener>();
        }
        containerListeners.add(listener);
    }

    public synchronized void removeListener(ObservableListener listener) {
        if (containerListeners != null) {
            containerListeners.remove(listener);
        }
    }

    /**
     * Make this observable list contain references to the same set of objects in another Observable
     * list.
     * 
     * @param other
     */
    public void set(ObservableList<T> other) {
        decoratee.clear();

        if (listListeners != null) {
            notifyCleared();
        }
        
        for (T t : other.decoratee) {
            add(t);
        }
        
        notifyParent();

    }

    @SuppressWarnings("unchecked") public ObservableList<T> duplicate() {

        Class<? extends List> containerClass = decoratee.getClass();
        List<T> decoratee = ReflectionUtils.instantiate(containerClass);

        ObservableList<T> duplicate = new ObservableList<T>(decoratee);

        duplicate.parent = null;
        duplicate.contentClass = this.contentClass;
        duplicate.name = this.name;

        // Dont duplicate the listeners?
        for (T element : this.decoratee) {

            if (element instanceof ObservableProperty<?>) {
                ObservableProperty<?> originalProperty = (ObservableProperty<?>) element;
                AbstractObservableProperty<?> duplicateProperty = originalProperty.duplicate();
                decoratee.add((T) duplicateProperty);
            }
            else if (element instanceof ObservableItem) {
                ObservableItem observableItem = (ObservableItem) element;
                decoratee.add((T) observableItem.duplicate());
            }
            else {
                // The object isn't another observable?! What do we do, include the original
                // instance? Or have some sort of generic copyable interface? What the hell does
                // clone() do anyway!
                throw new NotImplementedException(StringUtils.format("Can't duplicate instance type '{}'", element.getClass()));
            }

        }

        return duplicate;

    }

}
