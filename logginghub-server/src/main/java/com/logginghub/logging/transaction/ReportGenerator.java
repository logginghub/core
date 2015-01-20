package com.logginghub.logging.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.logging.Logger;

public class ReportGenerator {

    private static final Logger logger = Logger.getLoggerFor(ReportGenerator.class);

    public String generateReport(TransactionModel t, long warningAt) {

        String message = "";

        boolean performanceWarning = false;
        double elapsed = t.calculateElapsedMilliseconds();

        if (t.isSuccess()) {
            message = StringUtils.format("Transaction '{}' completed successfully in {} ms:", t.getTransactionID(), elapsed);
            if (elapsed > warningAt) {
                performanceWarning = true;
            }
        }
        else {
            long startTime = t.getEvents().get(0).getOriginTime();
            elapsed = System.currentTimeMillis() - startTime;
            message = StringUtils.format("Transaction '{}' timed out after {} ms", t.getTransactionID(), elapsed);
        }

        // Build the start of the message
        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine(message);
        builder.appendLine();

        if (performanceWarning) {
            builder.appendLine("The transaction was slower than the warning threshold however - it took {} (warnings for anything greater than {})",
                               TimeUtils.formatIntervalMilliseconds(elapsed),
                               TimeUtils.formatIntervalMilliseconds(warningAt));
            builder.appendLine();
        }

        // Add on the individual events
        EventReporterEventFormatter formatter = new EventReporterEventFormatter();
        List<LogEvent> events = t.getEvents();
        if (events.size() > 0) {
            for (LogEvent logEvent : events) {
                builder.appendLine(formatter.format(logEvent));
            }
        }

        if (t.getCurrentState().getTransitions().size() > 0) {
            builder.newline();
            builder.appendLine("We expecting one of the following state transitions, but nothing arrived during the timeout window:");
            builder.newline();
            List<StateNodeModel> transitions = t.getCurrentState().getTransitions();
            for (StateNodeModel stateNodeModel : transitions) {
                builder.appendLine("{} | {}", StringUtils.padRight(stateNodeModel.getStripper().getPatternName(), 20), stateNodeModel.getStripper()
                                                                                                                                     .getPattern());
            }
            builder.newline();
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
        long totalTime = (long) elapsed;
        long unaccountedFor = totalTime - timeInProcesses;

        builder.newline();
        builder.appendLine("Time accounted for in processes             : {}", TimeUtils.formatIntervalMilliseconds(timeInProcesses));
        builder.appendLine("Total elapsed time                          : {}", TimeUtils.formatIntervalMilliseconds(totalTime));
        builder.appendLine("Unaccounted time outside of processes       : {}", TimeUtils.formatIntervalMilliseconds(unaccountedFor));

        return builder.toString();
    }

    private void addInProcessingTimingsSection(TransactionModel t, List<Long> relativeTimingValues, StringUtilsBuilder builder) {
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

        builder.newline();
        String divider = StringUtils.repeat("=", chartWidth + widestState + 10 + 34);
        builder.appendLine(divider);

        Iterator<Double> timingsIterator = timingValues.iterator();
        Iterator<String> stateNameIterator = stateNames.iterator();
        Iterator<Long> relativeIterator = relativeTimingValues.iterator();
        
        boolean header = false;
        
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

            if(!header) {
                builder.appendLine("| {} | {} | {} | Time visualisation",
                                   StringUtils.padRight("State", widestState + 2),
                                   StringUtils.padLeft("Timestamp", 10),
                                   StringUtils.padLeft("Log time", 9));
                builder.appendLine(divider);
                header = true;
            }
            
            builder.appendLine("| {} | {} ms | {} ms | [{}|{}]{} |",
                               StringUtils.padRight(stateName, widestState + 2),
                               StringUtils.padLeft(StringUtils.format2dp(timing), 7),
                               StringUtils.padLeft(StringUtils.format2dp(relativeTime), 6),
                               inProcessChars > 0 ? StringUtils.repeat("-", inProcessChars) : "",
                               elapsedExtraChars > 0 ? StringUtils.repeat("-", elapsedExtraChars) : "",
                               StringUtils.repeat(" ", gap));

        }
        builder.appendLine(divider);
    }

    public String generateSingleLineReport(TransactionModel t, long warningAt) {
        String message = "";

        boolean performanceWarning = false;
        double elapsed = t.calculateElapsedMilliseconds();

        if (t.isSuccess()) {
            message = StringUtils.format("Transaction '{}' completed successfully in {} ms", t.getTransactionID(), elapsed);
            if (elapsed > warningAt) {
                performanceWarning = true;
            }
        }
        else {
            long startTime = t.getEvents().get(0).getOriginTime();
            elapsed = System.currentTimeMillis() - startTime;
            message = StringUtils.format("Transaction '{}' timed out after {} ms", t.getTransactionID(), elapsed);
        }

        // Build the start of the message
        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.append(message);

        if (performanceWarning) {
            builder.appendLine(" but took longer than the warning threshold ({})",
                               TimeUtils.formatIntervalMilliseconds(warningAt));
            builder.appendLine();
        }

        return builder.toString();
    }
}
