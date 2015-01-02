package com.logginghub.utils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractBean
{
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener, String... propertyNames)
    {
        for (String propertyName : propertyNames) {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);    
        }
    }
    
    public void addPropertyChangeListener(Enum<?> propertyName, PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(propertyName.name(), listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }


    public void removePropertyChangeListener(Enum<?> propertyName, PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(propertyName.name(), listener);
    }

    
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
        
    protected void firePropertyChange(Enum<?> propertyName, Object oldValue, Object newValue)
    {
        propertyChangeSupport.firePropertyChange(propertyName.name(), oldValue, newValue);
    }
}