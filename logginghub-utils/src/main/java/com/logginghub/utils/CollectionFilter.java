package com.logginghub.utils;

public interface CollectionFilter<T> {
    boolean passes(T t);
}
