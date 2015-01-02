package com.logginghub.utils;

public interface StreamingDestination<T> extends Destination<T>{
    void onStreamComplete();   
}
