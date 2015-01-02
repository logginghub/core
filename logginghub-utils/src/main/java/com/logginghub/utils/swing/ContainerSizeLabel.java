package com.logginghub.utils.swing;

import javax.swing.JLabel;

import com.logginghub.utils.container.Container;
import com.logginghub.utils.container.ContainerListener;

public class ContainerSizeLabel<T> extends JLabel implements
                ContainerListener<T>
{
    private static final long serialVersionUID = 1L;
    private final Container<T> m_container;

    public ContainerSizeLabel(Container<T> container)
    {
        m_container = container;
        container.addContainerListener(this);
        update();
    }

    private void update()
    {
        setText(Integer.toString(m_container.size()));
    }

    public void onItemAdded(Container<T> container, T item, int index)
    {
        update();
    }

    public void onItemMoved(Container<T> container,
                                      T item,
                                      int newIndex,
                                      int oldIndex)
    {

    }

    public void onItemRemoved(Container<T> container, T item, int index)
    {
        update();
    }
}
