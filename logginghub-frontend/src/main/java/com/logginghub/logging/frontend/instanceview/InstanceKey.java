package com.logginghub.logging.frontend.instanceview;

import com.logginghub.utils.CompareUtils;

public class InstanceKey implements Comparable<InstanceKey> {
    private String instanceName;
    private String ip;
    private int localPort;

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getAddress() {
        return ip;
    }

    public void setAddress(String host) {
        this.ip = host;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instanceName == null) ? 0 : instanceName.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + localPort;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InstanceKey other = (InstanceKey) obj;
        if (instanceName == null) {
            if (other.instanceName != null) {
                return false;
            }
        }
        else if (!instanceName.equals(other.instanceName)) {
            return false;
        }
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        }
        else if (!ip.equals(other.ip)) {
            return false;
        }
        if (localPort != other.localPort) {
            return false;
        }
        return true;
    }

    @Override public int compareTo(InstanceKey other) {
        return CompareUtils.add(this.instanceName, other.instanceName).add(this.ip, other.ip).add(this.localPort, other.localPort).compare();
    }

}
