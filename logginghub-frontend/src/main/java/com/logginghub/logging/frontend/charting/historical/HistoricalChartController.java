package com.logginghub.logging.frontend.charting.historical;

import java.net.InetSocketAddress;

import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.Destination;
import com.logginghub.utils.StreamingDestination;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.logging.Logger;

public class HistoricalChartController {

    private static final Logger logger = Logger.getLoggerFor(HistoricalChartController.class);
    private HistoricalChartModel model;
    private SocketClient client;

    public HistoricalChartController(HistoricalChartModel model) {
        super();
        this.model = model;

        client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress("vl-ec2", VLPorts.getSocketHubDefaultPort()));
        client.setDebug(true);

        SocketClientManager manager = new SocketClientManager(client);
        manager.start();

    }

    public HistoricalChartModel getModel() {
        return model;
    }

    public void getChartData(long startTime, long duration, final Destination<TimeSeriesDataPoint> destination) {

        logger.info("Making historical data request...");
        client.getHistoricalDataAPI().streamHistoricalAggregatedEvents(startTime,
                                                                       startTime + duration,
                                                                       new StreamingDestination<AggregatedLogEvent>() {
                                                                           @Override public void send(AggregatedLogEvent t) {
                                                                               if (t.getAggregationID() == 1)  { // && t.getSeriesKey().equals("WorkWin7")) {
                                                                                   destination.send(new TimeSeriesDataPoint(t.getTime(), t.getValue()));
                                                                               }
                                                                           }

                                                                           @Override public void onStreamComplete() {}
                                                                       });

    }

}
