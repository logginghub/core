package com.logginghub.utils.sof;


public class ThreadLocalCharArrays {

    private ThreadLocal<char[]> buffers = new ThreadLocal<char[]>() {
        protected char[] initialValue() {
            return new char[10240];
        };
    };
    
    public char[] get(int requiredCapacity) {
        
        char[] array = buffers.get();
        if(array.length < requiredCapacity) {
            array = new char[requiredCapacity];
            buffers.set(array);
        }
        
        return array;
         
    }

}
