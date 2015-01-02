package com.logginghub.utils.filter;

public interface Filter<T> {
    boolean passes(T t);
}
