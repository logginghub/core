package com.logginghub.logging.frontend.services;

import com.logginghub.logging.frontend.modules.EnvironmentNotificationListener;

public interface EnvironmentNotificationService {

    void addListener(EnvironmentNotificationListener environmentNotificationListener);
    void removeListener(EnvironmentNotificationListener environmentNotificationListener);
    boolean isEnvironmentConnectionEstablished();

}
