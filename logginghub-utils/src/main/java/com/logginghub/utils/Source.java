package com.logginghub.utils;

public interface Source<T> {
    void addDestination(Destination<T> listener);
    void removeDestination(Destination<T> listener);
}
