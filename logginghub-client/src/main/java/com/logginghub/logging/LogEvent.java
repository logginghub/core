package com.logginghub.logging;

import java.util.Map;
import java.util.logging.Level;

/**
 * The main model that represents a single item of logging.
 *
 * @author admin
 */
public interface LogEvent {
    String getChannel();

    /**
     * Return something to indicate the flavour of this event: eg Juli, Log4j etc
     *
     * @return
     */
    String getFlavour();

    String getFormattedException();

    String[] getFormattedObject();

    long getHubTime();

    Level getJavaLevel();

    int getLevel();

    String getLevelDescription();

    String getLoggerName();

    String getMessage();

    Map<String, String> getMetadata();

    long getOriginTime();

    int getPid();

    long getSequenceNumber();

    String getSourceAddress();

    String getSourceApplication();

    String getSourceClassName();

    String getSourceHost();

    String getSourceMethodName();

    String getThreadName();
}
