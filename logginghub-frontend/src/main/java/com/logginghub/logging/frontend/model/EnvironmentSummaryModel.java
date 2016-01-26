package com.logginghub.logging.frontend.model;

import com.logginghub.logging.LevelConstants;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.model.EnvironmentLevelStatsModel.Level;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.logging.Logger;

public class EnvironmentSummaryModel implements LogEventListener {

    private static final Logger logger = Logger.getLoggerFor(EnvironmentSummaryModel.class);
    private EnvironmentLevelStatsModel severeLevelStatsModel = new EnvironmentLevelStatsModel();
    private EnvironmentLevelStatsModel warningLevelStatsModel = new EnvironmentLevelStatsModel();
    private EnvironmentLevelStatsModel infoLevelStatsModel = new EnvironmentLevelStatsModel();

    private EnvironmentLevelStatsModel severeLevelStatsPerSecondModel = new EnvironmentLevelStatsModel();
    private EnvironmentLevelStatsModel warningLevelStatsPerSecondModel = new EnvironmentLevelStatsModel();
    private EnvironmentLevelStatsModel infoLevelStatsPerSecondModel = new EnvironmentLevelStatsModel();

    private volatile int severeSinceLastUpdate = 0;
    private volatile int warningSinceLastUpdate = 0;
    private volatile int infoSinceLastUpdate = 0;

    private volatile int severeRunningTotal = 0;
    private volatile int warningRunningTotal = 0;
    private volatile int infoRunningTotal = 0;

    public EnvironmentSummaryModel() {
        severeLevelStatsModel.getLevel().set(Level.Severe);
        warningLevelStatsModel.getLevel().set(Level.Warning);
        infoLevelStatsModel.getLevel().set(Level.Info);

        severeLevelStatsPerSecondModel.getLevel().set(Level.Severe);
        warningLevelStatsPerSecondModel.getLevel().set(Level.Warning);
        infoLevelStatsPerSecondModel.getLevel().set(Level.Info);
    }

    public void onEventRemoved(LogEvent event) {
        switch (event.getLevel()) {
            case LevelConstants.SEVERE:
                severeRunningTotal--;
                break;
            case LevelConstants.WARNING:
                warningRunningTotal--;
                break;
            case LevelConstants.INFO:
                infoRunningTotal--;
                break;
        }
        logger.trace("Event removed, updating summary stats for event level {} - counts are now sev {} warn {} info {}", event.getLevelDescription(), severeRunningTotal, warningRunningTotal, infoRunningTotal);
    }

    @Override public void onNewLogEvent(LogEvent event) {
        switch (event.getLevel()) {
            case LevelConstants.SEVERE:
                severeRunningTotal++;
                severeSinceLastUpdate++;
                break;
            case LevelConstants.WARNING:
                warningRunningTotal++;
                warningSinceLastUpdate++;
                break;
            case LevelConstants.INFO:
                infoRunningTotal++;
                infoSinceLastUpdate++;
                break;
        }
        logger.trace("New event received, updating summary stats for event level {} - counts are now sev {} warn {} info {}", event.getLevelDescription(), severeRunningTotal, warningRunningTotal, infoRunningTotal);
    }

    public void updateEachSecond() {

        logger.trace("Updating environment summary model values on timer call");
        severeLevelStatsModel.getValue().set(severeRunningTotal);
        warningLevelStatsModel.getValue().set(warningRunningTotal);
        infoLevelStatsModel.getValue().set(infoRunningTotal);

        severeLevelStatsPerSecondModel.getValue().set(severeSinceLastUpdate);
        warningLevelStatsPerSecondModel.getValue().set(warningSinceLastUpdate);
        infoLevelStatsPerSecondModel.getValue().set(infoSinceLastUpdate);

        severeSinceLastUpdate = 0;
        warningSinceLastUpdate = 0;
        infoSinceLastUpdate = 0;
    }

    public EnvironmentLevelStatsModel getInfoLevelStatsPerSecondModel() {
        return infoLevelStatsPerSecondModel;
    }

    public EnvironmentLevelStatsModel getSevereLevelStatsPerSecondModel() {
        return severeLevelStatsPerSecondModel;
    }

    public EnvironmentLevelStatsModel getWarningLevelStatsPerSecondModel() {
        return warningLevelStatsPerSecondModel;
    }

    public EnvironmentLevelStatsModel getInfoLevelStatsModel() {
        return infoLevelStatsModel;
    }

    public EnvironmentLevelStatsModel getSevereLevelStatsModel() {
        return severeLevelStatsModel;
    }

    public EnvironmentLevelStatsModel getWarningLevelStatsModel() {
        return warningLevelStatsModel;
    }

}
