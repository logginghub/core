package com.logginghub.utils.container;

import java.util.Comparator;

import com.logginghub.utils.filter.Filter;


public interface Container<T> extends Iterable<T>
{
    void addContainerListener(ContainerListener<T> listener);
    void removeContainerListener(ContainerListener<T> listener);
    int size();
    
    void addComparator(Comparator<T> comparator);
    void removeComparator(Comparator<T> comparator);
    void clearComparators();
    
    Container<T> filteredCopy(Filter<T> filter);
    T get(int index);
}
