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
    @XmlAttribute private String channel;

    @XmlAttribute private boolean clustered=false;

    @XmlAttribute private double eventMemoryMB = Double.NaN;

    @XmlAttribute private boolean autoLocking;
    @XmlAttribute private boolean writeOutputLog = false;

    @XmlAttribute private boolean showRegexOptionOnQuickFilters = true;
    @XmlAttribute private boolean showHistoryTab = false;

    @XmlAttribute private boolean repoEnabled = false;
    @XmlAttribute private String repoConnectionPoints = "localhost:58780";
    @XmlAttribute private String autoRequestHistory = null;
    @XmlAttribute private boolean disableAutoScrollPauser = false;

    @XmlElement List<FilterConfiguration> filter = new ArrayList<FilterConfiguration>();
    @XmlElement List<ColumnConfiguration> columnSetting = new ArrayList<ColumnConfiguration>();
    @XmlElement List<CustomFilterConfiguration> customFilter = new ArrayList<CustomFilterConfiguration>();
    @XmlElement List<CustomDateFilterConfiguration> customDateFilter = new ArrayList<CustomDateFilterConfiguration>();

    @XmlElement List<NameMappingConfiguration> columnMapping = new ArrayList<NameMappingConfiguration>();
    @XmlElement private List<NameMappingConfiguration> levelMapping = new ArrayList<NameMappingConfiguration>();

    @XmlElement private TimestampVariableRollingFileLoggerConfiguration outputLogConfiguration = new TimestampVariableRollingFileLoggerConfiguration();

    @XmlAttribute private boolean stillUsingdefaultOutputLogConfiuguration = true;
    @XmlAttribute private boolean filterCaseSensitive = false;
    @XmlAttribute private boolean filterUnicode = false;
    @XmlAttribute private boolean disableColumnFile = false;
    @XmlAttribute private String eventDetailsSeparatorLocation = "-1";
    @XmlAttribute private boolean eventDetailsSeparatorHorizontalOrientiation = true;

    @XmlAttribute private boolean showHTMLEventDetails = false;
    @XmlAttribute private boolean showClearEvents = true;
    @XmlAttribute private boolean showTimeControl = true;
    @XmlAttribute private boolean showAddFilter = true;
    @XmlAttribute private boolean showEventDetailSummary;

    public EnvironmentConfiguration() {
        setupOutputLogConfiguration();
    }

    public List<CustomFilterConfiguration> getCustomFilters() {
        return customFilter;
    }

    public boolean isShowAddFilter() {
        return showAddFilter;
    }

    public boolean isShowClearEvents() {
        return showClearEvents;
    }

    public boolean isShowEventDetailSummary() {
        return showEventDetailSummary;
    }

    public boolean isShowRegexOptionOnQuickFilters() {
        return showRegexOptionOnQuickFilters;
    }

    public boolean isEventDetailsSeparatorHorizontalOrientiation() {
        return eventDetailsSeparatorHorizontalOrientiation;
    }

    public boolean isShowTimeControl() {
        return showTimeControl;
    }

    public void setEventDetailsSeparatorHorizontalOrientiation(boolean eventDetailsSeparatorHorizontalOrientiation) {
        this.eventDetailsSeparatorHorizontalOrientiation = eventDetailsSeparatorHorizontalOrientiation;
    }

    public List<CustomDateFilterConfiguration> getCustomDateFilters() {
        return customDateFilter;
    }


    public List<ColumnConfiguration> getColumnSetting() {
        return columnSetting;
    }

    public List<NameMappingConfiguration> getColumnMappings() {
        return columnMapping;
    }

    public String getAutoRequestHistory() {
        return autoRequestHistory;
    }

    public boolean getDisableAutoScrollPauser() {
        return disableAutoScrollPauser;
    }


    public List<NameMappingConfiguration> getLevelMappings() {
        return levelMapping;
    }

    public boolean isDisableAutoScrollPauser() {
        return disableAutoScrollPauser;
    }

    public boolean isDisableColumnFile() {
        return disableColumnFile;
    }

    public boolean isFilterCaseSensitive() {
        return filterCaseSensitive;
    }

    public boolean isFilterUnicode() {
        return filterUnicode;
    }

    public boolean isShowHTMLEventDetails() {
        return showHTMLEventDetails;
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

    public void setDisableColumnFile(boolean disableColumnFile) {
        this.disableColumnFile = disableColumnFile;
    }


    public String getEventDetailsSeparatorLocation() {
        return eventDetailsSeparatorLocation;
    }

    public void setEventDetailsSeparatorLocation(String eventDetailsSeparatorLocation) {
        this.eventDetailsSeparatorLocation = eventDetailsSeparatorLocation;
    }

    public void setFilterCaseSensitive(boolean filterCaseSensitive) {
        this.filterCaseSensitive = filterCaseSensitive;
    }

    public void setFilterUnicode(boolean filterUnicode) {
        this.filterUnicode = filterUnicode;
    }

    public void setLevelMappings(List<NameMappingConfiguration> levelMappings) {
        this.levelMapping = levelMappings;
    }

    public void setShowAddFilter(boolean showAddFilter) {
        this.showAddFilter = showAddFilter;
    }

    public void setShowClearEvents(boolean showClearEvents) {
        this.showClearEvents = showClearEvents;
    }

    public void setShowEventDetailSummary(boolean showEventDetailSummary) {
        this.showEventDetailSummary = showEventDetailSummary;
    }

    public void setShowHTMLEventDetails(boolean showHTMLEventDetails) {
        this.showHTMLEventDetails = showHTMLEventDetails;
    }

    public void setShowTimeControl(boolean showTimeControl) {
        this.showTimeControl = showTimeControl;
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

