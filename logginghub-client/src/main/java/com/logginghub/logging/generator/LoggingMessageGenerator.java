package com.logginghub.logging.generator;

import com.logginghub.utils.Stopwatch;

public class LoggingMessageGenerator {

    public enum OperationState {
        Successful,
        Unsuccessful,
        Failed
    }

    public String generateMessage(String operation, OperationState state, Stopwatch stopwatch, Object... formatPairs) {

        StringBuilder builder = new StringBuilder();

        builder.append(operation);

        switch (state) {
            case Successful: {
                builder.append(" completed successfully");
                break;
            }
            case Failed: {
                builder.append(" failed");
                break;
            }
            case Unsuccessful: {
                builder.append(" completed unsuccessfully");
                break;
            }
        }

        builder.append(" in ").append(stopwatch.stopAndGetFormattedDurationMillis()).append(" ms : ");

        String div = "";
        for (int i = 0; i < formatPairs.length; i += 2) {
            builder.append(div).append(" ");
            builder.append(formatPairs[i]).append(" was '");
            builder.append(formatPairs[i + 1]);
            builder.append("'");
            div = ",";
        }
        
        String message = builder.toString();
        return message;
    }

}
