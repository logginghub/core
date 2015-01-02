package com.logginghub.utils.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;

import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.container.Container;

public class ContainerTable<T> extends JTable
{
    private static final long serialVersionUID = 1L;
    private int m_currentSortingColumn = -1;
    private boolean m_sortAscending = false;
    private final ContainerTableModel<T> m_model;

    Comparator<T> comparator = new Comparator<T>()
    {
        public int compare(T a, T b)
        {
            Object aValue = m_model.getColumnValue(a, m_currentSortingColumn);
            Object bValue = m_model.getColumnValue(b, m_currentSortingColumn);

            return CompareUtils.compare(aValue, bValue);
        }
    };

    public ContainerTable(final ContainerTableModel<T> model)
    {
        m_model = model;
        setModel(model);

        JTableHeader header = getTableHeader();
        header.addMouseListener(new MouseAdapter()
        {
            @Override public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);

                final int columnModelIndex = columnModel.getColumnIndexAtX(e.getX());
                int columnIndex = columnModel.getColumn(columnModelIndex)
                                             .getModelIndex();

                if (m_currentSortingColumn == columnIndex)
                {
                    m_sortAscending = !m_sortAscending;
                }
                else
                {
                    m_currentSortingColumn = columnIndex;
                    m_sortAscending = false;
                }
                
                Container<T> container = model.getContainer();
                container.clearComparators();

                if (m_sortAscending)
                {
                    container.addComparator(comparator);
                }
                else
                {
                    container.addComparator(Collections.reverseOrder(comparator));
                }

                tableChanged(new TableModelEvent(getModel()));
                repaint();
                
            }
        });
    }
}
