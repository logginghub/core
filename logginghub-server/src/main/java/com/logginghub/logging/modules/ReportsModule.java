package com.logginghub.logging.modules;

import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.modules.configuration.ReportsConfiguration;
import com.logginghub.logging.servers.ServerMessageHandler;
import com.logginghub.logging.servers.ServerSubscriptionsService;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

/**
 * Created by james on 04/02/15.
 */
public class ReportsModule implements Module<ReportsConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(ReportsModule.class);
    private ReportsConfiguration configuration;
    private ServerSubscriptionsService channelMessagingService;

    //    public ReportsModule(ChannelMessagingService channelMessagingService) {
    //        this.channelMessagingService = channelMessagingService;
    //    }

    public ReportsModule() {

    }

    @Override public void configure(final ReportsConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        channelMessagingService = discovery.findService(ServerSubscriptionsService.class);

        channelMessagingService.subscribe(Channels.reportListRequests, new ServerMessageHandler() {
            @Override public void onMessage(LoggingMessage message, LoggingMessageSender source) {
//                logger.info("Report list request received");
//                ChannelMessage channelMessage = (ChannelMessage) message;
//                ReportListRequest reportListRequest = (ReportListRequest) channelMessage.getPayload();
//                ReportListResponse response = handle(reportListRequest);
//
//                response.setCorrelationID(channelMessage.getCorrelationID());
//                try {
//                    source.send(response);
//                } catch (LoggingMessageSenderException e) {
//                    logger.info(e, "Failed to send report list response");
//                }
            }
        });

        channelMessagingService.subscribe(Channels.reportRunRequests, new ServerMessageHandler() {
            @Override public void onMessage(LoggingMessage message, LoggingMessageSender source) {
//                logger.info("Report run request received");
//                ChannelMessage channelMessage = (ChannelMessage) message;
//                ReportRunRequest reportRunRequest = (ReportRunRequest) channelMessage.getPayload();
//
//                final String reportName = reportRunRequest.getReportName();
//                for (ReportsConfiguration.ReportConfiguration reportConfiguration : configuration.getReports()) {
//                    if (reportConfiguration.getName().equals(reportName)) {
//                        runReport(reportConfiguration);
//                    }
//                }

                //                ReportListResponse response = handle(reportListRequest);
                //
                //                response.setCorrelationID(channelMessage.getCorrelationID());
                //                try {
                //                    source.send(response);
                //                } catch (LoggingMessageSenderException e) {
                //                    logger.info(e, "Failed to send report list response");
                //                }
            }
        });
    }

//    private void runReport(ReportsConfiguration.ReportConfiguration reportConfiguration) {
//
//        // Broadcast the message out to the world
//        ReportExecuteRequest request = new ReportExecuteRequest();
//        request.setReportName(reportConfiguration.getName());
//        ChannelMessage message = new ChannelMessage(Channels.reportExecuteRequests, request);
//
//        try {
//            channelMessagingService.send(message);
//        } catch (LoggingMessageSenderException e) {
//            logger.info(e, "Failed to sent report execute message");
//        }
//
//    }

//    private ReportListResponse handle(ReportListRequest reportListRequest) {
//        ReportListResponse response = new ReportListResponse();
//
//        for (ReportsConfiguration.ReportConfiguration reportConfiguration : configuration.getReports()) {
//
//            ReportDetails reportDetails = new ReportDetails();
//            reportDetails.setName(reportConfiguration.getName());
//
//            StringBuilder command = new StringBuilder();
//            String div = " ";
//            command.append(reportConfiguration.getCommand());
//            for (ReportsConfiguration.ArgumentConfiguration argumentConfiguration : reportConfiguration.getArgument()) {
//                command.append(div);
//                command.append(argumentConfiguration.getValue());
//            }
//
//            reportDetails.setCommand(command.toString());
//            response.getReportDetails().add(reportDetails);
//
//        }
//
//        return response;
//    }

    @Override public void start() {

    }

    @Override public void stop() {

    }
}
