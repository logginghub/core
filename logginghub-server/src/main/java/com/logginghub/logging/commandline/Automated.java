package com.logginghub.logging.commandline;

import java.io.IOException;

import com.logginghub.utils.Out;

public class Automated {

    public static void main(String[] args) throws IOException, InterruptedException {
     
        CommandLineController controller = new CommandLineController() {
            @Override public void append(String line, Object... args) {
                Out.out(line, args);
            }
        };
        
        controller.processCommand("connect vl-ec2 false");        
        controller.processCommand("mkpattern sigar_os2 Sigar OS - mfp={memoryfree} mup={memoryused} mt={memorytotal} mu={memoryused} neti={networkin} neto={networkout} cpu={cpu-used} us={cpu-user} sy={cpu-sys} id={cpu-idle} wa={cpu-waiting}");        
        controller.processCommand("lspatterns");
        controller.processCommand("subscribepattern 1");
        
        Thread.sleep(10000);
        
        System.exit(0);
    }
    
}
