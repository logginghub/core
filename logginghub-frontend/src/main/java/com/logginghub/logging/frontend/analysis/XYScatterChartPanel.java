package com.logginghub.logging.frontend.analysis;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

public class XYScatterChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private XYScatterChart chart;
    
    public XYScatterChartPanel() {
        chart = new XYScatterChart();
        ChartPanel chartpanel = chart.getChartpanel();
        chartpanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        chartpanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        add(chartpanel);
    }
    
    public XYScatterChart getChart() {
        return chart;
    }
    
}
