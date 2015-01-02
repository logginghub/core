package com.logginghub.utils;

public interface Destination<T> {
    void send(T t);
}
