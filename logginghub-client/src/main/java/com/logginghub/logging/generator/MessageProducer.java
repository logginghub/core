package com.logginghub.logging.generator;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.generator.LoggingMessageGenerator.OperationState;
import com.logginghub.utils.Is;
import com.logginghub.utils.RandomWithMomentum;
import com.logginghub.utils.Stopwatch;

public class MessageProducer {

    private LoggingMessageGenerator generator = new LoggingMessageGenerator();
    private RandomWithMomentum timeGenerator;
    private MessageProducerConfig config;

    public MessageProducer(MessageProducerConfig config) {
        this.config = config;
        timeGenerator = new RandomWithMomentum(0, config.getMinimumTime(), config.getMaximumTime(), 1, 5);
    }

    public void amendEvent(DefaultLogEvent logEvent) {

        OperationState state = config.generateState();

        String operation = config.getOperation();
        String[] labels = config.getLabels();
        StringProducer[] producers = config.getProducers();

        Object[] array = new Object[labels.length * 2];
        for (int i = 0; i < labels.length; i++) {
            array[i * 2] = labels[i];
            array[(i * 2) + 1] = producers[i].produce();
        }

        Stopwatch stopwatch = new Stopwatch();
        double next = timeGenerator.next();
        double doctored = config.doctorTime(next, array);
        stopwatch.forceElapsedMillis(doctored);

        String generateMessage = generator.generateMessage(operation, state, stopwatch, array);

        logEvent.setMessage(generateMessage);

        String[] metadataLabels = config.getMetadataLabels();
        StringProducer[] metadataProducers = config.getMetadataProducers();

        Is.equals(metadataLabels.length, metadataLabels.length, "You must have the same number of metadata labels as producers");

        for(int i = 0; i < metadataLabels.length; i++) {
            String label = metadataLabels[i];
            String value = metadataProducers[i].produce();

            logEvent.getMetadata().put(label, value);
        }
    }

}
