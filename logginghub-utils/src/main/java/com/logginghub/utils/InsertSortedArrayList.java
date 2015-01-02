package com.logginghub.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class InsertSortedArrayList<T> extends ArrayList<T> {

    private static final long serialVersionUID = 1L;
    private Comparator<T> comparator;

    public InsertSortedArrayList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public int addAndReturnIndex(T item) {

        // Its not, so do an insert search at the correct index
        int index = Collections.binarySearch(this, item, comparator);

        if (index < 0) {
            index = ~index;
        }

        super.add(index, item);
        return index;
    }
    
    public boolean add(T item) {
        addAndReturnIndex(item);
        return true;
    }

}
