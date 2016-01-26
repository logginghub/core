package com.logginghub.logging.frontend.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

import java.awt.*;

public class EnvironmentLevelStatsModel extends Observable {

    private ObservableInteger count = createIntProperty("count", 0);
    private ObservableInteger value = createIntProperty("value", 0);
    private ObservableProperty<Level> level = createProperty("level", Level.class, Level.Info);
    private ObservableProperty<Trend> trend = createProperty("trend", Trend.class, Trend.Same);
    private ObservableProperty<Color> foreground = createProperty("foreground", Color.class, Color.white);
    private ObservableProperty<Color> background = createProperty("background", Color.class, Color.green);

    public EnvironmentLevelStatsModel() {
        getValue().set(0);
        getLevel().set(Level.Info);
        getTrend().set(Trend.Same);
        getForeground().set(Color.white);
        getBackground().set(Color.green);
    }

    public void decrementCount() {
        count.increment(-1);
        getValue().set(count.get());
    }

    public ObservableInteger getCount() {
        return count;
    }

    public ObservableInteger getValue() {
        return value;
    }

    public ObservableProperty<Color> getBackground() {
        return background;
    }

    public ObservableProperty<Color> getForeground() {
        return foreground;
    }

    public ObservableProperty<Level> getLevel() {
        return level;
    }

    public ObservableProperty<Trend> getTrend() {
        return trend;
    }

    public void incrementCount() {
        incrementCount(1);
    }

    public void incrementCount(int amount) {
        count.increment(amount);
        value.set(count.get());
    }

    public enum Trend {
        Up,
        Down,
        Same;
    }

    public enum Level {
        Severe,
        Warning,
        Info
    }


}
