package com.logginghub.logging.frontend.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @deprecated Should be using the new charting configurations now
 * @author James
 *
 */
@XmlAccessorType(XmlAccessType.FIELD) public class ChartConfiguration {
    @XmlElement(name = "matcher") private List<MatcherConfiguration> matchers = new ArrayList<MatcherConfiguration>();
    @XmlAttribute private String title = "<no title set>";
    @XmlAttribute private String type;
    @XmlAttribute private String yLabel = "yLabel";
    @XmlAttribute private String xLabel = "xLabel";
    @XmlAttribute private String onlyShowValuesAbove;

    @XmlAttribute private boolean showLegend = true;
    @XmlAttribute private boolean forceYZero = true;
    @XmlAttribute private boolean sideLegend = false;

    @XmlAttribute private int dataPoints = 60 * 5;

    @XmlAttribute private double warningThreshold = Double.NaN;
    @XmlAttribute private double severeThreshold = Double.NaN;
    @XmlAttribute private double yAxisLock = Double.NaN;

    // Histogram entries (should this be a different class?)
    @XmlAttribute private float minimumBucket = 0f;
    @XmlAttribute private float maximumBucket = 100f;
    @XmlAttribute private int granularity = 0;
    @XmlAttribute private long timeLimit = 0;
    @XmlAttribute private boolean realtimeUpdate = false;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<MatcherConfiguration> getMatchers() {
        return matchers;
    }

    public String getTitle() {
        return title;
    }

    public boolean getShowLegend() {
        return showLegend;
    }

    public void setMatchers(List<MatcherConfiguration> matchers) {
        this.matchers = matchers;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getyLabel() {
        return yLabel;
    }

    public void setyLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public String getxLabel() {
        return xLabel;
    }

    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public String getOnlyShowValuesAbove() {
        return onlyShowValuesAbove;
    }

    public void setOnlyShowValuesAbove(String onlyShowValuesAbove) {
        this.onlyShowValuesAbove = onlyShowValuesAbove;
    }

    public boolean isForceYZero() {
        return forceYZero;
    }

    public void setForceYZero(boolean forceYZero) {
        this.forceYZero = forceYZero;
    }

    public boolean isSideLegend() {
        return sideLegend;
    }

    public void setSideLegend(boolean sideLegend) {
        this.sideLegend = sideLegend;
    }

    public float getMinimumBucket() {
        return minimumBucket;
    }

    public void setMinimumBucket(float minimumBucket) {
        this.minimumBucket = minimumBucket;
    }

    public float getMaximumBucket() {
        return maximumBucket;
    }

    public void setMaximumBucket(float maximumBucket) {
        this.maximumBucket = maximumBucket;
    }

    public int getGranularity() {
        return granularity;
    }

    public void setGranularity(int granularity) {
        this.granularity = granularity;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isRealtimeUpdate() {
        return realtimeUpdate;
    }

    public void setRealtimeUpdate(boolean realtimeUpdate) {
        this.realtimeUpdate = realtimeUpdate;
    }

    public int getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(int dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public void setSevereThreshold(double severeThreshold) {
        this.severeThreshold = severeThreshold;
    }

    public void setyAxisLock(double yAxisLock) {
        this.yAxisLock = yAxisLock;
    }

    public double getSevereThreshold() {
        return severeThreshold;
    }

    public double getWarningThreshold() {
        return warningThreshold;
    }

    public double getyAxisLock() {
        return yAxisLock;
    }

}
