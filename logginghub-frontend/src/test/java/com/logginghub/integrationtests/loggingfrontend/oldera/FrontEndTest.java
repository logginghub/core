package com.logginghub.integrationtests.loggingfrontend.oldera;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.fest.swing.core.MouseButton;
import org.fest.swing.data.TableCell;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JLabelFixture;
import org.fest.swing.fixture.JTableCellFixture;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.frontend.ComponentKeys;
import com.logginghub.logging.frontend.ConfigurationProxy;
import com.logginghub.logging.frontend.LoggingMainPanel;
import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTable;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class FrontEndTest {

    @BeforeClass public static void setup() {
        FailOnThreadViolationRepaintManager.install();
        System.setProperty(DetailedLogEventTable.useDefaultColumnPropertiesKey, "true");
    }

    @AfterClass public static void teardown() {
        FailOnThreadViolationRepaintManager.install();
        System.setProperty(DetailedLogEventTable.useDefaultColumnPropertiesKey, "true");
    }

    private SwingFrontEnd swingFrontEnd;
    private FrameFixture frameFixture;
    private JTableCellFixture cell;
    private SwingFrontEndDSL dsl;

    @Before public void before() {
        swingFrontEnd = GuiActionRunner.execute(new GuiQuery<SwingFrontEnd>() {
            @Override protected SwingFrontEnd executeInEDT() throws Throwable {
                Metadata settings = new Metadata();

                settings.put("frame.x", 10);
                settings.put("frame.y", 10);
                settings.put("frame.width", 1024);
                settings.put("frame.height", 768);

                LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default")
                                                                                .highlighter("blue", "0x0000ff", false)
                                                                                .highlighter("green", "0x00ff00", false))
                                .toConfiguration();


                ConfigurationProxy proxy = new ConfigurationProxy(settings, "propertiesName", configuration, "parsers.xml");
                SwingFrontEnd swingFrontEnd = new SwingFrontEnd(proxy);
                swingFrontEnd.setVisible(true);
                return swingFrontEnd;
            }
        });

        frameFixture = new FrameFixture(swingFrontEnd);
        dsl = new SwingFrontEndDSL(swingFrontEnd, frameFixture);
    }

    @After public void after() {
        if (frameFixture != null) {
            frameFixture.cleanUp();
        }
        
        swingFrontEnd.close();
        swingFrontEnd.dispose();
    }

//    @Test public void renameTab() {
//        frameFixture.tabbedPane("mainTabbedPane").requireTabTitles("Log view", "Charting", "Configuration");
//        frameFixture.menuItemWithPath("Edit", "Rename current tab").click();
//        frameFixture.optionPane().textBox().setText("New tab title");
//        frameFixture.optionPane().button(JButtonMatcher.withText("OK")).click();
//        frameFixture.tabbedPane("mainTabbedPane").requireTabTitles("New tab title", "Charting", "Configuration");
//    }

//    @Test public void addTab() {
//        // Create a new tab
//        frameFixture.menuItemWithPath("Edit", "Add new tab").click();
//
//        // Make sure both tabs start empty
//        frameFixture.tabbedPane("mainTabbedPane").selectTab("Log view");
//        frameFixture.panel("logEventPanel-0").table("logEventTable").requireRowCount(0);
//
//        frameFixture.tabbedPane("mainTabbedPane").selectTab("New tab");
//        frameFixture.panel("logEventPanel-1").table("logEventTable").requireRowCount(0);
//
//        // Publish an event
//        DefaultLogEvent publishEvent = publishEvent();
//
//        // Make sure the event appears in both tabs
//        frameFixture.tabbedPane("mainTabbedPane").selectTab("Log view");
//        frameFixture.panel("logEventPanel-0").table("logEventTable").requireRowCount(1);
//        assertTableRow(frameFixture.panel("logEventPanel-0").table("logEventTable"), 0, publishEvent);
//
//        frameFixture.tabbedPane("mainTabbedPane").selectTab("New tab");
//        frameFixture.panel("logEventPanel-1").table("logEventTable").requireRowCount(1);
//        assertTableRow(frameFixture.panel("logEventPanel-1").table("logEventTable"), 0, publishEvent);
//
//        // Clear the second tab and make sure it doesn't clear the first one
//        frameFixture.panel("logEventPanel-1").button("clear").click();
//        frameFixture.panel("logEventPanel-1").table("logEventTable").requireRowCount(0);
//
//        frameFixture.tabbedPane("mainTabbedPane").selectTab("Log view");
//        frameFixture.panel("logEventPanel-0").table("logEventTable").requireRowCount(1);
//    }

    public static void assertTableRow(JTableFixture table, int row, DefaultLogEvent event) {
        int levelIndex = table.columnIndexFor("Level");
        int messageIndex = table.columnIndexFor("Message");
        int sourceAppIndex = table.columnIndexFor("Source");

        table.requireCellValue(TableCell.row(0).column(sourceAppIndex), event.getSourceApplication());
        table.requireCellValue(TableCell.row(0).column(levelIndex), event.getJULILevelDescription());
        table.requireCellValue(TableCell.row(0).column(messageIndex), event.getMessage());
    }

    @Test public void testAddingASingleEntry() throws InterruptedException {
        final LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();

        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        dsl.publishEvent(event);

        waitForBatch(mainPanel);
        
        JTableFixture table = frameFixture.table("logEventTable");

        int levelIndex = table.columnIndexFor("Level");
        int messageIndex = table.columnIndexFor("Message");
        int sourceAppIndex = table.columnIndexFor("Source");

        assertThat(table.rowCount(), is(1));
        table.requireCellValue(TableCell.row(0).column(sourceAppIndex), "TestApplication");
        table.requireCellValue(TableCell.row(0).column(levelIndex), "INFO");
        table.requireCellValue(TableCell.row(0).column(messageIndex), "This is mock record 1");

        table.click(TableCell.row(0).column(0), MouseButton.LEFT_BUTTON);

        JLabelFixture textBox = frameFixture.label(ComponentKeys.EventDetailSourceApplication.name());
        String text = textBox.text();
        assertThat(textBox.text().length(), is(greaterThan(0)));
        assertThat(text.contains("TestApplication"), is(true));

        Thread.sleep(1000);
        JLabelFixture label = frameFixture.label(ComponentKeys.StatusText.name());
        assertThat(label.text(), is(startsWith("1")));
    }

    private void waitForBatch(final LoggingMainPanel mainPanel) {
        ThreadUtils.untilTrue("Wating for log event batch publishing to complete", 1, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return mainPanel.getFirstDetailPanel().getCurrentBatchSize() == 0;
            }
        });
    }

    @Test public void testClearingTheEvents() {
        final LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();

        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        dsl.publishEvent(event);

        waitForBatch(mainPanel);

        JTableFixture table = frameFixture.table("logEventTable");

        assertThat(table.rowCount(), is(1));

        int levelIndex = table.columnIndexFor("Level");
        int messageIndex = table.columnIndexFor("Message");
        int sourceAppIndex = table.columnIndexFor("Source");

        table.requireCellValue(TableCell.row(0).column(sourceAppIndex), "TestApplication");
        table.requireCellValue(TableCell.row(0).column(levelIndex), "INFO");
        table.requireCellValue(TableCell.row(0).column(messageIndex), "This is mock record 1");

        JTextComponentFixture textBox = frameFixture.textBox(ComponentKeys.EventDetailMessage.name());
        table.click(TableCell.row(0).column(0), MouseButton.LEFT_BUTTON);

        frameFixture.label("clear").click();

        assertThat(table.rowCount(), is(0));

        String text = textBox.text();
        int length = text.length();
        assertThat(text.contains("TestApplication"), is(false));
        JLabelFixture label = frameFixture.label(ComponentKeys.StatusText.name());
        assertThat(label.text(), is(startsWith("0")));
        // try
        // {
        // Thread.sleep(100000);
        // }
        // catch (InterruptedException e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }
}
