package com.logginghub.logging.frontend.connectionmanager;

import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;

public interface ConnectionManagerListener {
    void onOpenEnvironment(EnvironmentConfiguration environmentConfiguration);
}
