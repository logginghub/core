package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;
import java.io.File;
import java.util.List;

import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.modules.ViewDetails;
import com.logginghub.logging.frontend.visualisations.configuration.EmitterConfig;
import com.logginghub.logging.frontend.visualisations.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.visualisations.configuration.VectorConfig;
import com.logginghub.logging.frontend.visualisations.configuration.VisualisationConfig;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.FileUtils.FileWatcherListener;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.SystemErrStream;

public class Visualiser {

    private static Logger logger = Logger.getLoggerFor(Visualiser.class);
    private static VisualiserModel model;
    private static FireworksController controller;
    private static LibGDXSpritesView view;
    private static TradingDemoLoggingConnector loggingConnector;
    private static TestLoggingConnector testConnector;

    public static void main(String[] args) {

        SystemErrStream.gapThreshold = 1010;

        String config;
        if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists()) {

                logger.info("Using configuration '{}'", file.getAbsolutePath());

                FileUtils.watchFile(file, new FileWatcherListener() {
                    public void onFileChanged(File file) {
                        System.out.println("Reloading configuration");
                        reinitialise(file);
                    }
                });

                initialise(file);
            }
            else {
                logger.info("Using classpath configuration '{}", args[0]);
                VisualisationConfig visualisationConfig;
                visualisationConfig = VisualisationConfig.loadConfiguration(args[0]);
                initialise(visualisationConfig);
            }
        }
        else {

            VisualisationConfig visualisationConfig;
            if (Boolean.getBoolean("demo")) {
                logger.info("Using demo configuration");
                visualisationConfig = VisualisationConfig.loadConfiguration("config/demo.xml");
            }
            else {
                logger.info("Using default configuration - pass the path to a config xml on the command line to change this");
                visualisationConfig = VisualisationConfig.loadConfiguration("config/default.xml");
            }

            initialise(visualisationConfig);
        }
    }

    private static VisualisationConfig createDemoConfig() {
        EnvironmentConfiguration environmentConfig = new EnvironmentConfiguration();
        environmentConfig.getHubs().add(new HubConfiguration("demo"));
        EmitterConfig emitterConfig = new EmitterConfig();
        emitterConfig.getSourcePosition().set(400, 50);
        emitterConfig.getReleaseVelocity().setAngular(90, 20);
        emitterConfig.setName("1");
        emitterConfig.setSizeTimeMultipler(1);
        emitterConfig.setLifeLimit(7);
        emitterConfig.setGravity(-0.01);
        emitterConfig.getDirectionVariance().setAngular(10, 5);
        emitterConfig.getTestConnectionRate().set(0.04, 0.05);

        VisualisationConfig visualisationConfig = new VisualisationConfig();
        visualisationConfig.getEmitters().add(emitterConfig);
        visualisationConfig.getEnvironments().add(environmentConfig);
        return visualisationConfig;
    }

    public void run(File file) {
        initialise(file);
    }

    protected static void initialise(File file) {
        VisualisationConfig config = VisualisationConfig.loadConfiguration(file.getAbsolutePath());
        initialise(config);
    }

    protected static void initialise(VisualisationConfig config) {

        VectorConfig screenSize = config.getScreenSize();

        // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        model = new VisualiserModel(1000, new ViewDetails(screenSize.getXInt(), screenSize.getYInt()));
        controller = new FireworksController(model, config);
        view = new LibGDXSpritesView(model, new ViewDetails(100, 100));

        start(config);

        List<EnvironmentConfiguration> environments = config.getEnvironments();
        for (EnvironmentConfiguration environmentConfiguration : environments) {
            if (environmentConfiguration.getName().equals("demo") && environmentConfiguration.getHubs().isEmpty()) {

                logger.info("Starting up demo logging feed...");

                DemoLoggingConnector connector = new DemoLoggingConnector();
                connector.start(new Generator() {
                    @Override public void generate(Color color, double factor, double velocity, double size) {
                        controller.generate("demo", color, factor, velocity, size);
                    }
                });

                connector.getBus().getPatternisedStream().addListener(new StreamListener<PatternisedLogEvent>() {
                    @Override public void onNewItem(PatternisedLogEvent t) {
                        controller.processPatternisedEvent("demo", t);
                    }
                });

                // Only go once!
                break;
            }
        }

        TimerUtils.everySecond("FPS", new Runnable() {
            int lastFrames = 0;
            long lastParticles = 0;

            @Override public void run() {
                int newFrames = view.getRenderedFrames();
                long newParticles = view.getRenderedParticles();

                int deltaFrames = newFrames - lastFrames;

                long deltaParticles = newParticles - lastParticles;

                logger.info("{} frames/second : {} particles", deltaFrames, StringUtils.format2dp(deltaParticles / (double) deltaFrames));

                lastFrames = newFrames;
                lastParticles = newParticles;
            }
        });

        // view.show();
    }

    protected static void reinitialise(File file) {
        VisualisationConfig config = VisualisationConfig.loadConfiguration(file.getAbsolutePath());
        stop();
        start(config);
    }

    protected static void start(VisualisationConfig config) {
        controller.start(config);
        view.start(config);
    }

    protected static void stop() {
        controller.stop();
    }

}
