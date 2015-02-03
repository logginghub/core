package com.logginghub.logging.frontend.binary;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.views.logeventdetail.time.TimeController;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.utils.BlockIndex;
import com.logginghub.logging.utils.LogEventBlockElement;
import com.logginghub.utils.StreamListener;

public class ImportController implements LogEventListener, StreamListener<LogEvent> {

    private TimeController timeFilterController;
    private BlockIndex blockIndex = new BlockIndex();

    public ImportController(TimeController timeFilterController) {
        this.timeFilterController = timeFilterController;
    }

    @Override public void onNewLogEvent(LogEvent event) {
        timeFilterController.addEvent(event);
    }

    @Override public void onNewItem(LogEvent t) {
        timeFilterController.addEvent(t);
    }

    public void addBlock(LogEventBlockElement t) {
        blockIndex.addBlock(t);
    }

    public void loadData(long start, long end, StreamListener<LogEvent> events) {
        blockIndex.loadData(start, end, events);
    }

}
