package com.logginghub.utils.container;

public interface ContainerListener<T>
{
    void onItemAdded(Container<T> container, T item, int index);
    void onItemRemoved(Container<T> container, T item, int index);
    void onItemMoved(Container<T> container, T item, int newIndex, int oldIndex);
}
