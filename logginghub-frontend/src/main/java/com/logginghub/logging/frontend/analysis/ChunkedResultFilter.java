package com.logginghub.logging.frontend.analysis;


public interface ChunkedResultFilter
{
    boolean passes(ChunkedResult result);
}
