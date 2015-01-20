package com.logginghub.logging.modules;

import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;

public class StackCaptureConfiguration {

    // TODO : lower the default when we are done
    private String snapshotInterval = "1 second";

    // TODO : have a go at guessing a lot of this stuff
    private int instanceNumber = 0;
    private String instanceType = "instance?";
    private String host = NetUtils.getLocalHostname();
    private String environment = "environment?";

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

    public void setSnapshotInterval(long interval) {
        setSnapshotInterval(Long.toString(interval));
    }

    public void parseApplicationName(String sourceApplication) {

        try {
            String trailingNumber = StringUtils.trailingInteger(sourceApplication);
            if (StringUtils.isNotNullOrEmpty(trailingNumber)) {
                instanceNumber = Integer.parseInt(trailingNumber);
                String remained = StringUtils.before(sourceApplication, trailingNumber);
                if(remained.endsWith(".") || remained.endsWith("-")) {
                    remained = remained.substring(0, remained.length()-1);
                }
                
                instanceType = remained;
            }
            else {
                instanceType = sourceApplication;
                instanceNumber = 1;
            }
        }
        catch (RuntimeException e) {
            Out.err("Failed to parse application name - will use default values : {}", e.getMessage());
        }

    }

}
