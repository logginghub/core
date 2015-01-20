package com.logginghub.logging.frontend.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableField<T> {

    private List<ObservableFieldListener<T>> listeners = new CopyOnWriteArrayList<ObservableFieldListener<T>>();
    private T value;

    public ObservableField(T initialValue) {
        value = initialValue;
    }

    public void addListener(ObservableFieldListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ObservableFieldListener<T> listener) {
        listeners.remove(listener);
    }

    public T get(){
        return value;
    }
    
    public void setIfGreater(T comparison) {

        if (value instanceof Number) {
            Number thisNumber = (Number) value;
            Number otherNumber = (Number) comparison;

            if (otherNumber.doubleValue() > thisNumber.doubleValue()) {
                set(comparison);
            }
        }else{
            throw new RuntimeException(String.format("This observable value isn't numeric (its a %s), so we can't do a greater than comparison!",value.getClass().getName()));
        }
    }

    public void set(T value) {
        T old = this.value;
        this.value = value;
        fireChanged(old, this.value);
    }

    private void fireChanged(T oldValue, T newValue) {
        for (ObservableFieldListener<T> observableFieldListener : listeners) {
            observableFieldListener.onChanged(oldValue, newValue);
        }
    }
}
