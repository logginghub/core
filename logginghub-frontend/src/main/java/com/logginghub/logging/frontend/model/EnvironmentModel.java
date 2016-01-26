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
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableProperty;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EnvironmentModel extends Observable implements LogEventSource, LogEventListener, StreamListener<LogEvent> {

    private static final Logger logger = Logger.getLoggerFor(EnvironmentModel.class);
    private ObservableList<HubConnectionModel> hubs = createListProperty("hubs", HubConnectionModel.class);
    private ObservableList<HighlighterModel> highlighters = createListProperty("highlighters", HighlighterModel.class);
    private LogEventMultiplexer multiplexer = new LogEventMultiplexer();
    private AtomicInteger sequenceIDGenerator = new AtomicInteger(0);
    private boolean autoLockWarning;
    private boolean writeOutputLog = false;
    private EnvironmentSummaryModel environmentSummaryModel = new EnvironmentSummaryModel();
    private LogEventContainerController logEventContainerController = new LogEventContainerController();
    private TimestampVariableRollingFileLoggerConfiguration outputLogConfiguration = null;
    private Stream<ConnectionStateChangedEvent> connectionStateStream = new Stream<ConnectionStateChangedEvent>();


    private QuickFilterHistoryModel quickFilterHistoryModel = new QuickFilterHistoryModel();

    private ObservableList<QuickFilterModel> quickFilterModels = createListProperty("quickFilterModels", QuickFilterModel.class);
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

    private ObservableList<CustomQuickFilterModel> customFilters = createListProperty("customFilters", CustomQuickFilterModel.class);
    private ObservableList<CustomDateFilterModel> customDateFilters = createListProperty("customDateFilters", CustomDateFilterModel.class);


    private ObservableProperty<String> name = createStringProperty("name", "");
    private ObservableProperty<String> repoConnectionPoints = createStringProperty("repoConnectionPoints", "");
    private ObservableProperty<Boolean> openOnStartup = createBooleanProperty("openOnStartup", true);
    private ObservableProperty<Boolean> showHTMLEventDetails = createBooleanProperty("showHTMLEventDetails", false);
    private ObservableProperty<Boolean> autoLocking = createBooleanProperty("autoLocking", true);
    private ObservableProperty<Boolean> showHistoryTab = createBooleanProperty("showHistoryTab", true);
    private ObservableProperty<Boolean> repoEnabled = createBooleanProperty("repoEnabled", true);
    private ObservableProperty<String> channel = createStringProperty("channel", "");
    private final ObservableInteger highestLevelSinceLastSelected = createIntProperty("highestLevelSinceLastSelected", -1);


    public EnvironmentModel() {

        // We need to write up any hubs that get added to we can multiplex their
        // log events automatically
        final ObservableListListener<HubConnectionModel> listener = new ObservableListListener<HubConnectionModel>() {
            @Override
            public void onAdded(HubConnectionModel hubConnectionModel) {
                hubConnectionModel.addLogEventListener(EnvironmentModel.this);
            }

            @Override
            public void onRemoved(HubConnectionModel hubConnectionModel, int index) {
                hubConnectionModel.removeLogEventListener(EnvironmentModel.this);
            }

            @Override
            public void onCleared() {

            }
        };

        hubs.addListener(listener);

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

        getAutoLocking().set(false);

        getRepoEnabled().set(false);
    }

    public ObservableProperty<Boolean> getRepoEnabled() {
        return repoEnabled;
    }

    public void addFilters(EnvironmentConfiguration configuration) {
        List<FilterConfiguration> filters = configuration.getFilters();
        for (FilterConfiguration filterConfiguration : filters) {
            excludeFilter.addFilter(filterConfiguration);
        }
    }

    @Override
    public void addLogEventListener(LogEventListener logEventListener) {
        multiplexer.addLogEventListener(logEventListener);
    }

    @Override
    public void removeLogEventListener(LogEventListener logEventListener) {
        multiplexer.removeLogEventListener(logEventListener);
    }

    public ObservableProperty<String> getChannel() {
        return channel;
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

    public ObservableList<CustomDateFilterModel> getCustomDateFilters() {
        return customDateFilters;
    }

    public ObservableList<CustomQuickFilterModel> getCustomFilters() {
        return customFilters;
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

    public ObservableList<HubConnectionModel> getHubs() {
        return hubs;
    }

    public ObservableProperty<Boolean> getAutoLocking() {
        return autoLocking;
    }

    public ObservableProperty<Boolean> getOpenOnStartup() {
        return openOnStartup;
    }

    public ObservableProperty<Boolean> getShowHistoryTab() {
        return showHistoryTab;
    }

    public ObservableProperty<String> getRepoConnectionPoints() {
        return repoConnectionPoints;
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

    public ObservableProperty<String> getName() {
        return name;
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

    public void updateEachSecond() {
        logger.trace("Updating environment model {}", getName());
        environmentSummaryModel.updateEachSecond();
    }

    public ObservableInteger getHighestLevelSinceLastSelected() {
        return highestLevelSinceLastSelected;
    }

    public ObservableProperty<Boolean> getShowHTMLEventDetails() {
        return showHTMLEventDetails;
    }
}
