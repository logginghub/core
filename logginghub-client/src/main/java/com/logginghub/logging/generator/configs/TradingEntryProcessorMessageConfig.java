package com.logginghub.logging.generator.configs;

import com.logginghub.logging.generator.LoggingMessageGenerator.OperationState;
import com.logginghub.logging.generator.MessageProducerConfig;
import com.logginghub.logging.generator.StringProducer;

import java.util.concurrent.atomic.AtomicLong;

public class TradingEntryProcessorMessageConfig implements MessageProducerConfig {


    private StringProducer[] producers;
    private String[] labels;

    public TradingEntryProcessorMessageConfig() {

        StringProducer transactionProducer = new StringProducer() {
            private AtomicLong id = new AtomicLong();
            public String produce() {
                return "" + (id.getAndIncrement());
            }
        };
        
        producers = new StringProducer[] { transactionProducer };
        labels = new String[] { "txid" };
    }

    public OperationState generateState() {
        OperationState state = OperationState.Successful;
        return state;
    }

    public double getMaximumTime() {
        return 4;

    }

    public double getMinimumTime() {
        return 1;

    }

    public StringProducer[] getProducers() {
        return producers;
    }

    public String[] getLabels() {
        return labels;

    }

    public String getOperation() {
        return "TradeEntryProcessor";

    }

    public double doctorTime(double original, Object[] array) {
        return original;
    }

    public String[] getMetadataLabels() {
        return new String[] {};
    }

    public StringProducer[] getMetadataProducers() {
        return new StringProducer[]{};
    }

}
