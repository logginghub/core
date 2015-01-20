package com.logginghub.logging.frontend.charting.historical;

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;

import com.logginghub.logging.frontend.modules.BorderFrameModule;
import com.logginghub.utils.TimeUtils;

public class Tester {

    public static void main(String[] args) {
                
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {                
                BorderFrameModule frameModule = new BorderFrameModule("Chart history tester");
                ScrollbarHistoricalChartView scrollbarView = new ScrollbarHistoricalChartView();
                HistoricalChartView chartView = new HistoricalChartView();
                
                HistoricalChartModel model = new HistoricalChartModel();
                model.getStartTime().set(TimeUtils.chunk(System.currentTimeMillis(), TimeUtils.hours));
                HistoricalChartController controller = new HistoricalChartController(model);
                
                scrollbarView.bind(controller, model);
                chartView.bind(controller, model);

                frameModule.add(chartView, BorderLayout.CENTER);
                frameModule.add(scrollbarView, BorderLayout.SOUTH);
            }
        });       
        
    }
}
