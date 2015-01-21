package com.logginghub.logging.modules;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.logginghub.integrationtests.logging.HubTestFixture;

public class BaseHub {

    protected HubTestFixture fixture;
    
    @Before public void setupHubFixture() {
        fixture = new HubTestFixture();
    }
    
    @After public void shutdownHubFixture() throws IOException {
        fixture.stop();
    }
    
}
