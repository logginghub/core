package com.logginghub.logging;


public interface AppenderHelperEventConvertor {
    LogEvent createLogEvent();
    EventSnapshot createSnapshot();
}
