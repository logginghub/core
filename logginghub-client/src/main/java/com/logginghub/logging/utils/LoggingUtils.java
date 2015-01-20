package com.logginghub.logging.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.VLLogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.handlers.SocketHandler;
import com.logginghub.logging.internallogging.LoggingHubStream;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.telemetry.SigarHelper;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.SizeOf;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.logging.LoggerStream;
import com.logginghub.utils.logging.SingleLineFormatter;

public class LoggingUtils {
    
    public static String getLog4jLevelDescription(LogEvent event) {
        return getLog4jLevelDescription(event.getLevel());
    }
    
    public static String getLog4jLevelDescription(int level) {
        String description;

        switch (level) {
            case com.logginghub.utils.logging.Logger.finest:
                description = "TRACE";
                break;
            case com.logginghub.utils.logging.Logger.finer:
                description = "TRACE";
                break;
            case com.logginghub.utils.logging.Logger.fine:
                description = "DEBUG";
                break;
            case com.logginghub.utils.logging.Logger.config:
                description = "INFO";
                break;
            case com.logginghub.utils.logging.Logger.info:
                description = "INFO";
                break;
            case com.logginghub.utils.logging.Logger.warning:
                description = "WARN";
                break;
            case com.logginghub.utils.logging.Logger.severe:
                description = "ERROR";
                break;
            default:
                description = Integer.toString(level);
        }

        return description;
    }
    
    public static long sizeof(LogEvent event) {

        long size = SizeOf.instanceSize;
        size += SizeOf.intSize + SizeOf.longSize + SizeOf.longSize;

        String sourceClassName = event.getSourceClassName();
        String sourceMethodName = event.getSourceMethodName();
        String message = event.getMessage();
        String threadName = event.getThreadName();
        String loggerName = event.getLoggerName();
        String sourceHost = event.getSourceHost();
        String sourceAddress = event.getSourceAddress();
        String sourceApplication = event.getSourceApplication();
        String formattedException = event.getFormattedException();
        String[] formattedObject = event.getFormattedObject();

        long sizeSourceClassName = SizeOf.sizeOf(sourceClassName);
        long sizeSourceMethodName = SizeOf.sizeOf(sourceMethodName);
        long sizeMessage = SizeOf.sizeOf(message);
        long sizeThreadName = SizeOf.sizeOf(threadName);
        long sizeLoggerName = SizeOf.sizeOf(loggerName);
        long sizeHost = SizeOf.sizeOf(sourceHost);
        long sizeSourceAddress = SizeOf.sizeOf(sourceAddress);
        long sizeSourceApplication = SizeOf.sizeOf(sourceApplication);
        long sizeFormattedException = SizeOf.sizeOf(formattedException);

        size += sizeSourceClassName;
        size += sizeSourceMethodName;
        size += sizeMessage;
        size += sizeThreadName;
        size += sizeLoggerName;
        size += sizeHost;
        size += sizeSourceAddress;
        size += sizeSourceApplication;
        size += sizeFormattedException;

        if (formattedObject != null) {
            for (String string : formattedObject) {
                size += SizeOf.sizeOf(string);
            }
        }

        size = SizeOf.roundUp(size);

        return size;
    }

    public static Level getJuliLevel(int levelValue) {
        Level level = null;

        if (levelValue == Level.INFO.intValue()) {
            level = Level.INFO;
        }
        else if (levelValue == Level.WARNING.intValue()) {
            level = Level.WARNING;
        }
        else if (levelValue == Level.SEVERE.intValue()) {
            level = Level.SEVERE;
        }
        else if (levelValue == Level.SEVERE.intValue()) {
            level = Level.SEVERE;
        }
        else if (levelValue == Level.CONFIG.intValue()) {
            level = Level.CONFIG;
        }
        else if (levelValue == Level.FINE.intValue()) {
            level = Level.FINE;
        }
        else if (levelValue == Level.FINER.intValue()) {
            level = Level.FINER;
        }
        else if (levelValue == Level.FINEST.intValue()) {
            level = Level.FINEST;
        }
        else if (levelValue == Level.OFF.intValue()) {
            level = Level.OFF;
        }

        return level;

    }

    public static Level getLevelProperty(String name) {
        Level level;

        LogManager logManager = LogManager.getLogManager();
        String val = logManager.getProperty(name);

        if (val != null) {
            level = Level.parse(val.trim());
        }
        else {
            level = null;
        }

        return level;
    }

    public static void loadLoggingConfiguration(String filename) {
        try {
            InputStream resourceAsStream = LoggingUtils.class.getResourceAsStream(filename);

            if (resourceAsStream != null) {
                LogManager.getLogManager().readConfiguration(resourceAsStream);
            }
            else {
                throw new RuntimeException("Failed to access filename " + filename + " as a stream, null was returned.");
            }
        }
        catch (SecurityException e) {
            throw new RuntimeException("SecurityException caugh reading configuration from " + filename, e);
        }
        catch (IOException e) {
            throw new RuntimeException("IOException caugh reading configuration from " + filename, e);
        }
    }

    public static void outputSampleLogging(Logger logger) {
        logger.log(Level.FINEST, "This is a line of finest logging");
        logger.log(Level.FINER, "This is a line of finer logging");
        logger.log(Level.FINE, "This is a line of fine logging");
        logger.log(Level.INFO, "This is a line of info logging");
        logger.log(Level.CONFIG, "This is a line of config logging");
        logger.log(Level.WARNING, "This is a line of warning logging");
        logger.log(Level.SEVERE, "This is a line of severe logging");

        RuntimeException re = new RuntimeException("This is an example exception");
        re.fillInStackTrace();

        logger.log(Level.WARNING, "This is a line of warning logging with an exception", re);

        logger.log(Level.FINE, "This is a line of fine logging with an object array", new Object[] { "Object1", "Object2" });
    }

    /**
     * Closes the socket quickly and quietly, ignoring any exceptions
     * 
     * @param socket
     */
    public static void close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException e) {}
        }
    }

    public static void setupRemoteVLLoggingFromSystemProperties() {
        com.logginghub.utils.logging.Logger.setLevelFromSystemProperty();
        final String sourceApplication = System.getProperty("vllogging.sourceApplication", "<unknown application>");

        String remote = System.getProperty("vllogging.remote", null);
        if (remote != null) {

            final SocketClient socketClient = new SocketClient("VertexLabs-vlloggingAppender");
            socketClient.addConnectionPoints(NetUtils.toInetSocketAddressList(remote, LoggingPorts.getSocketHubDefaultPort()));
            SocketClientManager socketClientManager = new SocketClientManager(socketClient);
            socketClientManager.startDaemon();

            InetAddress localHost = null;
            try {
                localHost = InetAddress.getLocalHost();
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            }

            int pid = -1;
            // Make a cautious attempt at getting the pid - we dont want things to
            // blow up if this doesn't work though
            try {
                pid = SigarHelper.getPid();
            }
            catch (Throwable t) {
                t.printStackTrace();
            }

            final int finalPid = pid;
            final InetAddress finalHost = localHost;

            com.logginghub.utils.logging.Logger.root().addStream(new LoggerStream() {
                public void onNewLogEvent(com.logginghub.utils.logging.LogEvent event) {
                    VLLogEvent vlevent = new VLLogEvent(event, finalPid, sourceApplication, finalHost);
                    try {
                        socketClient.send(new LogEventMessage(vlevent));
                    }
                    catch (LoggingMessageSenderException e) {}
                }
            });
        }

        // String telemetry = System.getProperty("vllogging.telemetry");
        // if (telemetry != null) {
        // KryoTelemetryClient telemetryClient = new KryoTelemetryClient();
        // List<InetSocketAddress> inetSocketAddressList =
        // NetUtils.toInetSocketAddressList(telemetry, LoggingPorts.getTelemetryHubDefaultPort());
        // telemetryClient.start(sourceApplication, inetSocketAddressList);
        // telemetryClient.startMachineTelemetryGenerator();
        // telemetryClient.startProcessTelemetryGenerator(sourceApplication);
        // }

    }

    public static LoggingHubStream logToHub2(final String sourceApplication, String host, int port) {
        LoggingHubStream stream = new LoggingHubStream();
        stream.setHost(host + ":"  + port);
        stream.setSourceApplication(sourceApplication);
        com.logginghub.utils.logging.Logger.root().addStream(stream);
        stream.startPropertiesWatcher();
        return stream;
    }

    public static void logToHub(final String sourceApplication, String host, int port) {

        final SocketClient socketClient = new SocketClient("VertexLabs-vlloggingAppender");
        socketClient.addConnectionPoints(NetUtils.toInetSocketAddressList(host, port));
        SocketClientManager socketClientManager = new SocketClientManager(socketClient);
        socketClientManager.startDaemon();

        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }

        int pid = -1;
        // Make a cautious attempt at getting the pid - we dont want things to
        // blow up if this doesn't work though
        try {
            pid = SigarHelper.getPid();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }

        final int finalPid = pid;
        final InetAddress finalHost = localHost;

        com.logginghub.utils.logging.Logger.root().addStream(new LoggerStream() {
            public void onNewLogEvent(com.logginghub.utils.logging.LogEvent event) {
                VLLogEvent vlevent = new VLLogEvent(event, finalPid, sourceApplication, finalHost);
                try {
                    socketClient.send(new LogEventMessage(vlevent));
                }
                catch (LoggingMessageSenderException e) {}
            }
        });
    }

    public static void setupJUL(String sourceApplication) {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SingleLineFormatter());
        rootLogger.addHandler(consoleHandler);

        SocketHandler socketHandler = new SocketHandler();
        socketHandler.setSourceApplication(sourceApplication);
        socketHandler.addConnectionPoint(new InetSocketAddress(VLPorts.getSocketHubDefaultPort()));
        socketHandler.setUseDispatchThread(true);
        rootLogger.addHandler(socketHandler);
    }

    
}
