package com.logginghub.messaging2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.logginghub.messaging2.messages.BasicMessage;

public class PointToPointTest extends AbstractTwoClientBase {

    @Test
    public void test() {

        clientA.send("clientB", "hello");

        clientBBucket.waitForMessages(1, 5, TimeUnit.SECONDS);

        assertThat(clientABucket.size(), is(0));
        assertThat(clientBBucket.size(), is(1));
        assertThat(clientBBucket.get(0), is(instanceOf(BasicMessage.class)));
        assertThat(clientBBucket.get(0).getPayload().toString(), is("hello"));
    }
}
