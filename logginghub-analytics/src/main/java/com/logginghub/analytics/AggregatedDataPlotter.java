package com.logginghub.analytics;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;

import com.logginghub.analytics.charting.DifferencesChartPanel;
import com.logginghub.analytics.charting.XYTimeChartPanel;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.AggregatedDataPoint;
import com.logginghub.utils.swing.TestFrame;


public class AggregatedDataPlotter {

    private int imageWidth = 1024;
    private int imageHeight = 768;

    public void plot(AggregatedData data, AggregatedDataKey... keys) {

        XYTimeChartPanel panel = new XYTimeChartPanel();
        
        for (AggregatedDataPoint aggregatedDataPoint : data) {
            for (AggregatedDataKey key : keys) {
                double value = aggregatedDataPoint.getValue(key);
                panel.addValue(key.name(), aggregatedDataPoint.getStartTime(), value);    
            }            
        }

        TestFrame.show(panel);
        
    }
    
    public void plot(File folder, String title, AggregatedData data, AggregatedDataKey... standard) {
        XYTimeChartPanel panel = new XYTimeChartPanel();
        
        
        for (AggregatedDataPoint aggregatedDataPoint : data) {
            for (AggregatedDataKey key : standard) {
                double value = aggregatedDataPoint.getValue(key);
                panel.addValue(key.name(), aggregatedDataPoint.getStartTime(), value);    
            }            
        }
        
        panel.setSize(imageWidth, imageHeight);
        
        File file = new File(folder, title + ".png");
        try {
            ChartUtilities.saveChartAsPNG(file, panel.getChart(), imageWidth, imageHeight);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to save chart to file '" + file.getAbsolutePath() + "'",e);
        }

    }
    
    public void plotDifferences(File folder, String title, AggregatedData data, AggregatedDataKey low, AggregatedDataKey high, AggregatedDataKey... standard) {

        DifferencesChartPanel panel = new DifferencesChartPanel();
        
        panel.addDifferencesSeries(data, low, high);
        
        for (AggregatedDataPoint aggregatedDataPoint : data) {
            for (AggregatedDataKey key : standard) {
                double value = aggregatedDataPoint.getValue(key);
                panel.addValue(key.name(), aggregatedDataPoint.getStartTime(), value);    
            }            
        }
        
        panel.setSize(imageWidth, imageHeight);
        
        File file = new File(folder, title + ".png");
        try {
            ChartUtilities.saveChartAsPNG(file, panel.getChart(), imageWidth, imageHeight);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to save chart to file '" + file.getAbsolutePath() + "'",e);
        }

    }
    
    public void plotDifferences(AggregatedData data, AggregatedDataKey low, AggregatedDataKey high, AggregatedDataKey... standard) {

        DifferencesChartPanel panel = new DifferencesChartPanel();
        
        panel.addDifferencesSeries(data, low, high);
        
        for (AggregatedDataPoint aggregatedDataPoint : data) {
            for (AggregatedDataKey key : standard) {
                double value = aggregatedDataPoint.getValue(key);
                panel.addValue(key.name(), aggregatedDataPoint.getStartTime(), value);    
            }            
        }

        TestFrame.show(panel);
        
    }


}
