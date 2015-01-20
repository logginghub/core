package com.logginghub.logging.modules;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.modules.configuration.RestfulListenerConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Stream;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.web.Form;
import com.logginghub.web.JettyLauncher;
import com.logginghub.web.RequestContext;
import com.logginghub.web.WebController;

@WebController(staticFiles = "/static/hubrestful") public class RestfulListenerModule implements Module<RestfulListenerConfiguration> {

    private static Logger logger = Logger.getLoggerFor(RestfulListenerModule.class);
    private JettyLauncher jettyLauncher;
    private Stream<LogEvent> logEventStream = new Stream<LogEvent>();
    private RestfulListenerConfiguration configuration;

    public void send() {
        Form form = RequestContext.getRequestContext().getForm();

        String level = form.getFirstValue("level", "INFO");
        long sequenceNumber = form.getFirstLong("sequence", 0);
        String sourceClassName = form.getFirstValue("sourceClass", "");
        String sourceMethodName = form.getFirstValue("sourceMethod", "");
        String message = form.getFirstValue("message");
        String threadName = form.getFirstValue("thread", "");
        long localCreationTimeMillis = form.getFirstLong("time", System.currentTimeMillis());
        String loggerName = form.getFirstValue("logger", "");
        String sourceHost = form.getFirstValue("host", "");
        String sourceAddress = form.getFirstValue("hostip", "");
        String sourceApplication = form.getFirstValue("application", "");
        int pid = form.getFirstInt("pid", 0);
        String formattedException = form.getFirstValue("exception");
        String formattedObject = form.getFirstValue("object");
        String channel = form.getFirstValue("channel");

        if (message != null) {
            DefaultLogEvent event = new DefaultLogEvent();
            event.setChannel(channel);
            event.setLevel(Logger.parseLevel(level));
            event.setFormattedException(formattedException);
            if (formattedObject != null) {
                event.setFormattedObject(new String[] { formattedObject });
            }
            event.setLocalCreationTimeMillis(localCreationTimeMillis);
            event.setLoggerName(loggerName);
            event.setMessage(message);
            event.setPid(pid);
            event.setSequenceNumber(sequenceNumber);
            event.setSourceAddress(sourceAddress);
            event.setSourceApplication(sourceApplication);
            event.setSourceClassName(sourceClassName);
            event.setSourceHost(sourceHost);
            event.setSourceMethodName(sourceMethodName);
            event.setThreadName(threadName);

            logEventStream.send(event);
        }
        else {
            logger.warn("Message field must be populated at the very least, ignoring request");
        }
    }

    public Stream<LogEvent> getLogEventStream() {
        return logEventStream;
    }

    @Override public void configure(RestfulListenerConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        @SuppressWarnings("unchecked") Destination<LogEvent> destination = discovery.findService(Destination.class,
                                                                                                 LogEvent.class,
                                                                                                 configuration.getEventDestinationRef());
        logEventStream.addDestination(destination);
    }

    @Override public void start() {
        try {
            jettyLauncher = JettyLauncher.launchNonBlocking(this, configuration.getPort());
        }
        catch (Exception e) {
            throw new FormattedRuntimeException("Failed to start jetty launcher", e);
        }
    }

    public void stop() {
        jettyLauncher.close();
    }

}
