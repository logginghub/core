package com.logginghub.logging.frontend.charting.model;

import java.util.ArrayList;

import com.logginghub.utils.observable.ObservableList;

public class StreamElementSelectionModel {

    private ObservableList<String> items = new ObservableList<String>(new ArrayList<String>());
    
    public ObservableList<String> getItems() {
        return items;
    }
    
}
