package com.logginghub.logging.frontend.analysis;


public interface ChunkedResultHandler
{
    void onNewChunkedResult(ChunkedResult result);
    void complete();
}
