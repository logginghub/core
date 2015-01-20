package com.logginghub.logging.frontend.charting.model;

import java.util.ArrayList;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

public class ChartingModel extends Observable {

    private ObservableList<PageModel> pages = new ObservableList<PageModel>(PageModel.class, new ArrayList<PageModel>());
    private ObservableList<TimeChunkerModel> timeChunkers = new ObservableList<TimeChunkerModel>(TimeChunkerModel.class, new ArrayList<TimeChunkerModel>());
    
    public ObservableList<PageModel> getPages() {
        return pages;
    }
    
    public ObservableList<TimeChunkerModel> getTimeChunkers() {
        return timeChunkers;
    }
}
