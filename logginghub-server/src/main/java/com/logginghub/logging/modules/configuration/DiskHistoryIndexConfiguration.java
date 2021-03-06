package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.DiskHistoryIndexModule;
import com.logginghub.utils.module.Configures;

@Configures(DiskHistoryIndexModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class DiskHistoryIndexConfiguration {
    
    @XmlAttribute private int streamingBatchSize = 200;
    @XmlAttribute private String logEventSourceRef;
    @XmlAttribute private String folder = "binarylogs/index";
    @XmlAttribute private boolean readOnly = false;

    @XmlAttribute private String totalFileSizeLimit = "1 GB";
    @XmlAttribute private String fileSizeLimit = "128 MB";
    @XmlAttribute private String blockSize = "10 MB";
    @XmlAttribute private String maximumFlushInterval = "5 seconds";
    @XmlAttribute private boolean triggerFromEventTimes = false;

    public String getBlockSize() {
        return blockSize;
    }

    public String getFileSizeLimit() {
        return fileSizeLimit;
    }

    public void setFileSizeLimit(String fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;
    }

    public void setBlockSize(String blockSize) {
        this.blockSize = blockSize;
    }

    public void setTotalFileSizeLimit(String totalFileSizeLimit) {
        this.totalFileSizeLimit = totalFileSizeLimit;
    }

    public String getTotalFileSizeLimit() {
        return totalFileSizeLimit;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getLogEventSourceRef() {
        return logEventSourceRef;
    }

    public void setLogEventSourceRef(String logEventSourceRef) {
        this.logEventSourceRef = logEventSourceRef;
    }

    public void setMaximumFlushInterval(String maximumFlushInterval) {
        this.maximumFlushInterval = maximumFlushInterval;
    }

    public String getMaximumFlushInterval() {
        return maximumFlushInterval;
    }

    public void setTriggerFromEventTimes(boolean triggerFromEventTimes) {
        this.triggerFromEventTimes = triggerFromEventTimes;
    }

    public boolean isTriggerFromEventTimes() {
        return triggerFromEventTimes;
    }
    
    public int getStreamingBatchSize() {
        return streamingBatchSize;
    }
    
    public void setStreamingBatchSize(int streamingBatchSize) {
        this.streamingBatchSize = streamingBatchSize;
    }
}
