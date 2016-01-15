package com.logginghub.logging.frontend.model;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LevelConstants;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventMultiplexer;
import com.logginghub.logging.frontend.components.QuickFilterHistoryModel;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.interfaces.LogEventSource;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.servers.FilterHelper;
import com.logginghub.utils.Stream;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EnvironmentModel extends ObservableModel implements LogEventSource, LogEventListener, StreamListener<LogEvent> {

    private static final Logger logger = Logger.getLoggerFor(EnvironmentModel.class);
    private ObservableList<HubConnectionModel> hubs = new ObservableArrayList<HubConnectionModel>();
    private ObservableList<HighlighterModel> highlighters = new ObservableArrayList<HighlighterModel>();
    private LogEventMultiplexer multiplexer = new LogEventMultiplexer();
    private AtomicInteger sequenceIDGenerator = new AtomicInteger(0);
    private boolean autoLockWarning;
    private boolean writeOutputLog = false;
    private EnvironmentSummaryModel environmentSummaryModel = new EnvironmentSummaryModel();
    private LogEventContainerController logEventContainerController = new LogEventContainerController();
    private TimestampVariableRollingFileLoggerConfiguration outputLogConfiguration = null;
    private Stream<ConnectionStateChangedEvent> connectionStateStream = new Stream<ConnectionStateChangedEvent>();
    private String channel;

    private QuickFilterHistoryModel quickFilterHistoryModel = new QuickFilterHistoryModel();

    private com.logginghub.utils.observable.ObservableList<QuickFilterModel> quickFilterModels = new com.logginghub.utils.observable.ObservableList<QuickFilterModel>(
            new ArrayList<QuickFilterModel>());
    private ObservableInteger filterUpdateCount = new ObservableInteger(0);

    private FilterHelper excludeFilter = new FilterHelper();
    private double eventMemoryMB;
    private String autoRequestHistory = "";
    private boolean disableAutoScrollPauser = false;

    private EventTableColumnModel eventTableColumnModel = new EventTableColumnModel();
    private LevelNamesModel levelNamesModel = new LevelNamesModel();
    private boolean filterCaseSensitive;
    private boolean filterUnicode;
    private ColumnSettingsModel columnSettingsModel = new ColumnSettingsModel();
    private int eventDetailsSeparatorLocation = -1;
    private boolean clustered;

    private ObservableList<CustomQuickFilterModel> customFilters = new ObservableArrayList<CustomQuickFilterModel>();

    public EnvironmentModel() {

        // We need to write up any hubs that get added to we can multiplex their
        // log events automatically
        final ObservableListListener<HubConnectionModel> listener = new ObservableListListener<HubConnectionModel>() {
            @Override
            public void onItemAdded(HubConnectionModel t) {
                t.addLogEventListener(EnvironmentModel.this);
            }

            @Override
            public void onItemRemoved(HubConnectionModel t) {
                t.removeLogEventListener(EnvironmentModel.this);
            }
        };

        hubs.addListListener(listener);

        logEventContainerController.addLogEventContainerListener(new LogEventContainerListener() {
            @Override
            public void onAdded(LogEvent event) {
                environmentSummaryModel.onNewLogEvent(event);
            }

            @Override
            public void onPassedFilter(LogEvent event) {
            }

            @Override
            public void onRemoved(LogEvent event) {
                environmentSummaryModel.onEventRemoved(event);
            }

            @Override
            public void onCleared() {
            }
        });

        setAutoLocking(false);
        set(Fields.RepoEnabled, false);
    }

    public void addFilters(EnvironmentConfiguration configuration) {
        List<FilterConfiguration> filters = configuration.getFilters();
        for (FilterConfiguration filterConfiguration : filters) {
            excludeFilter.addFilter(filterConfiguration);
        }
    }

    public ObservableList<CustomQuickFilterModel> getCustomFilters() {
        return customFilters;
    }

    @Override
    public void addLogEventListener(LogEventListener logEventListener) {
        multiplexer.addLogEventListener(logEventListener);
    }

    @Override
    public void removeLogEventListener(LogEventListener logEventListener) {
        multiplexer.removeLogEventListener(logEventListener);
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public ColumnSettingsModel getColumnSettingsModel() {
        return columnSettingsModel;
    }

    public void setColumnSettingsModel(ColumnSettingsModel columnSettingsModel) {
        this.columnSettingsModel = columnSettingsModel;
    }

    public Stream<ConnectionStateChangedEvent> getConnectionStateStream() {
        return connectionStateStream;
    }

    public EnvironmentSummaryModel getEnvironmentSummaryModel() {
        return environmentSummaryModel;
    }

    public LogEventContainerController getEventController() {
        return logEventContainerController;
    }

    public int getEventDetailsSeparatorLocation() {
        return eventDetailsSeparatorLocation;
    }

    public void setEventDetailsSeparatorLocation(int eventDetailsSeparatorLocation) {
        this.eventDetailsSeparatorLocation = eventDetailsSeparatorLocation;
    }

    public double getEventMemoryMB() {
        return eventMemoryMB;
    }

    public void setEventMemoryMB(double eventMemoryMB) {
        this.eventMemoryMB = eventMemoryMB;
    }

    public EventTableColumnModel getEventTableColumnModel() {
        return eventTableColumnModel;
    }

    public ObservableInteger getFilterUpdateCount() {
        return filterUpdateCount;
    }

    public ObservableList<HighlighterModel> getHighlighters() {
        return highlighters;
    }

    public void setHighlighters(ObservableList<HighlighterModel> highlighters) {
        this.highlighters = highlighters;
    }

    public ObservableList<HubConnectionModel> getHubConnectionModels() {
        return hubs;
    }

    public void setHubConnectionModels(ObservableList<HubConnectionModel> hubs) {
        this.hubs = hubs;
    }

    public LevelNamesModel getLevelNamesModel() {
        return levelNamesModel;
    }

    public TimestampVariableRollingFileLoggerConfiguration getOutputLogConfiguration() {
        return outputLogConfiguration;
    }

    public void setOutputLogConfiguration(TimestampVariableRollingFileLoggerConfiguration outputLogConfiguration) {
        this.outputLogConfiguration = outputLogConfiguration;
    }

    public QuickFilterHistoryModel getQuickFilterHistoryModel() {
        return quickFilterHistoryModel;
    }

    public com.logginghub.utils.observable.ObservableList<QuickFilterModel> getQuickFilterModels() {
        return quickFilterModels;
    }

    public String getRepoConnectionPoints() {
        return getString(Fields.RepoConnectionPoints);
    }

    public boolean isAutoLocking() {
        return getBoolean(Fields.AutoLocking);
    }

    public void setAutoLocking(boolean autoLocking) {
        set(Fields.AutoLocking, autoLocking);
    }

    public String isAutoRequestHistory() {
        return autoRequestHistory;
    }

    public boolean isClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public boolean isDisableAutoScrollPauser() {
        return disableAutoScrollPauser;
    }

    public void setDisableAutoScrollPauser(boolean disableAutoScrollPauser) {
        this.disableAutoScrollPauser = disableAutoScrollPauser;
    }

    public boolean isFilterCaseSensitive() {
        return filterCaseSensitive;
    }

    public void setFilterCaseSensitive(boolean filterCaseSensitive) {
        this.filterCaseSensitive = filterCaseSensitive;
    }

    public boolean isFilterUnicode() {
        return filterUnicode;
    }

    public void setFilterUnicode(boolean filterUnicode) {
        this.filterUnicode = filterUnicode;
    }

    public boolean isOpenOnStartup() {
        return getBoolean(Fields.OpenOnStartup);
    }

    public boolean isRepoEnabled() {
        return getBoolean(Fields.RepoEnabled);
    }

    public boolean isShowHistoryTab() {
        return getBoolean(Fields.ShowHistoryTab);
    }

    public boolean isWriteOutputLog() {
        return writeOutputLog;
    }

    public void setWriteOutputLog(boolean writeOutputLog) {
        this.writeOutputLog = writeOutputLog;
    }

    @Override
    public void onNewItem(LogEvent t) {
        onNewLogEvent(t);
    }

    @Override
    public void onNewLogEvent(LogEvent event) {
        logger.trace("New log event received from environment '{}' - {} : {}", getName(), event.getLevelDescription(), event.getMessage());

        if (!excludeFilter.passes(event)) {
            if (event instanceof DefaultLogEvent) {
                DefaultLogEvent defaultLogEvent = (DefaultLogEvent) event;
                defaultLogEvent.setSequenceNumber(sequenceIDGenerator.getAndIncrement());

                if (autoLockWarning && event.getLevel() >= LevelConstants.WARNING) {
                    defaultLogEvent.getMetadata().put("locked", "true");
                }
            }

            multiplexer.onNewLogEvent(event);
        }
    }

    public void setAutoLockWarning(boolean autoLockWarning) {
        this.autoLockWarning = autoLockWarning;
    }

    public void setAutoRequestHistory(String autoRequestHistory) {
        this.autoRequestHistory = autoRequestHistory;
    }

    @Override
    public String toString() {
        return "EnvironmentModel [mame =" + getName() + "]";
    }

    public String getName() {
        return get(Fields.Name);
    }

    public void setName(String name) {
        set(Fields.Name, name);
    }

    public void updateEachSecond() {
        logger.trace("Updating environment model {}", getName());
        environmentSummaryModel.updateEachSecond();
    }

    public enum Fields implements FieldEnumeration {
        Name,
        OpenOnStartup,
        AutoLocking,
        RepoEnabled,
        RepoConnectionPoints,
        Channel,
        ShowHistoryTab
    }
}
