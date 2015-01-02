package com.logginghub.utils;

public interface IteratingRunnable {

    void beforeFirst();

    /**
     * 
     * @return the number of milliseconds the iteration took. If you return a
     *         greater than zero value, the iterating thread will take your
     *         value instead of the one it calculates. This allows you code to
     *         influence the rate at which the thread executes.
     */
//    long 
    void iterate();

    void afterLast();

}
