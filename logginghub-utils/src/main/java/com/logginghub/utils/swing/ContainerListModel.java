package com.logginghub.utils.swing;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.logginghub.utils.container.Container;
import com.logginghub.utils.container.ContainerListener;

/**
 * Wraps a list container in a list model for display via a JList
 * 
 * @author James
 * 
 * @param <T>
 */
public class ContainerListModel<T> implements ListModel, ContainerListener<T>,
                ListCellRenderer
{
    private static final long serialVersionUID = 1L;
    private final Container<T> m_container;
    private List<ListDataListener> m_listeners = new CopyOnWriteArrayList<ListDataListener>();

    // private List<JComponent> m_components = new ArrayList<JComponent>();
    // private Map<T, JComponent> m_itemToComponent = new HashMap<T,
    // JComponent>();
    private final ContainerItemRenderer<T> m_renderer;

    public ContainerListModel(Container<T> container,
                              ContainerItemRenderer<T> renderer)
    {
        m_container = container;
        m_renderer = renderer;

        m_container.addContainerListener(this);

        addStartingItems();
    }

    public T getItemAt(int index)
    {
        return m_container.get(index);
    }
    
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // ListModel implementation
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    public void addListDataListener(ListDataListener l)
    {
        m_listeners.add(l);
    }

    public Object getElementAt(int index)
    {
        Object object = m_container.get(index);
        return object;
    }

    public int getSize()
    {
        return m_container.size();
    }

    public void removeListDataListener(ListDataListener l)
    {
        m_listeners.remove(l);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // ContainerListener implementation
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    public void onItemAdded(Container<T> container, T item, int index)
    {
        addItem(item);
    }

    public void onItemMoved(Container<T> container,
                                      T item,
                                      int newIndex,
                                      int oldIndex)
    {
    // TODO : implement me
    }

    public void onItemRemoved(Container<T> container,
                                        T item,
                                        int index)
    {
        removeItem(item, index);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // ListCellRenderer implementation
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked") public Component getListCellRendererComponent(JList list,
                                                                                           Object value,
                                                                                           int index,
                                                                                           boolean isSelected,
                                                                                           boolean cellHasFocus)
    {
        return m_renderer.getRendererForItem((T) value);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    private void addStartingItems()
    {
        for (T t : m_container)
        {
            addItem(t);
        }
    }

    private void addItem(T item)
    {
        int index = m_container.size() - 1;
        ListDataEvent event = new ListDataEvent(this,
                                                ListDataEvent.INTERVAL_ADDED,
                                                index,
                                                index);
        fireIntervalAdded(event);
    }

    private void removeItem(T item, int index)
    {
        ListDataEvent event = new ListDataEvent(this,
                                                ListDataEvent.INTERVAL_REMOVED,
                                                index,
                                                index);
        fireIntervalRemoved(event);
    }

    private void fireIntervalAdded(ListDataEvent e)
    {
        for (ListDataListener listDataListener : m_listeners)
        {
            listDataListener.intervalAdded(e);
        }
    }

    private void fireIntervalRemoved(ListDataEvent e)
    {
        for (ListDataListener listDataListener : m_listeners)
        {
            listDataListener.intervalRemoved(e);
        }
    }

    private void fireContentsChanged(ListDataEvent e)
    {
        for (ListDataListener listDataListener : m_listeners)
        {
            listDataListener.contentsChanged(e);
        }
    }

    
}
