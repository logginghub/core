package com.logginghub.logging.frontend;

import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.frontend.images.Icons.IconIdentifier;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.LoggingFrontendController;
import com.logginghub.logging.frontend.model.LoggingFrontendModel;
import com.logginghub.logging.frontend.model.ModelBuilder;
import com.logginghub.logging.frontend.model.ObservableList;
import com.logginghub.logging.frontend.modules.AggregatedDataViewModule;
import com.logginghub.logging.frontend.modules.InstanceViewModule;
import com.logginghub.logging.frontend.modules.PatternisedDataViewModule;
import com.logginghub.logging.frontend.modules.SwingPauseDetectorModule;
import com.logginghub.logging.frontend.modules.VMPauseDetectorModule;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.swingutils.ButtonTabComponent;
import com.logginghub.utils.DelayedAutosavingFileBasedMetadata;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.MainUtils;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.VisualStopwatchController;
import com.logginghub.utils.Xml;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ClassResolver;
import com.logginghub.utils.module.Container2;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableListener;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.Closeable;
import java.io.File;
import java.util.Timer;

public class SwingFrontEnd extends SmartJFrame implements Closeable {
    private LoggingFrontendModel model;

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLoggerFor(SwingFrontEnd.class);
    private LoggingMainPanel mainPanel;
    private String propertiesName;
    // private LoggingFrontendConfiguration configuration;
    private ConfigurationProxy proxy;
    private Timer modelUpdateTimer;

    private LoggingFrontendController controller;

    public SwingFrontEnd() {

        setIcon();
        mainPanel = new LoggingMainPanel();
        mainPanel.setParentFrame(this);
        getContentPane().add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public SwingFrontEnd(ConfigurationProxy proxy) {
        super(proxy.getDynamicSettings());
        this.proxy = proxy;
        logger.info("Creating new swing front end with configuration '{}'...", proxy);
        this.propertiesName = proxy.getPropertiesName();

        setTitle("Logging Front End - " + proxy.getLoggingFrontendConfiguration().getTitle());
        setIcon();

        ModelBuilder builder = new ModelBuilder();
        model = builder.buildModel(proxy.getLoggingFrontendConfiguration());
        setupChartingModel(model);
        startModelUpdateTimer(model);

        controller = new LoggingFrontendController(model);
        controller.loadPersistedQuickFilters();
        controller.startQuickFilterPersistence();

        mainPanel = new LoggingMainPanel();
        mainPanel.setConfigurationProxy(proxy);
        mainPanel.setModel(model, proxy.getDynamicSettings(), this);

        getContentPane().add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        logger.info("Front end instance has been created..");
    }

    private void setIcon() {
        setIconImage(Icons.get(IconIdentifier.LoggingHubLogo).getImage());
    }

    private void setupChartingModel(LoggingFrontendModel model) {

        final NewChartingModel newChartingModel = model.getChartingModel();

        final File config = new File(proxy.getLoggingFrontendConfiguration().getChartingConfigurationFile());
        logger.info("Using charting configuration '{}'", config.getAbsoluteFile());
        if (config.exists()) {
            Xml xml = new Xml(FileUtils.read(config));
            newChartingModel.fromXml(xml.getRoot());
        }

        newChartingModel.addListener(new ObservableListener() {
            @Override public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                logger.info("Charting configuration changed, saving to '{}'", config.getAbsoluteFile());
                String xml = newChartingModel.toXml("chartingModel");
                FileUtils.write(xml, config);
            }
        });

    }

    public LoggingFrontendModel getModel() {
        return model;
    }

    public ConfigurationProxy getProxy() {
        return proxy;
    }

    @Deprecated/**
                * @deprecated only used by some tests
                */
    public void setConfiguration(ConfigurationProxy proxy) {
        this.proxy = proxy;

        ModelBuilder builder = new ModelBuilder();
        model = builder.buildModel(proxy.getLoggingFrontendConfiguration());
        startModelUpdateTimer(model);

        setTitle("Logging Front End - " + proxy.getLoggingFrontendConfiguration().getTitle());

        mainPanel.setConfigurationProxy(proxy);
        mainPanel.setModel(model, proxy.getDynamicSettings(), this);
    }

    private void startModelUpdateTimer(final LoggingFrontendModel model) {
        modelUpdateTimer = TimerUtils.everySecond("Model update timer", new Runnable() {
            @Override public void run() {
                model.updateEachSecond();
            }
        });
    }

    public void stopModelUpdateTimer() {
        modelUpdateTimer.cancel();
    }

    public LoggingMainPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {

        // JFreeChartStackAnalysisViewer.showAnalyser();

        setLAF();

        try {
            mainInternal(args);
        }
        catch (Throwable t) {
            logger.warning(t);
            t.printStackTrace();
            System.exit(-1);
        }
    }

    private static void setLAF() {
//        if (OSUtils.isUnix()) {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())) {
//                    try {
//                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    }
//                    catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    catch (InstantiationException e) {
//                        e.printStackTrace();
//                    }
//                    catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
//                    catch (UnsupportedLookAndFeelException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//            }
//        }
    }

    public static SwingFrontEnd mainInternal(String[] args) {
        Logger.setRootLevel(Logger.info);

        if (Boolean.getBoolean("visualStopwatchController.visible")) {
            VisualStopwatchController.getInstance().show();
        }

        if (Boolean.getBoolean("useCountingEventQueue")) {
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            CountingEventQueue newEventQueue = new CountingEventQueue(eventQueue);
            eventQueue.push(newEventQueue);
        }

        if (Boolean.getBoolean("pauseDetector.enabled")) {
            VMPauseDetectorModule pauseDetector = new VMPauseDetectorModule();
            pauseDetector.start();

            SwingPauseDetectorModule swingPauseDetector = new SwingPauseDetectorModule();
            swingPauseDetector.start();
        }

        // StackCapture stackCapture = new StackCapture();
        // stackCapture.startThreadDumper("AWT-EventQueue-0");

        String propertiesName = "swingFrontEnd";

        File properties = PathHelper.getSettingsFile(propertiesName);

        logger.info("Loading/saving local properties : {}", properties.getAbsolutePath());
        DelayedAutosavingFileBasedMetadata settings = new DelayedAutosavingFileBasedMetadata(properties, 1000);
        if (properties.exists()) {
            settings.load();
        }
        else {
            createDefaultSettings(settings);
        }

        String configurationPath = MainUtils.getStringArgument(args, 0, "logging.frontend.xml");
        logger.info("Loading config from : " + configurationPath);

        String restOfTheURL = StringUtils.beforeLast(configurationPath, "/");
        if (restOfTheURL.length() != 0) {
            restOfTheURL = restOfTheURL + "/";
        }
        String parsers = restOfTheURL + "parsers.xml";

        LoggingFrontendConfiguration loggingFrontendConfiguration = LoggingFrontendConfiguration.loadConfiguration(configurationPath);

        ConfigurationProxy proxy = new ConfigurationProxy(settings, propertiesName, loggingFrontendConfiguration, parsers);

        return mainInternal(proxy);
    }

    private static void intialiseContainerHybrid(SwingFrontEnd swingFrontEnd) {

        try {
            Container2 container = new Container2();

            container.addClassResolver(new ClassResolver() {
                @Override public String resolve(String name) {
                    return "com.logginghub.logging.frontend.modules." + StringUtils.capitalise(name) + "Module";
                }
            });

            final LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();
            container.addExternalInstance(mainPanel);
            container.addExternalInstance(mainPanel.getTimeProvider());

            LayoutService layoutService = new LayoutService() {
                @Override public void add(Component component, String layout) {
                    JTabbedPane tabbedPane = mainPanel.getTabbedPane();
                    
                    int index = tabbedPane.getTabCount();
                    tabbedPane.addTab(layout, component);
                    tabbedPane.setTabComponentAt(index, new ButtonTabComponent(tabbedPane));
                }
            };
            
            PatternisedDataViewModule patternisedDataViewModule = new PatternisedDataViewModule(mainPanel, mainPanel, layoutService);
            container.addInstance(patternisedDataViewModule);
            
            AggregatedDataViewModule aggregatedDataViewModule = new AggregatedDataViewModule(mainPanel, mainPanel, layoutService);
            container.addInstance(aggregatedDataViewModule);
            
            InstanceViewModule instanceViewModule = new InstanceViewModule(mainPanel, mainPanel, layoutService);
            container.addInstance(instanceViewModule);
            
            File file = new File("modules.xml");
            if (file.exists()) {
                container.fromXmlString(FileUtils.read(file));
                container.dump();
                container.initialise();
                container.start();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The is used from unit tests - it doesn't use any shared system resources like the files in
     * the home directory
     * 
     * @param proxy
     * @return
     */
    public static SwingFrontEnd mainInternal(ConfigurationProxy proxy) {
        final SwingFrontEnd swingFrontEnd = new SwingFrontEnd(proxy);
        logger.info("Processing new instance...");

        intialiseContainerHybrid(swingFrontEnd);

        makeVisible(swingFrontEnd);
        swingFrontEnd.start();

        logger.info("New instance successfully started.");
        return swingFrontEnd;
    }

    private static void makeVisible(final SwingFrontEnd swingFrontEnd) {
        logger.info("Making front end visible...");
        swingFrontEnd.setVisible(true);

        LoggingFrontendModel model = swingFrontEnd.getModel();
        if (model.isPopoutCharting()) {
            LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();

            JFrame oldFrame = mainPanel.getChartingPopoutFrameOld();
            JFrame newFrame = mainPanel.getChartingPopoutFrameNew();

            if (oldFrame != null) {
                oldFrame.setVisible(true);
            }

            if (newFrame != null) {
                newFrame.setVisible(true);
            }
        }

        logger.info("Front end should now be visible ({}) at {},{} size {},{} state {} extended state {}",
                    swingFrontEnd.isVisible(),
                    swingFrontEnd.getLocation().x,
                    swingFrontEnd.getLocation().y,
                    swingFrontEnd.getSize().width,
                    swingFrontEnd.getSize().height,
                    swingFrontEnd.getState(),
                    swingFrontEnd.getExtendedState());
    }

    private void start() {
        logger.info("Starting front end...");
        startConnections();
        mainPanel.setInitialPropertyValues();
        mainPanel.start();
        logger.info("Front end started.");
    }

    private static Metadata createDefaultSettings(Metadata data) {

        data.set("frame.state", 0);
        data.set("frame.extendedState", 0);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        data.set("frame.x", (int) (screenSize.width * 0.1));
        data.set("frame.y", (int) (screenSize.height * 0.1));
        data.set("frame.width", (int) (screenSize.width * 0.8));
        data.set("frame.height", (int) (screenSize.height * 0.8));

        return data;
    }

    public void startConnections() {
        logger.info("Starting connections for Swing front end...");
        ObservableList<EnvironmentModel> environments = model.getEnvironments();
        for (EnvironmentModel environmentModel : environments) {
            if (environmentModel.isOpenOnStartup()) {
                mainPanel.startConnections(environmentModel);
            }
        }

//        mainPanel.startAggregatedPatternSubscriptions();

        logger.info("Connections started.");
    }

    @Override public void close() {
        logger.info("Stopping connections for Swing front end");
        if (model != null) {
            ObservableList<EnvironmentModel> environments = model.getEnvironments();
            for (EnvironmentModel environmentModel : environments) {
                mainPanel.stopConnections(environmentModel);
            }
        }

        mainPanel.closeOutputLogs();
    }

    public JFrame getChartingPopoutFrameOld() {
        return mainPanel.getChartingPopoutFrameOld();
    }

    public JFrame getChartingPopoutFrameNew() {
        return mainPanel.getChartingPopoutFrameNew();
    }

}
