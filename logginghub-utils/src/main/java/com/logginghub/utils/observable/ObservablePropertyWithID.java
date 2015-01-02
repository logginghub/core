package com.logginghub.utils.observable;


public class ObservablePropertyWithID<T> extends ObservableProperty<T> {

    private int id = unset;
    public static final int unset = -1;

    public ObservablePropertyWithID(T initialValue, Observable parent) {
        super(initialValue, parent);
    }

    public ObservablePropertyWithID(T initialValue) {
        super(initialValue);
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    

}
