package com.logginghub.messaging2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;
import com.logginghub.messaging2.messages.RequestMessage;

public class RequestResponseTest extends AbstractTwoClientBase {
    
       
    @Test public void test() {

        clientB.addMessageListener(new MessageListener() {
            public void onNewMessage(Message message) {
                if (message instanceof RequestMessage) {
                    RequestMessage requestMessage = (RequestMessage) message;
                    Object requestPayload = requestMessage.getPayload();
                    clientB.sendResponse(requestMessage.getSourceID(), requestMessage.getRequestID(), requestPayload.toString() + " back");
                }
            }
        });

        String reply = clientA.sendRequest("clientB", "Hello", 10, TimeUnit.SECONDS);
        assertThat(reply, is(String.class));
        assertThat(reply.toString(), is("Hello back"));
    }
}
