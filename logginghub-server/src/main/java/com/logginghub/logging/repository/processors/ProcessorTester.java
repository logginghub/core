package com.logginghub.logging.repository.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.repository.LogDataProcessor;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ResourceUtils;

public class ProcessorTester {

    public static List<DefaultLogEvent> createLogEventsFromResource(String resourcePath) {

        List<DefaultLogEvent> events = new ArrayList<DefaultLogEvent>();

        long startingTime = getStartingTime();

        String[] csvLines = ResourceUtils.readLines(resourcePath);
        for (String line : csvLines) {
            String[] split = line.split(",");
            if (split[0].trim().equals("Time")) {
                // Header row, skip it
            }
            else {
                events.add(createEvent(startingTime, split));
            }
        }

        return events;
    }

    public static void process(File resultsFolder, LogDataProcessor processor, String resourcePath) {

        long startingTime = getStartingTime();

        processor.processingStarted(resultsFolder);

        String[] csvLines = ResourceUtils.readLines(resourcePath);
        for (String line : csvLines) {
            String[] split = line.split(",");
            if (split[0].equals("Time")) {
                // Header row, skip it
            }
            else {
                DefaultLogEvent event = createEvent(startingTime, split);
                processor.onNewLogEvent(event);

            }
        }

        processor.processingEnded();
    }

    private static long getStartingTime() {
        long startingTime;
        try {
            startingTime = DateFormat.getInstance().parse("01/01/12 10:00").getTime();
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return startingTime;
    }

    private static DefaultLogEvent createEvent(long startingTime, String[] split) {
        DefaultLogEvent event = new DefaultLogEvent();

        event.setLocalCreationTimeMillis(startingTime + Long.parseLong(split[0]));
        event.setLevel(Level.parse(split[1]).intValue());
        event.setSourceHost(split[2]);
        event.setSourceAddress(split[3]);
        event.setSourceApplication(split[4]);
        event.setLoggerName(split[5]);
        event.setThreadName(split[6]);
        event.setSourceClassName(split[7]);
        event.setSourceMethodName(split[8]);

        // This bit allows us to put comma's in the message!
        // TODO : refactor to use a proper csv library that supports
        // quoted strings.
        StringBuilder messageBuilder = new StringBuilder();
        String div = "";
        for (int i = 9; i < split.length; i++) {
            if (split[i].length() > 0) {
                messageBuilder.append(div);
                messageBuilder.append(split[i]);
                div = ",";
            }
        }

        event.setMessage(messageBuilder.toString());
        return event;
    }

    public static void processFromAccessLog(File resultsFolder, RegexExtractingProcessor processor, String path) {

        long startingTime = getStartingTime();
        InputStream openStream = ResourceUtils.openStream(path);
        
        try {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(openStream));
            
            processor.processingStarted(resultsFolder);
            
            long time = startingTime;
            String line;
            while((line = reader.readLine()) != null){
                DefaultLogEvent event = new DefaultLogEvent();

                event.setLocalCreationTimeMillis(time);
                event.setLevel(Level.INFO.intValue());
                event.setSourceHost("www.vertexlabs.co.uk");
                event.setSourceAddress("123.123.123.123");
                event.setSourceApplication("httpd");
                event.setLoggerName("access");
                event.setThreadName("httphandler");
                event.setSourceClassName("");
                event.setSourceMethodName("");
                event.setMessage(line);
                
                processor.onNewLogEvent(event);
                time += 1000;
            }
            
            processor.processingEnded();
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read data from '%s'", path), e);
        }finally{
            FileUtils.closeQuietly(openStream);
        }
    }

}
