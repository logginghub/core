package com.logginghub.logging.transaction;

import com.logginghub.logging.transaction.configuration.EmailReporterConfiguration;
import com.logginghub.logging.transaction.configuration.EmailTemplateConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class EmailReporter implements StreamListener<TransactionModel>, Module<EmailReporterConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(EmailReporter.class);

    private Destination<EmailContent> failureDestination;
    private Destination<EmailContent> successDestination;

    private EmailTemplateConfiguration successEventTemplate;
    private EmailTemplateConfiguration failureEventTemplate;
    private String sourceAddress;
    private String sourceHost;

    private EmailReporterConfiguration configuration;

    public EmailReporter() {
        sourceAddress = NetUtils.getLocalIP();
        sourceHost = NetUtils.getLocalHostname();
    }

    @Override public void onNewItem(TransactionModel t) {

        EmailTemplateConfiguration config;
        Destination<EmailContent> destination;

        if (t.isSuccess()) {
            config = successEventTemplate;
            destination = successDestination;
        }
        else {
            config = failureEventTemplate;
            destination = failureDestination;
        }

        long warningInterval = Long.MAX_VALUE;
        if (StringUtils.isNotNullOrEmpty(this.configuration.getWarningAt())) {
            warningInterval = TimeUtils.parseInterval(this.configuration.getWarningAt());
        }

        String report;

        EmailContent email = config.buildEmailContent();
        if (config.isHtml()) {
            HtmlReportGenerator generator = new HtmlReportGenerator();
            report = generator.generateReport(t, warningInterval);
            email.setHtml(true);
        }
        else {
            ReportGenerator generator = new ReportGenerator();
            report = generator.generateReport(t, warningInterval);
            email.setHtml(false);
        }

        email.setMessage(report);

        // Send it on its way
        destination.send(email);
    }

    @SuppressWarnings("unchecked") public void configure(EmailReporterConfiguration eventReporterConfiguration, ServiceDiscovery serviceDiscovery) {
        this.configuration = eventReporterConfiguration;
        successEventTemplate = eventReporterConfiguration.getSuccessTemplate();
        failureEventTemplate = eventReporterConfiguration.getFailureTemplate();

        this.successDestination = serviceDiscovery.findService(Destination.class,
                                                               EmailContent.class,
                                                               eventReporterConfiguration.getSuccessDestination());
        this.failureDestination = serviceDiscovery.findService(Destination.class,
                                                               EmailContent.class,
                                                               eventReporterConfiguration.getFailureDestination());
    }

    @Override public void start() {}

    @Override public void stop() {}

}
