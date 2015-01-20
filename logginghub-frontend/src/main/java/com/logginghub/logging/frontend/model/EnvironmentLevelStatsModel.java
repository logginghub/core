package com.logginghub.logging.frontend.model;

import java.awt.Color;

import com.logginghub.logging.LogEvent;

public class EnvironmentLevelStatsModel extends ObservableModel {

    private int count;

    public enum Fields implements FieldEnumeration {
        Value,
        Level,
        Trend,
        Foreground,
        Background;
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

    public EnvironmentLevelStatsModel() {
        set(Fields.Value, 0);
        set(Fields.Level, "No level");
        set(Fields.Trend, Trend.Same);
        set(Fields.Foreground, Color.white);
        set(Fields.Background, Color.green);
    }
    
    public void setValue(int value) {
        set(Fields.Value, value);
    }

    public void setLevel(Level level) {
        set(Fields.Level, level);
    }

    public void setTrend(Trend trend) {
        set(Fields.Trend, trend);
    }

    public int getValue() {
        return getInt(Fields.Value);
    }

    public Level getLevel() {
        return get(Fields.Level);
    }

    public Trend getTrend() {
        return getEnum(Fields.Trend);
    }

    
    public void incrementCount(int amount) {
        count+=amount;
        setValue(count);
    }
    public void incrementCount() {
        incrementCount(1);
    }
    
    public void decrementCount() {
        count--;
        setValue(count);
    }


}
