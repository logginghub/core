package com.logginghub.logging.frontend.components;

import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.utils.observable.ObservableProperty;

public class QuickFilterHistoryController {

    private QuickFilterHistoryModel model;
    private final EnvironmentModel environmentModel;

    private ObservableProperty<QuickFilterHistoryEntryModel> selectedEntry = new ObservableProperty<QuickFilterHistoryEntryModel>(null);

    public QuickFilterHistoryController(QuickFilterHistoryModel model, EnvironmentModel environmentModel) {
        this.model = model;
        this.environmentModel = environmentModel;
    }

    public void clearAllFilters() {

    }

    public EnvironmentModel getEnvironmentModel() {
        return environmentModel;
    }

    public void addItem(QuickFilterHistoryEntryModel entry) {
        model.getEntries().add(entry);
    }
    
    public void selectItem(QuickFilterHistoryEntryModel entry) {
        entry.getCount().increment(1);
        entry.getLastUsed().set(System.currentTimeMillis());
        selectedEntry.set(entry);
    }
    
    public void deleteItem(QuickFilterHistoryEntryModel entry) {
        model.getEntries().remove(entry);
    }

    public void clearSelection() {
        selectedEntry.set(null);
    }
    
    public QuickFilterHistoryModel getModel() {
        return model;
    }
 
    public ObservableProperty<QuickFilterHistoryEntryModel> getSelectedEntry() {
        return selectedEntry;
    }
}
