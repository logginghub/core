package com.logginghub.logging;

import java.util.logging.Level;

/**
 * The main model that represents a single item of logging.
 * 
 * @author admin
 */
public interface LogEvent
{
    public int getLevel();

    public long getSequenceNumber();

    public String getSourceClassName();

    public String getSourceMethodName();

    public String getMessage();

    public String getThreadName();

    public long getOriginTime();
    public long getHubTime();

    public String getLoggerName();

    public String getSourceHost();
    public String getSourceAddress();

    public String getSourceApplication();

    public String getFormattedException();

    public String[] getFormattedObject();

    /**
     * Return something to indicate the flavour of this event: eg Juli, Log4j
     * etc
     * 
     * @return
     */
    public String getFlavour();

    public String getLevelDescription();

    public Level getJavaLevel();
    
    public int getPid();
    
    public String getChannel();
}
