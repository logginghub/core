package com.logginghub.logging.generator;

import com.logginghub.logging.generator.LoggingMessageGenerator.OperationState;

public interface MessageProducerConfig {
    public OperationState generateState();
    public double getMaximumTime();
    public double getMinimumTime();
    public StringProducer[] getProducers();
    public String[] getLabels();
    public String getOperation();
    public double doctorTime(double original, Object[] array);
}
