package com.logginghub.logging.frontend.analysis;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.utils.ValueStripper2.ValueStripper2ResultListener;

public class RawGenerator implements ValueStripper2ResultListener, ResultGenerator {
    private List<ChunkedResultHandler> resultHandlers;

    public void onNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
        if (resultHandlers != null) {            
            ChunkedResult result = new ChunkedResult(entry.getOriginTime(), 0, Double.parseDouble(value), label);
            for (ChunkedResultHandler handler : resultHandlers) {
                handler.onNewChunkedResult(result);
            }
        }
    }

    public void addChunkedResultHandler(ChunkedResultHandler chunkedResultHandler) {
        if (resultHandlers == null) {
            resultHandlers = new ArrayList<ChunkedResultHandler>();
        }
        this.resultHandlers.add(chunkedResultHandler);
    }

    public void clear() {

    }

    @Override public void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isNumeric, LogEvent entry) {}
}
