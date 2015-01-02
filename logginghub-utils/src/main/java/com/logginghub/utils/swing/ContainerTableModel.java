package com.logginghub.utils.swing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.logginghub.utils.container.Container;

public abstract class ContainerTableModel<T> implements TableModel
{
    private final Container<T> m_container;
    private List<TableModelListener> m_listeners = new CopyOnWriteArrayList<TableModelListener>();

    public ContainerTableModel(Container<T> container)
    {
        m_container = container;
    }
    
    public Container<T> getContainer()
    {
        return m_container;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // TableModel implementation
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract int getColumnCount();

    public abstract String getColumnName(int columnIndex);

    public int getRowCount()
    {
        return m_container.size();
    }

    public Object getValueAt(int row, int column)
    {
        T t = m_container.get(row);
        return getColumnValue(t, column);
    }

    protected abstract Object getColumnValue(T item, int column);

    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    public void setValueAt(Object object, int row, int column)
    {

    }
    
    public Class<?> getColumnClass(int columnIndex)
    {     
        return getValueAt(0, columnIndex).getClass();
    }

    public void addTableModelListener(TableModelListener l)
    {
        m_listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l)
    {
        m_listeners.remove(l);

    }
}
