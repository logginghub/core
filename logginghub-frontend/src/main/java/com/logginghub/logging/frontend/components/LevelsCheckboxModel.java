package com.logginghub.logging.frontend.components;

import java.util.logging.Level;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableProperty;

public class LevelsCheckboxModel extends Observable {

    private ObservableProperty<Boolean> severeVisible = createBooleanProperty("severeVisible", true);
    private ObservableProperty<Boolean> warningVisible = createBooleanProperty("warningVisible", true);
    private ObservableProperty<Boolean> infoVisible = createBooleanProperty("infoVisible", true);
    private ObservableProperty<Boolean> configVisible = createBooleanProperty("configVisible", true);
    private ObservableProperty<Boolean> fineVisible = createBooleanProperty("fineVisible", true);
    private ObservableProperty<Boolean> finerVisible = createBooleanProperty("finerVisible", true);
    private ObservableProperty<Boolean> finestVisible = createBooleanProperty("finestVisible", true);

    private ObservableProperty<Level> selectedLevel = createProperty("selectedLevel", Level.class, Level.INFO);

    public ObservableProperty<Boolean> getConfigVisible() {
        return configVisible;
    }

    public ObservableProperty<Boolean> getFinestVisible() {
        return finestVisible;
    }

    public ObservableProperty<Boolean> getFinerVisible() {
        return finerVisible;
    }

    public ObservableProperty<Boolean> getFineVisible() {
        return fineVisible;
    }

    public ObservableProperty<Boolean> getInfoVisible() {
        return infoVisible;
    }

    public ObservableProperty<Level> getSelectedLevel() {
        return selectedLevel;
    }

    public ObservableProperty<Boolean> getSevereVisible() {
        return severeVisible;
    }

    public ObservableProperty<Boolean> getWarningVisible() {
        return warningVisible;
    }

}
