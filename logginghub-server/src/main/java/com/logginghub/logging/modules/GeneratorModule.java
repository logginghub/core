package com.logginghub.logging.modules;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.generator.nextgen.SimulatorEventSource;
import com.logginghub.logging.modules.configuration.GeneratorConfiguration;
import com.logginghub.logging.modules.configuration.GeneratorMessageConfiguration;
import com.logginghub.logging.modules.configuration.VariableConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Is;
import com.logginghub.utils.Stream;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class GeneratorModule implements Module<GeneratorConfiguration> {

    private GeneratorConfiguration configuration;

    private List<SimulatorEventSource> sources = new ArrayList<SimulatorEventSource>();
    private Stream<LogEvent> logEventStream = new Stream<LogEvent>();
    private Random random = new Random();

    private FactoryMap<String, AtomicLong> sequences = new FactoryMap<String, AtomicLong>() {
        @Override
        protected AtomicLong createEmptyValue(String key) {
            return new AtomicLong();
        }
    };

    @Override
    public void configure(GeneratorConfiguration configuration, ServiceDiscovery serviceDiscovery) {
        Destination<LogEvent> service = serviceDiscovery.findService(Destination.class, LogEvent.class, configuration.getDestination());
        logEventStream.addDestination(service);

        this.configuration = configuration;
        // Do some quick and dirty validation so we fail at configuration time is something is wrong
        List<GeneratorMessageConfiguration> messages = configuration.getMessages();
        for (GeneratorMessageConfiguration message : messages) {

            if(StringUtils.isNotNullOrEmpty(message.getPatternFile())) {
                message.setPattern(FileUtils.read(message.getPatternFile()));
            }

            Is.notNullOrEmpty(message.getPattern(), "Pattern must be set on the generator message configuration element");
            Is.notNullOrEmpty(message.getLevel(), "Level must be set on the generator message configuration element");
            Level.parse(message.getLevel());
        }
    }

    public Stream<LogEvent> getLogEventStream() {
        return logEventStream;
    }

    @Override
    public void start() {

        List<GeneratorMessageConfiguration> messages = configuration.getMessages();
        for (final GeneratorMessageConfiguration generator : messages) {

            SimulatorEventSource source = new SimulatorEventSource(generator.isRandom(), generator.getRateMin(), generator.getRateMax(), generator.getLimit());
            source.getEventStream().addListener(new StreamListener<Long>() {
                @Override
                public void onNewItem(Long t) {
                    String message = buildMessage(t, generator);

                    DefaultLogEvent event = generator.getTemplate().createEvent();
                    if (StringUtils.isNotNullOrEmpty(generator.getLevel())) {
                        event.setLevel(Level.parse(generator.getLevel()).intValue());
                    }
                    event.setMessage(message);
                    logEventStream.onNewItem(event);
                }
            });
            source.start();

            sources.add(source);
        }
    }

    protected String buildMessage(long t, GeneratorMessageConfiguration generator) {

        String pattern = generator.getPattern();

        List<VariableConfiguration> variables = generator.getVariables();
        for (VariableConfiguration variableConfiguration : variables) {
            String name = "${" + variableConfiguration.getName() + "}";

            String type = variableConfiguration.getType();

            String value = null;
            if (StringUtils.isNotNullOrEmpty(type)) {
                if (type.equalsIgnoreCase("time")) {
                    value = new Date().toString();
                } else if (type.equalsIgnoreCase("sequence")) {
                    value = Long.toString(sequences.get(name).incrementAndGet());
                }
            }

            if (value == null) {
                value = variableConfiguration.getValues();
                value = randomiseValue(value, random.nextDouble());
            }
            pattern = pattern.replace(name, value);
        }

        pattern = pattern.replace("${sequence}", Long.toString(t));

        return pattern;
    }

    public static String randomiseValue(String value, double randomFactor) {
        // TODO : this is slow and nasty, at least cache something!
        String randomised = null;

        try {
            double totalWeighting = 0;

            String[] split = value.split(",");
            for (String string : split) {
                String weightingString = StringUtils.between(string, "[", "]");
                double weighting = Double.parseDouble(weightingString);
                totalWeighting += weighting;
            }

            double progress = 0;

            for (String string : split) {
                String weightingString = StringUtils.between(string, "[", "]");
                String randomValue = StringUtils.before(string, "[").trim();
                double weighting = Double.parseDouble(weightingString);
                double weightingFactor = weighting / totalWeighting;
                randomised = randomValue;

                if (randomFactor >= progress && randomFactor < progress + weightingFactor) {
                    break;
                }

                progress += weightingFactor;

            }
        } catch (RuntimeException e) {
            randomised = "<error in generator configuration>";
        }

        return randomised;

    }

    @Override
    public void stop() {

        for (SimulatorEventSource simulatorEventSource : sources) {
            simulatorEventSource.stop();
        }

        sources.clear();
    }

}
