package com.logginghub.logging.repository.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.logginghub.analytics.model.LongFrequencyCount;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.repository.LogDataProcessor;
import com.logginghub.utils.HTMLBuilder;
import com.logginghub.utils.MutableLongValue;

public class BadEventsReport implements LogDataProcessor {

    private HTMLBuilder builder;
    private String name = "badevents";

    private List<String> rollupRegexs = new ArrayList<String>();
    private List<Pattern> rollupPatterns = new ArrayList<Pattern>();
    private LongFrequencyCount count = new LongFrequencyCount();

    public void onNewLogEvent(LogEvent event) {
        if (event.getLevel() >= Level.WARNING.intValue()) {
            append(event);
        }
    }

    private void append(LogEvent event) {

        if (isRollup(event)) {
            // We've already counted it in the isRollup method
        }
        else {
            builder.tr();
            builder.td(new Date(event.getOriginTime()));
            builder.td(event.getLevelDescription());
            builder.td(event.getSourceHost());
            builder.td(event.getSourceAddress());
            builder.td(event.getSourceApplication());
            builder.td(event.getLoggerName());
            builder.td(event.getSourceClassName());
            builder.td(event.getSourceMethodName());
            builder.td(event.getThreadName());
            builder.td(event.getMessage());
            builder.endTr();
        }
    }

    private boolean isRollup(LogEvent event) {
        boolean rolledUp = false;
        String message = event.getMessage();        
        for (int i = 0; i < rollupPatterns.size(); i++){
            Pattern pattern = rollupPatterns.get(i);
            if(pattern.matcher(message).matches()){
                count.count(rollupRegexs.get(i), 1);
                rolledUp = true;
                break;
            }
        }

        return rolledUp;
    }

    public void processingStarted(File resultsFolder) {
        File output = new File(resultsFolder, name + ".html");
        try {
            builder = new HTMLBuilder(new FileWriter(output));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        builder.html().head().endHead().body();
        builder.table();
        builder.thead();
        builder.tr();
        builder.th("Time");
        builder.th("Level");
        builder.th("Host");
        builder.th("Address");
        builder.th("Application");
        builder.th("Logger");
        builder.th("Source class");
        builder.th("Source method");
        builder.th("Thread");
        builder.th("Message");
        builder.endTr();
        builder.endThead();
    }

    public void processingEnded() {
        builder.endTable();
        
        // Stick in the rolled up entries
        if(!count.isEmpty()){
            List<MutableLongValue> sortedValues = count.getSortedValues();
            builder.p();
            builder.table();
            builder.thead().tr().th("Message (regular expression)").th("Number of occurances").endTr().endThead();
            for (MutableLongValue mutableLongValue : sortedValues) {
                builder.tr().td(mutableLongValue.key).td(mutableLongValue.value).endTr();
            }
            builder.endTable();
            builder.endP();
        }
        
        builder.endBody().endHtml();
        builder.close();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRollupRegexs(List<String> rollupRegexs) {
        this.rollupRegexs = rollupRegexs;
        initialisePatterns();
    }

    private void initialisePatterns() {
        rollupPatterns = new ArrayList<Pattern>();
        for (String string : rollupRegexs) {
            rollupPatterns.add(Pattern.compile(string));
        }
    }

}
