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

    private com.logginghub.utils.observable.ObservableList<QuickFilterModel> quickFilterModels = new com.logginghub.utils.observable.ObservableList<QuickFilterModel>(new ArrayList<QuickFilterModel>());
    private ObservableInteger filterUpdateCount = new ObservableInteger(0);

    private FilterHelper excludeFilter = new FilterHelper();
    private double eventMemoryMB;
    private String autoRequestHistory= "";
    private boolean disableAutoScrollPauser = false;
    private EventTableModel eventTableModel = new EventTableModel();

    public double getEventMemoryMB() {
        return eventMemoryMB;
    }

    public EventTableModel getEventTableModel() {
        return eventTableModel;
    }

    public String isAutoRequestHistory() {
        return autoRequestHistory;
    }

    public boolean isDisableAutoScrollPauser() {
        return disableAutoScrollPauser;
    }

    public void setAutoRequestHistory(String autoRequestHistory) {
        this.autoRequestHistory = autoRequestHistory;
    }

    public void setDisableAutoScrollPauser(boolean disableAutoScrollPauser) {
        this.disableAutoScrollPauser = disableAutoScrollPauser;
    }

    public void setEventMemoryMB(double eventMemoryMB) {
        this.eventMemoryMB = eventMemoryMB;
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

    private boolean clustered;

    public EnvironmentModel() {

        // We need to write up any hubs that get added to we can multiplex their
        // log events automatically
        final ObservableListListener<HubConnectionModel> listener = new ObservableListListener<HubConnectionModel>() {
            @Override public void onItemRemoved(HubConnectionModel t) {
                t.removeLogEventListener(EnvironmentModel.this);
            }

            @Override public void onItemAdded(HubConnectionModel t) {
                t.addLogEventListener(EnvironmentModel.this);
            }
        };

        hubs.addListListener(listener);

        logEventContainerController.addLogEventContainerListener(new LogEventContainerListener() {
            @Override public void onRemoved(LogEvent event) {
                environmentSummaryModel.onEventRemoved(event);
            }

            @Override public void onAdded(LogEvent event) {
                environmentSummaryModel.onNewLogEvent(event);
            }

            @Override public void onCleared() {}

            @Override public void onPassedFilter(LogEvent event) {}
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

    public void setWriteOutputLog(boolean writeOutputLog) {
        this.writeOutputLog = writeOutputLog;
    }

    public boolean isWriteOutputLog() {
        return writeOutputLog;
    }

    public void setAutoLocking(boolean autoLocking) {
        set(Fields.AutoLocking, autoLocking);
    }

    public boolean isShowHistoryTab() {
        return getBoolean(Fields.ShowHistoryTab);
    }

    public boolean isAutoLocking() {
        return getBoolean(Fields.AutoLocking);
    }

    public boolean isOpenOnStartup() {
        return getBoolean(Fields.OpenOnStartup);
    }

    public TimestampVariableRollingFileLoggerConfiguration getOutputLogConfiguration() {
        return outputLogConfiguration;
    }

    public boolean isRepoEnabled() {
        return getBoolean(Fields.RepoEnabled);
    }

    public String getRepoConnectionPoints() {
        return getString(Fields.RepoConnectionPoints);
    }

    public ObservableList<HubConnectionModel> getHubConnectionModels() {
        return hubs;
    }

    public void setHubConnectionModels(ObservableList<HubConnectionModel> hubs) {
        this.hubs = hubs;
    }

    public ObservableList<HighlighterModel> getHighlighters() {
        return highlighters;
    }

    public void setHighlighters(ObservableList<HighlighterModel> highlighters) {
        this.highlighters = highlighters;
    }

    @Override public void addLogEventListener(LogEventListener logEventListener) {
        multiplexer.addLogEventListener(logEventListener);
    }

    @Override public void onNewLogEvent(LogEvent event) {
        logger.trace("New log event received from environment '{}' - {} : {}", getName(), event.getLevelDescription(), event.getMessage());
        
        if (!excludeFilter.passes(event)) {
            if (event instanceof DefaultLogEvent) {
                DefaultLogEvent defaultLogEvent = (DefaultLogEvent) event;
                defaultLogEvent.setSequenceNumber(sequenceIDGenerator.getAndIncrement());

                if (autoLockWarning && event.getLevel() >= LevelConstants.WARNING) {
                    defaultLogEvent.getMetadata().put("locked", true);
                }
            }

            multiplexer.onNewLogEvent(event);
        }
    }

    @Override public void removeLogEventListener(LogEventListener logEventListener) {
        multiplexer.removeLogEventListener(logEventListener);
    }

    public String getName() {
        return get(Fields.Name);
    }

    public void setName(String name) {
        set(Fields.Name, name);
    }

    @Override public String toString() {
        return "EnvironmentModel [mame =" + getName() + "]";
    }

    public void setAutoLockWarning(boolean autoLockWarning) {
        this.autoLockWarning = autoLockWarning;
    }

    public EnvironmentSummaryModel getEnvironmentSummaryModel() {
        return environmentSummaryModel;
    }

    public void updateEachSecond() {
        logger.trace("Updating environment model {}", getName());
        environmentSummaryModel.updateEachSecond();
    }

    public LogEventContainerController getEventController() {
        return logEventContainerController;
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

    public ObservableInteger getFilterUpdateCount() {
        return filterUpdateCount;
    }

    public Stream<ConnectionStateChangedEvent> getConnectionStateStream() {
        return connectionStateStream;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    @Override public void onNewItem(LogEvent t) {
        onNewLogEvent(t);
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public boolean isClustered() {
        return clustered;
    }
}
