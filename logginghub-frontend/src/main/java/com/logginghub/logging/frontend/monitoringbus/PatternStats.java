package com.logginghub.logging.frontend.monitoringbus;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.HTMLBuilder;
import com.logginghub.utils.StreamListener;

public class PatternStats implements StreamListener<PatternisedLogEvent>, WebRenderable {

    private int labelIndex = 0;
    private int patternID;
    private MinuteAggregator aggregator = new MinuteAggregator();

    public PatternStats(int patternID) {
        this.patternID = patternID;
    }

    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }

    public int getLabelIndex() {
        return labelIndex;
    }

    public int getPatternID() {
        return patternID;
    }

    @Override public void onNewItem(PatternisedLogEvent t) {
        // System.out.println(t.getPatternName());
        if (t.getPatternID() == patternID) {
            aggregator.add(Double.parseDouble(t.getVariables()[labelIndex]));
        }
    }

    public MinuteAggregator getAggregator() {
        return aggregator;
    }

    @Override public void render(HTMLBuilder builder) {
        // TODO : this needs access to a service to resolve the current pattern name!
        builder.append("<h1>" + patternID + "</h1>");
        aggregator.render(builder);
    }

}
