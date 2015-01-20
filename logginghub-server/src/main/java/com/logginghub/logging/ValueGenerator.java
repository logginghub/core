package com.logginghub.logging;

import com.logginghub.logging.listeners.GeneratedValueListener;

public interface ValueGenerator<T>
{
    public T getValue();
    public void reset();
    
    public void addValueListener(GeneratedValueListener<T> listener);
    public void removeValueListener(GeneratedValueListener<T> listener);
    
    public void generateValue();
}
