package com.logginghub.utils.swing;

import javax.swing.JList;

import com.logginghub.utils.container.Container;

public class ContainerList<T> extends JList
{
    private static final long serialVersionUID = 1L;

    private ContainerListModel<T> m_model;

    public ContainerList(Container<T> container,
                         ContainerItemRenderer<T> renderer)
    {
        m_model = new ContainerListModel<T>(container, renderer);
        setModel(m_model);
        setCellRenderer(m_model);
    }

    public T getSelectedItem()
    {
        int selectedIndex = getSelectedIndex();
        return m_model.getItemAt(selectedIndex);
    }
}
