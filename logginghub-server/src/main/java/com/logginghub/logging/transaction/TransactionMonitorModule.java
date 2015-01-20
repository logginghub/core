package com.logginghub.logging.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.transaction.configuration.EmailReporterConfiguration;
import com.logginghub.logging.transaction.configuration.EventReporterConfiguration;
import com.logginghub.logging.transaction.configuration.StateCaptureConfiguration;
import com.logginghub.logging.transaction.configuration.StateEngineConfiguration;
import com.logginghub.logging.transaction.configuration.TransactionMonitorConfiguration;
import com.logginghub.logging.transaction.configuration.TransitionConfiguration;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Is;
import com.logginghub.utils.Source;
import com.logginghub.utils.Stream;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class TransactionMonitorModule implements Module<TransactionMonitorConfiguration>, Destination<LogEvent> {

    private static final Logger logger = Logger.getLoggerFor(TransactionMonitorModule.class);

    private TimeProvider timeProvider = new SystemTimeProvider();
    private StateEngineModel model;

    private Stream<TransactionModel> successfulTransactionsStream = new Stream<TransactionModel>();
    private Stream<TransactionModel> failedTransactionsStream = new Stream<TransactionModel>();

    private List<EventReporter> eventReporters = new ArrayList<EventReporter>();
    private List<EmailReporter> emailReporters = new ArrayList<EmailReporter>();

    private Timer timer;

    public TransactionMonitorModule(StateEngineModel model) {
        this.model = model;
    }

    public TransactionMonitorModule() {
        this.model = new StateEngineModel();
    }

    public void configure(TransactionMonitorConfiguration configuration, ServiceDiscovery serviceDiscovery) {

        // Bind to the event source
        Source<LogEvent> logEventSource = serviceDiscovery.findService(Source.class, LogEvent.class, configuration.getSource());
        logEventSource.addDestination(this);

        List<StateEngineConfiguration> engines = configuration.getEngines();
        for (StateEngineConfiguration engineConfiguration : engines) {

            EngineModel engineModel = this.model.createNewEngine();

            // Load the capture state configurations in value strippers - remember their names, as
            // we'll need that later to link up the state diagram
            Map<String, ValueStripper2> strippers = new HashMap<String, ValueStripper2>();
            List<StateCaptureConfiguration> stateCaptures = engineConfiguration.getStateCaptures();
            for (StateCaptureConfiguration stateCaptureConfiguration : stateCaptures) {
                ValueStripper2 stripper = new ValueStripper2();
                stripper.setPatternName(stateCaptureConfiguration.getName());
                stripper.setPattern(stateCaptureConfiguration.getPattern());
                strippers.put(stripper.getPatternName(), stripper);
            }

            // Load in the transitions tree
            TransitionConfiguration transition = engineConfiguration.getTransition();

            StateNodeModel start = new StateNodeModel();
            start.buildFromConfiguration(transition, strippers);

            engineModel.setStartNode(start);

            List<EventReporterConfiguration> eventReporters = engineConfiguration.getEventReporters();
            for (EventReporterConfiguration eventReporterConfiguration : eventReporters) {

                EventReporter eventReporter = new EventReporter();
                eventReporter.configure(eventReporterConfiguration, serviceDiscovery);

                // Because we might want to report successful warnings, sign the streams up
                // regardless and let the reporter decide
                successfulTransactionsStream.addListener(eventReporter);

                if (eventReporterConfiguration.isReportFailure()) {
                    failedTransactionsStream.addListener(eventReporter);
                }

                this.eventReporters.add(eventReporter);
            }

            List<EmailReporterConfiguration> emailReporters = engineConfiguration.getEmailReporters();
            for (EmailReporterConfiguration emailReporterConfiguration : emailReporters) {
                EmailReporter emailReporter = new EmailReporter();
                emailReporter.configure(emailReporterConfiguration, serviceDiscovery);

                if (emailReporterConfiguration.isReportSuccess()) {
                    successfulTransactionsStream.addListener(emailReporter);
                }

                if (emailReporterConfiguration.isReportFailure()) {
                    failedTransactionsStream.addListener(emailReporter);
                }

                this.emailReporters.add(emailReporter);
            }
        }

    }

    public List<EventReporter> getEventReporters() {
        return eventReporters;
    }

    public synchronized void start() {
        timer = TimerUtils.everySecond("StateEngineTimeout", new Runnable() {
            @Override public void run() {
                checkForTimeouts();
            }
        });
    }

    public synchronized void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public StateEngineModel getModel() {
        return model;
    }

    public void onNewLogEvent(LogEvent event) {
        logger.trace("New event received '{}'", event);

        List<EngineModel> engines = model.getEngines();
        for (EngineModel engineModel : engines) {
            checkForStartingEvent(event, engineModel);
        }

        // TODO : this iteration is very inefficient - we really need to keep a list of all the
        // value strippers that a currently active, and then lookup the TransactionID from the list
        // of a current TransactionModels associated with each stripper. As it is we'll run the
        // stripper far too many times if there are a lot of transactions on a particular state

        List<TransactionModel> currentTransactions = model.getCurrentTransactions();
        // TODO : this will kill performance, need a better way to do this
        synchronized (currentTransactions) {

            Iterator<TransactionModel> iterator = currentTransactions.iterator();
            while (iterator.hasNext()) {
                TransactionModel transactionModel = (TransactionModel) iterator.next();

                StateNodeModel currentState = transactionModel.getCurrentState();
                List<StateNodeModel> transitions = currentState.getTransitions();
                for (StateNodeModel newState : transitions) {

                    ValueStripper2 stripper = newState.getStripper();
                    PatternisedLogEvent patternised = stripper.patternise2(event);
                    if (patternised != null) {

                        int labelIndex = stripper.getLabelIndex("tid");
                        Is.greaterThanOrZero(labelIndex, "Pattern {} doesn't seem to contain a 'tid' label", stripper.getPattern());
                        String transactionID = patternised.getVariables()[labelIndex];
                        logger.finest("Checking transaction '{}' against model '{}' : {}",
                                      transactionID,
                                      transactionModel.getTransactionID(),
                                      event.getMessage());

                        // Need to make sure the transactionIDs match up!!
                        if (transactionModel.getTransactionID().equals(transactionID)) {

                            // We've found a transition
                            transactionModel.setCurrentState(newState);
                            transactionModel.setTimeoutTime(timeProvider.getTime() + newState.getTimeout());

                            int timeIndex = stripper.getLabelIndex("time");
                            if (timeIndex >= 0) {
                                String timing = patternised.getVariables()[timeIndex];
                                transactionModel.addTiming(stripper.getPatternName(), timing);
                            }

                            logger.finer("State progression triggered by event '{}' - transaction '{}' progressing (new timeout at '{})",
                                         event.getMessage(),
                                         transactionID,
                                         Logger.toDateString(transactionModel.getTimeoutTime()));

                            // TODO : add optional timing information

                            transactionModel.addEvent(event);

                            if (newState.isEndState()) {
                                logger.fine("End state reached for transaction '{}'", transactionModel);
                                transactionModel.setSuccess(true);
                                iterator.remove();
                                successfulTransactionsStream.onNewItem(transactionModel);
                            }

                            // Only need to pick up one of the transitions if it matches
                            break;
                        }
                    }
                }
            }
        }
    }

    private void checkForStartingEvent(LogEvent event, EngineModel engineModel) {
        // See if this event starts anything
        StateNodeModel start = engineModel.getStart();

        ValueStripper2 stripper = start.getStripper();
        PatternisedLogEvent patternised = stripper.patternise2(event);
        if (patternised != null) {

            int labelIndex = stripper.getLabelIndex("tid");
            Is.greaterThanOrZero(labelIndex, "Pattern {} doesn't seem to contain a 'tid' label", stripper.getPattern());
            String transactionID = patternised.getVariables()[labelIndex];

            String timing = null;
            int timeIndex = stripper.getLabelIndex("time");
            if (timeIndex >= 0) {
                timing = patternised.getVariables()[timeIndex];
            }

            if (start.getTransitions().isEmpty()) {
                // This is also the end state! Skip to the reporting part
                TransactionModel newTransaction = new TransactionModel(transactionID, start);
                if (timing != null) {
                    newTransaction.addTiming(stripper.getPatternName(), timing);
                }
                newTransaction.addEvent(event);
                newTransaction.setSuccess(true);
                logger.fine("Single state transition generated by event '{}' - creating new transaction '{}'", event.getMessage(), transactionID);

                successfulTransactionsStream.send(newTransaction);
            }
            else {
                // Setup the first node timeout
                TransactionModel newTransaction = model.createTransaction(transactionID, start);
                if (timing != null) {
                    newTransaction.addTiming(stripper.getPatternName(), timing);
                }
                newTransaction.addEvent(event);
                newTransaction.setTimeoutTime(timeProvider.getTime() + start.getTimeout());

                logger.fine("Starting state triggered by event '{}' - creating new transaction '{}' (which will timeout at '{})",
                            event.getMessage(),
                            transactionID,
                            Logger.toDateString(newTransaction.getTimeoutTime()));

            }
        }
    }

    public void checkForTimeouts() {

        long timeNow = timeProvider.getTime();

        List<TransactionModel> currentTransactions = model.getCurrentTransactions();
        synchronized (currentTransactions) {
            Iterator<TransactionModel> iterator = currentTransactions.iterator();
            while (iterator.hasNext()) {
                TransactionModel transactionModel = (TransactionModel) iterator.next();

                long timeoutTime = transactionModel.getTimeoutTime();

                if (timeNow >= timeoutTime) {
                    logger.fine("Timing out transaction '{}' - time is now '{}' and timeout time was '{}'",
                                transactionModel,
                                Logger.toDateString(timeNow),
                                Logger.toDateString(timeoutTime));
                    transactionModel.setSuccess(false);

                    failedTransactionsStream.onNewItem(transactionModel);
                    iterator.remove();
                }
            }
        }
    }

    public Stream<TransactionModel> getSuccessfulTransactionsStream() {
        return successfulTransactionsStream;
    }

    public Stream<TransactionModel> getFailedTransactionsStream() {
        return failedTransactionsStream;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    @Override public void send(LogEvent t) {
        onNewLogEvent(t);
    }

    public static void main(String[] args) {
        LoggingContainer.createContainer("state.engine.configuration.xml");
    }

}
