package com.logginghub.utils;

public interface SearchResultsListener
{
    public void onNewResult(String source, int lineNumber, String line);
}
