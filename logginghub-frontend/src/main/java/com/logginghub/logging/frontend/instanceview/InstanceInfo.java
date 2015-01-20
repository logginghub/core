package com.logginghub.logging.frontend.instanceview;

import com.logginghub.logging.api.patterns.PingResponse;

public class InstanceInfo {

    private String name;
    private String host;
    private String ip;
    private int localPort;
    private long delay;
    private int pid;
    private long lastResponse;

    public InstanceInfo() {}

    public InstanceInfo(PingResponse t) {
        this.name = t.getInstanceDetails().getInstanceName();
        this.host = t.getInstanceDetails().getHostname();
        this.ip = t.getInstanceDetails().getHostIP();
        this.delay = 0;
        this.localPort = t.getInstanceDetails().getLocalPort();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPid() {
        return pid;
    }
    
    public void setLastResponse(long lastResponse) {
        this.lastResponse = lastResponse;
    }
    
    public long getLastResponse() {
        return lastResponse;
    }
    
    public int getLocalPort() {
        return localPort;
    }
    
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
}
