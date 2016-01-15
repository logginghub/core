package com.logginghub.integrationtests.loggingfrontend.helpers;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.frontend.ConfigurationProxy;
import com.logginghub.logging.frontend.ConnectionPanel;
import com.logginghub.logging.frontend.LoggingMainPanel;
import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.logging.frontend.components.LevelsCheckboxListView;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTablePanel;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.swing.Utils;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.MouseButton;
import org.fest.swing.core.MouseClickInfo;
import org.fest.swing.data.TableCell;
import org.fest.swing.driver.BasicJTableCellReader;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.exception.ActionFailedException;
import org.fest.swing.exception.WaitTimedOutError;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JCheckBoxFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JLabelFixture;
import org.fest.swing.fixture.JMenuItemFixture;
import org.fest.swing.fixture.JOptionPaneFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JRadioButtonFixture;
import org.fest.swing.fixture.JTabbedPaneFixture;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.fixture.JTextComponentFixture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class SwingFrontEndDSL {

    private static final String LOG_EVENT_PANEL_DEFAULT = "logEventPanel-default";
    private static final String LOG_VIEW = "Log view";
    private long quickFilterUpdateTime = 500;
    private SwingFrontEnd swingFrontEnd;
    private FrameFixture frameFixture;
    private JTextComponentFixture target;
    private LoggingFrontendConfiguration configuration;

    private SwingFrontEndDSL() {

    }

    public SwingFrontEndDSL(SwingFrontEnd swingFrontEnd, FrameFixture frameFixture) {
        this.swingFrontEnd = swingFrontEnd;
        this.frameFixture = frameFixture;
    }

    public static SwingFrontEndDSL createDSL(final LoggingFrontendConfiguration configuration) {
        SwingFrontEndDSL dsl = new SwingFrontEndDSL();
        dsl.reconfigure(configuration);
        return dsl;
    }

    public void reconfigure(final LoggingFrontendConfiguration configuration) {
        this.configuration = configuration;
        if (frameFixture != null) {
            frameFixture.cleanUp();
        }

        swingFrontEnd = GuiActionRunner.execute(new GuiQuery<SwingFrontEnd>() {
            @Override
            protected SwingFrontEnd executeInEDT() throws Throwable {
                Metadata settings = new Metadata();

                settings.put("frame.x", 10);
                settings.put("frame.y", 10);
                settings.put("frame.width", 1024);
                settings.put("frame.height", 768);

                settings.put("charting.frame.x", 10 + 1024);
                settings.put("charting.frame.y", 10);
                settings.put("charting.frame.width", 1024);
                settings.put("charting.frame.height", 768);

                ConfigurationProxy proxy = new ConfigurationProxy(settings, "propertiesName", configuration, "parsers.xml");
                SwingFrontEnd swingFrontEnd = SwingFrontEnd.mainInternal(proxy);

                // jshaw - there is bug on osx that means fest thinks all the buttons are off-screen in a multiple monitor setup, so force the
                // window to be whereever 0,0 happens to be...
                if (OSUtils.isMac()) {
                    swingFrontEnd.setLocation(0, 0);
                }

                return swingFrontEnd;
            }
        });

        frameFixture = new FrameFixture(swingFrontEnd);
    }

    public static SwingFrontEndDSL createDSLWithRealMain(LoggingFrontendConfiguration configuration) {
        SwingFrontEndDSL dsl = new SwingFrontEndDSL();
        dsl.reconfigureWithRealMain(configuration);
        return dsl;
    }

    public void reconfigureWithRealMain(final LoggingFrontendConfiguration configuration) {
        this.configuration = configuration;
        if (frameFixture != null) {
            frameFixture.cleanUp();
        }

        swingFrontEnd = GuiActionRunner.execute(new GuiQuery<SwingFrontEnd>() {
            @Override
            protected SwingFrontEnd executeInEDT() throws Throwable {
                String[] args = new String[]{};
                SwingFrontEnd swingFrontEnd = SwingFrontEnd.mainInternal(args);

                return swingFrontEnd;
            }
        });

        frameFixture = new FrameFixture(swingFrontEnd);
    }

    public void addQuickFilter(String environment) {
        ensureSelected(environment);
        frameFixture.panel(LOG_EVENT_PANEL_DEFAULT).label("addQuickFilter").click();
    }

    private void ensureSelected(String tabName) {
        JTabbedPane target = frameFixture.tabbedPane("mainTabbedPane").target;
        if (!target.getTitleAt(target.getSelectedIndex()).equals(tabName)) {
            selectTab(tabName);
        }
    }

    public void selectTab(String environment) {
        if (configuration.getEnvironments().size() > 1) {
            frameFixture.tabbedPane("mainTabbedPane").selectTab(environment);
        } else {
            frameFixture.tabbedPane("mainTabbedPane").selectTab(LOG_VIEW);
        }
    }

    public void altClickTableRow(int row, int column) {
        JTableFixture table = getTable(LOG_VIEW);
        table.pressKey(KeyEvent.VK_ALT);
        table.click(TableCell.row(row).column(column), MouseClickInfo.leftButton());
        table.releaseKey(KeyEvent.VK_ALT);
    }

    public JTableFixture getTable(String tab) {
        ensureSelected(tab);
        JTableFixture tableFixture = frameFixture.panel(getDetailPanelName(tab)).table("logEventTable");
        return tableFixture;
    }

    private String getDetailPanelName(String tabname) {
        if (tabname.equals(LOG_VIEW)) {
            return LOG_EVENT_PANEL_DEFAULT;
        } else {
            return LoggingMainPanel.LOG_EVENT_PANEL + tabname;
        }
    }

    public void assertAutoLocking(String environment, boolean state) {
        FrameFixture frame = getFrameFixture();
        ensureSelected(environment);
        JMenuItemFixture menuItem = frame.menuItemWithPath("Sources", "Auto-lock warnings");
        assertThat(menuItem.target.isSelected(), is(state));
    }

    public FrameFixture getFrameFixture() {
        return frameFixture;
    }

    public void assertColumnHidden(String column) {
        assertColumnHidden(LOG_VIEW, column);
    }

    public void assertColumnHidden(String environment, String column) {
        JTableFixture table = getTable(environment);
        try {
            int index = table.columnIndexFor(column);
            throw new FormattedRuntimeException("Looks like column {} was found at index {}", column, index);
        } catch (ActionFailedException e) {
            assertThat(e.getMessage(), startsWith("Unable to find a column with id"));
        }

    }

    public void assertColumnIndex(String column, int index) {
        assertColumnIndex(LOG_VIEW, column, index);
    }

    public void assertColumnIndex(String environment, String column, int index) {
        JTableFixture table = getTable(environment);
        int foundIndex = table.columnIndexFor(column);
        assertThat("Column index was wrong", foundIndex, is(index));

    }

    public void assertColumnVisible(String column) {
        assertColumnVisible(LOG_VIEW, column);
    }

    public void assertColumnVisible(String environment, String column) {
        JTableFixture table = getTable(environment);
        table.columnIndexFor(column);
    }

    public void assertConnected(String host, int port) {
        FrameFixture frame = getFrameFixture();
        frame.tabbedPane("mainTabbedPane").selectTab("Configuration");
        JPanelFixture panel = frame.panel("ConfigurationPanel");
        final JLabelFixture label = panel.label(host + ":" + port);

        ThreadUtils.untilTrue(10, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return label.target.getIcon() == ConnectionPanel.green;
            }
        });
        assertThat(label.target.getIcon(), is((Icon) ConnectionPanel.green));
    }

    public void assertLogEventInTable(String environment, int row, LogEvent logEvent) {
        JTableFixture table = getTable(environment);

        int messageIndex = table.columnIndexFor("Message");
        int sourceAppIndex = table.columnIndexFor("Source");

        TableCell sourceColumn = TableCell.row(row).column(sourceAppIndex);
        TableCell messageColumn = TableCell.row(row).column(messageIndex);

        BasicJTableCellReader reader = new BasicJTableCellReader();
        String message = reader.valueAt(table.target, row, messageIndex);
        String sourceApp = reader.valueAt(table.target, row, sourceAppIndex);

        table.requireCellValue(sourceColumn, logEvent.getSourceApplication());
        table.requireCellValue(messageColumn, logEvent.getMessage());

        assertThat(sourceApp, is(logEvent.getSourceApplication()));
        assertThat(message, is(logEvent.getMessage()));
    }

    public void assertTableCell(String environment, int row, int column, String value) {
        JTableFixture table = getTable(environment);
        TableCell sourceColumn = TableCell.row(row).column(column);
        table.requireCellValue(sourceColumn, value);
    }

    public void assertTableCell(int row, int column, String value) {
        assertTableCell(LOG_VIEW, row, column, value);
    }


    public void assertLogEventInTable(int row, LogEvent logEvent) {
        assertLogEventInTable(LOG_VIEW, row, logEvent);
    }

    public void assertLogEventTable(LogEvent... events) {
        assertLogEventTableSize(events.length);
        for (int i = 0; i < events.length; i++) {
            LogEvent logEvent = events[i];
            assertLogEventInTable(i, logEvent);
        }
    }

    public void assertLogEventTableSize(int i) {
        assertLogEventTableSize(LOG_VIEW, i);
    }

    public void assertLogEventTableSize(String environment, int i) {
        JTableFixture table = getTable(environment);
        table.requireRowCount(i);
        table.rowCount();
    }

    public void assertMenuOption(String tab, String menu, String option, boolean enabled, boolean selected) {
        FrameFixture frame = getFrameFixture();
        ensureSelected(tab);
        JMenuItemFixture menuItem = frame.menuItemWithPath(menu, option);
        boolean isEnabled = menuItem.target.isEnabled();
        boolean isSelected = menuItem.target.isSelected();

        assertThat(isEnabled, is(enabled));
        assertThat(isSelected, is(selected));
    }

    public void assertQuickFilterPopupItem(int row, String filterText) {
        DialogFixture dialog = frameFixture.dialog("quickFilterPopupDialog");
        JTableFixture table = dialog.panel("quickFilterPopupPanel").table("quickFilterHistoryTable");
        BasicJTableCellReader reader = new BasicJTableCellReader();
        assertThat(reader.valueAt(table.target, row, 0), is(filterText));
    }

    public void assertQuickFilterPopupItemCount(int count) {
        DialogFixture dialog = frameFixture.dialog("quickFilterPopupDialog");
        JTableFixture table = dialog.panel("quickFilterPopupPanel").table("quickFilterHistoryTable");
        assertThat(table.rowCount(), is(count));
    }

    public void assertQuickFilterPopupWindowVisible(boolean isVisible) {
        try {
            DialogFixture dialog = frameFixture.dialog("quickFilterPopupDialog");
            assertThat(dialog.panel("quickFilterPopupPanel").target.isVisible(), is(isVisible));
        } catch (WaitTimedOutError e) {
            assertThat(false, is(isVisible));
        }
    }

    public void assertWriteOutputLog(String environment, boolean state) {
        FrameFixture frame = getFrameFixture();
        ensureSelected(environment);
        JMenuItemFixture menuItem = frame.menuItemWithPath("Sources", "Write output log");
        boolean selected = menuItem.target.isSelected();
        assertThat(selected, is(state));
    }

    public void backspace() {
        target.pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_BACK_SPACE));
    }

    public void changeLevelQuickFilter(Level level) {
        changeLevelQuickFilter(LOG_VIEW, level);
    }

    private void changeLevelQuickFilter(String tabName, Level level) {
        frameFixture.tabbedPane("mainTabbedPane").selectTab(tabName);
        JPanelFixture fancyThing = frameFixture.panel("quickLevelFilterCombo");
        LevelsCheckboxListView view = (LevelsCheckboxListView) fancyThing.target;
        view.selectLevel(level);
        // view.getModel().getSelectedLevel().set(level);
    }

    public void clearEvents() {
        ensureSelected(LOG_VIEW);
        JPanelFixture panel = frameFixture.panel(LOG_EVENT_PANEL_DEFAULT);
        Out.out(panel);
        Utils.dumpContainer(panel.target, 0);
        JLabelFixture clear = panel.label("clear");
        clear.click();
    }

    public void clearEvents(String tabname) {
        frameFixture.tabbedPane("mainTabbedPane").selectTab(tabname);
        String name = getDetailPanelName(tabname);
        frameFixture.panel(name).label("clear").click();
    }

    public void clearTimeRange(String environment) {
        ensureSelected(environment);
        JPanelFixture panelFixture = frameFixture.panel(getDetailPanelName(environment)).panel("TimeView");

        panelFixture.robot.click(panelFixture.target, new Point(10, 10), MouseButton.RIGHT_BUTTON, 1);
    }

    public void clickTableRow(String tab, int row, int column) {
        JTableFixture table = getTable(tab);
        table.click(TableCell.row(row).column(column), MouseClickInfo.leftButton());
    }

    public void clickTableRow(int row, int column) {
        clickTableRow(LOG_VIEW, row, column);
    }

    public void controlClickTableRow(int row, int column) {
        ensureSelected(LOG_VIEW);
        JTableFixture table = frameFixture.panel(LOG_EVENT_PANEL_DEFAULT).table("logEventTable");
        table.pressKey(KeyEvent.VK_CONTROL);
        table.click(TableCell.row(row).column(column), MouseClickInfo.leftButton());
        table.releaseKey(KeyEvent.VK_CONTROL);
    }

    public void debugSleep() {
        ThreadUtils.sleep(30000);
    }

    public void debugSleep(int i) {
        ThreadUtils.sleep(i);
    }

    public void debugWait() {
        ThreadUtils.sleep(300000);
    }

    public void downArrow() {
        target.pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_DOWN));
    }

    public void dragTimeRange(String environment, int fromX, int fromY, int toX, int toY) {
        ensureSelected(environment);
        JPanelFixture panelFixture = frameFixture.panel(getDetailPanelName(environment)).panel("TimeView");

        panelFixture.robot.pressMouse(panelFixture.target, new Point(fromX, fromY), MouseButton.LEFT_BUTTON);
        panelFixture.robot.moveMouse(panelFixture.target, new Point(toX, toY));
        panelFixture.robot.releaseMouse(MouseButton.LEFT_BUTTON);
    }

    public void enableQuickFilter(String environment, int filterIndex, boolean enabled) {
        selectTab(environment);

        JCheckBoxFixture checkBox = frameFixture.panel(getDetailPanelName(environment))
                                                .panel("quickFilter-" + filterIndex)
                                                .checkBox("enabledCheckbox");

        if (checkBox.target.isSelected() != enabled) {
            checkBox.click();
        }
    }

    public void ensureLevelFilter(String environment, Level level) {
        frameFixture.tabbedPane("mainTabbedPane").selectTab(environment);
        JPanelFixture fancyThing = frameFixture.panel("quickLevelFilterCombo");
        LevelsCheckboxListView view = (LevelsCheckboxListView) fancyThing.target;
        assertThat(view.getSelectedLevel(), is(level));
    }

    public void ensureTabSelected(String tabname) {
        JTabbedPaneFixture tabbedPane = frameFixture.tabbedPane("mainTabbedPane");
        int selectedIndex = tabbedPane.target.getSelectedIndex();
        String titleAt = tabbedPane.target.getTitleAt(selectedIndex);
        assertThat(titleAt, is(tabname));
    }

    public void enter() {
        target.pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_ENTER));
    }

    public void enterText(String string) {
        target.setText(string);
    }

    public void escape() {
        target.pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_ESCAPE));
    }

    public void focusOnQuickFilter() {
        focusOnQuickFilter("default");
    }

    public void focusOnQuickFilter(String environment) {
        if (configuration.getEnvironments().size() > 1) {
            frameFixture.tabbedPane("mainTabbedPane").selectTab(environment);
        } else {
            frameFixture.tabbedPane("mainTabbedPane").selectTab(LOG_VIEW);
        }
        target = frameFixture.panel(getDetailPanelName(environment)).textBox("quickFilterTestField");
        target.focus();
    }

    public int getLogEventTableSize() {
        ensureSelected(LOG_VIEW);
        JTableFixture table = frameFixture.panel(LOG_EVENT_PANEL_DEFAULT).table("logEventTable");
        return table.rowCount();
    }

    public SwingFrontEnd getSwingFrontEnd() {
        return swingFrontEnd;
    }

    public JTextComponentFixture getTarget() {
        return target;
    }

    public void lockRow(int row) {
        clickTableRow(row, 1);
        rightClickTableRow(row, 1);
    }

    public void openChartEditor() {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Edit", "Charting editor...");
        menuItemWithPath.click();
    }

    public void openRepoSearch() {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Edit", "Repository Search");
        menuItemWithPath.click();
    }

    public void pause() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(null, "Paused");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void pressDelete() {
        pressDelete(LOG_VIEW);
    }

    public void pressDelete(String tab) {
        ensureSelected(tab);
        JTableFixture table = frameFixture.panel(getDetailPanelName(tab)).table("logEventTable");
        table.pressKey(KeyEvent.VK_DELETE);
        table.releaseKey(KeyEvent.VK_DELETE);
    }

    public void pressModifiedKeys(int vkShift, int vkHome) {
        target.pressKey(vkShift);
        target.pressKey(vkHome);
        target.releaseKey(vkHome);
        target.releaseKey(vkShift);
    }

    public DefaultLogEvent publishEvent(int level, String message) {
        DefaultLogEvent event = LogEventBuilder.start().setMessage(message).setLevel(level).toLogEvent();
        LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();

        // Fire it directly into the first environment's event multiplexer
        mainPanel.getModel().getEnvironments().get(0).onNewLogEvent(event);
        return event;
    }

    public void publishEvent(String environment, LogEvent event) {
        LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();
        mainPanel.getModel().getEnvironment(environment).onNewLogEvent(event);
    }

    public DefaultLogEvent publishEvent(String environment, int level, String message) {
        DefaultLogEvent event = createEvent(environment, level, message);
        LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();
        mainPanel.getModel().getEnvironment(environment).onNewLogEvent(event);
        return event;
    }

    public static DefaultLogEvent createEvent(String environment, int level, String message) {
        DefaultLogEvent event = LogEventBuilder.start().setMessage(message).setLevel(level).toLogEvent();
        return event;
    }

    public void publishEvent(String environment, long time, String message) {
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        event.setMessage(message);
        event.setOriginTime(time);
        LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();
        mainPanel.getModel().getEnvironment(environment).onNewLogEvent(event);
    }

    public void publishEvent(String environment, String message) {
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        event.setMessage(message);
        LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();
        mainPanel.getModel().getEnvironment(environment).onNewLogEvent(event);
    }

    public DefaultLogEvent publishEvent(String message) {
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        event.setMessage(message);
        publishEvent(event);
        return event;
    }

    public void publishEvent(LogEvent... events) {
        LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();

        // Fire it directly into the first environment's event multiplexer
        for (LogEvent logEvent : events) {
            mainPanel.getModel().getEnvironments().get(0).onNewLogEvent(logEvent);
        }
    }

    public void publishEventAndWaitForBatch(LogEvent... events) {
        publishEvent(events);
        waitForBatch();
    }

    public void waitForBatch() {
        waitForBatch("default");
    }

    public void waitForBatch(String environment) {
        final DetailedLogEventTablePanel detailPanel = getDetailedLogEventTablePanel(environment);
        ThreadUtils.untilTrue("Waiting for the current logging batch to be posted", 1, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return detailPanel.getCurrentBatchSize() == 0;
            }
        });
    }

    public DetailedLogEventTablePanel getDetailedLogEventTablePanel(String environment) {
        final DetailedLogEventTablePanel detailPanel = swingFrontEnd.getMainPanel().getDetailedLogEventTablePanelForEnvironment(environment);
        return detailPanel;
    }

    public void publishEventAndWaitForBatch(String environment, LogEvent... events) {
        publishEvent(environment, events);
        waitForBatch(environment);
    }

    public void publishEvent(String environment, LogEvent... events) {
        LoggingMainPanel mainPanel = swingFrontEnd.getMainPanel();
        for (LogEvent logEvent : events) {
            mainPanel.getModel().getEnvironment(environment).onNewLogEvent(logEvent);
        }
    }

    public void removeQuickFilter(String environment, int filterIndex) {
        selectTab(environment);
        JLabelFixture removeLabel = frameFixture.panel(getDetailPanelName(environment)).label("quickFilter-" + filterIndex + ".remove");
        removeLabel.click();
    }

    public void rightClickTableRow(int row, int column) {
        ensureSelected(LOG_VIEW);
        JTableFixture table = frameFixture.panel(LOG_EVENT_PANEL_DEFAULT).table("logEventTable");
        table.pressKey(KeyEvent.VK_CONTROL);
        table.click(TableCell.row(row).column(column), MouseClickInfo.rightButton());
        table.releaseKey(KeyEvent.VK_CONTROL);
    }

    public void selectRow(String env, int row) {
        selectTab(env);
        getTable().selectRows(row);
    }

    public JTableFixture getTable() {
        return getTable(LOG_VIEW);
    }

    public void selectRow(int row) {
        getTable().selectRows(row);
    }

    public void sendEventToHub(SocketHub hub, LogEvent logEvent) {
        LogEventBucket senderBucket = new LogEventBucket();
        LogEventBucket receiverBucket = new LogEventBucket();

        SocketClient sender = null;
        SocketClient receiver = null;

        try {
            sender = SocketClient.connect(new InetSocketAddress(hub.getPort()), false, senderBucket);
            receiver = SocketClient.connect(new InetSocketAddress(hub.getPort()), true, receiverBucket);

            sender.send(new LogEventMessage(logEvent));
            receiverBucket.waitForMessages(1);
        } catch (ConnectorException e) {
            throw new RuntimeException(String.format("Failed to connect to hub"), e);
        } catch (LoggingMessageSenderException e) {
            throw new RuntimeException(String.format("Failed to send message to hub"), e);
        } finally {
            FileUtils.closeQuietly(sender, receiver);
        }

    }

    public void setAutoLockWarning(boolean on) {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Sources", "Auto-lock warnings");
        toggle(on, menuItemWithPath);
    }

    private void toggle(boolean on, JMenuItemFixture menuItemWithPath) {
        if (menuItemWithPath.target.isSelected()) {
            if (on) {
                // All good, already on
            } else {
                menuItemWithPath.click();
            }
        } else {
            if (on) {
                menuItemWithPath.click();
            } else {
                // All good, already off
            }
        }
    }

    public void setAutoScroll(boolean on) {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Sources", "Auto-scroll");
        toggle(on, menuItemWithPath);
    }

    public void setDetailViewOrientation(boolean horizonal) {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Edit", "Horizontal detail view");
        menuItemWithPath.click();
    }

    public void setDummySource(boolean on) {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Sources", "View dummy source");

        toggle(on, menuItemWithPath);
    }

    public void setHubFiltering(boolean on) {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Sources", "Hub filtering");

        toggle(on, menuItemWithPath);
    }

    public void setLevelFilter(Level level) {
        changeLevelQuickFilter(LOG_VIEW, level);
    }

    public void setLevelFilter(String tabName, Level level) {
        changeLevelQuickFilter(tabName, level);
    }

    public void setQuickFilter(String environment, String filter, int filterIndex) {
        selectTab(environment);

        target = frameFixture.panel(getDetailPanelName(environment)).panel("quickFilter-" + filterIndex).textBox("quickFilterTestField");
        target.setText("");

        target.robot.settings().delayBetweenEvents(1);
        target.enterText(filter);
    }

    public void setQuickFilter(String environment, String filter) {
        if (configuration.getEnvironments().size() > 1) {
            frameFixture.tabbedPane("mainTabbedPane").selectTab(environment);
        } else {
            frameFixture.tabbedPane("mainTabbedPane").selectTab(LOG_VIEW);
        }
        target = frameFixture.panel(getDetailPanelName(environment)).textBox("quickFilterTestField");
        target.setText("");

        target.robot.settings().delayBetweenEvents(1);

        if (filter.isEmpty()) {
            target.pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_BACK_SPACE));
        } else {
            target.enterText(filter);
        }
    }

    public void setQuickFilterRegex(String environment, int filterIndex, boolean value) {
        selectTab(environment);

        JRadioButtonFixture radioButton = frameFixture.panel(getDetailPanelName(environment))
                                                      .panel("quickFilter-" + filterIndex)
                                                      .radioButton("regexRadioButton");
        if (value) {
            radioButton.check();
        } else {
            radioButton.uncheck();
        }
    }

    public void setWriteOutputLog(boolean on) {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Sources", "Write output log");
        toggle(on, menuItemWithPath);
    }

    public void shutdown() {
        swingFrontEnd.close();
        getFrameFixture().cleanUp();
    }

    public void slow() {
        ThreadUtils.sleep(1000);
    }

    public void switchOutputLog(String string) {
        FrameFixture frame = getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Sources", LoggingMainPanel.CHANGE_OUTPUT_LOG_DESTINATION);
        menuItemWithPath.click();
        JComboBoxFixture combo = frame.comboBox(LoggingMainPanel.FILENAME_COMBO);
        combo.enterText(string);

        JOptionPaneFixture optionPane = frame.optionPane();
        optionPane.okButton().click();

    }

    public void waitForQuickFilter() {
        sleep(quickFilterUpdateTime);
        waitForSwingQueueToFlush();
    }

    public void sleep(long time) {
        ThreadUtils.sleep(time);
    }

    public void waitForSwingQueueToFlush() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    // System.out.println("Queue flushed to this point");
                }
            });
        } catch (InterruptedException e) {
        } catch (InvocationTargetException e) {
        }
    }


}
