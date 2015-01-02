package com.logginghub.utils.observable;

import com.logginghub.utils.Xml.XmlEntry;

public class ObservableDouble extends AbstractObservableProperty<Double> {

    private double value;

    public ObservableDouble(double initialValue, Observable parent) {
        super(parent);
        set(value);
    }

    public ObservableDouble copy() {
        return new ObservableDouble(value);
    }
    
    public ObservableDouble(double initialValue) {
        set(initialValue);
    }

    public void set(double value) {
        double old = this.value;
        this.value = value;
        fireChanged(value, old);
        notifyParent();
    }

    @Override public Double get() {
        return value;
    }

    public double doubleValue() {
        return value;
    }

    public void increment(double amount) {
        set(value + amount);
    }

    @Override public String asString() {
        return Double.toString(value);
    }

    @Override public void fromXml(XmlEntry xml) {
        set(xml.getAttributes().getDouble(getName()));
    }

    @Override public AbstractObservableProperty<?> duplicate() {
        ObservableDouble duplicate = new ObservableDouble(value);        
        return duplicate;
    }
}
