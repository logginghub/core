package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@SuppressWarnings("restriction") @XmlAccessorType(XmlAccessType.FIELD) public class EventCountingProcessorConfiguration {

    @XmlAttribute private long aggregationPeriod = 1000;

    public long getAggregationPeriod() {
        return aggregationPeriod;
    }

    public void setAggregationPeriod(long aggregationPeriod) {
        this.aggregationPeriod = aggregationPeriod;
    }

}
