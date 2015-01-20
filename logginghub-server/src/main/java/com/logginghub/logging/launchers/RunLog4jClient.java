package com.logginghub.logging.launchers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.ThreadUtils;

public class RunLog4jClient {

    static {
        System.setProperty("log4j.configuration", "configs/log4j/log4j.telemetry.xml");
        System.setProperty("log4j.debug", "true");

        if (System.getProperty("sourceApplication") == null) {
            System.setProperty("sourceApplication", "RunLog4jClient");
        }
    }

    private static final Logger logger = Logger.getLogger(RunLog4jClient.class);

    public static void main(String[] args) {

        while (true) {
            String[] readLines = ResourceUtils.readLines("testevents/twelfthnight.txt");
            for (String string : readLines) {

                if (string.trim().length() > 0) {
                    Level level;

                    String lowerCase = string.toLowerCase();
                    if (lowerCase.contains("toby")) {
                        level = Level.FATAL;
                    }
                    else if (lowerCase.contains("malvolio")) {
                        level = Level.WARN;
                    }
                    else if (lowerCase.contains("viola")) {
                        level = Level.DEBUG;
                    }
                    else if (lowerCase.contains("olivia")) {
                        level = Level.TRACE;
                    }
                    else {
                        level = Level.INFO;
                    }

                    logger.log(level, string);
                }
                ThreadUtils.sleep(500);
            }

        }

    }

}
