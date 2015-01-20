package com.logginghub.logging.adaptors.slf4j;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;

public class LoggerFactory
{
    private static ConcurrentHashMap<String, Logger> loggersByName = new ConcurrentHashMap<String, Logger>();

    public static Logger getLogger(String name)
    {
        Logger slf4jLogger = null;
        slf4jLogger = (Logger) loggersByName.get(name);
        if (slf4jLogger == null)
        {
            org.apache.log4j.Logger log4jLogger;
            if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME))
            {
                log4jLogger = LogManager.getRootLogger();
            }
            else
            {
                log4jLogger = LogManager.getLogger(name);
            }
            slf4jLogger = new Logger(log4jLogger);
            loggersByName.put(name, slf4jLogger);
        }

        return slf4jLogger;
    }

    public static Logger getLogger(Class<?> c)
    {
        return getLogger(c.getName());
    }
}
