package com.logginghub.logging.frontend.components;

import java.util.ArrayList;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

public class QuickFilterHistoryModel extends Observable {
    
    private ObservableList<QuickFilterHistoryEntryModel> entries = new ObservableList<QuickFilterHistoryEntryModel>(new ArrayList<QuickFilterHistoryEntryModel>());
    public ObservableList<QuickFilterHistoryEntryModel> getEntries() {
        return entries;
    }
}
