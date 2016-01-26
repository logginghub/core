package com.logginghub.utils.observable;

import com.logginghub.utils.Xml.XmlEntry;

public class ObservableInteger extends AbstractObservableProperty<Integer> {

    private int value;

    public ObservableInteger(int initialValue, Observable parent) {
        super(parent);
        set(initialValue);
    }

    public ObservableInteger copy() {
        return new ObservableInteger(value);
    }

    public ObservableInteger(int initialValue) {
        set(initialValue);
    }

    public void set(int value) {
        int old = this.value;
        this.value = value;
        fireChanged(value, old);
        notifyParent();
    }

    @Override public Integer get() {
        return value;
    }

    public int intValue() {
        return value;
    }

    public void increment(int amount) {
        set(value + amount);
    }

    @Override public String asString() {
        return Integer.toString(value);
    }

    @Override public void fromXml(XmlEntry xml) {
        set(xml.getAttributes().getInt(getName()));
    }

    public void increment() {
        increment(1);
    }

    @Override public AbstractObservableProperty<?> duplicate() {
        ObservableInteger duplicate = new ObservableInteger(value);        
        return duplicate;
    }

    public synchronized void setIfGreater(int newValue) {
        if(newValue > value) {
            value = newValue;
        }
    }
}
