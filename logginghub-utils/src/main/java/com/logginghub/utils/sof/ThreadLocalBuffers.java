package com.logginghub.utils.sof;

import java.nio.ByteBuffer;

public class ThreadLocalBuffers {

    private ThreadLocal<ByteBuffer> buffers = new ThreadLocal<ByteBuffer>() {
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(1024);
        };
    };
    
    public ByteBuffer get(int requiredCapacity) {
        ByteBuffer byteBuffer = buffers.get();
        byteBuffer.clear();
        
        if(byteBuffer.capacity() < requiredCapacity) {
            byteBuffer = ByteBuffer.allocate(requiredCapacity);
            buffers.set(byteBuffer);
        }
        
        return byteBuffer;
         
    }

}
