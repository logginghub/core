package com.logginghub.logging.generator.configs;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.logginghub.logging.generator.MessageProducerConfig;
import com.logginghub.logging.generator.StringProducer;
import com.logginghub.logging.generator.WeightedStringProducer;
import com.logginghub.logging.generator.LoggingMessageGenerator.OperationState;

public class TradingMessageConfig implements MessageProducerConfig {

    private WeightedStringProducer instrumentProducer = new WeightedStringProducer(new String[] { "GBPUSD Spot", "EURUSD Spot", "EURGBP Spot" }, new double[] { 10, 2, 1 });
    private WeightedStringProducer quantityProducer = new WeightedStringProducer(new String[] { "1000", "10,000", "50,000", "100,000", "500,000", "1,000,000" }, new double[] { 10, 8, 6, 2, 1, 0.1 });
    private WeightedStringProducer statusProducer = new WeightedStringProducer(new String[] { "Accepted", "Rejected-NoEnoughEquity", "Rejected-BadPrice", "Exception" }, new double[] { 10, 0.01 });
    private WeightedStringProducer accountProducer;

    private StringProducer[] producers;
    private String[] labels;

    public TradingMessageConfig() {

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

        StringProducer transactionProducer = new StringProducer() {
            private AtomicLong id = new AtomicLong();
            public String produce() {
                return "" + (id.getAndIncrement());
            }
        };
        
        producers = new StringProducer[] { transactionProducer, accountProducer, instrumentProducer, quantityProducer, statusProducer };
        labels = new String[] { "txid", "account", "instrument", "quantity", "status" };
    }

    public OperationState generateState() {
        String status = statusProducer.produce();

        OperationState state;
        if (status.contains("Rejected")) {
            state = OperationState.Unsuccessful;
        }
        else if (status.contains("Exception")) {
            state = OperationState.Failed;
        }
        else {
            state = OperationState.Successful;
        }
        
        return state;
    }

    public double getMaximumTime() {
        return 25;

    }

    public double getMinimumTime() {
        return 5;

    }

    public StringProducer[] getProducers() {
        return producers;
    }

    public String[] getLabels() {
        return labels;

    }

    public String getOperation() {
        return "ProcessTrade";

    }

    public double doctorTime(double original, Object[] array) {
        return original;
    }

}
