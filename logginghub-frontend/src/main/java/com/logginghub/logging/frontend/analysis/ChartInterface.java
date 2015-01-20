package com.logginghub.logging.frontend.analysis;

import java.io.File;

import org.jfree.chart.JFreeChart;

public interface ChartInterface extends ChunkedResultHandler, ComponentProvider {
    void clearChartData();
    void saveChartData(File folder);
    void saveChartImage(File folder);

    void setTitle(String chartTitle);

    JFreeChart getChart();

    void setYMinimumFilter(double yMinimumFilter);

    void setDatapoints(int intValue);

    void addFilter(ChunkedResultFilter filter);

    void setWarningThreshold(double warningThreshold);

    void setSevereThreshold(double severeThreshold);

    void setYAxisLock(double getyAxisLock);
    
    void addMarker(long time, String marker);
//    void tickOver();

}
