package com.logginghub.logging.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.modules.AggregatorModule;
import com.logginghub.utils.module.Configures;

@Configures(AggregatorModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class AggregatorConfiguration {

    @XmlElement List<AggregationConfiguration> aggregation = new ArrayList<AggregationConfiguration>();

    @XmlAttribute boolean bindToSocketHub = true;
    @XmlAttribute String socketHubRef;
    @XmlAttribute String patternisedEventSourceRef;

    @XmlAttribute boolean useEventTimes = false;
    @XmlAttribute private boolean outputStats = false;
    @XmlAttribute String statsInterval = "1000";

    public boolean isUseEventTimes() {
        return useEventTimes;
    }
    
    public void setUseEventTimes(boolean useEventTimes) {
        this.useEventTimes = useEventTimes;
    }

    public List<AggregationConfiguration> getAggregation() {
        return aggregation;
    }

    public void setAggregation(List<AggregationConfiguration> aggregation) {
        this.aggregation = aggregation;
    }

    public boolean isOutputStats() {
        return outputStats;
    }

    public void setOutputStats(boolean outputStats) {
        this.outputStats = outputStats;
    }

    public String getStatsInterval() {
        return statsInterval;
    }

    public void setStatsInterval(String statsInterval) {
        this.statsInterval = statsInterval;
    }

    public void setBindToSocketHub(boolean bindToSocketHub) {
        this.bindToSocketHub = bindToSocketHub;
    }

    public void setSocketHubRef(String socketHubRef) {
        this.socketHubRef = socketHubRef;
    }

    public String getSocketHubRef() {
        return socketHubRef;
    }

    public boolean isBindToSocketHub() {
        return bindToSocketHub;
    }

    public List<AggregationConfiguration> getAggregations() {
        return aggregation;
    }

    public String getPatternisedEventSourceRef() {
        return patternisedEventSourceRef;
    }

    public void setPatternisedEventSourceRef(String patternisedEventSourceRef) {
        this.patternisedEventSourceRef = patternisedEventSourceRef;
    }

}
