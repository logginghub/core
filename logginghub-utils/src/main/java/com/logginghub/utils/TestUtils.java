package com.logginghub.utils;

public class TestUtils {

    private static final long defaultIterationDelay = 100;
    private static final long defaultRetryDuration = 5000;
     
    public interface BooleanOperation {
        boolean run();
    }
    
    public static void runUntilTrue(BooleanOperation operation){
        
        long startTime = System.currentTimeMillis();
        boolean done = false;
        while(!done){
            done = operation.run();
            if(!done){
                if(System.currentTimeMillis() - startTime >= defaultRetryDuration){
                    throw new RuntimeException("Repeating operation timed out");
                }
                ThreadUtils.sleep(defaultIterationDelay);
            }
        }
    }

}
