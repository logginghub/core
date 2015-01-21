package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.EnumSet;

import org.junit.Test;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.api.patterns.PingResponse;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.MapMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.ThreadUtils;

public class TestChannelSubscriptionsModule extends BaseHub {

    @Test public void test_private_channel() throws ConnectorException, LoggingMessageSenderException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions));
        SocketHub hub = hubFixture.start();

        SocketClient clientA = fixture.createClient("clientA", hub);
        SocketClient clientB = fixture.createClient("clientB", hub);

        // TODO : work out how to solve the async subscriptions delay problem
        ThreadUtils.sleep(100);

        Bucket<LoggingMessage> clientAMessages = fixture.createMessageBucketFor(clientA);

        clientB.send(new ChannelMessage(Channels.getPrivateConnectionChannel(0), new MapMessage("message", "hello world")));

        clientAMessages.waitForMessages(1);

        assertThat(clientAMessages.size(), is(1));

        assertThat(((MapMessage) (((ChannelMessage) clientAMessages.get(0)).getPayload())).get("message"), is("hello world"));

    }

    @Test public void test_ping_response() throws ConnectorException, LoggingMessageSenderException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions));
        SocketHub hub = hubFixture.start();

        SocketClient clientA = fixture.createClient("clientA", hub);
        SocketClient clientB = fixture.createClient("clientB", hub);

        // TODO : work out how to solve the async subscriptions delay problem
        ThreadUtils.sleep(100);

        Bucket<LoggingMessage> clientBMessages = fixture.createMessageBucketFor(clientB);

        clientB.getInstanceManagementAPI().sendPing();

        clientBMessages.waitForMessages(1);

        assertThat(clientBMessages.size(), is(1));

        LoggingMessage loggingMessage = clientBMessages.get(0);
        ChannelMessage channelMessage = (ChannelMessage) loggingMessage;
        PingResponse pingResponse = (PingResponse) channelMessage.getPayload();

        assertThat(pingResponse.getInstanceDetails().getHostname(), is(NetUtils.getLocalHostname()));

        // jshaw - this is confusing - the actual address the socket is bound to on linux comes out
        // as 127.0.1.1 as this is defined in /etc/hosts due to some linux issue
        // (http://www.linuxquestions.org/questions/linux-networking-3/what-does-127-0-1-1-mean-623421/)
        // I still think it'll help to have the actual bind address in the instance response, but we
        // cant use the usual NetUtils approach
        // assertThat(pingResponse.getInstanceDetails().getHostIP(), is(NetUtils.getLocalIP()));
        // assertThat(pingResponse.getInstanceName(), is("Test"));
        // assertThat(pingResponse.getPid(), is(666));

    }
}
