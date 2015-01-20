package com.logginghub.logging.transaction;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.transaction.configuration.EventReporterConfiguration;
import com.logginghub.logging.transaction.configuration.LogEventTemplateConfiguration;
import com.logginghub.utils.*;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class EventReporter implements StreamListener<TransactionModel>, Module<EventReporterConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(EventReporter.class);

    private Destination<LogEvent> failureDestination;
    private Destination<LogEvent> successDestination;

    private LogEventTemplateConfiguration successEventTemplate;
    private LogEventTemplateConfiguration failureEventTemplate;
    private String sourceAddress;
    private String sourceHost;

    private EventReporterConfiguration configuration;

    public EventReporter() {
        sourceAddress = NetUtils.getLocalIP();
        sourceHost = NetUtils.getLocalHostname();
    }

    @Override public void onNewItem(TransactionModel t) {

        LogEventTemplateConfiguration config;
        Destination<LogEvent> destination;

        if (t.isSuccess()) {
            config = successEventTemplate;
            destination = successDestination;
        } else {
            config = failureEventTemplate;
            destination = failureDestination;
        }

        long warningInterval = Long.MAX_VALUE;
        if (StringUtils.isNotNullOrEmpty(this.configuration.getWarningAt())) {
            warningInterval = TimeUtils.parseInterval(this.configuration.getWarningAt());
        }

        boolean okToReport;
        if (t.isSuccess() && !this.configuration.isReportSuccess()) {
            double elapsed = t.calculateElapsedMilliseconds();
            if (elapsed > warningInterval) {
                // This is ok, its a warning
                okToReport = true;
            } else {
                // We shouldn't report this
                okToReport = false;
            }
        } else {
            okToReport = true;
        }

        if (okToReport) {
            report(t, config, destination, warningInterval);
        }

    }

    private void report(TransactionModel t,
                        LogEventTemplateConfiguration config,
                        Destination<LogEvent> destination,
                        long warningInterval) {

        ReportGenerator generator = new ReportGenerator();

        String report;

        if (configuration.isSingleLine()) {
            report = generator.generateSingleLineReport(t, warningInterval);
        } else {
            report = generator.generateReport(t, warningInterval);
        }

        // Turn all this into a log event
        DefaultLogEvent event = LogEventBuilder.start()
                                               .setLevel(config.getLevel())
                                               .setChannel(config.getChannel())
                                               .setMessage(report)
                                               .setSourceAddress(sourceAddress)
                                               .setSourceHost(sourceHost)
                                               .setSourceApplication(config.getSourceApplication())
                                               .setSourceClassName(config.getSourceClassName())
                                               .setSourceMethodName(config.getSourceMethodName())
                                               .setThreadName(config.getThreadName())
                                               .setLoggerName(config.getLoggerName())
                                               .toLogEvent();

        // Send it on its way
        destination.send(event);
    }

    @SuppressWarnings("unchecked") public void configure(EventReporterConfiguration eventReporterConfiguration,
                                                         ServiceDiscovery serviceDiscovery) {
        this.configuration = eventReporterConfiguration;
        successEventTemplate = eventReporterConfiguration.getSuccessEventTemplate();
        failureEventTemplate = eventReporterConfiguration.getFailureEventTemplate();

        this.successDestination = serviceDiscovery.findService(Destination.class,
                                                               LogEvent.class,
                                                               eventReporterConfiguration.getSuccessDestination());
        this.failureDestination = serviceDiscovery.findService(Destination.class,
                                                               LogEvent.class,
                                                               eventReporterConfiguration.getFailureDestination());
    }

    @Override public void start() {
    }

    @Override public void stop() {
    }

}
