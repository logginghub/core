package com.logginghub.logging.frontend;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.frontend.binary.ImportController;
import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.NewChartingView;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.charting.swing.ChartingTreeEditorView;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.RemoteChartConfiguration;
import com.logginghub.logging.frontend.connectionmanager.ConnectionManagerListener;
import com.logginghub.logging.frontend.connectionmanager.ConnectionManagerPanel;
import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.frontend.model.*;
import com.logginghub.logging.frontend.modules.*;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.services.MenuService;
import com.logginghub.logging.frontend.views.detail.DetailedLogEventTablePanel;
import com.logginghub.logging.frontend.views.detail.time.TimeController;
import com.logginghub.logging.frontend.views.environmentsummary.DashboardPanel;
import com.logginghub.logging.frontend.views.environmentsummary.DashboardSelectionListener;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.messaging.SocketClientManager.State;
import com.logginghub.logging.messaging.SocketClientManagerListener;
import com.logginghub.logging.modules.DecodeStrategy;
import com.logginghub.logging.modules.TimestampVariableRollingFileLogger;
import com.logginghub.logging.repository.BinaryLogFileReader;
import com.logginghub.logging.utils.KryoVersion1Decoder;
import com.logginghub.logging.utils.LogEventBlockElement;
import com.logginghub.swingutils.ButtonTabComponent;
import com.logginghub.utils.*;
import com.logginghub.utils.logging.Logger;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class LoggingMainPanel extends JPanel implements MenuService, SocketClientDirectAccessService {

    public static final String FILENAME_COMBO = "filenameCombo";
    public static final String CHANGE_OUTPUT_LOG_DESTINATION = "Change output log destination...";
    public static final String LOG_EVENT_PANEL = "logEventPanel-";
    private static final Logger logger = Logger.getLoggerFor(LoggingMainPanel.class);
    private static final long serialVersionUID = 1L;
    // This is the master time provider for the entire frontend. It should be passed down into
    // anything that needs the absolute time!
    private OffsetableSystemTimeProvider timeProvider = new OffsetableSystemTimeProvider();
    private OldChartingPanel chartingPanelx;
    private ConfigurationPanel configurationPanel;
    private JMenuBar menuBar;
    private JFrame mainFrame;
    private LoggingFrontendModel model;
    private int nextDummyApplicationIndex = 0;
    private AtomicInteger nextTabID = new AtomicInteger();
    private DetailedLogEventTablePanel previouslySelectedPane = null;
    private String propertiesName;
    private boolean serversideFilteringEnabled = false;
    private DetailedLogEventTablePanel firstDetailPanel = null;
    private JTabbedPane tabbedPane = null;
    private JCheckBoxMenuItem horizontalDetailView;
    private JCheckBoxMenuItem hubLevelFiltering;
    private JCheckBoxMenuItem autoLockWarnings;
    private JMenuItem viewRepo;
    private JCheckBoxMenuItem autoScroll;
    private JCheckBoxMenuItem writeOutputLog;
    private JMenuItem changeOutputLogDestination;
    private ConfigurationProxy proxy;

    private JFrame chartingPopoutFrameOldx;
    private JFrame chartingPopoutFrameNew;

    private List<OldChartingPanel> chartingPanelsx = new ArrayList<OldChartingPanel>();
    private Set<String> existingOutputLogFilenames = new HashSet<String>();

    private NewChartingController chartingController;
    private NewChartingView newChartingView;
    private JMenuItem exportBinaryMenuItem;
    private JCheckBoxMenuItem demoSourceMenuItem;
    private JCheckBoxMenuItem dummySourceMenuItem;
    private List<RemoteChartingTab> remoteChartingTabs = new ArrayList<RemoteChartingTab>();
    //    private Map<AggregationKey, Stream<ChunkedResult>> resultHandlers = new HashMap<AggregationKey, Stream<ChunkedResult>>();
    private JMenu editMenu;
    private JMenu viewMenu;

    public LoggingMainPanel() {
        setLayout(new BorderLayout());

        configurationPanel = new ConfigurationPanel();
        configurationPanel.setMinimumSize(new Dimension(0, 0));
        configurationPanel.setMaximumSize(new Dimension(10000, 10000));

        tabbedPane = new JTabbedPane();
        tabbedPane.setName("mainTabbedPane");
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupSingleEnvironmentMode(EnvironmentModel environmentModel,
                                            ConfigurationProxy proxy,
                                            LoggingFrontendModel newGoodModel) {

        if (model.getShowDashboard()) {
            createDashboard(model);
        }

        configurationPanel = new ConfigurationPanel();
        configurationPanel.setName("ConfigurationPanel");
        configurationPanel.setModel(newGoodModel);
        configurationPanel.setMinimumSize(new Dimension(0, 0));
        configurationPanel.setMaximumSize(new Dimension(10000, 10000));

        firstDetailPanel = createNewDetailedLogEventTablePanel(environmentModel);
        showModel(firstDetailPanel);

        // TODO : this is kind of temporary - we are feeding the charting world
        // a configuration object, whereas the rest of the code has been changed
        // to use the model instead.
        EnvironmentConfiguration config = getEnvironmentConfigurationFromModel(environmentModel, proxy);

        if (newGoodModel.isPopoutCharting()) {
            if (proxy.getLoggingFrontendConfiguration().isShowOldCharting()) {
                chartingPopoutFrameOldx = new SmartJFrame("charting", proxy.getDynamicSettings());
                chartingPopoutFrameOldx.setTitle("LogViewer Charting Popout");
            }

            chartingPopoutFrameNew = new SmartJFrame("newcharting", proxy.getDynamicSettings());
            chartingPopoutFrameNew.setTitle("LogViewer New Charting Popout");
        }

        // TODO : new charting needs to replace the old charting after a while
        newChartingView = setupNewCharting(newGoodModel);

        // Wire up the charting controller to this environments event stream
        environmentModel.addLogEventListener(chartingController.getLogEventMultiplexer());

        OldChartingPanel chartingPanelOld = null;
        if (proxy.getLoggingFrontendConfiguration().isShowOldCharting()) {
            chartingPanelOld = new OldChartingPanel(config.getChartingConfiguration(), proxy.getParsersLocation());
            chartingPanelsx.add(chartingPanelOld);
            environmentModel.addLogEventListener(chartingPanelOld);
        }

        // Wire up notification of the hub side filter changing
        firstDetailPanel.addChartCreationRequestListener(new DetailedLogEventPanelListener() {
            @Override public void onLevelFilterChanged(int levelFilter) {
                updateHubSideFilter(levelFilter);
            }

            @Override public void onCreateNewChartForEvent(LogEvent event) {
            }
        });

        if (newGoodModel.isPopoutCharting()) {
            if (proxy.getLoggingFrontendConfiguration().isShowOldCharting()) {
                chartingPopoutFrameOldx.getContentPane().add(chartingPanelOld);
            }
            chartingPopoutFrameNew.getContentPane().add(newChartingView);
        } else {
            if (proxy.getLoggingFrontendConfiguration().isShowOldCharting()) {
                tabbedPane.addTab("Charting", chartingPanelOld);
                tabbedPane.addTab("Charting (New)", newChartingView);
            } else {
                tabbedPane.addTab("Charting", newChartingView);
            }
        }

        // Add the config tab last to make sure it appears at the end
        tabbedPane.addTab("Configuration", configurationPanel);

        // Add the help menu last so it appears at the right of the menu bar
        setupHelpMenu(mainFrame);

        setupWriteOutputLogState(firstDetailPanel);

        // TODO : try and get this working, the mash up between modules and the trad style needs
        // some work!
        // if (environmentModel.isShowHistoryTab()) {
        // addHistoryTab(environmentModel);
        // }
    }

    public void start() {
        if (chartingController != null) {
            chartingController.start();
        }
    }

    public void stop() {
        if (chartingController != null) {
            chartingController.stop();
        }
    }

    private NewChartingView setupNewCharting(LoggingFrontendModel newGoodModel) {
        NewChartingView chartingPanel = new NewChartingView();
        // TODO : this will only plot disconnections from the first environment
        chartingController = new NewChartingController(newGoodModel.getChartingModel(), timeProvider);
        chartingController.bindDisconnectionEvents(model.getEnvironments().get(0));
        chartingPanel.bind(chartingController);
        return chartingPanel;
    }

    private EnvironmentConfiguration getEnvironmentConfigurationFromModel(EnvironmentModel environmentModel,
                                                                          ConfigurationProxy proxy) {
        EnvironmentConfiguration config = null;
        List<EnvironmentConfiguration> environments = proxy.getLoggingFrontendConfiguration().getEnvironments();
        for (EnvironmentConfiguration environmentConfiguration : environments) {
            if (environmentConfiguration.getName().equals(environmentModel.getName())) {
                config = environmentConfiguration;
                break;
            }
        }
        return config;
    }

    public LoggingFrontendModel getModel() {
        return model;
    }

    public void setModel(LoggingFrontendModel model, Metadata settings, SwingFrontEnd swingFrontEnd) {
        this.model = model;
        setParentFrame(swingFrontEnd);
        setMenuBar(swingFrontEnd.getJMenuBar());

        logger.info("Setting up main panel with model...");

        long memoryForEvents = (long) (Runtime.getRuntime().maxMemory() * Double.parseDouble(System.getProperty(
                "logginghub.eventmemoryratio",
                "0.1")));
        ObservableList<EnvironmentModel> environments = model.getEnvironments();
        for (EnvironmentModel environmentModel : environments) {
            long threshold = (long) (memoryForEvents / (double) environments.size());
            environmentModel.getEventController().setThreshold(threshold);
        }

        if (environments.size() == 1) {
            // This is the old single loggin tab with charting and configuration
            // tab option
            EnvironmentModel environmentModel = environments.get(0);
            setupSingleEnvironmentMode(environmentModel, proxy, model);

            if (environmentModel.isRepoEnabled()) {
                viewRepo.setVisible(true);
            } else {
                viewRepo.setVisible(false);
            }
        } else {
            if (model.getShowDashboard()) {
                createDashboard(model);
            }

            if (model.getEnvironments().size() > 0) {
                newChartingView = setupNewCharting(model);
            }

            JTabbedPane chartingTabsOld = new JTabbedPane();

            if (model.isPopoutCharting()) {
                if (swingFrontEnd.getProxy().getLoggingFrontendConfiguration().isShowOldCharting()) {
                    chartingPopoutFrameOldx = new SmartJFrame("charting", settings);
                    chartingPopoutFrameOldx.setTitle("LogViewer Charting Popout");
                    chartingPopoutFrameOldx.getContentPane().add(chartingTabsOld);
                }

                chartingPopoutFrameNew = new SmartJFrame("newcharting", settings);
                chartingPopoutFrameNew.setTitle("LogViewer Charting Popout (New)");
                chartingPopoutFrameNew.getContentPane().add(newChartingView);
            }

            for (EnvironmentModel environmentModel : environments) {

                if (environmentModel.isOpenOnStartup()) {
                    addEnvironmentLoggingDetailsTab(environmentModel);
                }

                if (environmentModel.isShowHistoryTab()) {
                    addHistoryTab(environmentModel);
                }

                environmentModel.addLogEventListener(chartingController.getLogEventMultiplexer());

                if (model.isPopoutCharting()) {
                    EnvironmentConfiguration config = getEnvironmentConfigurationFromModel(environmentModel, proxy);
                    OldChartingPanel chartingPanel = new OldChartingPanel(config.getChartingConfiguration(),
                                                                          proxy.getParsersLocation());
                    chartingPanelsx.add(chartingPanel);
                    environmentModel.addLogEventListener(chartingPanel);

                    chartingTabsOld.addTab(environmentModel.getName(), chartingPanel);

                    // TODO : create a new charting panel for this environment
                    // chartingTabsNew.addTab(environmentModel.getName(),
                    // chartingPanel);
                }
            }

            tabbedPane.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    handleTabChange();
                }
            });

            installMouseHandlerAndMenusForTabbedPane(tabbedPane);

            DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
            if (currentSelectedTabx != null) {
                autoLockWarnings.setSelected(currentSelectedTabx.getEnvironmentModel().isAutoLocking());

                setupWriteOutputLogState(currentSelectedTabx);

                EnvironmentModel environmentModel = currentSelectedTabx.getEnvironmentModel();
                if (environmentModel.isRepoEnabled()) {
                    viewRepo.setVisible(true);
                } else {
                    viewRepo.setVisible(false);
                }
            } else {
                autoLockWarnings.setEnabled(false);
            }

            // Once we've added everything, stick the help menu in so it always
            // appears last
            setupHelpMenu(mainFrame);
        }

        logger.info("Main panel setup complete.");
    }

    private void addHistoryTab(EnvironmentModel environmentModel) {

        HistoryViewModule module = new HistoryViewModule();

        EnvironmentAdaptor environmentAdaptor = new EnvironmentAdaptor(environmentModel);

        module.setMessagingService(environmentAdaptor);
        module.setEnvironmentNotificationService(environmentAdaptor);

        final String name = environmentModel.get(EnvironmentModel.Fields.Name) + " history";
        module.setLayoutService(new LayoutService() {
            @Override public void add(Component component, String layout) {
                int index = tabbedPane.getTabCount();
                tabbedPane.addTab(name, component);
                tabbedPane.setTabComponentAt(index, new ButtonTabComponent(tabbedPane));
            }
        });

        module.initialise();
        module.start();

        // EnvironmentModel tempModel = new EnvironmentModel();
        //
        // DetailedLogEventTablePanel detailPanel = createNewDetailedLogEventTablePanel(tempModel);
        // detailPanel.setEnvironmentModel(environmentModel);
        //
        // detailPanel.setName(LOG_EVENT_PANEL + environmentModel.getName());
        //
        // String name = environmentModel.get(EnvironmentModel.Fields.Name) + " history";
        // int tabIndex = tabbedPane.getTabCount();
        // tabbedPane.addTab(name, detailPanel);
    }

    private void configureRemoteChartingTab(RemoteChartConfiguration remoteChartConfiguration) {

        String filename = remoteChartConfiguration.getFilename();
        Xml xml = Xml.parse(new File(filename));
        NewChartingModel chartingModel = new NewChartingModel();
        chartingModel.fromXml(xml.getRoot());

        NewChartingController chartingController = new NewChartingController(chartingModel, timeProvider);

        RemoteChartingTab tab = new RemoteChartingTab();
        tab.configure(remoteChartConfiguration);
        tab.bind(chartingController);
        tabbedPane.add(remoteChartConfiguration.getName(), tab);

        remoteChartingTabs.add(tab);
    }

    //    private void startRemoteChartingTab(RemoteChartingTab tab) {
    //
    //        // Go through the series models and build AggregationKeys for each, we'll use these to do
    //        // the subscribing
    //        List<AggregationKey> aggregationKeys = new ArrayList<AggregationKey>();
    //
    //        com.logginghub.utils.observable.ObservableList<PageModel> pages = tab.getController().getModel().getPages();
    //        for (PageModel page : pages) {
    //            com.logginghub.utils.observable.ObservableList<LineChartModel> chartingModels = page.getChartingModels();
    //            for (LineChartModel lineChartModel : chartingModels) {
    //                com.logginghub.utils.observable.ObservableList<ChartSeriesModel> matcherModels = lineChartModel.getMatcherModels();
    //                for (ChartSeriesModel csm : matcherModels) {
    //
    //                    int pattern = csm.getPatternID().get();
    //                    String eventParts = csm.getEventParts().get();
    //                    int interval = csm.getInterval().get();
    //                    int labelIndex = csm.getLabelIndex().get();
    //                    String type = csm.getType().get();
    //
    //                    // TODO : this has been broken by the new aggregation code - DSL aren't using it anymore, so I'm not fixing it now
    ////                    AggregationKey key = new AggregationKey(pattern, labelIndex, AggregationType.valueOf(type), interval, null);
    ////
    ////                    if (StringUtils.isNotNullOrEmpty(eventParts)) {
    ////                        key.setEventParts(eventParts.split("/"));
    ////                    }
    ////
    ////                    aggregationKeys.add(key);
    ////
    ////                    resultHandlers.put(key, tab.getController().getResultStreamFor(csm));
    //                }
    //
    //            }
    //        }
    //
    //        // Now collect up all the environments we'll be pulling data from
    //        // TODO : its a bit shotgun - one day we might want to target specific environments for
    //        // specific keys?
    //        LoggingFrontendModel model = getModel();
    //
    //        List<EnvironmentModel> targetEnvironments = new ArrayList<EnvironmentModel>();
    //
    //        String target = tab.getRemoteChartConfiguration().getEnvironmentRef();
    //        if (StringUtils.isNotNullOrEmpty(target)) {
    //            EnvironmentModel environment = model.getEnvironment(target);
    //            if (environment != null) {
    //                targetEnvironments.add(environment);
    //            }
    //            else {
    //                logger.warn("Cound't find an environment called '{}' in your configuration - the remote charting panel '{}' wont be able to connect to the event stream. Please check your config!",
    //                            target,
    //                            tab.getRemoteChartConfiguration().getName());
    //            }
    //
    //        }
    //        else {
    //            ObservableList<EnvironmentModel> environments = model.getEnvironments();
    //            targetEnvironments.addAll(environments);
    //        }
    //
    //        // Now we go through each environment and make the subscriptions
    //
    //        final AggregationKey[] keyArray = CollectionUtils.toArray(aggregationKeys, AggregationKey.class);
    //
    //        for (EnvironmentModel environment : targetEnvironments) {
    //            ObservableList<HubConnectionModel> hubs = environment.getHubConnectionModels();
    //            for (HubConnectionModel hubConnectionModel : hubs) {
    //                SocketClientManager socketClientManager = hubConnectionModel.getSocketClientManager();
    //
    //                final SocketClient client = socketClientManager.getClient();
    //
    //                // Turn the key array into a channel name array
    //                String[] channelNames = new String[keyArray.length];
    //                for (int i = 0; i < channelNames.length; i++) {
    //                    channelNames[i] = keyArray[i].getChannel();
    //                }
    //
    //                client.subscribe(new Destination<ChannelMessage>() {
    //                    @Override public void send(ChannelMessage t) {
    //                        AggregatedPatternData data = (AggregatedPatternData) t.getPayload();
    //                        Stream<ChunkedResult> stream = resultHandlers.get(data.getKey());
    //                        if (stream != null) {
    //                            stream.send(new ChunkedResult(data.getTime(), data.getInterval(), data.getValue(), data.getSeries()));
    //                        }
    //                    }
    //                }, channelNames);
    //
    //            }
    //        }
    //
    //    }

    private void installMouseHandlerAndMenusForTabbedPane(final JTabbedPane tabbedPane) {
        MouseListener handler = findUIMouseListener(tabbedPane);
        tabbedPane.removeMouseListener(handler);
        tabbedPane.addMouseListener(new MouseListenerWrapper(handler) {
            @Override public void handleRightMouse(MouseEvent e) {

                Point point = e.getPoint();
                final int tabIndex = ((TabbedPaneUI) tabbedPane.getUI()).tabForCoordinate(tabbedPane, point.x, point.y);
                Component clickedTab = tabbedPane.getComponentAt(tabIndex);

                if (clickedTab instanceof DetailedLogEventTablePanel) {
                    final DetailedLogEventTablePanel detailedLogEventTablePanel = (DetailedLogEventTablePanel) clickedTab;
                    if (detailedLogEventTablePanel.isHistoricalView()) {
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem menuItem = new JMenuItem("Close tab");
                        menuItem.addActionListener(new ActionListener() {
                            @Override public void actionPerformed(ActionEvent e) {
                                detailedLogEventTablePanel.close();
                                tabbedPane.removeTabAt(tabIndex);
                            }
                        });

                        menu.add(menuItem);
                        menu.setLocation(point);
                        menu.setVisible(true);

                        menu.show(tabbedPane, point.x, point.y);
                        detailedLogEventTablePanel.setComponentPopupMenu(menu);
                    }
                }

            }
        });
    }

    // http://stackoverflow.com/questions/8080438/mouseevent-of-jtabbedpane
    private MouseListener findUIMouseListener(JComponent tabbedPane) {
        MouseListener[] listeners = tabbedPane.getMouseListeners();
        for (MouseListener l : listeners) {
            if (l.getClass().getName().contains("$Handler")) {
                return l;
            }
        }
        return null;
    }

    private void handleTabChange() {
        DetailedLogEventTablePanel selected = getCurrentSelectedTabx();
        if (selected != null) {
            // Its possible for no tabs to be selected
            if (previouslySelectedPane != null) {
                // Reset the state, this will redraw the background
                // correctly now its not selected
                previouslySelectedPane.getHighestStateSinceLastSelected().set(0);
            }

            // Setting this value to zero will make the tab clear again
            selected.getHighestStateSinceLastSelected().set(0);
            previouslySelectedPane = selected;

            autoLockWarnings.setEnabled(true);
            autoLockWarnings.setSelected(selected.getEnvironmentModel().isAutoLocking());

            setupWriteOutputLogState(selected);

            EnvironmentModel environmentModel = selected.getEnvironmentModel();
            if (environmentModel.isRepoEnabled()) {
                viewRepo.setVisible(true);
            } else {
                viewRepo.setVisible(false);
            }

            if (selected.isExportingBinary()) {
                setToStopExport();
            } else {
                setToStartExport();
            }

            demoSourceMenuItem.setSelected(selected.isDemoSourceActive());
            dummySourceMenuItem.setSelected(selected.isDummySourceActive());
        } else {
            autoLockWarnings.setEnabled(false);
            writeOutputLog.setEnabled(false);
            changeOutputLogDestination.setEnabled(false);
        }
    }

    private void setupWriteOutputLogState(DetailedLogEventTablePanel selected) {
        if (selected.getEnvironmentModel().getOutputLogConfiguration() == null) {
            writeOutputLog.setEnabled(false);
            changeOutputLogDestination.setEnabled(false);
        } else {
            writeOutputLog.setEnabled(true);
            writeOutputLog.setSelected(selected.getEnvironmentModel().isWriteOutputLog());
            changeOutputLogDestination.setEnabled(true);
        }
    }

    private void createDashboard(LoggingFrontendModel model) {
        DashboardPanel dashboardPanel = new DashboardPanel();
        dashboardPanel.setDashboardSelectionListener(new DashboardSelectionListener() {
            @Override public void onSelected(EnvironmentModel model, EnvironmentLevelStatsModel.Level level) {
                int tabIndex = findIndexForEnvironmentDetailView(model.getName());
                tabbedPane.setSelectedIndex(tabIndex);
                DetailedLogEventTablePanel panel = (DetailedLogEventTablePanel) tabbedPane.getComponentAt(tabIndex);

                Level realLevel;

                if (level == EnvironmentLevelStatsModel.Level.Severe) {
                    realLevel = Level.SEVERE;
                } else if (level == EnvironmentLevelStatsModel.Level.Warning) {
                    realLevel = Level.WARNING;
                } else {
                    realLevel = Level.INFO;
                }

                panel.setQuickLevelFilter(realLevel);
            }
        });
        dashboardPanel.bind(model.getEnvironments());
        tabbedPane.addTab("Dashboard", dashboardPanel);
    }

    protected int findIndexForEnvironmentDetailView(String name) {
        int foundIndex = -1;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component component = tabbedPane.getComponentAt(i);
            if (component instanceof DetailedLogEventTablePanel) {
                DetailedLogEventTablePanel detailedLogEventTablePanel = (DetailedLogEventTablePanel) component;
                if (detailedLogEventTablePanel.getEnvironmentModel().getName().equals(name)) {
                    foundIndex = i;
                    break;
                }
            }
        }

        return foundIndex;

    }

    public void setParentFrame(JFrame frame) {
        this.mainFrame = frame;
    }

    private void addEnvironmentLoggingDetailsTab(EnvironmentModel environmentModel) {
        DetailedLogEventTablePanel tablePanel = createNewDetailedLogEventTablePanel(environmentModel);
        String name = environmentModel.get(EnvironmentModel.Fields.Name);
        int tabIndex = tabbedPane.getTabCount();
        tabbedPane.addTab(name, tablePanel);
        TabColourManager.bind(tabbedPane, tabIndex, tablePanel, environmentModel);
    }

    private JMenuItem addMenuItem(JMenu menu, String menuItemText, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(menuItemText);
        menuItem.addActionListener(actionListener);
        menu.add(menuItem);
        return menuItem;
    }

    private DetailedLogEventTablePanel createNewDetailedLogEventTablePanel(EnvironmentModel environmentModel) {

        DetailedLogEventTablePanel tablePanel = new DetailedLogEventTablePanel(menuBar,
                                                                               environmentModel.getName(),
                                                                               environmentModel.getEventController(),
                                                                               timeProvider,
                                                                               proxy.getLoggingFrontendConfiguration()
                                                                                    .isShowHeapSlider());
        tablePanel.bind(environmentModel);
        tablePanel.setSelectedRowFormat(model.getSelectedRowFormat());

        ObservableList<com.logginghub.logging.frontend.model.HighlighterModel> highlighters2 = environmentModel.getHighlighters();
        for (com.logginghub.logging.frontend.model.HighlighterModel highlighterModel : highlighters2) {
            tablePanel.addHighlighter(highlighterModel);
        }

        tablePanel.setName(LOG_EVENT_PANEL + environmentModel.getName());

        environmentModel.addLogEventListener(tablePanel);
        tablePanel.setDynamicSettings(proxy.getDynamicSettings());
        tablePanel.setEnvironmentModel(environmentModel);

        return tablePanel;
    }

    private List<DetailedLogEventTablePanel> getDetailedLogEventTablePanels() {

        List<DetailedLogEventTablePanel> list = new ArrayList<DetailedLogEventTablePanel>();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component component = tabbedPane.getComponentAt(i);
            if (component instanceof DetailedLogEventTablePanel) {
                DetailedLogEventTablePanel detailedLogEventTablePanel = (DetailedLogEventTablePanel) component;
                list.add(detailedLogEventTablePanel);
            }
        }
        return list;
    }

    public DetailedLogEventTablePanel getDetailedLogEventTablePanelForEnvironment(String environment) {
        DetailedLogEventTablePanel found = null;
        List<DetailedLogEventTablePanel> detailedLogEventTablePanels = getDetailedLogEventTablePanels();
        for (DetailedLogEventTablePanel detailedLogEventTablePanel : detailedLogEventTablePanels) {
            if (detailedLogEventTablePanel.getEnvironmentModel().getName().equals(environment)) {
                found = detailedLogEventTablePanel;
                break;
            }
        }
        return found;
    }

    private DetailedLogEventTablePanel getCurrentSelectedTabx() {
        DetailedLogEventTablePanel selected;
        Component selectedComponent = tabbedPane.getSelectedComponent();
        if (selectedComponent instanceof DetailedLogEventTablePanel) {
            selected = (DetailedLogEventTablePanel) selectedComponent;
        } else {
            selected = null;
        }

        return selected;
    }

    private void setupFileMenu(JMenu fileMenu) {

        JMenuItem connectionManager = new JMenuItem("Connection manager...");
        connectionManager.setAccelerator(KeyStroke.getKeyStroke('c', InputEvent.CTRL_DOWN_MASK));
        connectionManager.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showConnectionManager();
            }
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke('e', InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        fileMenu.add(exitItem);
    }

    private void setupMenuBar() {
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');

        addMenuItem(fileMenu, "Import binary", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                importBinaryFile();
            }
        });

        exportBinaryMenuItem = addMenuItem(fileMenu, "Export binary", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (exportBinaryMenuItem.getText().equals("Export binary")) {
                    exportBinaryFile();
                    setToStopExport();
                } else {
                    stopExportBinary();
                    setToStartExport();
                }
            }

        });

        addMenuItem(fileMenu, "Open binary folder", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                openBinaryFolder();
            }
        });

        editMenu = new JMenu("Edit");
        editMenu.setMnemonic('e');

        viewMenu = new JMenu("View");
        viewMenu.setMnemonic('v');

        JMenu experimentalMenu = new JMenu("Experimental");
        experimentalMenu.setMnemonic('x');

        if (proxy.getLoggingFrontendConfiguration().isShowChartingEditor()) {
            addMenuItem(editMenu, "Charting editor...", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showChartingEditor();
                }
            });
        }

        addMenuItem(editMenu, "Clear chart data", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearChartData();
            }
        });

        addMenuItem(editMenu, "Save chart images", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveChartImages();
            }
        });

        addMenuItem(editMenu, "Save chart data", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveChartData();
            }
        });

        if (model.isPopoutCharting()) {
            addMenuItem(editMenu, "Show charting popout", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (proxy.getLoggingFrontendConfiguration().isShowOldCharting()) {
                        if (!chartingPopoutFrameOldx.isVisible()) {
                            chartingPopoutFrameOldx.setVisible(true);
                        }
                    }

                    if (!chartingPopoutFrameNew.isVisible()) {
                        chartingPopoutFrameNew.setVisible(true);
                    }
                }
            });
        }

        if (proxy.getLoggingFrontendConfiguration().isShowExperimental()) {
            addMenuItem(experimentalMenu, "History viewer...", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showHistoryViewer();
                }
            });
        }

        if (proxy.getLoggingFrontendConfiguration().isShowExperimental()) {
            addMenuItem(experimentalMenu, "Stack viewer...", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showStackViewer();
                }
            });
        }

        if (proxy.getLoggingFrontendConfiguration().isShowExperimental()) {
            addMenuItem(experimentalMenu, "Telemetry viewer...", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showTelemetryViewer();
                }
            });
        }

        if (proxy.getLoggingFrontendConfiguration().isShowExperimental()) {
            addMenuItem(experimentalMenu, "Visualisation viewer...", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showVisualisation();
                }
            });
        }

        JMenu sourcesMenu = new JMenu("Sources");
        sourcesMenu.setMnemonic('s');

        hubLevelFiltering = new JCheckBoxMenuItem("Hub filtering");
        hubLevelFiltering.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                setServerSideFiltering(hubLevelFiltering.isSelected());
            }
        });

        sourcesMenu.add(hubLevelFiltering);
        sourcesMenu.addSeparator();

        autoLockWarnings = new JCheckBoxMenuItem("Auto-lock warnings");
        autoLockWarnings.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                setAutoLockWarning(autoLockWarnings.isSelected());
            }
        });

        sourcesMenu.add(autoLockWarnings);
        sourcesMenu.addSeparator();

        autoScroll = new JCheckBoxMenuItem("Auto-scroll");
        autoScroll.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                setAutoScroll(autoScroll.isSelected());
            }
        });

        sourcesMenu.add(autoScroll);
        sourcesMenu.addSeparator();

        writeOutputLog = new JCheckBoxMenuItem("Write output log");
        writeOutputLog.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                setWriteOutputLog(writeOutputLog.isSelected());
            }
        });
        writeOutputLog.setMnemonic('w');
        sourcesMenu.add(writeOutputLog);

        changeOutputLogDestination = new JMenuItem(CHANGE_OUTPUT_LOG_DESTINATION);
        changeOutputLogDestination.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                changeOutputLogDestination();
            }
        });
        changeOutputLogDestination.setMnemonic('c');

        sourcesMenu.add(changeOutputLogDestination);
        sourcesMenu.addSeparator();

        dummySourceMenuItem = new JCheckBoxMenuItem("View dummy source");
        dummySourceMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (dummySourceMenuItem.isSelected()) {
                    activateDummySource();
                } else {
                    deactivateDummySource();
                }
            }
        });

        sourcesMenu.add(dummySourceMenuItem);

        demoSourceMenuItem = new JCheckBoxMenuItem("View demo source");
        demoSourceMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (demoSourceMenuItem.isSelected()) {
                    activateDemoSource();
                } else {
                    deactivateDemoSource();
                }
            }
        });

        sourcesMenu.add(demoSourceMenuItem);

        viewRepo = new JMenuItem("Repository Search");
        viewRepo.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                showRepoSearchDialog();
            }
        });

        editMenu.add(viewRepo);

        editMenu.addSeparator();
        horizontalDetailView = new JCheckBoxMenuItem("Horizontal detail view", true);
        horizontalDetailView.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                setDetailViewOrientation(horizontalDetailView.isSelected());
            }
        });

        editMenu.add(horizontalDetailView);
        editMenu.addSeparator();

        setupFileMenu(fileMenu);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);

        menuBar.add(sourcesMenu);

        if (proxy.getLoggingFrontendConfiguration().isShowExperimental()) {
            menuBar.add(experimentalMenu);
        }

    }

    protected void setDetailViewOrientation(boolean selected) {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.setDetailPaneOrientation(selected);
        }

        proxy.getDynamicSettings().set("loggingMainPanel.horizontalSplit", selected);
    }

    protected void deactivateDemoSource() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.deactivateDemoSource();
        }
    }

    protected void activateDemoSource() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.activateDemoSource();
        }
    }

    protected void deactivateDummySource() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.deactivateDummySource();
        }
    }

    protected void activateDummySource() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.activateDummySource();
        }
    }

    private void setToStartExport() {
        exportBinaryMenuItem.setText("Export binary");
    }

    private void setToStopExport() {
        exportBinaryMenuItem.setText("Stop exporting");
    }

    protected void openBinaryFolder() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.openBinaryFolder();
        }
    }

    protected void stopExportBinary() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.stopBinaryExport();
        } else {
            JOptionPane.showMessageDialog(this,
                                          "You must have a log tab selected before you can stop exporting binary events");
        }
    }

    protected void exportBinaryFile() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.exportBinaryFile();
        } else {
            JOptionPane.showMessageDialog(this, "You must have a log tab selected before you can export binary events");
        }
    }

    protected void importBinaryFile() {

        String binaryFolder;

        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            binaryFolder = proxy.getDynamicSettings().getString(currentSelectedTabx.getBinaryFileSettingsKey(), ".");
        } else {
            binaryFolder = proxy.getDynamicSettings().getString("binaryFolder", ".");
        }

        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(binaryFolder));
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();

            File parentFile = file.getParentFile();
            if (parentFile != null) {
                proxy.getDynamicSettings().set("binaryFolder", parentFile.getAbsolutePath());
            }

            List<DecodeStrategy> decodeStrategies = new ArrayList<DecodeStrategy>();

            decodeStrategies.add(new DecodeStrategy() {
                final BinaryLogFileReader decoder = new BinaryLogFileReader();

                @Override public boolean canParse(File input) {
                    return decoder.canParse(input);
                }

                @Override public void decode(File file,
                                             StreamListener<LogEventBlockElement> blockListener,
                                             StreamListener<LogEvent> eventListener) throws IOException {
                    decoder.readFileInternal(file, blockListener, eventListener);
                }

                @Override public String getStrategyName() {
                    return "BinaryLogFileReader";
                }
            });

            decodeStrategies.add(new DecodeStrategy() {
                final KryoVersion1Decoder decoder = new KryoVersion1Decoder();

                @Override public boolean canParse(File input) {
                    return decoder.canParse(input);
                }

                @Override public void decode(File file,
                                             StreamListener<LogEventBlockElement> blockListener,
                                             StreamListener<LogEvent> eventListener) throws IOException {
                    decoder.readFileInternal(file, blockListener, eventListener);
                }

                @Override public String getStrategyName() {
                    return "KryoVersion1Decoder";
                }
            });

            boolean parsed = false;

            for (final DecodeStrategy decodeStrategy : decodeStrategies) {
                if (decodeStrategy.canParse(file)) {

                    logger.info("Using decode strategy '{}'", decodeStrategy.getStrategyName());

                    // TODO : replace this stuff with attached streams
                    // final Stream<LogEvent> eventStream = new Stream<LogEvent>();

                    EnvironmentModel fileModel = new EnvironmentModel();
                    fileModel.setName(file.getName());
                    DetailedLogEventTablePanel detailPanel = createNewDetailedLogEventTablePanel(fileModel);
                    tabbedPane.add(fileModel.getName(), detailPanel);
                    tabbedPane.setSelectedComponent(detailPanel);
                    TimeController timeFilterController = detailPanel.getTimeFilterController();
                    final ImportController importHandler = new ImportController(timeFilterController);

                    final StreamListener<LogEventBlockElement> blockHandler = new StreamListener<LogEventBlockElement>() {
                        @Override public void onNewItem(LogEventBlockElement t) {
                            importHandler.addBlock(t);
                        }
                    };

                    detailPanel.bind(importHandler);

                    // DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
                    // if (currentSelectedTabx != null) {
                    // final EnvironmentModel environmentModel =
                    // currentSelectedTabx.getEnvironmentModel();
                    WorkerThread.execute("ImporterThread", new Runnable() {
                        @Override public void run() {
                            try {
                                decodeStrategy.decode(file, blockHandler, importHandler);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    parsed = true;
                    break;
                }
            }

            if (!parsed) {
                JOptionPane.showMessageDialog(this,
                                              "We have nothing that can decode that kind of file, did you select a binary log file?");
            }
        }
    }

    protected void showStackViewer() {

        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {

            MainFrameModule frame = new MainFrameModule();
            frame.setName("Stack Viewer");

//            MenuBarModule menuBar = new MenuBarModule();
//            menuBar.setFrameService(frame);
//            menuBar.setLayoutService(frame);
//
//            TabbedPaneModule tab = new TabbedPaneModule();
//            tab.setLayoutService(frame);

            EnvironmentAdaptor adaptor = new EnvironmentAdaptor(currentSelectedTabx.getEnvironmentModel());

            StackTraceViewModule stack = new StackTraceViewModule();
            stack.setLayoutService(frame);
            stack.setEnvironmentNotificationService(adaptor);
            stack.setMessagingService(adaptor);

            frame.initialise();
//            menuBar.initialise();
//            tab.initialise();
            stack.initialise();

            frame.start();
//            menuBar.start();
//            tab.start();
            stack.start();

        }
    }

    protected void showTelemetryViewer() {

        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {

            MainFrameModule frame = new MainFrameModule();
            frame.setName("Telemetry Viewer");
            frame.setIconResource("/icons/telemetry.png");

            MenuBarModule menuBar = new MenuBarModule();
            menuBar.setFrameService(frame);
            menuBar.setLayoutService(frame);

            EnvironmentAdaptor adaptor = new EnvironmentAdaptor(currentSelectedTabx.getEnvironmentModel());

            TelemetryViewModule telemetryViewModule = new TelemetryViewModule();
            telemetryViewModule.setEnvironmentNotificationService(adaptor);
            telemetryViewModule.setLayoutService(frame);
            telemetryViewModule.setMessagingService(adaptor);

            frame.initialise();
            menuBar.initialise();
            telemetryViewModule.initialise();

            frame.start();
            menuBar.start();
            telemetryViewModule.start();

        }
    }

    protected void showVisualisation() {

        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {

            MainFrameModule frame = new MainFrameModule();
            frame.setName("Visualisation Viewer");

            MenuBarModule menuBar = new MenuBarModule();
            menuBar.setFrameService(frame);
            menuBar.setLayoutService(frame);

            EnvironmentAdaptor adaptor = new EnvironmentAdaptor(currentSelectedTabx.getEnvironmentModel());

            PatterniserModule patterniserModule = new PatterniserModule();

            // Configure the patterniser module with the pattern models from the charting model.
            NewChartingModel chartingModel = model.getChartingModel();
            com.logginghub.utils.observable.ObservableList<PatternModel> patternModels = chartingModel.getPatternModels();
            patterniserModule.getPatterns().addAll(patternModels);
            patterniserModule.setEnvironmentMessagingService(adaptor);

            // Setup the visualiser now we have all the pre-reqs
            VisualisationViewModule visualisationViewModule = new VisualisationViewModule();
            visualisationViewModule.setPatterniserService(patterniserModule, patterniserModule);
            visualisationViewModule.setLayoutService(frame);
            visualisationViewModule.setMessagingService(adaptor);

            frame.initialise();
            menuBar.initialise();
            patterniserModule.initialise();
            visualisationViewModule.initialise();

            frame.start();
            menuBar.start();
            visualisationViewModule.start();
        }
    }

    protected void showHistoryViewer() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {

            MainFrameModule frame = new MainFrameModule();
            frame.setName("History Viewer");

            MenuBarModule menuBar = new MenuBarModule();
            menuBar.setFrameService(frame);
            menuBar.setLayoutService(frame);

            TabbedPaneModule tab = new TabbedPaneModule();
            tab.setLayoutService(frame);

            EnvironmentAdaptor adaptor = new EnvironmentAdaptor(currentSelectedTabx.getEnvironmentModel());

            HistoryViewModule history = new HistoryViewModule();
            history.setName("History View");
            history.setLayoutService(tab);
            history.setEnvironmentNotificationService(adaptor);
            history.setMessagingService(adaptor);

            frame.initialise();
            menuBar.initialise();
            tab.initialise();
            history.initialise();

            frame.start();
            menuBar.start();
            tab.start();
            history.start();

        }
    }

    protected void showChartingEditor() {

        ChartingTreeEditorView treeEditorView = new ChartingTreeEditorView();

        // Creating a new controller so we dont overwrite anything in the "real"
        // controller
        NewChartingController temporaryController = new NewChartingController(chartingController.getModel(),
                                                                              timeProvider);

        // Bind the new controller to the event stream
        this.chartingController.getLogEventMultiplexer()
                               .addLogEventListener(temporaryController.getLogEventMultiplexer());

        treeEditorView.bind(temporaryController);
        JDialog dialog = new JDialog();
        dialog.setIconImage(Icons.load("/icons/charting.png").getImage());
        dialog.setTitle("Charting Editor");
        dialog.add(treeEditorView);
        int width = (int) (mainFrame.getWidth() * 0.9);
        int height = (int) (mainFrame.getHeight() * 0.9f);

        int offsetX = (int) (mainFrame.getWidth() * 0.1) / 2;
        int offsetY = (int) (mainFrame.getHeight() * 0.1f) / 2;

        dialog.setLocation(mainFrame.getX() + offsetX, mainFrame.getY() + offsetY);
        dialog.setSize(width, height);
        dialog.setName("Charting Editor");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.setVisible(true);

        this.chartingController.getLogEventMultiplexer()
                               .removeLogEventListener(temporaryController.getLogEventMultiplexer());

    }

    protected void changeOutputLogDestination() {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            TimestampVariableRollingFileLogger outputLogger = currentSelectedTabx.getOutputLogger();
            if (outputLogger != null) {

                existingOutputLogFilenames.add(outputLogger.getFileName());
                DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
                for (String filename : existingOutputLogFilenames) {
                    comboBoxModel.addElement(filename);
                }

                final JComboBox combo = new JComboBox(comboBoxModel);
                combo.setName(FILENAME_COMBO);
                combo.setEditable(true);
                combo.setSelectedItem(outputLogger.getFileName());

                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        combo.getEditor().selectAll();
                        combo.requestFocus();
                    }
                });

                int result = JOptionPane.showConfirmDialog(this,
                                                           combo,
                                                           "Choose a new log file name (without the file extention) : ",
                                                           JOptionPane.OK_CANCEL_OPTION,
                                                           JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String filename = comboBoxModel.getSelectedItem().toString();
                    logger.info("Changing output log filename to '{}'", filename);
                    outputLogger.close();
                    outputLogger.setFileName(filename);
                    existingOutputLogFilenames.add(filename);
                } else {
                    logger.info("Changing output log filename was cancelled");
                }
            }
        }
    }

    protected void showRepoSearchDialog() {

        DetailedLogEventTablePanel detailPanel = getCurrentSelectedTabx();

        // Workaround for single environment mode
        if (detailPanel == null && getDetailedLogEventTablePanels().size() == 1) {
            detailPanel = getDetailedLogEventTablePanels().get(0);
        }

        if (detailPanel != null) {

            // EnvironmentModel environmentModel =
            // detailPanel.getEnvironmentModel();
            // NettyClientRepository repo = new NettyClientRepository();
            // String connectionPoints =
            // environmentModel.getRepoConnectionPoints();
            // repo.addConnectionPoints(NetUtils.toInetSocketAddressList(connectionPoints,
            // NettyClientRepository.defaultPort));
            // repo.start();
            //
            // RepositorySearchDialog dialog = new RepositorySearchDialog();
            // dialog.setRepositoryInterface(repo);
            // dialog.setSize((int) (getWidth() * 0.8), (int) (getHeight() *
            // 0.8));
            // dialog.setModal(false);
            // dialog.show(this);
        }

    }

    private void setupHelpMenu(JFrame mainFrame) {

        JMenu helpMenu = new JMenu("Help");

        JMenuItem onlineHelp = new JMenuItem("Vertex Labs Wiki");
        onlineHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BrowserUtils.browseTo(URI.create("http://www.vertexlabs.co.uk/vllogging/frontend"));
            }
        });
        helpMenu.add(onlineHelp);

        JMenuItem about = new JMenuItem("About the Logging Front End");
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAbout();
            }
        });
        helpMenu.addSeparator();
        helpMenu.add(about);

        menuBar.add(helpMenu);

        mainFrame.setJMenuBar(menuBar);
    }

    protected void showAbout() {

        JPanel aboutPanel = new JPanel(new MigLayout());

        ImageIcon image = new ImageIcon(FileUtils.readAsBytes(ResourceUtils.openStream("/icons/LoggingHubLogo.png")));
        JLabel label = new JLabel(image);
        aboutPanel.add(label);
        JLabel jLabel = new JLabel("Logging Hub Viewer");
        jLabel.setFont(Font.decode("Arial-BOLD-24"));
        aboutPanel.add(jLabel, "wrap");

        Properties p = new Properties();
        String version;
        try {
            p.load(ResourceUtils.openStream("/META-INF/maven/com.logginghub/vl-logging-frontend/pom.properties"));
            version = p.getProperty("version");
        } catch (Exception e1) {
            version = "Failed to load version from embedded maven properties";
        }
        JLabel versionLabel = new JLabel(version, JLabel.CENTER);

        versionLabel.setFont(Font.decode("Arial-18"));
        aboutPanel.add(versionLabel, "align center, span 2, wrap");
        aboutPanel.add(new JLabel(""), "align center, span 2, wrap");

        JLabel linkLabel = new JLabel("www.logginghub.com");
        linkLabel.setOpaque(true);
        linkLabel.setFont(Font.decode("Arial-16"));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                BrowserUtils.browseTo(URI.create("http://www.logginghub.com/"));
            }
        });
        aboutPanel.add(linkLabel, "align center, span 2, wrap");

        JOptionPane.showMessageDialog(this, aboutPanel, "About", JOptionPane.PLAIN_MESSAGE);
    }

    public void setInitialPropertyValues() {
        boolean filteringOn = proxy.getDynamicSettings().getBoolean("loggingMainPanel.hubFiltering", false);
        hubLevelFiltering.setSelected(filteringOn);
        setServerSideFiltering(filteringOn);

        boolean autoScrollProperty = proxy.getDynamicSettings().getBoolean("loggingMainPanel.autoScroll", true);
        autoScroll.setSelected(autoScrollProperty);
        setAutoScroll(autoScrollProperty);

        boolean horinzontalSplit = proxy.getDynamicSettings().getBoolean("loggingMainPanel.horizontalSplit", true);
        horizontalDetailView.setSelected(horinzontalSplit);
        setDetailViewOrientation(horinzontalSplit);
    }

    private void showModel(JComponent viewModel) {
        tabbedPane.addTab("Log view", viewModel);
        revalidate();
        doLayout();
    }

    private void updateHubSideFilter(int levelFilter) {
        if (serversideFilteringEnabled) {
            sendFilterToHubs(levelFilter);
        }
    }

    public void stopConnections(EnvironmentModel environmentModel) {
        logger.info("Stopping connections for environment '{}'", environmentModel.getName());

        ObservableList<HubConnectionModel> hubs = environmentModel.getHubConnectionModels();
        for (final HubConnectionModel hubModel : hubs) {
            hubModel.close();
        }
    }

    public void startConnections(final EnvironmentModel environmentModel) {
        logger.info("Starting connections for environment '{}'", environmentModel.getName());

        int pid = ProcessUtils.getPid();

        ObservableList<HubConnectionModel> hubs = environmentModel.getHubConnectionModels();
        for (final HubConnectionModel hubModel : hubs) {

            if (hubModel.getSocketClientManager() == null) {
                final SocketClient socketClient = new SocketClient();
                SocketClientManager socketClientManager = new SocketClientManager(socketClient);
                socketClientManager.setReconnectionTime(0);

                String hubChannel = hubModel.get(HubConnectionModel.Fields.Channel);
                if (hubChannel == null) {
                    hubChannel = environmentModel.get(EnvironmentModel.Fields.Channel);
                }

                if (hubChannel != null) {
                    socketClient.setAutoGlobalSubscription(false);
                    socketClient.addAutoSubscription(hubChannel);
                }

                socketClient.setAutoSubscribe(true);
                socketClient.setName("SwingFrontEnd");
                socketClient.setPid(pid);

                if (environmentModel.isClustered()) {
                    socketClient.addConnectionPoints(hubModel.getClusteredConnectionPoints());
                } else {
                    final InetSocketAddress address = new InetSocketAddress(hubModel.getHost(), hubModel.getPort());
                    socketClient.addConnectionPoint(address);
                }

                socketClientManager.addSocketClientManagerListener(new SocketClientManagerListener() {
                    public void onStateChanged(State fromState, State toState) {
                        InetSocketAddress address = socketClient.getConnector()
                                                                .getConnectionPointManager()
                                                                .getCurrentConnectionPoint();
                        switch (toState) {
                            case Connected:
                                logger.info("Socket client manager - connected to '{}'", address);
                                hubModel.setConnectionState(HubConnectionModel.ConnectionState.Connected);
                                sendHistoricalIndexRequests(environmentModel);
                                break;
                            case Connecting:
                                logger.fine("Socket client manager - connecting to '{}'", address);
                                hubModel.setConnectionState(HubConnectionModel.ConnectionState.AttemptingConnection);
                                break;
                            case NotConnected:
                                logger.fine("Socket client manager - not connected to '{}'", address);
                                hubModel.setConnectionState(HubConnectionModel.ConnectionState.NotConnected);
                                break;
                        }

                        ConnectionStateChangedEvent event = new ConnectionStateChangedEvent(toState,
                                                                                            hubModel.getHost() + ":" + hubModel
                                                                                                    .getPort());
                        environmentModel.getConnectionStateStream().send(event);
                    }
                });

                socketClient.addLogEventListener(hubModel);

                logger.info("Starting socket client manager for connection point '{}'", hubModel);
                socketClientManager.start();
                hubModel.setSocketClientManager(socketClientManager);
            }
        }


        configureAggregatedPatternDataSubscriptions();

    }

    private void sendHistoricalIndexRequests(EnvironmentModel environmentModel) {
        List<DetailedLogEventTablePanel> detailedLogEventTablePanels = getDetailedLogEventTablePanels();
        for (DetailedLogEventTablePanel detailedLogEventTablePanel : detailedLogEventTablePanels) {
            EnvironmentModel panelModel = detailedLogEventTablePanel.getEnvironmentModel();
            if (panelModel != null && panelModel.getName().equals(environmentModel.getName())) {
                detailedLogEventTablePanel.sendHistoricalIndexRequest();
            }
        }

    }

    private void configureAggregatedPatternDataSubscriptions() {
        List<RemoteChartConfiguration> remoteCharting = proxy.getLoggingFrontendConfiguration().getRemoteCharting();
        for (RemoteChartConfiguration remoteChartConfiguration : remoteCharting) {
            configureRemoteChartingTab(remoteChartConfiguration);
        }
    }

    protected void clearChartData() {
        for (OldChartingPanel chartingPanel : chartingPanelsx) {
            chartingPanel.clearChartData();
        }

        // chartingController.clearChartData();
        newChartingView.clearChartData();
        chartingController.clearChartData();
    }

    protected void saveChartData() {
        for (OldChartingPanel chartingPanel : chartingPanelsx) {
            chartingPanel.saveChartData();
        }

        newChartingView.saveChartData();
        // chartingController.saveChartData();
    }

    protected void saveChartImages() {
        for (OldChartingPanel chartingPanel : chartingPanelsx) {
            chartingPanel.saveChartImages();
        }

        // chartingController.saveChartImages();
        newChartingView.saveChartImages();
    }

    protected void setAutoLockWarning(boolean selected) {

        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.setAutoLockWarning(selected);
        }

        // List<DetailedLogEventTablePanel> detailedLogEventTablePanels =
        // getDetailedLogEventTablePanels();
        // for (DetailedLogEventTablePanel detailedLogEventTablePanel :
        // detailedLogEventTablePanels) {
        // detailedLogEventTablePanel.setAutoLockWarning(selected);
        // }
        // proxy.getDynamicSettings().set("loggingMainPanel.autoLockWarning",
        // selected);
    }

    protected void setWriteOutputLog(boolean selected) {
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            currentSelectedTabx.getEnvironmentModel().setWriteOutputLog(selected);
            currentSelectedTabx.setWriteOutputLog(selected);
        }
    }

    protected void setAutoScroll(boolean selected) {
        List<DetailedLogEventTablePanel> detailedLogEventTablePanels = getDetailedLogEventTablePanels();
        for (DetailedLogEventTablePanel detailedLogEventTablePanel : detailedLogEventTablePanels) {
            detailedLogEventTablePanel.setAutoScroll(selected);
        }
        proxy.getDynamicSettings().set("loggingMainPanel.autoScroll", selected);
    }

    private void setServerSideFiltering(boolean selected) {
        logger.info("Server side filtering menu option changed to {}", selected);
        serversideFilteringEnabled = selected;

        if (selected) {
            if (firstDetailPanel != null) {
                int levelFilter = firstDetailPanel.getLevelFilter();
                sendFilterToHubs(levelFilter);
            }
        } else {
            // If the item isn't selected, dont send anything to the hubs. This
            // allows backwards compatibility
        }

        proxy.getDynamicSettings().set("loggingMainPanel.hubFiltering", selected);
    }

    private void sendFilterToHubs(int levelFilter) {

        logger.info("Sending server side filtering value to hub, level is {}", Level.parse("" + levelFilter));

        ObservableList<EnvironmentModel> environments = model.getEnvironments();
        for (EnvironmentModel environmentModel : environments) {
            ObservableList<HubConnectionModel> hubConnectionModels = environmentModel.getHubConnectionModels();
            for (HubConnectionModel hubConnectionModel : hubConnectionModels) {
                try {
                    SocketClientManager socketClientManager = hubConnectionModel.getSocketClientManager();
                    if (socketClientManager != null) {
                        SocketClient client = socketClientManager.getClient();
                        logger.info("Sending server side filtering value to {}",
                                    client.getConnector().getConnectionPointManager().getCurrentConnectionPoint());
                        client.setLevelFilter(levelFilter);
                    } else {
                        // Probably means this environment exists but hasn't
                        // been started
                    }
                } catch (LoggingMessageSenderException e) {
                    // Maybe we aren't connected... dont worry about this.
                }
            }
        }
    }

    protected void showConnectionManager() {
        final JDialog dialog = new JDialog(mainFrame, "Connection manager");
        ConnectionManagerPanel connectionManagerPanel = new ConnectionManagerPanel();
        connectionManagerPanel.setListener(new ConnectionManagerListener() {
            @Override public void onOpenEnvironment(EnvironmentConfiguration environmentConfiguration) {
                dialog.dispose();

                ObservableList<EnvironmentModel> environments = model.getEnvironments();
                for (EnvironmentModel environmentModel : environments) {
                    if (environmentModel.get(EnvironmentModel.Fields.Name).equals(environmentConfiguration.getName())) {
                        addEnvironmentLoggingDetailsTab(environmentModel);
                        startConnections(environmentModel);
                    }
                }
            }
        });
        connectionManagerPanel.populate(proxy.getLoggingFrontendConfiguration());
        dialog.getContentPane().add(connectionManagerPanel);
        dialog.setSize(500, 400);
        dialog.setModal(true);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    public DetailedLogEventTablePanel getFirstDetailPanel() {
        return firstDetailPanel;
    }

    public void setConfigurationProxy(ConfigurationProxy proxy) {
        this.proxy = proxy;
    }

    public void closeOutputLogs() {

        List<DetailedLogEventTablePanel> detailedLogEventTablePanels = getDetailedLogEventTablePanels();
        for (DetailedLogEventTablePanel detailedLogEventTablePanel : detailedLogEventTablePanels) {
            detailedLogEventTablePanel.closeOutputLog();
        }
    }

    public JFrame getChartingPopoutFrameOld() {
        return chartingPopoutFrameOldx;
    }

    public JFrame getChartingPopoutFrameNew() {
        return chartingPopoutFrameNew;
    }

    public void setMenuBar(JMenuBar menuBar) {
        setupMenuBar();
        mainFrame.setJMenuBar(menuBar);
    }

    public NewChartingController getChartingController() {
        return chartingController;
    }

    @Override public void addMenuItem(String topLevel, String secondLevel, ActionListener action) {
        // TODO : this is a hack to expose the legacy menu to the modules world
        if (topLevel.equals("Edit")) {
            JMenuItem item = new JMenuItem(secondLevel);
            item.addActionListener(action);
            editMenu.add(item);
        } else if (topLevel.equals("View")) {
            JMenuItem item = new JMenuItem(secondLevel);
            item.addActionListener(action);
            viewMenu.add(item);
        }
    }

    //    public void startAggregatedPatternSubscriptions() {
    //        for (RemoteChartingTab remoteChartingTab : remoteChartingTabs) {
    //            startRemoteChartingTab(remoteChartingTab);
    //        }
    //    }

    public OffsetableSystemTimeProvider getTimeProvider() {
        return timeProvider;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    @Override public List<SocketClient> getDirectAccess() {
        List<SocketClient> clients = new ArrayList<SocketClient>();
        DetailedLogEventTablePanel currentSelectedTabx = getCurrentSelectedTabx();
        if (currentSelectedTabx != null) {
            EnvironmentModel environmentModel = currentSelectedTabx.getEnvironmentModel();
            ObservableList<HubConnectionModel> hubConnectionModels = environmentModel.getHubConnectionModels();
            for (HubConnectionModel hubConnectionModel : hubConnectionModels) {
                SocketClient client = hubConnectionModel.getSocketClientManager().getClient();
                clients.add(client);
            }
        }

        return clients;

    }

    public static abstract class MouseListenerWrapper implements MouseListener {

        private MouseListener delegate;

        public MouseListenerWrapper(MouseListener delegate) {
            this.delegate = delegate;
        }

        @Override public void mouseClicked(MouseEvent e) {
            delegate.mouseClicked(e);
        }

        @Override public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                handleRightMouse(e);
                return;
            }
            delegate.mousePressed(e);
        }

        @Override public void mouseReleased(MouseEvent e) {
            delegate.mouseReleased(e);
        }

        @Override public void mouseEntered(MouseEvent e) {
            delegate.mouseEntered(e);
        }

        @Override public void mouseExited(MouseEvent e) {
            delegate.mouseExited(e);
        }

        public abstract void handleRightMouse(MouseEvent e);
    }
}
