package com.logginghub.utils.swing;

import javax.swing.JComponent;

public abstract class ContainerComponent<T> extends JComponent 
{
    private static final long serialVersionUID = 1L;
    private final T m_item;

    public ContainerComponent(T item)
    {
        m_item = item;      
    }
    
    public T getContainerItem()
    {
        return m_item;
    }
}

