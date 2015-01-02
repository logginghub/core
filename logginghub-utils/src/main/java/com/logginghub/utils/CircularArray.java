package com.logginghub.utils;

import java.lang.reflect.Array;

/**
 * Very simple circular array.
 * 
 * @author James
 * 
 * @param <T>
 */
public class CircularArray<T> {

    private T[] data;

    private int currentPointer;
    private int startPointer;

    private int capacity;
    private int currentSize;

    private boolean wrapped;

    @SuppressWarnings("unchecked") public CircularArray(Class<T> c, int capacity) {
        this.capacity = capacity;
        data = (T[]) Array.newInstance(c, capacity);
        currentPointer = 0;
        startPointer = 0;
    }

    public T[] getData() {
        return data;
    }

    public int getCurrentPointer() {
        return currentPointer;
    }

    public int getStartPointer() {
        return startPointer;
    }
    
    public int getCapacity() {
        return capacity;
    }

    public synchronized T append(T value) {

        T old = data[currentPointer];
        data[currentPointer] = value;
        currentPointer++;


        if (!wrapped) {
            currentSize++;
        }
        else {
            startPointer++;

            if (startPointer == capacity) {
                startPointer = 0;
            }
        }
        
        if (currentPointer == capacity) {
            currentPointer = 0;
            
            // That's it, now we are in a different mode where we are always wrapped around
            wrapped = true;        
        }
        return old;
    }

    public int size() {
        return currentSize;
    }

    public T get(int index) {
        int modifiedIndex = index + startPointer;
        int actualIndex = wrapIndex(modifiedIndex);
        return data[actualIndex];
    }

    private int wrapIndex(int i) {
        int m = i % capacity;
        if (m < 0) { // java modulus can be negative
            m += capacity;
        }
        return m;
    }

    public void extract(T[] target) {
        System.arraycopy(data, 0, target, 0, currentSize);
    }
}
