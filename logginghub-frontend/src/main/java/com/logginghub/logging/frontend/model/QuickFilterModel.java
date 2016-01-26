package com.logginghub.logging.frontend.model;

import com.logginghub.logging.frontend.components.LevelsCheckboxModel;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

public class QuickFilterModel extends Observable {

    private ObservableProperty<LevelsCheckboxModel> levelFilter = new ObservableProperty<LevelsCheckboxModel>(new LevelsCheckboxModel());
    private ObservableProperty<Boolean> isEnabled = new ObservableProperty<Boolean>(true);    
    private ObservableProperty<Boolean> isRegex = new ObservableProperty<Boolean>(false);
    private ObservableProperty<String> filterText = new ObservableProperty<String>("");

    private ObservableList<CustomQuickFilterModel> customFilters = createListProperty("customFilters", CustomQuickFilterModel.class);
    private ObservableList<CustomDateFilterModel> customDateFilters = createListProperty("customDateFilters", CustomDateFilterModel.class);

    public ObservableList<CustomDateFilterModel> getCustomDateFilters() {
        return customDateFilters;
    }

    public ObservableList<CustomQuickFilterModel> getCustomFilters() {
        return customFilters;
    }

    public ObservableProperty<String> getFilterText() {
        return filterText;
    }
    
    public ObservableProperty<Boolean> getIsEnabled() {
        return isEnabled;
    }
    
    public ObservableProperty<Boolean> getIsRegex() {
        return isRegex;
    }
    
    public ObservableProperty<LevelsCheckboxModel> getLevelFilter() {
        return levelFilter;
    }
    
}
