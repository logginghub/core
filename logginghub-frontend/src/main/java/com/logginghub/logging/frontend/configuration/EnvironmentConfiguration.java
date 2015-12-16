package com.logginghub.logging.frontend.configuration;

import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD) public class EnvironmentConfiguration {

    @XmlAttribute String name = "no name";
    @XmlAttribute boolean openOnStartup = true;
    @XmlElement(name = "hub") private List<HubConfiguration> hubs = new ArrayList<HubConfiguration>();
    @XmlElement(name = "highlighter") private List<HighlighterConfiguration> highlighters = new ArrayList<HighlighterConfiguration>();
    @XmlElement(name = "charting") private ChartingConfiguration chartingConfiguration = new ChartingConfiguration();
    @XmlElement private List<String> quickFilters = new ArrayList<String>();
    @XmlElement private String channel;

    @XmlAttribute private boolean clustered=false;

    @XmlAttribute private double eventMemoryMB = Double.NaN;

    @XmlAttribute private boolean autoLocking;
    @XmlAttribute private boolean writeOutputLog = false;
    
    @XmlAttribute private boolean showHistoryTab = false;

    @XmlAttribute private boolean repoEnabled = false;
    @XmlAttribute private String repoConnectionPoints = "localhost:58780";
    @XmlAttribute private String autoRequestHistory = null;
    @XmlAttribute private boolean disableAutoScrollPauser = false;

    @XmlElement List<FilterConfiguration> filter = new ArrayList<FilterConfiguration>();

    @XmlElement List<ColumnMappingConfiguration> columnMapping = new ArrayList<ColumnMappingConfiguration>();

    @XmlElement private TimestampVariableRollingFileLoggerConfiguration outputLogConfiguration = new TimestampVariableRollingFileLoggerConfiguration();
    private boolean stillUsingdefaultOutputLogConfiuguration = true;

    public EnvironmentConfiguration() {
        setupOutputLogConfiguration();
    }

    public List<ColumnMappingConfiguration> getColumnMappings() {
        return columnMapping;
    }

    public String getAutoRequestHistory() {
        return autoRequestHistory;
    }

    public boolean getDisableAutoScrollPauser() {
        return disableAutoScrollPauser;
    }

    public boolean isDisableAutoScrollPauser() {
        return disableAutoScrollPauser;
    }

    public void setAutoRequestHistory(String autoRequestHistory) {
        this.autoRequestHistory = autoRequestHistory;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public String getChannel() {
        return channel;
    }

    public void setDisableAutoScrollPauser(boolean disableAutoScrollPauser) {
        this.disableAutoScrollPauser = disableAutoScrollPauser;
    }

    private void setupOutputLogConfiguration() {
        // Provide some better defaults for the output log configuration - the
        // defaults are originally for the logging hub
        outputLogConfiguration.setFolder("logs/" + name);
        outputLogConfiguration.setFilename(name);
        outputLogConfiguration.setForceFlush(true);
    }

    public void setOutputLogConfiguration(TimestampVariableRollingFileLoggerConfiguration outputLogConfiguration) {
        this.outputLogConfiguration = outputLogConfiguration;
        stillUsingdefaultOutputLogConfiuguration = false;
    }

    public boolean isWriteOutputLog() {
        return writeOutputLog;
    }

    public TimestampVariableRollingFileLoggerConfiguration getOutputLogConfiguration() {
        return outputLogConfiguration;
    }

    public String getRepoConnectionPoints() {
        return repoConnectionPoints;
    }

    public void setRepoConnectionPoints(String repoConnectionPoints) {
        this.repoConnectionPoints = repoConnectionPoints;
    }

    public ChartingConfiguration getChartingConfiguration() {
        return chartingConfiguration;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isOpenOnStartup() {
        return openOnStartup;
    }

    public List<HubConfiguration> getHubs() {
        return hubs;
    }

    public void setHubs(List<HubConfiguration> hubs) {
        this.hubs = hubs;
    }

    public List<HighlighterConfiguration> getHighlighters() {
        return highlighters;
    }

    public void setHighlighters(List<HighlighterConfiguration> highlighters) {
        this.highlighters = highlighters;
    }

    @Override public String toString() {
        return "EnvironmentConfiguration [name=" + name + ", openOnStartup=" + openOnStartup + ", hubs=" + hubs + ", highlighters=" + highlighters + "]";
    }

    public void setAutoLocking(boolean autoLocking) {
        this.autoLocking = autoLocking;
    }

    public boolean isAutoLocking() {
        return autoLocking;
    }

    public void setRepoEnabled(boolean repoEnabled) {
        this.repoEnabled = repoEnabled;
    }

    public boolean isRepoEnabled() {
        return repoEnabled;
    }

    public double getEventMemoryMB() {
        return eventMemoryMB;
    }

    public void setWriteOutputLog(boolean writeOutputLog) {
        this.writeOutputLog = writeOutputLog;
    }

    public void setupDefaultLogConfiguration() {
        if (stillUsingdefaultOutputLogConfiuguration) {
            setupOutputLogConfiguration();
        }        
    }

    public List<String> getQuickFilters() {
        return quickFilters;
    }
    
    public void setQuickFilters(List<String> quickFilters) {
        this.quickFilters = quickFilters;
    }
    
    public boolean isShowHistoryTab() {
        return showHistoryTab;
    }
    
    public void setShowHistoryTab(boolean showHistoryTab) {
        this.showHistoryTab = showHistoryTab;
    }
    
    public List<FilterConfiguration> getFilters() {
        return filter;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public boolean isClustered() {
        return clustered;
    }
}

