package com.logginghub.logging.launchers;

import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.utils.logging.Logger;

public class RunTelemetryAgent {

    private static Logger logger = Logger.getLoggerFor(RunTelemetryAgent.class);

    public static void main(String[] args) {
        String configPath = "telemetryAgent.xml";
        if (args.length > 0) {
            configPath = args[0];
        }

        logger.info("Loading configuration from '{}'", configPath);
        LoggingContainer.createContainer(configPath);
    }

}
