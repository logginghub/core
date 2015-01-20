package com.logginghub.logging.transaction;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.logging.LogEvent;

public class TransactionModel {

    private String transactionID;
    private List<LogEvent> events = new ArrayList<LogEvent>();
    private List<String> timings = new ArrayList<String>();
    private List<String> stateNames = new ArrayList<String>();
    private long timeoutTime;
    private StateNodeModel currentState;
    private boolean success;

    public TransactionModel(String transactionID, StateNodeModel startingStateNode) {
        this.transactionID = transactionID;
        currentState = startingStateNode;
    }

    public double calculateElapsedMilliseconds() {
        double elapsed = 0;

        if (events.size() == 1) {
            elapsed = Double.parseDouble(timings.get(0));
        }
        else if (events.size() > 1) {
            long startTime = events.get(0).getOriginTime();
            long endTime = events.get(events.size() - 1).getOriginTime();
            elapsed = endTime - startTime;
        }
        return elapsed;
    }

    @Override public String toString() {
        return "TransactionModel [transactionID=" +
               transactionID +
               ", timeoutTime=" +
               timeoutTime +
               ", currentState=" +
               currentState +
               ", success=" +
               success +
               "]";
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void addEvent(LogEvent event) {
        events.add(event);
    }

    public List<LogEvent> getEvents() {
        return events;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public long getTimeoutTime() {
        return timeoutTime;
    }

    public void setTimeoutTime(long timeoutTime) {
        this.timeoutTime = timeoutTime;
    }

    public StateNodeModel getCurrentState() {
        return currentState;
    }

    public void setCurrentState(StateNodeModel currentState) {
        this.currentState = currentState;
    }

    public void addTiming(String stateName, String timing) {
        stateNames.add(stateName);
        timings.add(timing);
    }

    public List<String> getStateNames() {
        return stateNames;
    }

    public List<String> getTimings() {
        return timings;
    }

    public long getStartTime() {
        return events.get(0).getOriginTime();

    }
}
