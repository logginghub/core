package com.logginghub.logging.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.HTMLBuilder2;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.HTMLBuilder2.Element;
import com.logginghub.utils.HTMLBuilder2.RowElement;
import com.logginghub.utils.HTMLBuilder2.TableElement;
import com.logginghub.utils.logging.Logger;

public class HtmlReportGenerator {
    private static final Logger logger = Logger.getLoggerFor(ReportGenerator.class);

    public String generateReport(TransactionModel t, long warningAt) {

        HTMLBuilder2 builder = new HTMLBuilder2();

        String message = "";

        boolean performanceWarning = false;
        long elapsed = 0;
        long startTime = 0;
        List<LogEvent> events = t.getEvents();
        if (events.size() > 0) {
            startTime = events.get(0).getOriginTime();
            long endTime = events.get(events.size() - 1).getOriginTime();
            elapsed = endTime - startTime;
        }

        if (t.isSuccess()) {
            message = StringUtils.format("Transaction '{}' completed successfully in {} ms:", t.getTransactionID(), elapsed);
            if (elapsed > warningAt) {
                performanceWarning = true;
            }
        }
        else {
            elapsed = System.currentTimeMillis() - startTime;
            message = StringUtils.format("Transaction '{}' timed out after {} ms", t.getTransactionID(), elapsed);
        }

        // Build the start of the message
        // StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.getBody().h1(message);

        if (performanceWarning) {
            builder.getBody()
                   .div("The transaction was slower than the warning threshold however - it took {} (warnings for anything greater than {})",
                              TimeUtils.formatIntervalMilliseconds(elapsed),
                              TimeUtils.formatIntervalMilliseconds(warningAt));
        }

        // Add on the individual events
        
        EventReporterEventFormatter formatter = new EventReporterEventFormatter();
        if (events.size() > 0) {
            builder.getBody().p("We tracked the folling events as part of this transaction:");
            TableElement eventTable = builder.getBody().createTable();
            
            Element headerRow = eventTable.getThead().createChild("tr");
            headerRow.createChild("th").setText("Time");
            headerRow.createChild("th").setText("Source host");
            headerRow.createChild("th").setText("Source address");
            headerRow.createChild("th").setText("Source PID");
            headerRow.createChild("th").setText("Source application");
            headerRow.createChild("th").setText("Source thread");
            headerRow.createChild("th").setText("Message");
            
            for (LogEvent logEvent : events) {
                RowElement row = eventTable.createRow();
                row.cell(formatter.formatDateTime(logEvent.getOriginTime()).toString());
                row.cell(logEvent.getSourceHost());
                row.cell(logEvent.getSourceAddress());
                row.cell("{}", logEvent.getPid());
                row.cell(logEvent.getSourceApplication());
                row.cell(logEvent.getThreadName());
                row.cell(logEvent.getMessage());
            }
        }

        if (t.getCurrentState().getTransitions().size() > 0) {
            Element div = builder.getBody().div();
            div.p("We expecting one of the following state transitions, but nothing arrived during the timeout window:");
            
            TableElement table = div.createTable();
            
            Element headerRow = table.getThead().createChild("tr");
            headerRow.createChild("th").setText("State name");
            headerRow.createChild("th").setText("Matcher pattern");
            
            List<StateNodeModel> transitions = t.getCurrentState().getTransitions();
            for (StateNodeModel stateNodeModel : transitions) {
                RowElement row = table.createRow();
                row.cell(stateNodeModel.getStripper().getPatternName());
                row.cell(stateNodeModel.getStripper().getRawPattern());
            }
        }

        // Work out the relative timings
        List<Long> relativeTimingValues = new ArrayList<Long>();
        if (events.size() > 0) {
            long previousTime = 0;
            for (LogEvent logEvent : events) {

                if (previousTime == 0) {
                    relativeTimingValues.add(0L);
                }
                else {
                    relativeTimingValues.add(logEvent.getOriginTime() - previousTime);
                }

                previousTime = logEvent.getOriginTime();
            }
        }

        // Add on the section about timings
        addInProcessingTimingsSection(t, relativeTimingValues, builder);

        // Time in and out of processes
        double totalTimeInStates = 0;
        for (String timing : t.getTimings()) {
            if (timing != null && timing.length() > 0) {
                double timingValue = Double.parseDouble(timing);
                totalTimeInStates += timingValue;
            }
        }

        long timeInProcesses = (long) totalTimeInStates;
        long totalTime = elapsed;
        long unaccountedFor = totalTime - timeInProcesses;

        Element div = builder.getBody().div();
        div.p("Time accounted for in processes             : {}", TimeUtils.formatIntervalMilliseconds(timeInProcesses));
        div.p("Total elapsed time                          : {}", TimeUtils.formatIntervalMilliseconds(totalTime));
        div.p("Unaccounted time outside of processes       : {}", TimeUtils.formatIntervalMilliseconds(unaccountedFor));

        return builder.toString();
    }

    private void addInProcessingTimingsSection(TransactionModel t, List<Long> relativeTimingValues, HTMLBuilder2 builder) {
        
        Element div = builder.getBody().div();
        div.p("Timing breakdowns :");
        
        List<String> timings = t.getTimings();
        List<Double> timingValues = new ArrayList<Double>();
        double maximumProcessTime = 0;

        Iterator<Long> iterator = relativeTimingValues.iterator();
        for (String timing : timings) {
            double timingValue;
            Long relativeTiming = iterator.next();

            if (timing != null && timing.length() > 0) {
                timingValue = Double.parseDouble(timing);
                maximumProcessTime = Math.max(maximumProcessTime, timingValue + relativeTiming);
            }
            else {
                timingValue = Double.NaN;
                maximumProcessTime = Math.max(maximumProcessTime, relativeTiming);
            }

            timingValues.add(timingValue);
        }

        int chartWidth = 100;

        List<String> stateNames = t.getStateNames();
        int widestState = 0;
        for (String string : stateNames) {
            widestState = Math.max(widestState, string.length());
        }

        TableElement table = builder.getBody().createTable();
        Element headerRow = table.getThead().createChild("tr");
        headerRow.createChild("th").setText("State name");
        headerRow.createChild("th").setText("Processing time from log");
        headerRow.createChild("th").setText("Elapsed time from events");
        headerRow.createChild("th").setText("Relative view");
        
        Iterator<Double> timingsIterator = timingValues.iterator();
        Iterator<String> stateNameIterator = stateNames.iterator();
        Iterator<Long> relativeIterator = relativeTimingValues.iterator();
        while (stateNameIterator.hasNext()) {

            double timing = timingsIterator.next();
            String stateName = stateNameIterator.next();
            long relativeTime = relativeIterator.next();

            int inProcessChars;

            if (Double.isNaN(timing)) {
                inProcessChars = 0;
            }
            else {
                inProcessChars = (int) (chartWidth * (timing / maximumProcessTime));
            }

            int elapsedProcessChars = (int) (chartWidth * (relativeTime / maximumProcessTime));
            int elapsedExtraChars = elapsedProcessChars - inProcessChars;
            int gap = chartWidth - Math.max(inProcessChars, elapsedProcessChars);

            logger.finest("Timing {} relative time {} inprocesschars {} elapsedChars {} gap {} width {}",
                          timing,
                          relativeTime,
                          inProcessChars,
                          elapsedProcessChars,
                          gap,
                          chartWidth);

            RowElement row = table.createRow();
            row.cell(StringUtils.padRight(stateName, widestState + 2));
            row.cell(StringUtils.padLeft(StringUtils.format2dp(timing), 10));
            row.cell(StringUtils.padLeft(StringUtils.format2dp(relativeTime), 10));
            row.cell(inProcessChars > 0 ? StringUtils.repeat("-", inProcessChars) : "");
            row.cell(elapsedExtraChars > 0 ? StringUtils.repeat("-", elapsedExtraChars) : "");
            row.cell(StringUtils.repeat(" ", gap));

        }
    }
}
