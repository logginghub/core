package com.logginghub.integrationtests.loggingfrontend.helpers;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.logginghub.logging.frontend.ConfigurationProxy;
import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.NetUtils;

public class NewEraBase {

    protected SwingFrontEnd swingFrontEnd;
    protected FrameFixture frameFixture;

    @BeforeClass public static void setupFest() {
        FailOnThreadViolationRepaintManager.install();
    }

    @After public void clearup() {
        if (swingFrontEnd != null) {
            swingFrontEnd.close();
            swingFrontEnd.dispose();
        }

        if (frameFixture != null) {
            frameFixture.cleanUp();
        }
    }

    @Before public void setup() {
        HubConfiguration hub1Configuration = new HubConfiguration();
        hub1Configuration.setName("Hub 1");
        hub1Configuration.setHost("localhost");
        hub1Configuration.setPort(NetUtils.findFreePort());

        HubConfiguration hub2Configuration = new HubConfiguration();
        hub2Configuration.setName("Hub 2");
        hub2Configuration.setHost("localhost");
        hub2Configuration.setPort(NetUtils.findFreePort());

        HubConfiguration hub3Configuration = new HubConfiguration();
        hub3Configuration.setName("Hub 3");
        hub3Configuration.setHost("localhost");
        hub3Configuration.setPort(NetUtils.findFreePort());

        EnvironmentConfiguration firstTab = new EnvironmentConfiguration();
        firstTab.setName("Environment 1");
        firstTab.getHubs().add(hub1Configuration);

        EnvironmentConfiguration secondTab = new EnvironmentConfiguration();
        secondTab.setName("Environment 2");
        secondTab.getHubs().add(hub2Configuration);
        secondTab.getHubs().add(hub3Configuration);

        final LoggingFrontendConfiguration configuration = new LoggingFrontendConfiguration();
        configuration.getEnvironments().add(firstTab);
        configuration.getEnvironments().add(secondTab);
        configuration.setTitle("Test with two tabs");

        swingFrontEnd = createFrontend(configuration);

        frameFixture = new FrameFixture(swingFrontEnd);
    }

    public static SwingFrontEnd createFrontend(final LoggingFrontendConfiguration configuration) {
        SwingFrontEnd swingFrontEnd = GuiActionRunner.execute(new GuiQuery<SwingFrontEnd>() {
            @Override protected SwingFrontEnd executeInEDT() throws Throwable {
                SwingFrontEnd swingFrontEnd = new SwingFrontEnd();
                swingFrontEnd.setConfiguration(new ConfigurationProxy(new Metadata(), configuration));
                swingFrontEnd.startConnections();
                swingFrontEnd.setVisible(true);
                return swingFrontEnd;
            }
        });
        return swingFrontEnd;
    }

}
