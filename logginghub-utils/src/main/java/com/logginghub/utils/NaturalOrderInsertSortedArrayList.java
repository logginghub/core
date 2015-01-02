package com.logginghub.utils;

import java.util.ArrayList;
import java.util.Collections;

public class NaturalOrderInsertSortedArrayList<T extends Comparable<T>> extends ArrayList<T> {

    private static final long serialVersionUID = 1L;

    public NaturalOrderInsertSortedArrayList() {
    }

    public int addAndReturnIndex(T item) {
        int index = Collections.binarySearch(this, item);

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

    public boolean removeFast(T item) {
        
        int index = Collections.binarySearch(this, item);

        boolean removed;
        if (index < 0) {
            // This didn't exist
            removed = false;
        }else{
            remove(index);
            removed = true;
        }
        
        return removed;
    }
    
}
