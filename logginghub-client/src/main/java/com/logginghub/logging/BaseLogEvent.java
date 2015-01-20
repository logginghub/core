package com.logginghub.logging;

public abstract class BaseLogEvent implements LogEvent {
    private int pid;
    private String channel;
    private String sourceApplication;
    private String sourceHost;
    private String sourceAddress;
    private String threadName;

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getSourceApplication() {
        return sourceApplication;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
