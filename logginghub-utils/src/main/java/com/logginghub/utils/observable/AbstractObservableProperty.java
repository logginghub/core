package com.logginghub.utils.observable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.Xml.XmlEntry;

public abstract class AbstractObservableProperty<T> implements ObservableItem {

    protected ObservableItemContainer parent;
    private List<ObservablePropertyListener<T>> listeners;
    private String name;
    private Class<?> type;

    public AbstractObservableProperty() {}

    public AbstractObservableProperty(ObservableItemContainer parent) {
        setParent(parent);
    }
    

    public abstract AbstractObservableProperty<?> duplicate();
    
    public void setParent(ObservableItemContainer parent) {
        if (this.parent != null) {
            this.parent.onChildRemoved(this);
        }
        this.parent = parent;

        parent.onChildAdded(this);
    }
    
    protected void notifyParent() {
        if(parent != null) {
            parent.onChildChanged(this);
        }
    }

    public ObservableItemContainer getParent() {
        return parent;
    }
    
    public abstract T get();

    public void addListenerAndNotifyCurrent(ObservablePropertyListener<T> listener) {
        addListener(listener);
        listener.onPropertyChanged(get(), get());
    }

    public synchronized void addListener(ObservablePropertyListener listener) {
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<ObservablePropertyListener<T>>();
        }
        listeners.add(listener);
    }

    public synchronized void removeListener(ObservablePropertyListener<T> listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    @Override public String toString() {
        T value = get();
        if(value == null) {
            return "null";
        }else{
            return value.toString();
        }
//        return "'" + value + "'" + " <" + (value == null ? "null" : value.getClass().getName()) + ">";
    }

    protected void fireChanged(T t, T old) {
        if (listeners != null) {
            for (ObservablePropertyListener<T> observableListener : listeners) {
                observableListener.onPropertyChanged(old, t);
            }
        }
    }
    
    public void touch() {
        fireChanged(get(), get());
    }

    public abstract String asString();
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public abstract void fromXml(XmlEntry xml);
}
