package com.logginghub.logging.frontend.charting.model;

import com.logginghub.logging.LogEvent;

/**
 * Encapsulates the output from a pattern extraction - when you give a {@link LogEvent} to a
 * {@link ValueStripper2} this will be the result, if it passes the regex match.
 * 
 * @author James
 */

public class PatternisedDataModel {

    private final String[] variables;
    private LogEvent event;
    private String[] labels;

    public PatternisedDataModel(LogEvent event, String[] variables, String[] labels) {
        super();
        this.event = event;
        this.variables = variables;
        this.labels = labels;
    }

    public LogEvent getEvent() {
        return event;
    }

    public String[] getLabels() {
        return labels;
    }

    public String[] getVariables() {
        return variables;
    }

}
