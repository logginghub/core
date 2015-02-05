package com.logginghub.logging.messages;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 05/02/15.
 */
public class InstanceKey implements SerialisableObject {

    private String environment;
    private String host;
    private String address;
    private int pid;
    private String instanceType;
    private String instanceIdentifier;

    // Derived field,but its used a lot so its worth caching
    private String sourceApplication;

    public InstanceKey(String environment, String host, String address, int pid, String instanceType, String instanceIdentifier) {
        this.environment = environment;
        this.host = host;
        this.address = address;
        this.pid = pid;
        this.instanceType = instanceType;
        this.instanceIdentifier = instanceIdentifier;
    }

    public String buildKey() {
        return StringUtils.format("{}.{}.{}.{}", environment, host, instanceType, instanceIdentifier);
    }

    public InstanceKey() {
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
        updateSourceApplication();
    }

    public String getInstanceIdentifier() {
        return instanceIdentifier;
    }

    public void setInstanceIdentifier(String instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
        updateSourceApplication();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InstanceKey))
            return false;

        InstanceKey that = (InstanceKey) o;

        if (pid != that.pid)
            return false;
        if (address != null ? !address.equals(that.address) : that.address != null)
            return false;
        if (environment != null ? !environment.equals(that.environment) : that.environment != null)
            return false;
        if (host != null ? !host.equals(that.host) : that.host != null)
            return false;
        if (instanceIdentifier != null ? !instanceIdentifier.equals(that.instanceIdentifier) : that.instanceIdentifier != null)
            return false;
        if (instanceType != null ? !instanceType.equals(that.instanceType) : that.instanceType != null)
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = environment != null ? environment.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + pid;
        result = 31 * result + (instanceType != null ? instanceType.hashCode() : 0);
        result = 31 * result + (instanceIdentifier != null ? instanceIdentifier.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("InstanceKey{");
        sb.append("environment='").append(environment).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", address='").append(address).append('\'');
        sb.append(", pid=").append(pid);
        sb.append(", instanceType='").append(instanceType).append('\'');
        sb.append(", instanceIdentifier='").append(instanceIdentifier).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override public void read(SofReader reader) throws SofException {
        environment = reader.readString(0);
        host = reader.readString(1);
        address = reader.readString(2);
        pid = reader.readInt(3);
        instanceType = reader.readString(4);
        instanceIdentifier = reader.readString(5);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, environment);
        writer.write(1, host);
        writer.write(2, address);
        writer.write(3, pid);
        writer.write(4, instanceType);
        writer.write(5, instanceIdentifier);
    }

    private void updateSourceApplication() {
        if(instanceIdentifier != null) {
            sourceApplication = instanceType + "-" + instanceIdentifier;
        }else{
            sourceApplication = instanceType;
        }
    }

    public String getSourceApplication() {
        return sourceApplication;
    }
}
