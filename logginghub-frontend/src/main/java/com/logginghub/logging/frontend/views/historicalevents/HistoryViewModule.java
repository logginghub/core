package com.logginghub.logging.frontend.views.historicalevents;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.binary.ImportController;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.modules.EnvironmentMessagingService;
import com.logginghub.logging.frontend.modules.configuration.HistoryViewConfiguration;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTablePanel;
import com.logginghub.logging.frontend.views.logeventdetail.time.TimeController;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexResponse;
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
import net.miginfocom.swing.MigLayout;

import javax.swing.*;


public class HistoryViewModule implements Module<HistoryViewConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(HistoryViewModule.class);
    private DetailedLogEventTablePanel detailedLogEventTablePanel;
    //    private EnvironmentMessagingService messagingService;
    private ImportController importController;
    private EnvironmentModel environmentModel;
    //    private EnvironmentNotificationService environmentNotificationService;
    private TimeProvider timeProvider = new SystemTimeProvider();

    private String name;
    private String layout;
    private LayoutService layoutService;

//    private TimeSelectionView timeSelectionView = new TimeSelectionView();
    private final JPanel contentPanel;

    //    private TimeSelectionModel timeSelectionModel =new TimeSelectionModel();

    private HistoricalSearchModel historicalSearchModel;
    private HistoricalSearchController historicalSearchController;
    private HistoricalSearchControlsView historicalSearchControlsView = new HistoricalSearchControlsView();

    public HistoryViewModule(EnvironmentMessagingService messagingService) {
        JMenuBar menuBar = new JMenuBar();
        String propertiesName = "realtimeView";

        historicalSearchModel = new HistoricalSearchModel();
        long now = timeProvider.getTime();
        historicalSearchModel.getTimeSelectionModel().getEndTime().getTime().set(now);
        historicalSearchModel.getTimeSelectionModel().getStartTime().getTime().set(TimeUtils.before(now, "1 day"));

        historicalSearchController = new HistoricalSearchController(messagingService, historicalSearchModel);
        historicalSearchControlsView.bind(historicalSearchController);

        contentPanel = new JPanel(new MigLayout("", "[fill, grow]", "[fill][fill][fill, grow]"));

//        timeSelectionView.bind(historicalSearchModel.getTimeSelectionModel());

//        contentPanel.add(timeSelectionView, "cell 0 0");
        contentPanel.add(historicalSearchControlsView, "cell 0 0");

//        timeSelectionView.setBorder(BorderFactory.createTitledBorder("Choose a time period"));
        historicalSearchControlsView.setBorder(BorderFactory.createTitledBorder("Commands"));

        // jshaw - due to convoluted legacy issues, the events take a detour through the table panel on their way through
        // to the actual event controller in the model.
        detailedLogEventTablePanel = new DetailedLogEventTablePanel(menuBar,
                propertiesName,
                historicalSearchModel.getEvents(),
                timeProvider,
                false);

        detailedLogEventTablePanel.setAutoScroll(false);

        historicalSearchModel.getLogEventStream().addDestination(new Destination<LogEvent>() {
            @Override public void send(LogEvent logEvent) {
                detailedLogEventTablePanel.onNewLogEvent(logEvent);
            }
        });

        contentPanel.add(detailedLogEventTablePanel, "cell 0 2");
        detailedLogEventTablePanel.setBorder(BorderFactory.createTitledBorder("Results"));

        environmentModel = new EnvironmentModel();
        detailedLogEventTablePanel.bind(environmentModel);
        environmentModel.addLogEventListener(detailedLogEventTablePanel);

        timeController = detailedLogEventTablePanel.getTimeFilterController();
        importController = new ImportController(timeController);
        detailedLogEventTablePanel.bind(importController);

        LogEventBlockElement t = new LogEventBlockElement(0, Long.MIN_VALUE, Long.MAX_VALUE);
        t.setDataProvider(new LogEventBlockDataProvider() {
            @Override public void provideData(long start, long end, StreamListener<LogEvent> destination) {
                //                historicalSearchController.requestStreamingData(detailedLogEventTablePanel.getLevelFilter(), detailedLogEventTablePanel.getFirstQuickFilter()
                //                                                                                                                        .getQuickFilterTextField()
                //                                                                                                                        .getText(), destination);
            }
        });
        importController.addBlock(t);
    }

    public void setName(String name) {
        this.name = name;
    }

    //    @Inject public void setMessagingService(EnvironmentMessagingService messagingService) {
    //        this.messagingService = messagingService;
    //    }

    //    @Inject public void setEnvironmentNotificationService(EnvironmentNotificationService environmentNotificationService) {
    //        this.environmentNotificationService = environmentNotificationService;
    //    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Override public void configure(HistoryViewConfiguration configuration, ServiceDiscovery discovery) {
        layoutService = discovery.findService(LayoutService.class);
        //        messagingService = discovery.findService(EnvironmentMessagingService.class, configuration.getEnvironmentRef());
        //        environmentNotificationService = discovery.findService(EnvironmentNotificationService.class, configuration.getEnvironmentRef());
    }

    public void initialise() {
        detailedLogEventTablePanel.setName(name);
        layoutService.add(contentPanel, layout);
    }

    protected void sendHistoryIndexRequest(final long from, final long to) {

        logger.info("Sending history index request");

        //        messagingService.sendStreaming(new HistoricalIndexRequest(from, to), new Destination<LoggingMessage>() {
        //            @Override public void send(LoggingMessage t) {
        //                processIndex((HistoricalIndexResponse) t);
        //            }
        //        });
    }


    protected void processIndex(HistoricalIndexResponse t) {

        HistoricalIndexElement[] elements = t.getElements();
        int count = elements.length;
        if (count > 0) {
            logger.fine("Historical index response received : {} elements from '{}' to '{}'",
                    count,
                    Logger.toDateString(elements[0].getTime()),
                    Logger.toDateString(elements[count - 1].getTime()));
        } else {
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

        //        environmentNotificationService.addListener(new EnvironmentNotificationListener() {
        //            @Override public void onHubConnectionEstablished(HubConfiguration hubConfiguration) {}
        //
        //            @Override public void onHubConnectionLost(HubConfiguration hubConfiguration) {}
        //
        //            @Override public void onEnvironmentConnectionLost() {}
        //
        //            @Override public void onEnvironmentConnectionEstablished() {
        //                sendInitialRequests();
        //            }
        //
        //            @Override public void onTotalEnvironmentConnectionEstablished() {}
        //
        //            @Override public void onTotalEnvironmentConnectionLost() {}
        //        });
        //
        //        if (environmentNotificationService.isEnvironmentConnectionEstablished()) {
        //            sendInitialRequests();
        //        }

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
        //        messagingService.subscribe(Channels.historyUpdates, new Destination<ChannelMessage>() {
        //            @Override public void send(ChannelMessage t) {
        //                HistoricalIndexResponse element = (HistoricalIndexResponse) t.getPayload();
        //                TimeController controller = detailedLogEventTablePanel.getTimeFilterController();
        //                controller.processUpdate(element);
        //            }
        //        });

        logger.info("Environment connection established, sending index request");

        final long viewStart = timeController.getModel().getViewStart().longValue();
        final long viewEnd = detailedLogEventTablePanel.getTimeView().getViewEndTime();

        sendHistoryIndexRequest(viewStart, viewEnd);
    }

    @Override public void stop() {}

}
