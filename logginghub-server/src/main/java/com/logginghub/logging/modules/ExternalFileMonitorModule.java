package com.logginghub.logging.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.modules.configuration.ExternalFileMonitorConfiguration;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.SystemErrExceptionHandler;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class ExternalFileMonitorModule implements Module<ExternalFileMonitorConfiguration>, Asynchronous {

    private Logger logger = Logger.getLoggerFor(ExternalFileMonitorModule.class);
    private ExternalFileMonitorConfiguration configuration;
    private WorkerThread thread;
    private ExceptionHandler exceptionHandler = new SystemErrExceptionHandler();
    private int level;

    private Multiplexer<LogEvent> logEventMultiplexer = new Multiplexer<LogEvent>();

    @Override public void configure(ExternalFileMonitorConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        level = Logger.parseLevel(configuration.getLevel());
        
        @SuppressWarnings("unchecked") Destination<LogEvent> eventDestination = discovery.findService(Destination.class,
                                                                                                      LogEvent.class,
                                                                                                      configuration.getDestination());
        logEventMultiplexer.addDestination(eventDestination);
    }
    
    @Override public void start() {


        final File file = new File(configuration.getPath());
        thread = WorkerThread.executeOngoing("FileReader", new Runnable() {
            @Override public void run() {
                if (file.exists()) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        String line = null;

                        // TODO : might be nice to have a feature to play since
                        // a last captured time stamp?

                        // TODO : need to deal with rotations, truncations etc

                        // If we aren't grabbing lines, skip straight to the end
                        if (!configuration.isReplayAll()) {
                            bufferedReader.skip(file.length());
                        }

                        while (thread.isRunning()) {
                            line = bufferedReader.readLine();
                            if (line == null) {
                                ThreadUtils.sleep(100);
                            }
                            else {
                                processLine(line);
                            }
                        }

                        FileUtils.closeQuietly(bufferedReader);
                    }
                    catch (IOException e) {
                        exceptionHandler.handleException("Opening file", e);
                    }
                    catch (LoggingMessageSenderException e) {
                        exceptionHandler.handleException("Sending event", e);
                    }
                }
            }
        });

        thread.setIterationDelay(1000);
    }

    protected void processLine(String line) throws LoggingMessageSenderException {
        DefaultLogEvent event = LogEventBuilder.start()
                                               .setMessage(configuration.getPrefix() + line + configuration.getPostfix())
                                               .setLevel(level)                                               
                                               .setChannel(configuration.getChannel())
                                               .toLogEvent();
        
        logEventMultiplexer.send(event);
    }
    
    public Multiplexer<LogEvent> getLogEventMultiplexer() {
        return logEventMultiplexer;
    }

    @Override public void stop() {
        thread.stop();
    }

    

}
