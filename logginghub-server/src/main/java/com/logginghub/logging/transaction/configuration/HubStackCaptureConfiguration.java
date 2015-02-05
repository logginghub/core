package com.logginghub.logging.transaction.configuration;

import com.logginghub.logging.messages.InstanceKey;
import com.logginghub.logging.modules.HubStackCaptureModule;
import com.logginghub.logging.telemetry.SigarHelper;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.module.Configures;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Configures(HubStackCaptureModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class HubStackCaptureConfiguration {
    // TODO : lower the default when we are done
    @XmlAttribute private String snapshotBroadcastInterval = "1 minute";
    @XmlAttribute private String snapshotRequestInterval = "1 minute";

    // TODO : have a go at guessing a lot of this stuff
    @XmlAttribute private String instanceIdentifier = null;
    @XmlAttribute private String instanceType = "logginghub";
    @XmlAttribute private String host = NetUtils.getLocalHostname();
    @XmlAttribute private String address = NetUtils.getLocalIP();
    @XmlAttribute private String environment = "environment?";

    @XmlAttribute private String destinationRef;

    @XmlAttribute private boolean respondToRequests=false;
    @XmlAttribute private boolean outputToLog=false;
    @XmlAttribute private String channel = "stack";

    public boolean isOutputToLog() {
        return outputToLog;
    }

    public void setOutputToLog(boolean outputToLog) {
        this.outputToLog = outputToLog;
    }

    public void setRespondToRequests(boolean respondToRequests) {
        this.respondToRequests = respondToRequests;
    }

    public boolean isRespondToRequests() {
        return respondToRequests;
    }

    public void setSnapshotBroadcastInterval(String snapshotBroadcastInterval) {
        this.snapshotBroadcastInterval = snapshotBroadcastInterval;
    }

    public String getSnapshotBroadcastInterval() {
        return snapshotBroadcastInterval;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getHost() {
        return host;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceIdentifier(String instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
    }

    public String getInstanceIdentifier() {
        return instanceIdentifier;
    }

    public String getDestinationRef() {
        return destinationRef;

    }

    public void setDestinationRef(String destinationRef) {
        this.destinationRef = destinationRef;
    }

    public String getSnapshotRequestInterval() {
        return snapshotRequestInterval;
    }

    public void setSnapshotRequestInterval(String snapshotRequestInterval) {
        this.snapshotRequestInterval = snapshotRequestInterval;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public InstanceKey getInstanceKey() {
        InstanceKey instanceKey = new InstanceKey();

        try {
            instanceKey.setPid(SigarHelper.getPid());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        instanceKey.setInstanceIdentifier(instanceIdentifier);
        instanceKey.setInstanceType(instanceType);
        instanceKey.setHost(host);
        instanceKey.setAddress(address);
        instanceKey.setEnvironment(environment);

        return instanceKey;
    }

}
