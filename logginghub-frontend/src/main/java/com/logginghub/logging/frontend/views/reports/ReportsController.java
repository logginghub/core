package com.logginghub.logging.frontend.views.reports;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.frontend.modules.EnvironmentMessagingService;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.ReportExecuteRequest;
import com.logginghub.logging.messages.ReportExecuteResponse;
import com.logginghub.logging.messages.ReportListRequest;
import com.logginghub.logging.messages.ReportListResponse;
import com.logginghub.utils.Destination;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.sof.SerialisableObject;

/**
 * Created by james on 04/02/15.
 */
public class ReportsController {
    private static final Logger logger = Logger.getLoggerFor(ReportsController.class);

    private final ReportsModel model;
    private final EnvironmentMessagingService messagingService;
    private Binder2 binder = new Binder2();

    public ReportsController(final ReportsModel model, final EnvironmentMessagingService messagingService) {
        this.model = model;
        this.messagingService = messagingService;

        final Destination<ChannelMessage> destination = new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage message) {
                logger.info("Received private channel message '{}'", message);
                final SerialisableObject payload = message.getPayload();

                if (payload instanceof ReportListResponse) {
                    final ReportListResponse response = (ReportListResponse) payload;
                    model.getReportDetails().clear();
                    model.getReportDetails().addAll(response.getReportDetails());
                } else if (payload instanceof ReportExecuteResponse) {
                    final ReportExecuteResponse response = (ReportExecuteResponse) payload;
                    model.getResponses().add(response);
                }
            }
        };

        messagingService.subscribeToPrivateChannel(destination);

        binder.addUnbinder(new Runnable() {
            @Override public void run() {
                messagingService.unsubscribeFromPrivateChannel(destination);
            }
        });
    }

    public ReportsModel getModel() {
        return model;
    }

    public void requestReportList() {
        ReportListRequest request = new ReportListRequest();
        request.setRespondToChannel(messagingService.getConnectionID());
        ChannelMessage message = new ChannelMessage(Channels.reportListRequests, request);

        try {
            messagingService.send(message);
        } catch (LoggingMessageSenderException e) {
            logger.warn(e, "Failed to send report list request");
        }
    }

    public void requestReport(String name) {

        logger.info("Requesting report '{}'", name);

        model.getResponses().clear();

        ReportExecuteRequest request = new ReportExecuteRequest();
        request.setRespondToChannel(messagingService.getConnectionID());
        request.setReportName(name);

        ChannelMessage message = new ChannelMessage(Channels.reportExecuteRequests, request);

        messagingService.send(message, new Destination<LoggingMessage>() {
            @Override public void send(LoggingMessage loggingMessage) {
                ReportListResponse response = (ReportListResponse) loggingMessage;
                model.getReportDetails().clear();
                model.getReportDetails().addAll(response.getReportDetails());
            }
        });

    }

    public void unbind() {
        binder.unbind();
    }
}
