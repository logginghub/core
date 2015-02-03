package com.logginghub.logging.frontend.views.historicalevents;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.modules.EnvironmentMessagingService;
import com.logginghub.logging.messages.HistoricalDataJobKillRequest;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.utils.Destination;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

import javax.swing.*;

/**
 * Created by james on 29/01/15.
 */
public class HistoricalSearchController {
    private static final Logger logger = Logger.getLoggerFor(HistoricalSearchController.class);

    private final EnvironmentMessagingService messagingService;

    private final HistoricalSearchModel historicalSearchModel;

    private Filter<LogEvent> filter = new Filter<LogEvent>() {
        @Override public boolean passes(LogEvent logEvent) {
            return true;
        }
    };
    private int jobNumber;

    public HistoricalSearchController(EnvironmentMessagingService messagingService,
                                      HistoricalSearchModel historicalSearchModel) {
        this.messagingService = messagingService;
        this.historicalSearchModel = historicalSearchModel;
    }

    public HistoricalSearchModel getHistoricalSearchModel() {
        return historicalSearchModel;
    }

    public void startSearch() {

        logger.fine("Requesting streaming data");

        HistoricalDataRequest request = new HistoricalDataRequest();
        request.setStart(historicalSearchModel.getTimeSelectionModel().getStartTime().getTime().get());
        request.setEnd(historicalSearchModel.getTimeSelectionModel().getEndTime().getTime().get());
        request.setLevelFilter(historicalSearchModel.getLevelFilter().get());
        request.setQuickfilter(historicalSearchModel.getKeywordFilter().get());
        request.setMostRecentFirst(true);

        historicalSearchModel.getSearchInProgress().set(true);

        messagingService.sendStreaming(request, new Destination<LoggingMessage>() {
            @Override public void send(LoggingMessage t) {
                HistoricalDataResponse response = (HistoricalDataResponse) t;
                jobNumber = response.getJobNumber();
                logger.fine("Streaming event data received : {} events", response.getEvents().length);
                final DefaultLogEvent[] events = response.getEvents();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        for (DefaultLogEvent defaultLogEvent : events) {
                            if (defaultLogEvent != null) {
                                historicalSearchModel.getLogEventStream().send(defaultLogEvent);
                            } else {
                                logger.warn("Null log event returned");
                            }
                        }
                    }
                });

                if (response.isLastBatch()) {
                    historicalSearchModel.getSearchInProgress().set(false);
                }
            }
        });
    }


    public void stopSearch() {
        messagingService.send(new HistoricalDataJobKillRequest(jobNumber), new Destination<LoggingMessage>() {
            @Override public void send(LoggingMessage loggingMessage) {
                logger.info("Job kill request has been acknowledged");
                historicalSearchModel.getSearchInProgress().set(false);
            }
        });
    }
}
