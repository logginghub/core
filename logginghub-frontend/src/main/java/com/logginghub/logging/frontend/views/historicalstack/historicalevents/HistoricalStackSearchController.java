package com.logginghub.logging.frontend.views.historicalstack.historicalevents;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.modules.EnvironmentMessagingService;
import com.logginghub.logging.messages.HistoricalStackDataJobKillRequest;
import com.logginghub.logging.messages.HistoricalStackDataRequest;
import com.logginghub.logging.messages.HistoricalStackDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

import javax.swing.SwingUtilities;

/**
 * Created by james on 29/01/15.
 */
public class HistoricalStackSearchController {
    private static final Logger logger = Logger.getLoggerFor(HistoricalStackSearchController.class);

    private final EnvironmentMessagingService messagingService;

    private final HistoricalStackSearchModel historicalSearchModel;

    private Filter<LogEvent> filter = new Filter<LogEvent>() {
        @Override public boolean passes(LogEvent logEvent) {
            return true;
        }
    };
    private int jobNumber;

    public HistoricalStackSearchController(EnvironmentMessagingService messagingService, HistoricalStackSearchModel historicalSearchModel) {
        this.messagingService = messagingService;
        this.historicalSearchModel = historicalSearchModel;
    }

    public HistoricalStackSearchModel getHistoricalSearchModel() {
        return historicalSearchModel;
    }

    public void startSearch() {

        logger.fine("Requesting streaming data");

        HistoricalStackDataRequest request = new HistoricalStackDataRequest();
        request.setStart(historicalSearchModel.getTimeSelectionModel().getStartTime().getTime().get());
        request.setEnd(historicalSearchModel.getTimeSelectionModel().getEndTime().getTime().get());
        request.setLevelFilter(historicalSearchModel.getLevelFilter().get());
        request.setQuickfilter(historicalSearchModel.getKeywordFilter().get());
        request.setMostRecentFirst(true);

        historicalSearchModel.getSearchKilled().set(false);
        historicalSearchModel.getSearchInProgress().set(true);

        messagingService.sendStreaming(request, new Destination<LoggingMessage>() {
            @Override public void send(LoggingMessage t) {
                HistoricalStackDataResponse response = (HistoricalStackDataResponse) t;
                jobNumber = response.getJobNumber();
                logger.info("Streaming event data received : {} snapshots", response.getSnapshots().length);
                final StackSnapshot[] snapshots = response.getSnapshots();

                if (!historicalSearchModel.getSearchKilled().get()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            for (StackSnapshot snapshot : snapshots) {
                                if (snapshot != null) {
                                    historicalSearchModel.getResultsStream().send(snapshot);
                                } else {
                                    logger.warn("Null log event returned");
                                }
                            }
                        }
                    });
                    ThreadUtils.sleep(100);
                }

                if (response.isLastBatch()) {
                    historicalSearchModel.getSearchInProgress().set(false);
                }
            }
        });
    }


    public void stopSearch() {
        historicalSearchModel.getSearchKilled().set(true);

        messagingService.send(new HistoricalStackDataJobKillRequest(jobNumber), new Destination<LoggingMessage>() {
            @Override public void send(LoggingMessage loggingMessage) {
                logger.info("Job kill request has been acknowledged");
                historicalSearchModel.getSearchInProgress().set(false);
            }
        });
    }
}
