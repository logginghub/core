package com.logginghub.utils.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.logginghub.utils.filter.CompositeAndFilter;
import com.logginghub.utils.filter.Filter;

public class ListContainer<T extends Object> implements Iterable<T>, ChangeListener<T>, Container<T> {
    private static final long serialVersionUID = 1L;
    private List<ContainerListener<T>> m_listeners = new ArrayList<ContainerListener<T>>();

    private CompositeAndFilter<T> m_filters = new CompositeAndFilter<T>();
    private CompositeComparator<T> m_comparator = new CompositeComparator<T>();

    private List<T> m_items = new ArrayList<T>();
    private List<T> m_visibleItems = new ArrayList<T>();

    public ListContainer() {

    }

    public ListContainer(Container<T> copyFrom) {
        addAll(copyFrom);
    }

    public Iterator<T> iterator() {
        return Collections.unmodifiableList(m_visibleItems).iterator();
    }

    public void addAll(Container<T> container) {
        for (T t : container) {
            add(t);
        }
    }

    @SuppressWarnings("unchecked") public void add(T t) {
        if (t instanceof Changeable<?>) {
            ((Changeable) t).addChangeListener(this);
        }

        m_items.add(t);

        if (m_filters.passes(t)) {
            m_visibleItems.add(t);
            update(t);
            fireItemAdded(t, m_visibleItems.indexOf(t));
        }
    }

    public void remove(T t) {
        if (t instanceof Changeable<?>) {
            ((Changeable) t).removeChangeListener(this);
        }

        m_items.remove(t);

        if (m_visibleItems.contains(t)) {
            m_visibleItems.remove(t);

            // TODO : work out whats going on with indexes
            fireItemRemoved(t, 0);
        }

        update();
    }

    public void addFilter(Filter<T> filter) {
        m_filters.addFilter(filter);
        refreshFilter();
    }

    public void removeFilter(Filter<T> filter) {
        m_filters.removeFilter(filter);
        refreshFilter();
    }

    public void addComparator(Comparator<T> comparator) {
        m_comparator.addComparator(comparator);
        update();
    }

    public void removeComparator(Comparator<T> comparator) {
        m_comparator.removeComparator(comparator);
        update();
    }

    public void clearComparators() {
        m_comparator.clear();
        update();
    }

    public int size() {
        return m_visibleItems.size();
    }

    public T get(int index) {
        return m_visibleItems.get(index);
    }

    public void refreshSorting() {
        if (m_comparator.getComparatorCount() > 0) {
            Collections.sort(m_visibleItems, m_comparator);
        }
        else {
            // Refreshing the filter will revert things back to the default
            // ordering
            refreshFilter();
        }
    }

    /**
     * Notify move listeners if anything has moved. It compares the originalOrder passed in versus
     * the current m_visibleItems.
     * 
     * @param dontNotifyItem
     *            If this item has moved, dont tell anyone. This is needed to stop move
     *            notifications for items that are about to be added from being fired before the
     *            object has actually been added. Can be null.
     */
    public void notifyDifferences(List<T> originalOrder, T dontNotifyItem) {
        if (m_comparator.getComparatorCount() > 0) {
            Collections.sort(m_visibleItems, m_comparator);

            for (int newPosition = 0; newPosition < m_visibleItems.size(); newPosition++) {
                T t = m_visibleItems.get(newPosition);

                if (t != dontNotifyItem) {
                    int currentPosition = originalOrder.indexOf(t);
                    if (currentPosition != newPosition) {
                        fireItemMoved(t, newPosition, currentPosition);
                    }
                }
            }
        }
        else {
            // Refreshing the filter will revert things back to the default
            // ordering
            refreshFilter();
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Checks the underlying items to see if this item can be found.
     * 
     * @param tag
     * @return
     */
    public boolean contains(T item) {
        return m_items.contains(item);
    }

    /**
     * Checks to see if this item is in the visible collection
     * 
     * @param item
     * @return
     */
    public boolean isVisible(T item) {
        return m_visibleItems.contains(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener faff
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    public void addContainerListener(ContainerListener<T> listener) {
        m_listeners.add(listener);
    }

    public void removeContainerListener(ContainerListener<T> listener) {
        m_listeners.remove(listener);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // ChangableListener implementation
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    public void onChanged(T t) {
        update();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    private void update() {
        update(null);
    }

    private void update(T dontNotifyThisOne) {
        if (hasFilters()) {
            refreshFilter();
        }

        if (hasComparator()) {
            refreshSorting();
        }

        if (hasFilters() || hasComparator()) {
            List<T> snapshot = new ArrayList<T>(m_visibleItems);
            notifyDifferences(snapshot, dontNotifyThisOne);
        }
    }

    private boolean hasComparator() {
        return m_comparator.getComparatorCount() > 0;
    }

    private boolean hasFilters() {
        return m_filters.getFilters().size() > 0;
    }

    private void refreshFilter() {
        if (m_filters != null) {
            Set<T> currentlyVisible = new HashSet<T>(m_visibleItems);

            m_visibleItems.clear();

            for (T t : m_items) {
                if (m_filters.passes(t)) {
                    m_visibleItems.add(t);

                    if (!currentlyVisible.contains(t)) {
                        fireItemAdded(t, m_visibleItems.size());
                    }
                }
                else {
                    if (currentlyVisible.contains(t)) {
                        // TODO : work out whats going on with indexes
                        fireItemRemoved(t, 0);
                    }
                }
            }
        }
    }

    private void fireItemMoved(T item, int newIndex, int oldIndex) {
        for (ContainerListener<T> listener : m_listeners) {
            listener.onItemMoved(this, item, newIndex, oldIndex);
        }
    }

    private void fireItemAdded(T item, int index) {
        for (ContainerListener<T> listener : m_listeners) {
            listener.onItemAdded(this, item, index);
        }
    }

    private void fireItemRemoved(T item, int index) {
        for (ContainerListener<T> listener : m_listeners) {
            listener.onItemRemoved(this, item, index);
        }
    }

    public ListContainer<T> sortedCopy(Comparator<T> comparator) {
        ListContainer<T> copy = new ListContainer<T>();
        for (T t : m_items) {
            copy.add(t);
        }

        copy.addComparator(comparator);
        return copy;
    }

    public ListContainer<T> filteredCopy(Filter<T> filter) {
        ListContainer<T> copy = new ListContainer<T>();
        for (T t : m_items) {
            if (filter.passes(t)) {
                copy.add(t);
            }
        }
        return copy;
    }

    public void clear() {
        m_items.clear();
        m_visibleItems.clear();
    }

    public List<T> getCopyAsList() {
        return new ArrayList<T>(m_visibleItems);
    }

    public ListContainer<T> copy() {
        ListContainer<T> copy = new ListContainer<T>(this);
        return copy;
    }

    public void shuffle() {
        Collections.shuffle(m_visibleItems);
    }

    // @Override public boolean addAll(Collection<? extends T> c)
    // {
    // for (T t : c)
    // {
    // add(t);
    // }
    // return true;
    // }
    //
    // @Override public boolean containsAll(Collection<?> c)
    // {
    // boolean containsAll = true;
    //
    // for (Object object : c)
    // {
    // containsAll = contains(object);
    // if(!containsAll)
    // {
    // break;
    // }
    // }
    //
    // return containsAll;
    // }
    //
    // @Override public boolean removeAll(Collection<?> c)
    // {
    // for (Object object : c)
    // {
    // remove(object);
    // }
    // return true;
    // }
    //
    // @Override public boolean retainAll(Collection<?> c)
    // {
    // return false;
    // }
    //
    // @Override public Object[] toArray()
    // {
    // return null;
    // }
    //
    // @Override public <T> T[] toArray(T[] a)
    // {
    // return null;
    // }
}
