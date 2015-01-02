package com.logginghub.utils.logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggingHelper {

    public static String getJULLevelDescription(int level) {

        String description;

        switch (level) {
            case com.logginghub.utils.logging.Logger.finest:
                description = "FINEST";
                break;
            case com.logginghub.utils.logging.Logger.finer:
                description = "FINER";
                break;
            case com.logginghub.utils.logging.Logger.fine:
                description = "FINE";
                break;
            case com.logginghub.utils.logging.Logger.config:
                description = "CONFIG";
                break;
            case com.logginghub.utils.logging.Logger.info:
                description = "INFO";
                break;
            case com.logginghub.utils.logging.Logger.warning:
                description = "WARNING";
                break;
            case com.logginghub.utils.logging.Logger.severe:
                description = "SEVERE";
                break;
            default:
                description = Integer.toString(level);
        }

        return description;
    }

    public static void setupWarningLogging() {
        System.setProperty("jme.stats", "true");
        Logger.getLogger("").setLevel(Level.WARNING);
        // Logger.getLogger("com.logginghub").setLevel(Level.WARNING);
        // Logger.getLogger("com.jme").setLevel(Level.WARNING);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
        Logger.getLogger("").getHandlers()[0].setFormatter(new SingleLineFormatter());
    }

    public static void setupInformationLogging() {
        System.setProperty("jme.stats", "true");
        Logger.getLogger("").setLevel(Level.INFO);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
        Logger.getLogger("").getHandlers()[0].setFormatter(new SingleLineFormatter());
    }

    public static void setupAllLogging() {
        System.setProperty("jme.stats", "true");
        setLevel(Level.ALL, "com.logginghub", "");
        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
        Logger.getLogger("").getHandlers()[0].setFormatter(new SingleLineFormatter());
    }

    public static void setupAllLoggingWithFullClassnames() {
        System.setProperty("jme.stats", "true");

        Logger.getLogger("com.logginghub").setLevel(Level.ALL);

        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
        SingleLineFormatter singleLineFormatter = new SingleLineFormatter();
        singleLineFormatter.setStripClass(false);
        singleLineFormatter.setWidths(10, 10, 100);
        Logger.getLogger("").getHandlers()[0].setFormatter(singleLineFormatter);
    }

    public static void setupFineLoggingWithFullClassnames() {
        System.setProperty("jme.stats", "true");

        Logger.getLogger("com.logginghub").setLevel(Level.FINE);

        Logger.getLogger("").getHandlers()[0].setLevel(Level.FINE);
        SingleLineFormatter singleLineFormatter = new SingleLineFormatter();
        singleLineFormatter.setStripClass(false);
        singleLineFormatter.setWidths(10, 10, 100);
        Logger.getLogger("").getHandlers()[0].setFormatter(singleLineFormatter);
    }

    public static void setupDebugLogging() {
        System.setProperty("jme.stats", "true");
        Logger.getLogger("valentino").setLevel(Level.FINER);
        Logger.getLogger("com.logginghub").setLevel(Level.FINER);
        Logger.getLogger("com.jme").setLevel(Level.WARNING);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
        Logger.getLogger("").getHandlers()[0].setFormatter(new SingleLineFormatter());
    }

    public static void setupFineLogging() {
        System.setProperty("jme.stats", "true");
        setLevel(Level.FINE, "com.logginghub", "");
        Logger.getLogger("").getHandlers()[0].setLevel(Level.FINE);
        Logger.getLogger("").getHandlers()[0].setFormatter(new SingleLineFormatter());
    }

    public static void setupInformationLoggingWithFullClassNames() {
        System.setProperty("jme.stats", "true");
        Logger.getLogger("").setLevel(Level.INFO);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);

        SingleLineFormatter singleLineFormatter = new SingleLineFormatter();
        singleLineFormatter.setStripClass(false);
        singleLineFormatter.setWidths(10, 10, 100);
        Logger.getLogger("").getHandlers()[0].setFormatter(singleLineFormatter);
    }

    private static List<Logger> m_savedLoggers = new ArrayList<Logger>();

    public static void setLevel(Level level, String... loggers) {
        for (String loggerName : loggers) {
            Logger logger = Logger.getLogger(loggerName);
            logger.setLevel(level);

            // We have to keep a reference to the loggers as logger manager uses
            // weak references to bin old ones, handily loosing the levels
            // you've set on them
            m_savedLoggers.add(logger);
        }
    }

    public static void dumpLoggingSetup() {
        System.out.println("--------------------------- Logging setup dump ----------------------------");
        LogManager logManager = LogManager.getLogManager();
        Enumeration<String> loggerNames = logManager.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            String name = loggerNames.nextElement();
            System.out.println("Logger name : " + name);

            Logger logger = logManager.getLogger(name);
            if (logger != null) {
                Level level = logger.getLevel();
                if (level != null) {
                    System.out.println("level       : " + level.toString());
                }

                Handler[] handlers = logger.getHandlers();
                for (Handler handler : handlers) {
                    System.out.println("handler     : " + handler.getClass().getSimpleName());
                    Level handlerLevel = handler.getLevel();
                    if (handlerLevel != null) {
                        System.out.println("handler level: " + handlerLevel.toString());
                    }
                }
            }
            else {
                System.out.println("<Logger was null>");
            }
        }
        System.out.println("---------------------------------------------------------------------------");
        System.out.println();
    }

    public static LoggingSnapshot takeSnapshot() {
        LoggingSnapshot snapshot = new LoggingSnapshot();
        snapshot.capture();
        return snapshot;
    }
}
