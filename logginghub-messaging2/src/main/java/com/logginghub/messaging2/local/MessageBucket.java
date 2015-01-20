package com.logginghub.messaging2.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;

public class MessageBucket implements MessageListener {

    private List<Message> messages = new ArrayList<Message>();
    private CountDownLatch latch;

    public int size() {
        return messages.size();
    }

    @SuppressWarnings("unchecked") public <T extends Message> T get(int i) {
        return (T) messages.get(i);
    }

    public void onNewMessage(Message message) {
        messages.add(message);
        if (latch != null) {
            latch.countDown();
        }
    }

    public void waitForItems(int count, int time, TimeUnit units) {
        if (messages.size() == count) {
            // Return straight away
        }
        else {
            latch = new CountDownLatch(count - messages.size());
            try {
                boolean await = latch.await(time, units);
                if(!await){
                    throw new RuntimeException("Timed out waiting for bucket items");
                }
            }
            catch (InterruptedException e) {}
            latch = null;
        }
    }
}
