package com.logginghub.utils.observable;

public class ObservableWithID extends Observable {

    private int id = unset;
    public static final int unset = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
