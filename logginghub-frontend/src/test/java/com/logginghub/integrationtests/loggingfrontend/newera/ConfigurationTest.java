package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTabbedPaneFixture;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.logginghub.logging.frontend.ConfigurationProxy;
import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.model.HubConnectionModel;
import com.logginghub.utils.Metadata;

public class ConfigurationTest {

    private SwingFrontEnd swingFrontEnd;
    private FrameFixture frameFixture;

    @BeforeClass public static void setup() {
        FailOnThreadViolationRepaintManager.install();
    }

    @Test public void testEmpty() {

        swingFrontEnd = GuiActionRunner.execute(new GuiQuery<SwingFrontEnd>() {
            @Override protected SwingFrontEnd executeInEDT() throws Throwable {

                SwingFrontEnd swingFrontEnd = new SwingFrontEnd();
                swingFrontEnd.setVisible(true);
                return swingFrontEnd;
            }
        });

        frameFixture = new FrameFixture(swingFrontEnd);
    }
    
    @After public void cleanup() {
        swingFrontEnd.close();
        swingFrontEnd.dispose();        
        frameFixture.cleanUp();
    }
    
    @Test public void testWithTwoTabs() {

        HubConfiguration dummyHub = new HubConfiguration("Dummy", "localhost", 12312);
                
        EnvironmentConfiguration firstTab = new EnvironmentConfiguration();
        firstTab.setName("Environment 1");
        firstTab.getHubs().add(dummyHub);
        
        EnvironmentConfiguration secondTab = new EnvironmentConfiguration();
        secondTab.setName("Environment 2");
        secondTab.getHubs().add(dummyHub);
        
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
        assertThat(tabbedPane.tabTitles(), is(new String[] {"Environment 1", "Environment 2"}));
        
        assertThat(swingFrontEnd.getTitle(), is("Logging Front End - Test with two tabs"));
    }

}
