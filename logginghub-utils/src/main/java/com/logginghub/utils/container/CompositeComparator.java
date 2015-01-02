package com.logginghub.utils.container;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CompositeComparator<T> implements Comparator<T>
{
    private List<Comparator<T>> m_comparators = new ArrayList<Comparator<T>>();

    public int compare(T a, T b)
    {
        int compare = 0;

        for (Comparator<T> comparator : m_comparators)
        {
            compare = comparator.compare(a, b);
            if (compare != 0)
            {
                break;
            }
        }

        return compare;
    }

    public void addComparator(Comparator<T> comparator)
    {
        m_comparators.add(comparator);
    }

    public void removeComparator(Comparator<T> comparator)
    {
        m_comparators.remove(comparator);
    }

    public int getComparatorCount()
    {
        return m_comparators.size();
    }

    public void clear()
    {
        m_comparators.clear();
    }
}
