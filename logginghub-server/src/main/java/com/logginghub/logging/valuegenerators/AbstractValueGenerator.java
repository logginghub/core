package com.logginghub.logging.valuegenerators;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.ValueGenerator;
import com.logginghub.logging.listeners.GeneratedValueListener;

public abstract class AbstractValueGenerator<T> implements ValueGenerator<T>
{
    private List<GeneratedValueListener<T>> m_listeners = new CopyOnWriteArrayList<GeneratedValueListener<T>>();

    public void addValueListener(GeneratedValueListener<T> listener)
    {
        m_listeners.add(listener);
    }

    public void removeValueListener(GeneratedValueListener<T> listener)
    {
        m_listeners.remove(listener);
    }
    
    public void generateValue()
    {
        fireNewValue(getValue());
        reset();
    }
    
    protected void fireNewValue(T value)
    {
        for(GeneratedValueListener<T> listener :  m_listeners)
        {
            listener.onNewValue(value);
        }
    }
}
