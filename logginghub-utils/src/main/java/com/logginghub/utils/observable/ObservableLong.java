package com.logginghub.utils.observable;

import com.logginghub.utils.Xml.XmlEntry;

public class ObservableLong extends AbstractObservableProperty<Long> {

    private long value;

    public ObservableLong(long initialValue, Observable parent) {
        super(parent);
        set(value);
    }

    public ObservableLong(long initialValue) {
        set(initialValue);
    }

    public ObservableLong copy(){ 
        return new ObservableLong(value);
    }
    
    public void set(long value) {
        long old = this.value;
        this.value = value;
        fireChanged(value, old);
    }

    @Override public Long get() {
        return value;
    }

    public long longValue() {
        return value;
    }

    public void increment(long amount) {
        set(value + amount);
    }
    
    @Override public String asString() {
        return Long.toString(value);
    }

    
    @Override public void fromXml(XmlEntry xml) {
        set(xml.getAttributes().getLong(getName()));
    }
    
    @Override public AbstractObservableProperty<?> duplicate() {
        ObservableLong duplicate = new ObservableLong(value);        
        return duplicate;
    }
}
