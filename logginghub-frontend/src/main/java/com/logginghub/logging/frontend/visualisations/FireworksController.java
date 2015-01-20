package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.monitoringbus.MonitoringBus;
import com.logginghub.logging.frontend.visualisations.configuration.EmitterConfig;
import com.logginghub.logging.frontend.visualisations.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.visualisations.configuration.LevelTriggerConfig;
import com.logginghub.logging.frontend.visualisations.configuration.PatternConfiguration;
import com.logginghub.logging.frontend.visualisations.configuration.TriggerConfig;
import com.logginghub.logging.frontend.visualisations.configuration.VisualisationConfig;
import com.logginghub.logging.logeventformatters.FullEventSingleLineTextFormatter;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.Closer;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.logging.Logger;

public class FireworksController {

    private static final Logger logger = Logger.getLoggerFor(FireworksController.class);
    private VisualiserModel model;
//    private EntitySource entitySource;

    private VisualisationConfig config;

    private List<EmitterController> emitterControllers = new CopyOnWriteArrayList<EmitterController>();

    private ColourInterpolation levelColourInterpolation = new ColourInterpolation(Color.pink, // n/a
                                                                                   Color.pink, // n/a
                                                                                   Color.pink, // n/a
                                                                                   Color.green.darker(), // finest
                                                                                   Color.green, // finer
                                                                                   Color.green.brighter(), // fine
                                                                                   Color.pink, // n/a
                                                                                   Color.white, // config
                                                                                   Color.blue, // info
                                                                                   Color.yellow, // warning
                                                                                   Color.red // severe
    );

    public FireworksController(VisualiserModel model, VisualisationConfig config) {
        this.model = model;
        this.config = config;

        setupEmitters();
        bind(model);
    }

    private void setupEmitters() {

        List<EmitterConfig> emitters = config.getEmitters();
        for (EmitterConfig emitterConfig : emitters) {
            EmitterController controller = new EmitterController(model, config, emitterConfig);

            LevelTriggerConfig levelTrigger = emitterConfig.getLevelTrigger();
            if (levelTrigger != null) {
                String environment = levelTrigger.getEnvironment();
                setupLevelTrigger(environment, controller);
            }

            bindToPatternisedResults(emitterConfig, controller);
            emitterControllers.add(controller);
        }

    }

    private void bindToPatternisedResults(EmitterConfig emitterConfig, final EmitterController emitterController) {

        final TriggerConfig trigger = emitterController.getEmitterConfig().getTrigger();
        if (trigger != null) {

            String environment = trigger.getEnvironment();

            MonitoringBus monitoringBus = buses.get(environment);
            monitoringBus.getPatternisedStream().addListener(new StreamListener<PatternisedLogEvent>() {

                @Override public void onNewItem(PatternisedLogEvent event) {

                    if (trigger.getPattern() == event.getPatternID()) {

                        String host = trigger.getHost();
                        if (host != null && (host.length() == 0 || event.getSourceHost().contains(host))) {

                            String application = trigger.getApplication();
                            if (application != null && (application.length() == 0 || event.getSourceApplication().contains(application))) {

                                String value = event.getVariables()[trigger.getLabel()];

                                double doubleValue = Double.parseDouble(value);
                                if (doubleValue > trigger.getMaximumValue()) {
                                    doubleValue = trigger.getMaximumValue();
                                }
                                double factor = doubleValue / trigger.getMaximumValue();

                                String colourGradient = trigger.getColourGradient();
                                ColourInterpolation interp = getGradient(colourGradient);
                                Color color = interp.interpolate(factor);

                                double velocity;
                                double size;

                                if (trigger.getVelocityFactor() != -1) {
                                    velocity = (1 - factor) * trigger.getVelocityFactor();
                                }
                                else {
                                    // TODO : why is this an arbitrary number!
                                    velocity = 0.15;
                                }

                                if (velocity < trigger.getMinimumVelocity()) {
                                    velocity = trigger.getMinimumVelocity();
                                }

                                if (trigger.getSizeFactor() != -1) {
                                    size = factor * trigger.getSizeFactor();
                                }
                                else {
                                    size = 1;
                                }
                                
                                double maximumSize = trigger.getMaximumSize();
                                if(!Double.isNaN(maximumSize) && size > maximumSize) {
                                    size = maximumSize;
                                }
                                
                                double minimumSize = trigger.getMinimumSize();
                                if(!Double.isNaN(minimumSize) && size < minimumSize) {
                                    size = minimumSize;
                                }

                                emitterController.generate(color, factor, velocity, size, velocity);
                            }
                        }
                    }
                }
            });
        }

    }

    private void setupLevelTrigger(String environment, final EmitterController emitterController) {

        buses.get(environment).getEventStream().addListener(new StreamListener<LogEvent>() {
            Random random = new Random();

            @Override public void onNewItem(LogEvent t) {

                int level = t.getLevel();

                double size;
                double doubleValue = level;
                if (level <= Level.INFO.intValue()) {
                    // Add some random variation to the colours
                    int variation = 60;
                    doubleValue += variation - random.nextInt(variation * 2);
                }

                double factor = doubleValue / Level.SEVERE.intValue();

                ColourInterpolation interp = levelColourInterpolation;
                Color color = interp.interpolate(factor);

                double velocity;

                if (level <= Level.INFO.intValue()) {
                    // Add some random variation to the colours
                    int variation = 60;
                    doubleValue += variation - random.nextInt(variation * 2);
                    size = factor * 0.9;
                }
                else {
                    size = factor * 1.5;
                }

                velocity = 0.15;

                emitterController.generate(color, factor, velocity, size, (0.5 - (random.nextDouble())) / 5);
            }
        });

    }

    public VisualisationConfig getConfig() {
        return config;
    }

    private void bind(VisualiserModel model) {

        // model.getVaryX().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
        // @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
        //
        // }
        // });
        //
        // model.getReleaseAngle().addListenerAndNotifyCurrent(new
        // ObservablePropertyListener<Double>() {
        // @Override public void onPropertyChanged(Double oldValue, Double newValue) {
        // updateReleaseVectors();
        // }
        // });
        //
        // model.getReleaseVelocity().addListenerAndNotifyCurrent(new
        // ObservablePropertyListener<Double>() {
        // @Override public void onPropertyChanged(Double oldValue, Double newValue) {
        // updateReleaseVectors();
        // }
        // });
        //
        // model.getReleaseX().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>()
        // {
        // @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
        // }
        // });
        //
        // model.getReleaseY().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>()
        // {
        // @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
        // }
        // });

    }

    protected void updateReleaseVectors() {

        // double radians = Math.toRadians(model.getReleaseAngle().doubleValue());
        // double x = Math.cos(radians);
        // double y = Math.sin(radians);
        //
        // double velocity = model.getReleaseVelocity().doubleValue();
        //
        // directionVector.x = x * velocity;
        // directionVector.y = y * velocity;
        //
        // System.out.println(directionVector);

    }

    public VisualiserModel getModel() {
        return model;
    }

    public void update(float delta) {
        for (EmitterController emitterController : emitterControllers) {
            emitterController.update(delta);
        }

    }
    
    public void stop() {
        for (EmitterController emitterController : emitterControllers) {
            emitterController.stop();
        }
        
        emitterControllers.clear();
        
        Collection<MonitoringBus> values = buses.values();
        for (MonitoringBus monitoringBus : values) {
            monitoringBus.stop();
        }
        buses.clear();
    }

    public void start(VisualisationConfig config) {
        this.config = config;
        setupEmitters();
        startConnectionManagers();
    }

    public void generate(String string, Color color, double factor, double velocity, double size) {
        // TODO : fix this up a bit
        for (EmitterController emitterController : emitterControllers) {
            emitterController.generate(color, factor, velocity, size, velocity);
        }
    }

    public void processPatternisedEvent(String environment, PatternisedLogEvent event) {
        for (EmitterController emitterController : emitterControllers) {
            TriggerConfig trigger = emitterController.getEmitterConfig().getTrigger();
            if (trigger != null) {
                if (trigger.getEnvironment().equals(environment)) {
                    if (trigger.getPattern() == event.getPatternID()) {

                        String host = trigger.getHost();
                        if (host != null && host.length() > 0 && event.getSourceHost().contains(host)) {

                            String application = trigger.getApplication();
                            if (application != null && application.length() > 0 && event.getSourceApplication().contains(application)) {

                                String value = event.getVariables()[trigger.getLabel()];

                                double doubleValue = Double.parseDouble(value);
                                if (doubleValue > trigger.getMaximumValue()) {
                                    doubleValue = trigger.getMaximumValue();
                                }
                                double factor = doubleValue / trigger.getMaximumValue();

                                String colourGradient = trigger.getColourGradient();
                                ColourInterpolation interp = getGradient(colourGradient);
                                Color color = interp.interpolate(factor);

                                double velocity;
                                double size;

                                if (trigger.getVelocityFactor() != -1) {
                                    velocity = (1 - factor) * trigger.getVelocityFactor();
                                }
                                else {
                                    // TODO : why is this an arbitrary number!
                                    velocity = 0.15;
                                }

                                if (velocity < trigger.getMinimumVelocity()) {
                                    velocity = trigger.getMinimumVelocity();
                                }

                                if (trigger.getSizeFactor() != -1) {
                                    size = factor * trigger.getSizeFactor();
                                }
                                else {
                                    size = 1;
                                }

                                emitterController.generate(color, factor, velocity, size, velocity);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, ColourInterpolation> interpolators = new HashMap<String, ColourInterpolation>();

    private ColourInterpolation getGradient(String colourGradient) {

        ColourInterpolation colourInterpolation = interpolators.get(colourGradient);
        if (colourInterpolation == null) {

            String[] split = colourGradient.split(",");
            Color[] colours = new Color[split.length];
            for (int i = 0; i < split.length; i++) {
                colours[i] = ColourUtils.parseColor(split[i]);
            }

            colourInterpolation = new ColourInterpolation(colours);
            interpolators.put(colourGradient, colourInterpolation);
        }

        return colourInterpolation;
    }

    // public void generate(Color color, double factor, double velocity, double size) {
    // Entity entity = entitySource.spawn(color, velocity, size);
    //
    // if (model.getVaryX().asBoolean()) {
    // entity.getPosition().x += (model.getXDeviation().doubleValue() / -2d) +
    // (model.getXDeviation().doubleValue() * factor);
    // }
    //
    // }

    private Closer closer = new Closer();

    public synchronized void close() {
        closer.closeQuietly();
        closer = new Closer();
    }

    // private Map<String, Stream<LogEvent>> streams = new FactoryMap<String, Stream<LogEvent>>() {
    // @Override protected Stream<LogEvent> createEmptyValue(String key) {
    // return new Stream<LogEvent>();
    // }
    // };

    private Map<String, MonitoringBus> buses = new FactoryMap<String, MonitoringBus>() {
        @Override protected MonitoringBus createEmptyValue(String key) {
            return new MonitoringBus();
        }
    };

    public void startConnectionManagers() {
        final FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();

        List<EnvironmentConfiguration> environments = config.getEnvironments();
        for (final EnvironmentConfiguration environmentConfiguration : environments) {

            MonitoringBus bus = buses.get(environmentConfiguration.getName());

            // final Stream<LogEvent> stream = streams.get(environmentConfiguration.getName());

            List<HubConfiguration> hubs = environmentConfiguration.getHubs();
            for (final HubConfiguration hubConfiguration : hubs) {
                if (hubConfiguration.getHost() != null && hubConfiguration.getHost().length() > 0) {
                    String host = hubConfiguration.getHost();
                    int port = hubConfiguration.getPort();

                    bus.addHub(host, port);

                    if (hubConfiguration.isDebug()) {
                        bus.getEventStream().addListener(new StreamListener<LogEvent>() {                            
                            @Override public void onNewItem(LogEvent t) {
                                logger.info("{} | {} | {} | {} ", environmentConfiguration.getName(), hubConfiguration.getHost(), hubConfiguration.getPort(), formatter.format(t));
                            }
                        });
                    }

                    //
                    // SocketClient client = new SocketClient();
                    // client.addConnectionPoint(new InetSocketAddress(host, port));
                    // client.setAutoSubscribe(true);
                    //
                    // final SocketClientManager manager = new SocketClientManager(client);
                    // manager.start();
                    //
                    // closer.register(new Closeable() {
                    // @Override public void close() throws IOException {
                    // manager.stop();
                    // }
                    // });
                    //
                    // client.addLogEventListener(new LogEventListener() {
                    // @Override public void onNewLogEvent(LogEvent event) {
                    //
                    // if (hubConfiguration.isDebug()) {
                    // logger.info("{} | {} | {} | {} ", environmentConfiguration.getName(),
                    // hubConfiguration.getHost(), hubConfiguration.getPort(),
                    // formatter.format(event));
                    // }
                    //
                    // stream.send(event);
                    // }
                    // });
                }
            }

            List<PatternConfiguration> patterns = environmentConfiguration.getPatterns();
            for (PatternConfiguration patternConfiguration : patterns) {
                bus.addPattern(patternConfiguration.getName(), patternConfiguration.getPattern());
            }

            bus.start();
        }
    }

}
