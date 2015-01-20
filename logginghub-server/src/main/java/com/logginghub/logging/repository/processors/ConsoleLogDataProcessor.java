package com.logginghub.logging.repository.processors;

import java.io.File;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.logeventformatters.SingleLineLogEventTextFormatter;
import com.logginghub.logging.repository.LogDataProcessor;

public class ConsoleLogDataProcessor implements LogDataProcessor {

    private SingleLineLogEventTextFormatter formatter = new SingleLineLogEventTextFormatter();
    
    public void onNewLogEvent(LogEvent event) {
        System.out.println(formatter.format(event));
    }

    public void processingStarted(File resultsFolder) {}

    public void processingEnded() {}

}
