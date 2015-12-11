package com.logginghub.logging.modules.configuration;

import com.logginghub.logging.modules.InMemoryHistoryModule;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.module.Configures;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Configures(InMemoryHistoryModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class InMemoryHistoryConfiguration {
    
    @XmlAttribute private int streamingBatchSize = 200;
    @XmlAttribute private String logEventSourceRef;
    
    @XmlAttribute private String blockSize = calculateDefaultBlockSize();
    @XmlAttribute private String maximumSize = calculateDefaultMaximumSize();
    
    @XmlAttribute private boolean disableSafetyChecks = false;
    @XmlAttribute private String indexBatcherTimeout = "1 second";
    @XmlAttribute private boolean allowClearEvents = false;

    public String getLogEventSourceRef() {
        return logEventSourceRef;
    }

    private String calculateDefaultMaximumSize() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long dataSize = maxMemory / 4;
        return ByteUtils.format(dataSize);
    }

    private String calculateDefaultBlockSize() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long dataSize = maxMemory / 4 / 10;
        return ByteUtils.format(dataSize);
    }

    public boolean isAllowClearEvents() {
        return allowClearEvents;
    }

    public void setLogEventSourceRef(String logEventSourceRef) {
        this.logEventSourceRef = logEventSourceRef;
    }

    public String getBlockSize() {
        return blockSize;
    }
    
    public void setBlockSize(String blockSize) {
        this.blockSize = blockSize;
    }

    
    public void setMaximumSize(String maximumSize) {
        this.maximumSize = maximumSize;
    }
    
    public String getMaximumSize() {
        return maximumSize;
    }

    public boolean isDisableSafetyChecks() {
        return disableSafetyChecks;
    }
    
    public void setDisableSafetyChecks(boolean disableSafetyChecks) {
        this.disableSafetyChecks = disableSafetyChecks;
    }
    
    public int getStreamingBatchSize() {
        return streamingBatchSize;
    }
    
    public void setStreamingBatchSize(int streamingBatchSize) {
        this.streamingBatchSize = streamingBatchSize;
    }

    public String getIndexBatcherTimeout() {
        return indexBatcherTimeout;
    }
}
