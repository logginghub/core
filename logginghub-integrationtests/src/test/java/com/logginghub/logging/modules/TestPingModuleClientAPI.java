package com.logginghub.logging.modules;

import java.util.EnumSet;

import org.junit.Test;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.api.patterns.InstanceManagementAPI;
import com.logginghub.logging.api.patterns.PingResponse;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;

public class TestPingModuleClientAPI extends BaseHub {

    @Test public void test_ping() throws ConnectorException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions,
                                                                   HubTestFixture.Features.PatternManager));
        SocketHub hub = hubFixture.start();

        SocketClient clientA = fixture.createClient("clientA", hub);
        SocketClient clientB = fixture.createClient("clientB", hub);
        
        InstanceManagementAPI instanceManagementAPI = clientA.getInstanceManagementAPI();
        
        final Bucket<PingResponse> responses = new Bucket<PingResponse>();
        instanceManagementAPI.addPingListener(new Destination<PingResponse>() {
            @Override public void send(PingResponse t) {
                responses.add(t);
            }
        });
        
        instanceManagementAPI.sendPing();

        responses.waitForMessages(1);
        
    }
}
