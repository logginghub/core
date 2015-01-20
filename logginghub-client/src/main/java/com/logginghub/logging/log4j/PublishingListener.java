package com.logginghub.logging.log4j;

import com.logginghub.logging.LogEvent;

public interface PublishingListener
{
    public void onSuccessfullyPublished(LogEvent event);
    public void onUnsuccessfullyPublished(LogEvent event, Exception failureReason);
}
