package com.logginghub.logging.transaction.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.HubStackCaptureModule;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.module.Configures;

@Configures(HubStackCaptureModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class HubStackCaptureConfiguration {
    // TODO : lower the default when we are done
    @XmlAttribute private String snapshotInterval = "1 second";

    // TODO : have a go at guessing a lot of this stuff
    @XmlAttribute private int instanceNumber = 0;
    @XmlAttribute private String instanceType = "instance?";
    @XmlAttribute private String host = NetUtils.getLocalHostname();
    @XmlAttribute private String environment = "environment?";

    @XmlAttribute private String destinationRef;

    public void setSnapshotInterval(String snapshotInterval) {
        this.snapshotInterval = snapshotInterval;
    }

    public String getSnapshotInterval() {
        return snapshotInterval;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
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

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public String getDestinationRef() {
        return destinationRef;

    }

    public void setDestinationRef(String destinationRef) {
        this.destinationRef = destinationRef;
    }

}
