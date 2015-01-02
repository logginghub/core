package com.logginghub.utils.filter;

import java.util.HashSet;
import java.util.Set;

public class SetFilter<T> implements Filter<T> {

    private Set<T> set = new HashSet<T>();
    private boolean isWhitelist = true;

    public SetFilter() {}
    
    public SetFilter(T... items) {
        add(items);
    }

    public SetFilter(Set<T> items) {
        set.addAll(items);
    }

    public void add(T... items) {
        for (T t : items) {
            set.add(t);
        }
    }

    public boolean isWhitelist() {
        return isWhitelist;
    }

    public void setWhitelist(boolean isWhitelist) {
        this.isWhitelist = isWhitelist;
    }

    public void remove(T... items) {
        for (T t : items) {
            set.remove(t);
        }
    }

    public boolean passes(T t) {
        boolean inSet = set.contains(t);
        boolean passes;

        if (isWhitelist) {
            passes = inSet;
        }
        else {
            passes = !inSet;
        }

        return passes;
    }

}
