package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.frontend.modules.PatterniserModule;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.swing.TestFrame;

import java.net.InetSocketAddress;

/**
 * Created by james on 02/02/15.
 */
public class GCMonitor {
    public static void main(String[] args) {

        ChartDetailsModel chartDetailsModel = new ChartDetailsModel();
        chartDetailsModel.getDuration().set(TimeUtils.parseInterval("10 seconds"));

        XYChartDataModel dataModel1 = new XYChartDataModel();
        XYChartDataModel dataModel2 = new XYChartDataModel();

        DualChart chart = new DualChart();

        chart.bind(chartDetailsModel, dataModel1, dataModel2);

        TestFrame.show(chart);

        chartDetailsModel.getTitle().set("My fancy chart");
        chartDetailsModel.getSubtitle().set("And its fancy and data bound");

        SocketClient client = new SocketClient("Local");
        client.addConnectionPoint(new InetSocketAddress("localhost", 15000));
        client.setAutoSubscribe(true);

        SocketClientManager manager = new SocketClientManager(client);
        manager.start();

        PatterniserModule patterniserModule = new PatterniserModule(client);
        patterniserModule.addPattern(new Pattern(0, "GC", "GC pause {time} ms collected {size} kb - [what]"));
        patterniserModule.addPattern(new Pattern(1, "Performance", "String reverse (client) complete in {time} ms"));

        PerformanceBinder performanceBinder = new PerformanceBinder();
        performanceBinder.bind(dataModel1, patterniserModule);

        GCOutputBinder gcbinder = new GCOutputBinder();
        gcbinder.bind(dataModel2, patterniserModule);

        TimeUpdater timeUpdater = new TimeUpdater(chartDetailsModel, dataModel1, dataModel2);
        timeUpdater.start();



    }
}
