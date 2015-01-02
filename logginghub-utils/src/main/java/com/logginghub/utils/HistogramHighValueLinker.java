package com.logginghub.utils;

import java.util.ArrayList;
import java.util.List;

public class HistogramHighValueLinker {

    private int highValue = 0;

    private List<Histogram> histograms = new ArrayList<Histogram>();

    public void addHistogram(Histogram histogram) {
        histograms.add(histogram);
    }

    public double findHighValue() {
        this.highValue = -1;
        for (Histogram histogram : histograms) {
            highValue = Math.max(highValue, histogram.getHighValue());
        }
        return highValue;
    }

    public double getHighValue() {
        if(highValue == -1) {
            findHighValue();
        }
        return highValue;
    }

    public void onNewValue(int value) {
        highValue = Math.max(value, highValue);
    }

    public void onValueRemoved(int value) {
        if (value == highValue) {
            invalidateHighValue();
        }
    }

    private void invalidateHighValue() {
        highValue = -1;
    }

}