package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.integrationtests.loggingfrontend.helpers.NewEraBase;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.logging.frontend.Utils;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.HubConnectionModel;
import com.logginghub.logging.frontend.model.HubConnectionModel.ConnectionState;
import com.logginghub.logging.frontend.model.LoggingFrontendModel;
import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTablePanel;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.SwingHelper;
import com.logginghub.utils.TestUtils;
import com.logginghub.utils.TestUtils.BooleanOperation;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.observable.ObservableList;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTabbedPaneFixture;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TestTabsChangeColourWhenEventsComeIn {

    private SwingFrontEnd swingFrontEnd;
    private FrameFixture frameFixture;

    @Ignore // jshaw - OS specific tab colours
    @Test public void testWithTwoEnvironments() throws IOException {

        SocketHub hub1 = new SocketHub();
        SocketHub hub2 = new SocketHub();
        SocketHub hub3 = new SocketHub();

        hub1.useRandomPort();
        hub2.useRandomPort();
        hub3.useRandomPort();

        hub1.start();
        hub2.start();
        hub3.start();

        HubConfiguration hub1Configuration = new HubConfiguration();
        hub1Configuration.setName("Hub 1");
        hub1Configuration.setHost("localhost");
        hub1Configuration.setPort(hub1.getPort());

        HubConfiguration hub2Configuration = new HubConfiguration();
        hub2Configuration.setName("Hub 2");
        hub2Configuration.setHost("localhost");
        hub2Configuration.setPort(hub2.getPort());

        HubConfiguration hub3Configuration = new HubConfiguration();
        hub3Configuration.setName("Hub 3");
        hub3Configuration.setHost("localhost");
        hub3Configuration.setPort(hub3.getPort());

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

        swingFrontEnd = NewEraBase.createFrontend(configuration);

        frameFixture = new FrameFixture(swingFrontEnd);

        JTabbedPaneFixture tabbedPane = frameFixture.tabbedPane("mainTabbedPane");
        assertThat(tabbedPane.tabTitles(), is(new String[] { "Environment 1", "Environment 2" }));

        LogEvent event1 = LogEventBuilder.start()
                                         .setSourceApplication("app1")
                                         .setMessage("Env1-warning")
                                         .setLevel(Level.WARNING.intValue())
                                         .toLogEvent();
        LogEvent event2 = LogEventBuilder.start()
                                         .setSourceApplication("app2")
                                         .setMessage("Env2-warning")
                                         .setLevel(Level.WARNING.intValue())
                                         .toLogEvent();
        LogEvent event3 = LogEventBuilder.start()
                                         .setSourceApplication("app3")
                                         .setMessage("Env2-severe")
                                         .setLevel(Level.SEVERE.intValue())
                                         .toLogEvent();

        final List<HubConnectionModel> hubModels = new ArrayList<HubConnectionModel>();
        LoggingFrontendModel model = swingFrontEnd.getModel();
        ObservableList<EnvironmentModel> environments = model.getEnvironments();
        for (EnvironmentModel environmentModel : environments) {
            ObservableList<HubConnectionModel> hubs = environmentModel.getHubConnectionModels();
            for (HubConnectionModel hubModel : hubs) {
                hubModels.add(hubModel);
            }
        }

        assertThat(hubModels.size(), is(3));

        // We have no way to wait until the client connections to the hubs
        // complete, so just loop (I guess we could add listeners at the start?)
        TestUtils.runUntilTrue(new BooleanOperation() {
            @Override public boolean run() {
                boolean done = true;
                for (HubConnectionModel hubModel : hubModels) {
                    ConnectionState connectionState = hubModel.getConnectionState().get();
                    done &= (connectionState == ConnectionState.Connected);
                }
                return done;
            }
        });

        LogEventBucket env1Bucket = new LogEventBucket();
        LogEventBucket env2Bucket = new LogEventBucket();

        final EnvironmentModel environment1Model = swingFrontEnd.getModel().getEnvironment("Environment 1");
        final EnvironmentModel environment2Model = swingFrontEnd.getModel().getEnvironment("Environment 2");
        environment1Model.addLogEventListener(env1Bucket);
        environment2Model.addLogEventListener(env2Bucket);

        hub1.processLogEvent(new LogEventMessage(event1), null);
        hub2.processLogEvent(new LogEventMessage(event2), null);
        hub3.processLogEvent(new LogEventMessage(event3), null);

        env1Bucket.waitForMessages(1);
        env2Bucket.waitForMessages(2);

        final DetailedLogEventTablePanel env1DetailPanel = swingFrontEnd.getMainPanel().getDetailedLogEventTablePanelForEnvironment("Environment 1");
        final DetailedLogEventTablePanel env2DetailPanel = swingFrontEnd.getMainPanel().getDetailedLogEventTablePanelForEnvironment("Environment 2");

        ThreadUtils.untilTrue("Waiting for the events to be posted", 1, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return env1DetailPanel.getCurrentBatchSize() == 0 &&
                       env2DetailPanel.getCurrentBatchSize() == 0 &&
                       environment1Model.getEventController().getLiveEventsThatPassFilter().size() == 1 &&
                       environment2Model.getEventController().getLiveEventsThatPassFilter().size() == 2;
            }
        });

        // This complex - we can do all the waiting in the world, but the final thing that switches
        // the colour is on an invoke later
        SwingHelper.waitForQueueToFlush();

        JTabbedPane target = tabbedPane.target;
        Color backgroundAt1 = target.getBackgroundAt(0);
        Color backgroundAt2 = target.getBackgroundAt(1);

        Color foregroundAt1 = target.getForegroundAt(0);
        Color foregroundAt2 = target.getForegroundAt(1);

        // The first tab will be selected, so that should be the default
        assertThat(ColourUtils.toHex(foregroundAt1), is("333333"));
        assertThat(ColourUtils.toHex(backgroundAt1), is("808080"));

//        ThreadUtils.sleep(1234123);
        // The second tab should be red
        assertThat(ColourUtils.toHex(foregroundAt2), is("404040"));
        assertThat(ColourUtils.toHex(backgroundAt2), is(ColourUtils.toHex(Utils.getBackgroundColourForLevel(Level.SEVERE.intValue()))));

        swingFrontEnd.dispose();
        frameFixture.cleanUp();

        hub1.close();
        hub2.close();
        hub3.close();
    }

}
