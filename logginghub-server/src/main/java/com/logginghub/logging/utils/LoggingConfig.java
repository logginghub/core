package com.logginghub.logging.utils;

import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.SingleLineTextFormatter;
import com.logginghub.logging.handlers.DirectHandler;
import com.logginghub.logging.handlers.SocketHandler;
import com.logginghub.logging.servers.Hub;

public class LoggingConfig {
    public static void setupDirectToHubLogging(Hub hub) {
        removeAllHandlers();

        DirectHandler directToHubHandler = new DirectHandler(hub);
        directToHubHandler.setSourceApplication("test");
        directToHubHandler.setResuseLogEvents(false);

        Logger root = Logger.getLogger("");
        root.addHandler(directToHubHandler);
        root.setLevel(Level.ALL);

        // Stop the loggers logging to themselves
        ConsoleHandler localConsoleHandler = new ConsoleHandler();
        localConsoleHandler.setFormatter(new SingleLineTextFormatter());
        localConsoleHandler.setLevel(Level.ALL);

        Logger loggingHandlers = Logger.getLogger("com.logginghub.logging");
        loggingHandlers.setUseParentHandlers(false);
        loggingHandlers.addHandler(localConsoleHandler);
        loggingHandlers.setLevel(Level.ALL);
    }

    public static void setupRemoteLogging(String applicationName) {
        removeAllHandlers();

        SocketHandler socketHandler = new SocketHandler();
        socketHandler.setSourceApplication(applicationName);
        socketHandler.addConnectionPoint(new InetSocketAddress("localhost", LoggingPorts.getSocketHubDefaultPort()));
        socketHandler.setLevel(Level.ALL);

        Logger root = Logger.getLogger("");
        root.addHandler(socketHandler);
        root.setLevel(Level.ALL);

        // Stop the loggers logging to themselves
        ConsoleHandler localConsoleHandler = new ConsoleHandler();
        localConsoleHandler.setFormatter(new SingleLineTextFormatter());
        localConsoleHandler.setLevel(Level.ALL);

        Logger loggingHandlers = Logger.getLogger("com.logginghub.logging");
        loggingHandlers.setUseParentHandlers(false);
        loggingHandlers.addHandler(localConsoleHandler);
        loggingHandlers.setLevel(Level.ALL);
    }

    public static void setupConsoleLogging(Level level) {
        removeAllHandlers();

        Logger root = Logger.getLogger("");

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SingleLineTextFormatter());
        handler.setLevel(level);

        root.addHandler(handler);
        root.setLevel(level);
    }

    public static void setupConsoleLogging() {
        setupConsoleLogging(Level.ALL);
    }

    public static void removeAllHandlers() {
        LogManager logManager = LogManager.getLogManager();

        Enumeration<String> loggerNames = logManager.getLoggerNames();

        while (loggerNames.hasMoreElements()) {
            String loggerName = loggerNames.nextElement();
            Logger logger = logManager.getLogger(loggerName);

            if (logger != null) {
                Handler[] handlers = logger.getHandlers();
                for (Handler handler : handlers) {
                    logger.removeHandler(handler);
                }
            }
        }
    }

}
