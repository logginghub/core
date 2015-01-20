package com.logginghub.logging.generator.configs;

import java.util.Random;

import com.logginghub.logging.generator.MessageProducerConfig;
import com.logginghub.logging.generator.WeightedStringProducer;
import com.logginghub.logging.generator.LoggingMessageGenerator.OperationState;

public class TradeBlotterMessageConfig implements MessageProducerConfig {

    private WeightedStringProducer accountProducer;
    private WeightedStringProducer[] producers;
    private String[] labels;

    public TradeBlotterMessageConfig() {

        int clientAccounts = 100;

        String[] names = new String[clientAccounts];
        double[] weights = new double[clientAccounts];

        Random random = new Random();
        for (int i = 0; i < clientAccounts; i++) {
            names[i] = "client-" + i;
            weights[i] = random.nextInt(5);
        }

        // Put in a few big spenders
        weights[30] = 10;
        weights[65] = 20;

        accountProducer = new WeightedStringProducer(names, weights);

        producers = new WeightedStringProducer[] { accountProducer };
        labels = new String[] { "account",  };
    }

    public OperationState generateState() {
        OperationState state = OperationState.Successful;
        return state;
    }

    public double getMaximumTime() {
        return 500;

    }

    public double getMinimumTime() {
        return 10;
    }

    public WeightedStringProducer[] getProducers() {
        return producers;

    }

    public String[] getLabels() {
        return labels;

    }

    public String getOperation() {
        return "TradeBlotterRequest";

    }

    public double doctorTime(double original, Object[] array) {
        return original;
    }

}
