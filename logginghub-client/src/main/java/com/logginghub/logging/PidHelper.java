package com.logginghub.logging;

import com.logginghub.logging.telemetry.SigarHelper;
import com.logginghub.utils.ProcessUtils;

/**
 * As we don't have a sigar lib for arm64 osx we need to handle all the sigar calls failing, and this will solve
 * the problem for PID using the java jmx workaround for now. We've also added -Dcom.logginghub.logging.nosigar=true
 * as a way to force sigar off on the client side.
 */
public class PidHelper {


    public static int getPid() {
        int pid;

        if(SigarSetting.noSigar()) {
            pid = ProcessUtils.getPid();
        }else {
            try {
                pid = SigarHelper.getPid();
            } catch (UnsatisfiedLinkError t) {
                pid = ProcessUtils.getPid();
            }
        }

        return pid;
    }
}
