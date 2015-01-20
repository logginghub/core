package com.logginghub.logging.hub.configuration;

public interface FileLogConfiguration {

    boolean getWriteAsynchronously();

    int getAsynchronousQueueDiscardSize();

    int getAsynchronousQueueWarningSize();

    String getFormatter();

    String getPattern();

    boolean getAutoNewline();
    
    boolean getForceFlush();
}
