package com.logginghub.logging.frontend.model;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Encapsulates the configuration and model attributes needed to name log levels.
 */
public class LevelNamesModel {
    private Map<Integer, String> levelNames = new HashMap<Integer, String>();

    public LevelNamesModel() {
        // Add the default level name mappings based on j.u.l. values
        levelNames.put(Level.SEVERE.intValue(), Level.SEVERE.getName());
        levelNames.put(Level.WARNING.intValue(), Level.WARNING.getName());
        levelNames.put(Level.INFO.intValue(), Level.INFO.getName());
        levelNames.put(Level.CONFIG.intValue(), Level.SEVERE.getName());
        levelNames.put(Level.FINE.intValue(), Level.FINE.getName());
        levelNames.put(Level.FINER.intValue(), Level.FINER.getName());
        levelNames.put(Level.FINEST.intValue(), Level.FINEST.getName());
        levelNames.put(Level.ALL.intValue(), Level.ALL.getName());
    }

    public String getLevelName(int levelIntegerValue) {
        return levelNames.get(levelIntegerValue);
    }

    public void useLog4jNames() {

        levelNames.clear();

        levelNames.put(Level.SEVERE.intValue(), "severe");
        levelNames.put(Level.WARNING.intValue(), "warning");
        levelNames.put(Level.INFO.intValue(), "info");
        levelNames.put(Level.CONFIG.intValue(), "config");
        levelNames.put(Level.FINE.intValue(), "debug");
        levelNames.put(Level.FINER.intValue(), "trace");
        levelNames.put(Level.FINEST.intValue(), "trace2");
        levelNames.put(Level.ALL.intValue(), "all");

    }

    public Map<Integer, String> getLevelNames() {
        return levelNames;
    }
}
