package com.logginghub.logging.frontend.analysis;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChunkedResultMultiplexer implements ChunkedResultHandler {

    private List<ChunkedResultHandler> listeners = new CopyOnWriteArrayList<ChunkedResultHandler>();

    public void addChunkedResultHandler(ChunkedResultHandler chunkedResultHandler) {
        listeners.add(chunkedResultHandler);
    }

    public void removeChunkedResultHandler(ChunkedResultHandler chunkedResultHandler) {
        listeners.remove(chunkedResultHandler);
    }


    public void clear() {
        listeners.clear();
    }

    @Override public void onNewChunkedResult(ChunkedResult result) {
        for (ChunkedResultHandler chunkedResultHandler : listeners) {
            chunkedResultHandler.onNewChunkedResult(result);
        }
    }

    @Override public void complete() {
        for (ChunkedResultHandler chunkedResultHandler : listeners) {
            chunkedResultHandler.complete();
        }
    }

}
