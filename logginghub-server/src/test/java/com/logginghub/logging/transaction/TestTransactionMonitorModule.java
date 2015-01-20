package com.logginghub.logging.transaction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.transaction.EmailContent;
import com.logginghub.logging.transaction.StateEngineModel;
import com.logginghub.logging.transaction.TransactionModel;
import com.logginghub.logging.transaction.TransactionMonitorModule;
import com.logginghub.logging.transaction.configuration.EmailReporterConfiguration;
import com.logginghub.logging.transaction.configuration.EventReporterConfiguration;
import com.logginghub.logging.transaction.configuration.LogEventTemplateConfiguration;
import com.logginghub.logging.transaction.configuration.StateCaptureConfiguration;
import com.logginghub.logging.transaction.configuration.StateEngineConfiguration;
import com.logginghub.logging.transaction.configuration.TransactionMonitorConfiguration;
import com.logginghub.logging.transaction.configuration.TransitionConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Source;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

public class TestTransactionMonitorModule {

    private StateEngineModel model;
    private TransactionMonitorModule controller;
    private Bucket<TransactionModel> successfulTransactions = new Bucket<TransactionModel>();
    private Bucket<TransactionModel> failedTransactions = new Bucket<TransactionModel>();
    
    private Bucket<LogEvent> successEvents = new Bucket<LogEvent>();
    private Bucket<LogEvent> failedEvents = new Bucket<LogEvent>();
    
    private Bucket<EmailContent> successEmails = new Bucket<EmailContent>();
    private Bucket<EmailContent> failedEmails = new Bucket<EmailContent>();
    
    private FixedTimeProvider timeProvider = new FixedTimeProvider(0);
    private TransactionMonitorConfiguration configuration;
    private Multiplexer<LogEvent> logEventSource;

    @Before public void setup() {
        StateEngineConfiguration engineConfiguration = new StateEngineConfiguration();
        engineConfiguration.getStateCaptures().add(new StateCaptureConfiguration("one", "First stage transaction id '[tid]' time {time} ms"));
        engineConfiguration.getStateCaptures().add(new StateCaptureConfiguration("two", "Second stage transaction id '[tid]' time {time} ms"));
        engineConfiguration.getStateCaptures().add(new StateCaptureConfiguration("threeA",
                                                                                 "Third optional A stage transaction id '[tid]' time {time} ms"));
        engineConfiguration.getStateCaptures().add(new StateCaptureConfiguration("threeB",
                                                                                 "Third optional B stage transaction id '[tid]' time {time} ms"));
        engineConfiguration.getStateCaptures().add(new StateCaptureConfiguration("four", "Last stage transaction id '[tid]' time {time} ms"));

        TransitionConfiguration transition1 = new TransitionConfiguration("one", "10 second");
        TransitionConfiguration transition2 = new TransitionConfiguration("two", "20 seconds");
        TransitionConfiguration transition3 = new TransitionConfiguration("threeA", "30 seconds");
        TransitionConfiguration transition4 = new TransitionConfiguration("threeB", "40 seconds");
        TransitionConfiguration transition5 = new TransitionConfiguration("four", "50 seconds");

        transition1.getTransitions().add(transition2);
        transition2.getTransitions().add(transition3);
        transition2.getTransitions().add(transition5);
        transition3.getTransitions().add(transition4);
        transition4.getTransitions().add(transition5);

        engineConfiguration.setTransition(transition1);

        EventReporterConfiguration eventReporterConfiguration = new EventReporterConfiguration();
        LogEventTemplateConfiguration successEventTemplate = new LogEventTemplateConfiguration();
        LogEventTemplateConfiguration failureEventTemplate = new LogEventTemplateConfiguration();
        
        eventReporterConfiguration.setFailureDestination("failure");
        eventReporterConfiguration.setSuccessDestination("success");

        successEventTemplate.setChannel("stateengine/success");
        failureEventTemplate.setChannel("stateengine/failure");

        successEventTemplate.setLevel("info");
        failureEventTemplate.setLevel("warning");

        eventReporterConfiguration.setSuccessEventTemplate(successEventTemplate);
        eventReporterConfiguration.setFailureEventTemplate(failureEventTemplate);

        EmailReporterConfiguration emailReporterConfiguration = new EmailReporterConfiguration();
        
        emailReporterConfiguration.getSuccessTemplate().setBccAddress("successBcc");
        emailReporterConfiguration.getSuccessTemplate().setCcAddress("successCc");
        emailReporterConfiguration.getSuccessTemplate().setFromAddress("successFrom");
        emailReporterConfiguration.getSuccessTemplate().setHtml(false);
        emailReporterConfiguration.getSuccessTemplate().setSubject("successSubject");
        emailReporterConfiguration.getSuccessTemplate().setToAddress("successTo");

        emailReporterConfiguration.getFailureTemplate().setBccAddress("failureBcc");
        emailReporterConfiguration.getFailureTemplate().setCcAddress("failureCc");
        emailReporterConfiguration.getFailureTemplate().setFromAddress("failureFrom");
        emailReporterConfiguration.getFailureTemplate().setHtml(true);
        emailReporterConfiguration.getFailureTemplate().setSubject("failureSubject");
        emailReporterConfiguration.getFailureTemplate().setToAddress("failureTo");

        eventReporterConfiguration.setReportSuccess(true);
        eventReporterConfiguration.setReportFailure(true);
        emailReporterConfiguration.setReportSuccess(true);
        emailReporterConfiguration.setReportFailure(true);
        emailReporterConfiguration.setFailureDestination("failure");
        emailReporterConfiguration.setSuccessDestination("success");
        
        configuration = new TransactionMonitorConfiguration();
        configuration.getEngines().add(engineConfiguration);
        engineConfiguration.getEventReporters().add(eventReporterConfiguration);
        engineConfiguration.getEmailReporters().add(emailReporterConfiguration);

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.getExceptionPolicy().setPolicy(Policy.RethrowOnAny);
        
        logEventSource = new Multiplexer<LogEvent>();        
        discovery.bind(Source.class, LogEvent.class, logEventSource);
        discovery.bind(Destination.class, LogEvent.class, "success", successEvents);
        discovery.bind(Destination.class, LogEvent.class, "failure", failedEvents);
        
        discovery.bind(Destination.class, EmailContent.class, "failure", failedEmails);
        discovery.bind(Destination.class, EmailContent.class, "success", successEmails);
        
        reconfigure(configuration, discovery);
    }

    private void reconfigure(TransactionMonitorConfiguration configuration, ConfigurableServiceDiscovery discovery) {
        model = new StateEngineModel();
        controller = new TransactionMonitorModule(model);

        controller.getSuccessfulTransactionsStream().addDestination(successfulTransactions);
        controller.getFailedTransactionsStream().addDestination(failedTransactions);
        controller.setTimeProvider(timeProvider);
        
        controller.configure(configuration, discovery);
    }

    @Test public void test_start_event() throws Exception {
        DefaultLogEvent event = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        controller.onNewLogEvent(event);

        assertThat(model.getCurrentTransactions().size(), is(1));
        TransactionModel transactionModel = model.getCurrentTransactions().get(0);
        assertThat(transactionModel.getTransactionID(), is("transaction-0001"));
        assertThat((LogEvent) transactionModel.getEvents().get(0), is((LogEvent) event));
    }

    @Test public void test_start_timeout() throws Exception {
        DefaultLogEvent event = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        controller.onNewLogEvent(event);
        assertThat(model.getCurrentTransactions().size(), is(1));

        timeProvider.setTime(TimeUtils.parseTimeUTC("1/1/1970 00:00:09"));
        controller.checkForTimeouts();
        assertThat(model.getCurrentTransactions().size(), is(1));

        timeProvider.setTime(TimeUtils.parseTimeUTC("1/1/1970 00:00:10"));
        controller.checkForTimeouts();
        assertThat(model.getCurrentTransactions().size(), is(0));
        assertThat(failedTransactions.size(), is(1));
        assertThat(failedTransactions.get(0).isSuccess(), is(false));
    }

    @Test public void test_progress_to_second_state() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();

        controller.onNewLogEvent(event1);
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("one"));

        controller.onNewLogEvent(event2);
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("two"));

    }

    @Test public void test_progress_to_second_state_wrong_transaction_id() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0002' time 34.12 ms").toLogEvent();

        controller.onNewLogEvent(event1);
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("one"));
        assertThat(model.getCurrentTransactions().size(), is(1));

        controller.onNewLogEvent(event2);
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("one"));
        assertThat(model.getCurrentTransactions().size(), is(1));

    }

    @Test public void test_progress_to_second_state_timeout() throws Exception {
        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();

        controller.onNewLogEvent(event1);
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("one"));

        timeProvider.setTime(TimeUtils.parseTimeUTC("1/1/1970 00:00:09"));
        controller.checkForTimeouts();

        controller.onNewLogEvent(event2);
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("two"));

        // This would have caused a timeout in state one
        timeProvider.setTime(TimeUtils.parseTimeUTC("1/1/1970 00:00:11"));
        controller.checkForTimeouts();
        assertThat(model.getCurrentTransactions().size(), is(1));
        assertThat(failedTransactions.size(), is(0));

        timeProvider.setTime(TimeUtils.parseTimeUTC("1/1/1970 00:00:28"));
        controller.checkForTimeouts();
        assertThat(model.getCurrentTransactions().size(), is(1));
        assertThat(failedTransactions.size(), is(0));

        timeProvider.setTime(TimeUtils.parseTimeUTC("1/1/1970 00:00:29"));
        controller.checkForTimeouts();
        assertThat(model.getCurrentTransactions().size(), is(0));
        assertThat(failedTransactions.size(), is(1));

    }

    @Test public void test_progress_optional_states() throws Exception {
        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();

        DefaultLogEvent event3 = LogEventBuilder.start()
                                                .setMessage("Third optional A stage transaction id 'transaction-0001' time 34.12 ms")
                                                .toLogEvent();
        DefaultLogEvent event4 = LogEventBuilder.start()
                                                .setMessage("Third optional B stage transaction id 'transaction-0001' time 34.12 ms")
                                                .toLogEvent();

        DefaultLogEvent event5 = LogEventBuilder.start().setMessage("Last stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();

        controller.onNewLogEvent(event1);
        controller.onNewLogEvent(event2);
        controller.onNewLogEvent(event3);
        controller.onNewLogEvent(event4);
        controller.onNewLogEvent(event5);

        assertThat(model.getCurrentTransactions().size(), is(0));
        assertThat(successfulTransactions.size(), is(1));
        assertThat(successfulTransactions.get(0).getEvents().size(), is(5));
    }

    @Test public void test_progress_completes() throws Exception {
        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event3 = LogEventBuilder.start().setMessage("Last stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();

        controller.onNewLogEvent(event1);
        controller.onNewLogEvent(event2);
        controller.onNewLogEvent(event3);

        assertThat(model.getCurrentTransactions().size(), is(0));
        assertThat(successfulTransactions.size(), is(1));
        assertThat(successfulTransactions.get(0).isSuccess(), is(true));
    }

    @Test public void test_start_event_multiple_transactions() throws Exception {
        DefaultLogEvent event1a = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2a = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event3a = LogEventBuilder.start()
                                                 .setMessage("Third optional A stage transaction id 'transaction-0001' time 34.12 ms")
                                                 .toLogEvent();
        DefaultLogEvent event4a = LogEventBuilder.start()
                                                 .setMessage("Third optional B stage transaction id 'transaction-0001' time 34.12 ms")
                                                 .toLogEvent();
        DefaultLogEvent event5a = LogEventBuilder.start().setMessage("Last stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();

        DefaultLogEvent event1b = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0002' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2b = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0002' time 34.12 ms").toLogEvent();
        DefaultLogEvent event5b = LogEventBuilder.start().setMessage("Last stage transaction id 'transaction-0002' time 34.12 ms").toLogEvent();

        assertThat(model.getCurrentTransactions().size(), is(0));

        controller.onNewLogEvent(event1a);

        assertThat(model.getCurrentTransactions().size(), is(1));
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("one"));

        controller.onNewLogEvent(event1b);

        assertThat(model.getCurrentTransactions().size(), is(2));
        assertThat(model.getCurrentTransactions().get(1).getCurrentState().getStripper().getPatternName(), is("one"));

        controller.onNewLogEvent(event2a);

        assertThat(model.getCurrentTransactions().size(), is(2));
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("two"));
        assertThat(model.getCurrentTransactions().get(1).getCurrentState().getStripper().getPatternName(), is("one"));

        controller.onNewLogEvent(event2b);

        assertThat(model.getCurrentTransactions().size(), is(2));
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("two"));
        assertThat(model.getCurrentTransactions().get(1).getCurrentState().getStripper().getPatternName(), is("two"));

        controller.onNewLogEvent(event3a);
        controller.onNewLogEvent(event5b);

        assertThat(model.getCurrentTransactions().size(), is(1));
        assertThat(model.getCurrentTransactions().get(0).getCurrentState().getStripper().getPatternName(), is("threeA"));
        assertThat(successfulTransactions.size(), is(1));
        assertThat(successfulTransactions.get(0).getCurrentState().getStripper().getPatternName(), is("four"));

        controller.onNewLogEvent(event4a);
        controller.onNewLogEvent(event5a);

        assertThat(successfulTransactions.size(), is(2));
        assertThat(successfulTransactions.get(0).getEvents().size(), is(3));
        assertThat(successfulTransactions.get(1).getEvents().size(), is(5));
    }

    @Test public void test_event_generator_success() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").setLocalCreationTimeMillis(0).toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").setLocalCreationTimeMillis(10).toLogEvent();
        DefaultLogEvent event3 = LogEventBuilder.start().setMessage("Last stage transaction id 'transaction-0001' time 34.12 ms").setLocalCreationTimeMillis(20).toLogEvent();

        controller.onNewLogEvent(event1);
        controller.onNewLogEvent(event2);
        controller.onNewLogEvent(event3);

        assertThat(successEvents.size(), is(1));
        assertThat(successEvents.get(0).getChannel(), is("stateengine/success"));
        assertThat(successEvents.get(0).getLoggerName(), is(""));
        assertThat(successEvents.get(0).getMessage().split(StringUtils.newline)[0], is("Transaction 'transaction-0001' completed successfully in 20.0 ms:"));
        assertThat(successEvents.get(0).getSourceAddress(), is(NetUtils.getLocalIP()));
        assertThat(successEvents.get(0).getSourceApplication(), is(""));
        assertThat(successEvents.get(0).getSourceClassName(), is(""));
        assertThat(successEvents.get(0).getSourceHost(), is(NetUtils.getLocalHostname()));
        assertThat(successEvents.get(0).getSourceMethodName(), is(""));
        assertThat(successEvents.get(0).getThreadName(), is(""));
    }

    @Test public void test_event_generator_failure() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();

        controller.onNewLogEvent(event1);
        controller.onNewLogEvent(event2);

        timeProvider.setTime(TimeUtils.parseTimeUTC("1/1/1970 00:01:29"));
        controller.checkForTimeouts();
        
        assertThat(failedEvents.size(), is(1));
        assertThat(failedEvents.get(0).getChannel(), is("stateengine/failure"));
        assertThat(failedEvents.get(0).getLoggerName(), is(""));
        assertThat(failedEvents.get(0).getMessage(), is(startsWith("Transaction 'transaction-0001' timed out")));
        System.out.println(failedEvents.get(0).getMessage());
        assertThat(failedEvents.get(0).getSourceAddress(), is(NetUtils.getLocalIP()));
        assertThat(failedEvents.get(0).getSourceApplication(), is(""));
        assertThat(failedEvents.get(0).getSourceClassName(), is(""));
        assertThat(failedEvents.get(0).getSourceHost(), is(NetUtils.getLocalHostname()));
        assertThat(failedEvents.get(0).getSourceMethodName(), is(""));
        assertThat(failedEvents.get(0).getThreadName(), is(""));
    }
    
    @Test public void test_email_generator_success() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").setLocalCreationTimeMillis(0).toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").setLocalCreationTimeMillis(10).toLogEvent();
        DefaultLogEvent event3 = LogEventBuilder.start().setMessage("Last stage transaction id 'transaction-0001' time 34.12 ms").setLocalCreationTimeMillis(20).toLogEvent();

        controller.onNewLogEvent(event1);
        controller.onNewLogEvent(event2);
        controller.onNewLogEvent(event3);

        assertThat(successEmails.size(), is(1));
        assertThat(successEmails.get(0).getToAddress(), is("successTo"));
        assertThat(successEmails.get(0).getBccAddress(), is("successBcc"));
        assertThat(successEmails.get(0).getCcAddress(), is("successCc"));
        assertThat(successEmails.get(0).getFromAddress(), is("successFrom"));
        assertThat(successEmails.get(0).getMessage().split(StringUtils.newline)[0], is("Transaction 'transaction-0001' completed successfully in 20.0 ms:"));
        assertThat(successEmails.get(0).getSubject(), is("successSubject"));
        assertThat(successEmails.get(0).isHTML(), is(false));
        
    }

    @Test public void test_email_generator_failure() throws Exception {
        
        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();
        DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Second stage transaction id 'transaction-0001' time 34.12 ms").toLogEvent();

        controller.onNewLogEvent(event1);
        controller.onNewLogEvent(event2);

        timeProvider.setTime(TimeUtils.parseTimeUTC("1/1/1970 00:01:29"));
        controller.checkForTimeouts();
        
        assertThat(failedEmails.size(), is(1));
        assertThat(failedEmails.get(0).getToAddress(), is("failureTo"));
        assertThat(failedEmails.get(0).getBccAddress(), is("failureBcc"));
        assertThat(failedEmails.get(0).getCcAddress(), is("failureCc"));
        assertThat(failedEmails.get(0).getFromAddress(), is("failureFrom"));
//        BrowserUtils.viewInBrowser(failedEmails.get(0).getMessage());
        assertThat(failedEmails.get(0).getMessage(), is(startsWith("<html><head></head><body><h1>Transaction 'transaction-0001' timed out")));
        assertThat(failedEmails.get(0).getSubject(), is("failureSubject"));
        assertThat(failedEmails.get(0).isHTML(), is(true));

    }

    @Test public void test_single_transition() {
        setupSingleTransition();
        
        DefaultLogEvent event1 = LogEventBuilder.start().setMessage("First stage transaction id 'transaction-0001' time 34000.12 ms").setLocalCreationTimeMillis(0).toLogEvent();

        controller.onNewLogEvent(event1);
        
        assertThat(successfulTransactions.size(), is(1));        

        assertThat(successEvents.size(), is(1));
        assertThat(successEvents.get(0).getChannel(), is("stateengine/success"));
        assertThat(successEvents.get(0).getLoggerName(), is(""));
        assertThat(successEvents.get(0).getMessage().split(StringUtils.newline)[0], is("Transaction 'transaction-0001' completed successfully in 34000.12 ms:"));
        assertThat(successEvents.get(0).getSourceAddress(), is(NetUtils.getLocalIP()));
        assertThat(successEvents.get(0).getSourceApplication(), is(""));
        assertThat(successEvents.get(0).getSourceClassName(), is(""));
        assertThat(successEvents.get(0).getSourceHost(), is(NetUtils.getLocalHostname()));
        assertThat(successEvents.get(0).getSourceMethodName(), is(""));
        assertThat(successEvents.get(0).getThreadName(), is(""));
    }

    private void setupSingleTransition() {
        StateEngineConfiguration engineConfiguration = new StateEngineConfiguration();
        engineConfiguration.getStateCaptures().add(new StateCaptureConfiguration("one", "First stage transaction id '[tid]' time {time} ms"));

        TransitionConfiguration transition1 = new TransitionConfiguration("one", "10 second");
        engineConfiguration.setTransition(transition1);

        EventReporterConfiguration eventReporterConfiguration = new EventReporterConfiguration();
        LogEventTemplateConfiguration successEventTemplate = new LogEventTemplateConfiguration();
        LogEventTemplateConfiguration failureEventTemplate = new LogEventTemplateConfiguration();
        
        eventReporterConfiguration.setFailureDestination("failure");
        eventReporterConfiguration.setSuccessDestination("success");

        successEventTemplate.setChannel("stateengine/success");
        failureEventTemplate.setChannel("stateengine/failure");

        successEventTemplate.setLevel("info");
        failureEventTemplate.setLevel("warning");

        eventReporterConfiguration.setSuccessEventTemplate(successEventTemplate);
        eventReporterConfiguration.setFailureEventTemplate(failureEventTemplate);

        eventReporterConfiguration.setWarningAt("10 seconds");
        eventReporterConfiguration.setReportSuccess(false);
        eventReporterConfiguration.setReportFailure(true);
        
        configuration = new TransactionMonitorConfiguration();
        configuration.getEngines().add(engineConfiguration);
        engineConfiguration.getEventReporters().add(eventReporterConfiguration);

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.getExceptionPolicy().setPolicy(Policy.RethrowOnAny);
        
        logEventSource = new Multiplexer<LogEvent>();        
        discovery.bind(Source.class, LogEvent.class, logEventSource);
        discovery.bind(Destination.class, LogEvent.class, "success", successEvents);
        discovery.bind(Destination.class, LogEvent.class, "failure", failedEvents);
        
        reconfigure(configuration, discovery);
    }
    
}
