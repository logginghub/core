package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.frontend.ConfigurationProxy;
import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.HubConnectionModel;
import com.logginghub.logging.frontend.model.HubConnectionModel.ConnectionState;
import com.logginghub.logging.frontend.model.LoggingFrontendModel;
import com.logginghub.logging.frontend.model.ObservableList;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.TestUtils;
import com.logginghub.utils.TestUtils.BooleanOperation;
import com.logginghub.utils.ThreadUtils;
import org.fest.swing.data.TableCell;
import org.fest.swing.driver.BasicJTableCellReader;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTabbedPaneFixture;
import org.fest.swing.fixture.JTableFixture;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TestConfigurationWithActualHubs {

    private SwingFrontEnd swingFrontEnd;
    private FrameFixture frameFixture;
    
    @After public void clearup() {
        frameFixture.cleanUp();
        swingFrontEnd.close();
    }

    @Ignore // jshaw - broken after OSX migration
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

        swingFrontEnd = GuiActionRunner.execute(new GuiQuery<SwingFrontEnd>() {
            @Override protected SwingFrontEnd executeInEDT() throws Throwable {
                SwingFrontEnd swingFrontEnd = new SwingFrontEnd();
                swingFrontEnd.setConfiguration(new ConfigurationProxy(new Metadata(), configuration));
                swingFrontEnd.startConnections();
                swingFrontEnd.setVisible(true);
                return swingFrontEnd;
            }
        });

        frameFixture = new FrameFixture(swingFrontEnd);

        JTabbedPaneFixture tabbedPane = frameFixture.tabbedPane("mainTabbedPane");
        assertThat(tabbedPane.tabTitles(), is(new String[] { "Environment 1", "Environment 2" }));

        LogEvent event1 = LogEventFactory.createFullLogEvent1("app1");
        LogEvent event2 = LogEventFactory.createFullLogEvent1("app2");
        LogEvent event3 = LogEventFactory.createFullLogEvent1("app3");

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
                    ConnectionState connectionState = hubModel.getConnectionState();
                    done &= (connectionState == ConnectionState.Connected);
                }
                return done;
            }
        });

        // TODO : replace these with something a little more robust!
        hub1.processLogEvent(new LogEventMessage(event1), null);
        ThreadUtils.sleep(100);
        hub2.processLogEvent(new LogEventMessage(event2), null);
        ThreadUtils.sleep(100);
        hub3.processLogEvent(new LogEventMessage(event3), null);
        
        ThreadUtils.sleep(1000);

        frameFixture.tabbedPane("mainTabbedPane").selectTab("Environment 1");
        frameFixture.panel("logEventPanel-Environment 1").table("logEventTable").requireRowCount(1);
        assertTableRow(frameFixture.panel("logEventPanel-Environment 1").table("logEventTable"), 0, event1);
        
        frameFixture.tabbedPane("mainTabbedPane").selectTab("Environment 2");
        frameFixture.panel("logEventPanel-Environment 2").table("logEventTable").requireRowCount(2);
        assertTableRow(frameFixture.panel("logEventPanel-Environment 2").table("logEventTable"), 0, event2);
        assertTableRow(frameFixture.panel("logEventPanel-Environment 2").table("logEventTable"), 1, event3);

        swingFrontEnd.dispose();
        frameFixture.cleanUp();
    }
    

    public static void assertTableRow(JTableFixture table, int row, LogEvent event) {
        int messageIndex = table.columnIndexFor("Message");
        int sourceAppIndex = table.columnIndexFor("Source");

        TableCell sourceColumn = TableCell.row(row).column(sourceAppIndex);
        TableCell messageColumn = TableCell.row(row).column(messageIndex);
        
        String sourceColumnText = sourceColumn.toString();
        String messageColumnText = messageColumn.toString();
        
        BasicJTableCellReader reader = new BasicJTableCellReader();
        String message = reader.valueAt(table.target, 0, messageIndex);
        String sourceApp = reader.valueAt(table.target, 0, sourceAppIndex);
        
        
        table.requireCellValue(sourceColumn, event.getSourceApplication());
        table.requireCellValue(messageColumn, event.getMessage());
    }
}
