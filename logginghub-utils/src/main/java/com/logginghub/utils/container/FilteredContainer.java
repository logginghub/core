package com.logginghub.utils.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.logginghub.utils.filter.Filter;

/**
 * Decorates a Container to provide filtering features
 * 
 * @author James
 * 
 * @param <T>
 */
public class FilteredContainer<T extends Object> implements Iterable<T>, ChangeListener<T>, Container<T> {
    private final Container<T> container;
    private final Filter<T> filter;

    private Set<T> visibleItemsSet = new HashSet<T>();
    private List<T> visibleItemsList = new ArrayList<T>();

    @SuppressWarnings("unchecked") public FilteredContainer(Container<T> container, Filter<T> filter) {
        this.container = container;
        this.filter = filter;

        container.addContainerListener(new ContainerListener<T>() {
            public void onItemAdded(Container<T> container, T item, int index) {
                processItem(item);
            }

            public void onItemMoved(Container<T> container, T item, int newIndex, int oldIndex) {

            }

            public void onItemRemoved(Container<T> container, T item, int index) {
                if (visibleItemsSet.contains(item)) {
                    removeItem(item, index);
                }

            }
        });

        addInitialItems();

        if (filter instanceof Changeable<?>) {
            ((Changeable<T>) filter).addChangeListener(new ChangeListener<T>() {
                public void onChanged(T t) {
                    refilterAll();
                }
            });
        }
    }

    protected void removeItem(T t, int index) {
        visibleItemsSet.remove(t);
        visibleItemsList.remove(index);
        fireItemRemoved(t, index);
    }

    protected void refilterAll() {
        for (T t : container) {
            processItem(t);
        }
    }

    private void addInitialItems() {
        for (T t : container) {
            if (filter.passes(t)) {
                visibleItemsSet.add(t);
                visibleItemsList.add(t);
            }
        }
    }

    public Iterator<T> iterator() {
        return Collections.unmodifiableList(visibleItemsList).iterator();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // ChangableListener implementation
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    public void onChanged(T t) {
        processItem(t);
    }

    private void processItem(T t) {
        if (filter.passes(t)) {
            if (visibleItemsSet.contains(t)) {
                // Fine
            }
            else {
                visibleItemsSet.add(t);
                visibleItemsList.add(t);
                int index = visibleItemsList.size() - 1;
                fireItemAdded(t, index);
            }
        }
        else {
            if (visibleItemsSet.contains(t)) {
                int index = visibleItemsList.indexOf(t);
                removeItem(t, index);
            }
            else {
                // Fine
            }
        }
    }

    public void addContainerListener(ContainerListener<T> listener) {
        m_listeners.add(listener);
    }

    public void removeContainerListener(ContainerListener<T> listener) {
        m_listeners.remove(listener);
    }

    public int size() {
        return visibleItemsSet.size();
    }

    public void addComparator(Comparator<T> comparator) {
        // TODO Auto-generated method stub

    }

    public Container<T> filteredCopy(Filter<T> filter) {
        FilteredContainer<T> copy = new FilteredContainer<T>(this, filter);
        return copy;
    }

    public T get(int index) {
        return visibleItemsList.get(index);
    }

    public void removeComparator(Comparator<T> comparator) {
        // TODO Auto-generated method stub

    }

    // private static final long serialVersionUID = 1L;
    private List<ContainerListener<T>> m_listeners = new ArrayList<ContainerListener<T>>();

    //
    // private AndFilter<T> m_filters = new AndFilter<T>();
    // private CompositeComparator<T> m_comparator = new
    // CompositeComparator<T>();
    //
    // private List<T> m_items = new ArrayList<T>();
    //
    //

    //
    // @SuppressWarnings("unchecked") public void add(T t)
    // {
    // if (t instanceof Changeable<?>)
    // {
    // ((Changeable) t).addCountItemListener(this);
    // }
    //
    // m_items.add(t);
    //
    // if (m_filters.passes(t))
    // {
    // m_visibleItems.add(t);
    // update(t);
    // fireItemAdded(t, m_visibleItems.indexOf(t));
    // }
    // }
    //
    // public void remove(T t)
    // {
    // if (t instanceof Changeable<?>)
    // {
    // ((Changeable) t).removeCountItemListener(this);
    // }
    //
    // m_items.remove(t);
    //
    // if (m_visibleItems.contains(t))
    // {
    // m_visibleItems.remove(t);
    // fireItemRemoved(t);
    // }
    //
    // update();
    // }
    //
    // public void addFilter(Filter<T> filter)
    // {
    // m_filters.addFilter(filter);
    // refreshFilter();
    // }
    //
    // public void removeFilter(Filter<T> filter)
    // {
    // m_filters.removeFilter(filter);
    // refreshFilter();
    // }
    //
    // public void addComparator(Comparator<T> comparator)
    // {
    // m_comparator.addComparator(comparator);
    // update();
    // }
    //
    // public void removeComparator(Comparator<T> comparator)
    // {
    // m_comparator.removeComparator(comparator);
    // update();
    // }
    //
    // public int size()
    // {
    // return m_visibleItems.size();
    // }
    //
    // public T get(int index)
    // {
    // return m_visibleItems.get(index);
    // }
    //
    // public void refreshSorting()
    // {
    // if (m_comparator.getComparatorCount() > 0)
    // {
    // Collections.sort(m_visibleItems, m_comparator);
    // }
    // else
    // {
    // // Refreshing the filter will revert things back to the default
    // // ordering
    // refreshFilter();
    // }
    // }
    //
    // /**
    // * Notify move listeners if anything has moved. It compares the
    // * originalOrder passed in versus the current m_visibleItems.
    // *
    // * @param dontNotifyItem
    // * If this item has moved, dont tell anyone. This is needed to
    // * stop move notifications for items that are about to be added
    // * from being fired before the object has actually been added.
    // * Can be null.
    // */
    // public void notifyDifferences(List<T> originalOrder, T dontNotifyItem)
    // {
    // if (m_comparator.getComparatorCount() > 0)
    // {
    // Collections.sort(m_visibleItems, m_comparator);
    //
    // for (int newPosition = 0; newPosition < m_visibleItems.size();
    // newPosition++)
    // {
    // T t = m_visibleItems.get(newPosition);
    //
    // if (t != dontNotifyItem)
    // {
    // int currentPosition = originalOrder.indexOf(t);
    // if (currentPosition != newPosition)
    // {
    // fireItemMoved(t, newPosition, currentPosition);
    // }
    // }
    // }
    // }
    // else
    // {
    // // Refreshing the filter will revert things back to the default
    // // ordering
    // refreshFilter();
    // }
    // }
    //
    // public boolean isEmpty()
    // {
    // return size() == 0;
    // }
    //
    // /**
    // * Checks the underlying items to see if this item can be found.
    // *
    // * @param tag
    // * @return
    // */
    // public boolean contains(T item)
    // {
    // return m_items.contains(item);
    // }
    //
    // /**
    // * Checks to see if this item is in the visible collection
    // *
    // * @param item
    // * @return
    // */
    // public boolean isVisible(T item)
    // {
    // return m_visibleItems.contains(item);
    // }
    //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // // Listener faff
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // public void addContainerListener(ContainerListener<T> listener)
    // {
    // m_listeners.add(listener);
    // }
    //
    // public void removeContainerListener(ContainerListener<T> listener)
    // {
    // m_listeners.remove(listener);
    // }
    //

    //
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // // Private methods
    // //
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // private void update()
    // {
    // update(null);
    // }
    //
    // private void update(T dontNotifyThisOne)
    // {
    // List<T> snapshot = new ArrayList<T>(m_visibleItems);
    //
    // if (hasFilters())
    // {
    // refreshFilter();
    // }
    //
    // if (hasComparator())
    // {
    // refreshSorting();
    // }
    //
    // if (hasFilters() || hasComparator())
    // {
    // notifyDifferences(snapshot, dontNotifyThisOne);
    // }
    // }
    //
    // private boolean hasComparator()
    // {
    // return m_comparator.getComparatorCount() > 0;
    // }
    //
    // private boolean hasFilters()
    // {
    // return m_filters.getFilters().size() > 0;
    // }
    //
    // private void refreshFilter()
    // {
    // if (m_filters != null)
    // {
    // Set<T> currentlyVisible = new HashSet<T>(m_visibleItems);
    //
    // m_visibleItems.clear();
    //
    // for (T t : m_items)
    // {
    // if (m_filters.passes(t))
    // {
    // m_visibleItems.add(t);
    //
    // if (!currentlyVisible.contains(t))
    // {
    // fireItemAdded(t, m_visibleItems.size());
    // }
    // }
    // else
    // {
    // if (currentlyVisible.contains(t))
    // {
    // fireItemRemoved(t);
    // }
    // }
    // }
    // }
    // }
    //
    // private void fireItemMoved(T item, int newIndex, int oldIndex)
    // {
    // for (ContainerListener<T> listener : m_listeners)
    // {
    // listener.onItemMoved(this, item, newIndex, oldIndex);
    // }
    // }
    //
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

    public void clearComparators() {
        // TODO Auto-generated method stub

    }

    // public FilteredContainer<T> filteredCopy(Filter<T> filter)
    // {
    // FilteredContainer<T> copy = new FilteredContainer<T>();
    // for (T t : m_items)
    // {
    // if (filter.passes(t))
    // {
    // copy.add(t);
    // }
    // }
    // return copy;
    // }
}
