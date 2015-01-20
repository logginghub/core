package com.logginghub.logging.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.logginghub.logging.modules.PatterniserModule;
import com.logginghub.utils.module.Configures;

@XmlRootElement @Configures(PatterniserModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class PatterniserConfiguration {

    @XmlAttribute private boolean matchAgainstAllPatterns = false;
    @XmlElement private List<PatternConfiguration> pattern = new ArrayList<PatternConfiguration>();
    @XmlAttribute private String logEventSourceRef;
    @XmlAttribute private boolean useQueue = true;
    @XmlAttribute private int maximumQueueSize = 10000;
    @XmlAttribute private boolean outputStats = false;

    public boolean isOutputStats() {
        return outputStats;
    }

    public void setOutputStats(boolean outputStats) {
        this.outputStats = outputStats;
    }
    
    public boolean isUseQueue() {
        return useQueue;
    }

    public int getMaximumQueueSize() {
        return maximumQueueSize;
    }

    public void setUseQueue(boolean useQueue) {
        this.useQueue = useQueue;
    }

    public void setMaximumQueueSize(int maximumQueueSize) {
        this.maximumQueueSize = maximumQueueSize;
    }

    public boolean isMatchAgainstAllPatterns() {
        return matchAgainstAllPatterns;
    }

    public void setMatchAgainstAllPatterns(boolean matchAgainstAllPatterns) {
        this.matchAgainstAllPatterns = matchAgainstAllPatterns;
    }

    public List<PatternConfiguration> getPatterns() {
        return pattern;
    }

    public String getLogEventSourceRef() {
        return logEventSourceRef;

    }
}
