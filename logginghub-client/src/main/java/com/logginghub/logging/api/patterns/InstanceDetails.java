package com.logginghub.logging.api.patterns;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class InstanceDetails implements SerialisableObject {

    private String hostname;
    private String hostIP;
    private String instanceName;
    private int pid;
    private int localPort;

    @Override public void read(SofReader reader) throws SofException {
        hostname = reader.readString(0);
        hostIP = reader.readString(1);
        instanceName = reader.readString(2);
        pid = reader.readInt(3);
        localPort = reader.readInt(4);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, hostname);
        writer.write(1, hostIP);
        writer.write(2, instanceName);
        writer.write(3, pid);
        writer.write(4, localPort);
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override public String toString() {
        return "InstanceDetails [hostname=" + hostname + ", hostIP=" + hostIP + ", instanceName=" + instanceName + ", pid=" + pid + "]";
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPid() {
        return pid;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
    
    public int getLocalPort() {
        return localPort;
    }
}
