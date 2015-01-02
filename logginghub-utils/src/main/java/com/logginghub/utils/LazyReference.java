package com.logginghub.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class LazyReference<T> {

    private Lock lock = new ReentrantLock();
    private T reference;
    
    public T get() {
        lock.lock();
        try {
            if(reference == null) {
                reference = instantiate();
            }            
        }finally {
            lock.unlock();
        }
        
        return reference;
    }

    protected abstract T instantiate();
    
}
