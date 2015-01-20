package com.logginghub.logging.frontend.modules;

import javax.swing.JMenuBar;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.binary.ImportController;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.frontend.modules.configuration.HistoryViewConfiguration;
import com.logginghub.logging.frontend.services.EnvironmentNotificationService;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.views.detail.DetailedLogEventTablePanel;
import com.logginghub.logging.frontend.views.detail.time.TimeController;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexRequest;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.logging.utils.LogEventBlockDataProvider;
import com.logginghub.logging.utils.LogEventBlockElement;
import com.logginghub.utils.Destination;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Inject;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;


public class HistoryViewModule implements Module<HistoryViewConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(HistoryViewModule.class);
    private DetailedLogEventTablePanel detailedLogEventTablePanel;
    private EnvironmentMessagingService messagingService;
    private ImportController importController;
    private EnvironmentModel environmentModel;
    private EnvironmentNotificationService environmentNotificationService;
    private TimeProvider timeProvider = new SystemTimeProvider();

    private String name;
    private String layout;
    private LayoutService layoutService;

    public HistoryViewModule() {
        JMenuBar menuBar = new JMenuBar();
        String propertiesName = "realtimeView";
        LogEventContainerController eventController = new LogEventContainerController();

        detailedLogEventTablePanel = new DetailedLogEventTablePanel(menuBar, propertiesName, eventController, timeProvider, false);

        environmentModel = new EnvironmentModel();
        detailedLogEventTablePanel.bind(environmentModel);
        environmentModel.addLogEventListener(detailedLogEventTablePanel);

        timeController = detailedLogEventTablePanel.getTimeFilterController();
        importController = new ImportController(timeController);
        detailedLogEventTablePanel.bind(importController);

        LogEventBlockElement t = new LogEventBlockElement(0, Long.MIN_VALUE, Long.MAX_VALUE);
        t.setDataProvider(new LogEventBlockDataProvider() {
            @Override public void provideData(long start, long end, StreamListener<LogEvent> destination) {
                requestStreamingData(start, end, detailedLogEventTablePanel.getLevelFilter(), detailedLogEventTablePanel.getFirstQuickFilter()
                                                                                                                        .getQuickFilterTextField()
                                                                                                                        .getText(), destination);
            }
        });
        importController.addBlock(t);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Inject public void setMessagingService(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Inject public void setEnvironmentNotificationService(EnvironmentNotificationService environmentNotificationService) {
        this.environmentNotificationService = environmentNotificationService;
    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Override public void configure(HistoryViewConfiguration configuration, ServiceDiscovery discovery) {
        layoutService = discovery.findService(LayoutService.class);
        messagingService = discovery.findService(EnvironmentMessagingService.class, configuration.getEnvironmentRef());
        environmentNotificationService = discovery.findService(EnvironmentNotificationService.class, configuration.getEnvironmentRef());
    }

    public void initialise() {
        detailedLogEventTablePanel.setName(name);
        layoutService.add(detailedLogEventTablePanel, layout);
    }

    protected void sendHistoryIndexRequest(final long from, final long to) {

        logger.info("Sending history index request");
        
        messagingService.sendStreaming(new HistoricalIndexRequest(from, to), new Destination<LoggingMessage>() {
            @Override public void send(LoggingMessage t) {
                processIndex((HistoricalIndexResponse) t);
            }
        });
    }

    protected void requestStreamingData(long start, long end, int levelFilter, String quickFilter, StreamListener<LogEvent> destination) {
        logger.fine("Requesting streaming data");

        HistoricalDataRequest request = new HistoricalDataRequest();
        request.setStart(start);
        request.setEnd(end);
        request.setLevelFilter(levelFilter);
        request.setQuickfilter(quickFilter);

        messagingService.sendStreaming(request, new Destination<LoggingMessage>() {
            @Override public void send(LoggingMessage t) {
                HistoricalDataResponse response = (HistoricalDataResponse) t;
                logger.fine("Streaming event data received : {} events", response.getEvents().length);
                DefaultLogEvent[] events = response.getEvents();
                for (DefaultLogEvent defaultLogEvent : events) {
                    if (defaultLogEvent != null) {
                        environmentModel.onNewLogEvent(defaultLogEvent);
                        // detailedLogEventTablePanel.onNewLogEvent(defaultLogEvent);
                    }
                    else {
                        logger.warn("Null log event returned");
                    }
                }
            }
        });

    }

    protected void processIndex(HistoricalIndexResponse t) {

        HistoricalIndexElement[] elements = t.getElements();
        int count = elements.length;
        if (count > 0) {
            logger.fine("Historical index response received : {} elements from '{}' to '{}'",
                        count,
                        Logger.toDateString(elements[0].getTime()),
                        Logger.toDateString(elements[count - 1].getTime()));
        }
        else {
            logger.fine("Historical index response received : {} elements", count);
        }

        TimeController controller = detailedLogEventTablePanel.getTimeFilterController();
//        TimeModel model = controller.getModel();
        controller.processUpdate(t);
        if (t.isLastBatch()) {
            controller.moveToEarliestTime(TimeUtils.parseInterval("2 minutes"));
        }
    }

    @Override public void start() {

        environmentNotificationService.addListener(new EnvironmentNotificationListener() {
            @Override public void onHubConnectionEstablished(HubConfiguration hubConfiguration) {}

            @Override public void onHubConnectionLost(HubConfiguration hubConfiguration) {}

            @Override public void onEnvironmentConnectionLost() {}

            @Override public void onEnvironmentConnectionEstablished() {
                sendInitialRequests();
            }

            @Override public void onTotalEnvironmentConnectionEstablished() {}

            @Override public void onTotalEnvironmentConnectionLost() {}
        });

        if (environmentNotificationService.isEnvironmentConnectionEstablished()) {
            sendInitialRequests();
        }

    }

    private boolean requestsSent = false;
    private TimeController timeController;

    private void sendInitialRequests() {

        // Make sure we only do this once
        synchronized (this) {
            if (requestsSent == true) {
                return;
            }

            requestsSent = true;
        }

        logger.info("Environment connection established, subscribing to real-time index updates");
        messagingService.subscribe(Channels.historyUpdates, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                HistoricalIndexResponse element = (HistoricalIndexResponse) t.getPayload();
//                Debug.out("Received history index '{}'", Arrays.toString(element.getElements()));
                TimeController controller = detailedLogEventTablePanel.getTimeFilterController();
                controller.processUpdate(element);
            }
        });

        logger.info("Environment connection established, sending index request");
        
        final long viewStart = timeController.getModel().getViewStart().longValue();
        final long viewEnd = detailedLogEventTablePanel.getTimeView().getViewEndTime();
        
        sendHistoryIndexRequest(viewStart, viewEnd);
    }

    @Override public void stop() {}

}
