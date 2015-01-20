package com.logginghub.logging.frontend.analysis;

import java.io.File;

import org.jfree.chart.JFreeChart;

public interface ChartProvider
{
    JFreeChart getChart();

    void clearChartData();

    void saveChartImage(File folder);

    void saveChartData(File folder);
}
