package com.logginghub.logging.modules;

import com.logginghub.logging.messages.InstanceKey;

public class StackCaptureConfiguration {

    private String snapshotInterval = "1 minute";

    private InstanceKey instanceKey;

    private String requestInterval = "0";
    private boolean respondToRequests = true;

    public void setInstanceKey(InstanceKey instanceKey) {
        this.instanceKey = instanceKey;
    }

    public InstanceKey getInstanceKey() {
        return instanceKey;
    }

    public void setSnapshotInterval(String snapshotInterval) {
        this.snapshotInterval = snapshotInterval;
    }

    public String getSnapshotInterval() {
        return snapshotInterval;
    }

    public void setSnapshotInterval(long interval) {
        setSnapshotInterval(Long.toString(interval));
    }

    public String getRequestInterval() {
        return requestInterval;
    }

    public void setRequestInterval(String requestInterval) {
        this.requestInterval = requestInterval;
    }

    public boolean isRespondToRequests() {
        return respondToRequests;
    }

    public void setRespondToRequests(boolean respondToRequests) {
        this.respondToRequests = respondToRequests;
    }
}
