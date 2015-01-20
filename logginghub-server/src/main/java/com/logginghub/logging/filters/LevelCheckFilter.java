package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

/**
 * A Filter<LogEvent> that passes events based on a specific selection of permitted levels.
 * 
 * @author James
 */
public class LevelCheckFilter implements Filter<LogEvent> {

    private boolean severeAllowed = true;
    private boolean warningAllowed = true;
    private boolean infoAllowed = true;
    private boolean configAllowed = true;
    private boolean fineAllowed = true;
    private boolean finerAllowed = true;
    private boolean finestAllowed = true;

    public LevelCheckFilter() {}

    public boolean passes(LogEvent event) {
        int level = event.getLevel();

        boolean passes = true;

        switch (level) {
            case Logger.severe:
                passes = severeAllowed;
                break;
            case Logger.warning:
                passes = warningAllowed;
                break;
            case Logger.info:
                passes = infoAllowed;
                break;
            case Logger.config:
                passes = configAllowed;
                break;
            case Logger.fine:
                passes = fineAllowed;
                break;
            case Logger.finer:
                passes = finerAllowed;
                break;
            case Logger.finest:
                passes = finestAllowed;
                break;
        }

        return passes;
    }

    public boolean isSevereAllowed() {
        return severeAllowed;
    }

    public void setSevereAllowed(boolean severeAllowed) {
        this.severeAllowed = severeAllowed;
    }

    public boolean isWarningAllowed() {
        return warningAllowed;
    }

    public void setWarningAllowed(boolean warningAllowed) {
        this.warningAllowed = warningAllowed;
    }

    public boolean isInfoAllowed() {
        return infoAllowed;
    }

    public void setInfoAllowed(boolean infoAllowed) {
        this.infoAllowed = infoAllowed;
    }

    public boolean isConfigAllowed() {
        return configAllowed;
    }

    public void setConfigAllowed(boolean configAllowed) {
        this.configAllowed = configAllowed;
    }

    public boolean isFineAllowed() {
        return fineAllowed;
    }

    public void setFineAllowed(boolean fineAllowed) {
        this.fineAllowed = fineAllowed;
    }

    public boolean isFinerAllowed() {
        return finerAllowed;
    }

    public void setFinerAllowed(boolean finerAllowed) {
        this.finerAllowed = finerAllowed;
    }

    public boolean isFinestAllowed() {
        return finestAllowed;
    }

    public void setFinestAllowed(boolean finestAllowed) {
        this.finestAllowed = finestAllowed;
    }

    @Override public String toString() {
        return "LevelCheckFilter [severeAllowed=" +
               severeAllowed +
               ", warningAllowed=" +
               warningAllowed +
               ", infoAllowed=" +
               infoAllowed +
               ", configAllowed=" +
               configAllowed +
               ", fineAllowed=" +
               fineAllowed +
               ", finerAllowed=" +
               finerAllowed +
               ", finestAllowed=" +
               finestAllowed +
               "]";
    }

}
