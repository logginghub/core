package com.logginghub.logging.frontend.charting.swing;

import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.charting.swing.fancypattern.FancyPatternEditorControl;
import com.logginghub.logging.frontend.charting.swing.fancypattern.FancyPatternElement;
import com.logginghub.logging.frontend.charting.swing.fancypattern.FancyPatternModelEditor;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ThreadUtils;
import org.fest.swing.core.BasicComponentFinder;
import org.fest.swing.core.ComponentFinder;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.MouseButton;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.fest.swing.fixture.JTreeFixture;
import org.fest.swing.keystroke.KeyStrokeMap;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@Ignore // jshaw : not working in OS X
public class TestChartingTreeEditorViewIntegration {

    private SwingFrontEndDSL dsl;

    @After public void cleanup() throws IOException {
        dsl.shutdown();
    }

    @Test public void test_full_process() {

        File file = FileUtils.createRandomTestFileForClass(TestChartingTreeEditorViewIntegration.class);

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .chartingConfiguration(file.getAbsolutePath())
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1"))
                                                                                        .toConfiguration();

        dsl = SwingFrontEndDSL.createDSL(configuration);

        dsl.openChartEditor();

        // Find the new dialog window, there must be an easier way...
        ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();
        JDialog dialog = finder.findByName("Charting Editor", JDialog.class);
        DialogFixture dialogFixture = new DialogFixture(dsl.getFrameFixture().robot, dialog);

        // Get hold of the patterns node and bring up the context menu
        JTreeFixture tree = dialogFixture.tree("ChartingTreeEditorView.tree");
        tree.clickPath("Patterns");
        tree.rightClickPath("Patterns");

        // Click the add pattern menu item
        dialogFixture.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override protected boolean isMatching(JMenuItem component) {
                return component.getText().equals("Add pattern");
            }
        }).click();

        // Push an event into the system
        dsl.publishEvent("Operation A completed successfully in 8 ms - user was Bob");

        // Quick check to make sure the pattern editor is resizing properly
        final DialogFixture patternEditorDialog = dsl.getFrameFixture().dialog("Pattern Editor");
        JTextComponentFixture textBox = patternEditorDialog.textBox("Filter");
        assertThat(textBox.target.getHeight(), is(lessThan(30)));

        // Make sure the event has appeared in the editor
        final JTableFixture table = patternEditorDialog.table("SimpleEventMessageTable");
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return table.rowCount() == 1;
            }
        });

        // Select it
        table.click();

        // Click on the variable items in the fancy editor
        JPanelFixture fancyEditor = patternEditorDialog.panel("FancyPatternModelEditor");
        FancyPatternModelEditor editor = (FancyPatternModelEditor) fancyEditor.target;
        FancyPatternEditorControl fancyPatternEditor = editor.getFancyPatternEditor();
        List<FancyPatternElement> elements = fancyPatternEditor.getElements();
        for (FancyPatternElement fancyPatternElement : elements) {
            if (fancyPatternElement.getText().equals("8") || fancyPatternElement.getText().equals("Bob")) {
                Point locationOnScreen = fancyPatternEditor.getLocationOnScreen();
                Rectangle bounds = fancyPatternElement.getBounds();
                Point where = new Point(locationOnScreen.x + bounds.x + 3, locationOnScreen.y + bounds.y + 3);
                patternEditorDialog.robot.click(where, MouseButton.LEFT_BUTTON, 2);
            }
        }

        // Fill in the pattern name and variable names
        fancyEditor.textBox("Pattern name").enterText("pattern1");
        fancyEditor.textBox("Pattern").select("group0").enterText("time");
        fancyEditor.textBox("Pattern").select("group1").enterText("user");

        // Push some more events into the mix
        dsl.publishEvent("Operation A completed successfully in 1 ms - user was Jane");
        dsl.publishEvent("Operation A completed successfully in 2 ms - user was Frank");

        // Make sure we can see them in the extract table
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return patternEditorDialog.table("Pattern extract table").rowCount() == 2;
            }
        });

        // Add the pattern
        patternEditorDialog.button("Add pattern button").click();

        // Make sure the tree has opened up to show the new item
        TreePath selectionPaths = tree.target.getSelectionPath();
        Object[] path = selectionPaths.getPath();
        assertThat(path.length, is(3));
        assertThat(path[0].toString(), is("root"));
        assertThat(path[1].toString(), is("Patterns"));
        assertThat(path[2].toString(), is("[0] pattern1"));

        // Create a new charting page
        tree.clickPath("Charting");
        tree.rightClickPath("Charting");

        dialogFixture.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override protected boolean isMatching(JMenuItem component) {
                return component.getText().equals("Add page");
            }
        }).click();

        // Make sure the tree has opened up to show the new page
        selectionPaths = tree.target.getSelectionPath();
        path = selectionPaths.getPath();
        assertThat(path.length, is(3));
        assertThat(path[0].toString(), is("root"));
        assertThat(path[1].toString(), is("Charting"));
        assertThat(path[2].toString(), is("New page"));

        // Create a new chart on that page
        tree.clickPath("Charting/New page");
        tree.rightClickPath("Charting/New page");

        dialogFixture.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override protected boolean isMatching(JMenuItem component) {
                return component.getText().equals("Add line chart");
            }
        }).click();

        // Make sure the tree has opened up to show the new page
        selectionPaths = tree.target.getSelectionPath();
        path = selectionPaths.getPath();
        assertThat(path.length, is(4));
        assertThat(path[0].toString(), is("root"));
        assertThat(path[1].toString(), is("Charting"));
        assertThat(path[2].toString(), is("New page"));
        assertThat(path[3].toString(), is("Chart title"));

        // Create a new series on the chart
        tree.clickPath("Charting/New page/Chart title");
        tree.rightClickPath("Charting/New page/Chart title");

        dialogFixture.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override protected boolean isMatching(JMenuItem component) {
                return component.getText().equals("Add series");
            }
        }).click();

        // Fill in the series dialog
        // TODO : no idea why the name isn't being set properly
        final DialogFixture seriesEditorDialog = dsl.getFrameFixture().dialog("dialog0");
        // final DialogFixture seriesEditorDialog =
        // dsl.getFrameFixture().dialog("Chart series editor");
        JPanelFixture panel = seriesEditorDialog.panel("ChartSeriesModelEditor");
        panel.comboBox("Pattern Combo").selectItem("pattern1");
        panel.comboBox("Label Combo").selectItem("time");
        panel.comboBox("Type Combo").selectItem("Median");
        seriesEditorDialog.button("Apply").click();

        NewChartingModel model = dsl.getSwingFrontEnd().getMainPanel().getChartingController().getModel();
        ChartSeriesModel chartSeriesModel = model.getPages().get(0).getChartingModels().get(0).getMatcherModels().get(0);
        assertThat(chartSeriesModel.getLabelIndex().get(), is(0));
        assertThat(chartSeriesModel.getPatternID().get(), is(0));
        assertThat(chartSeriesModel.getType().asString(), is("Median"));

        dialogFixture.close();
        dialogFixture.target.dispose();
    }

    @Test public void test_quotes_in_names() {

        File file = FileUtils.createRandomTestFileForClass(TestChartingTreeEditorViewIntegration.class);

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .chartingConfiguration(file.getAbsolutePath())
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1"))
                                                                                        .toConfiguration();

        ComponentFinder finder = BasicComponentFinder.finderWithNewAwtHierarchy();
        dsl = SwingFrontEndDSL.createDSL(configuration);

        dsl.openChartEditor();

        // Find the new dialog window, there must be an easier way...
        JDialog dialog = finder.findByName("Charting Editor", JDialog.class);
        DialogFixture dialogFixture = new DialogFixture(dsl.getFrameFixture().robot, dialog);

        // Get hold of the patterns node and bring up the context menu
        JTreeFixture tree = dialogFixture.tree("ChartingTreeEditorView.tree");
        tree.clickPath("Patterns");
        tree.rightClickPath("Patterns");

        // Click the add pattern menu item
        dialogFixture.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override protected boolean isMatching(JMenuItem component) {
                return component.getText().equals("Add pattern");
            }
        }).click();

        DialogFixture patternEditorDialog = dsl.getFrameFixture().dialog("Pattern Editor");


        // Need to setup english keystroke mappings or "\"" will type @        
        KeyStrokeMap.addKeyStrokesFrom(new FestKeyMappingUK());

        // Fill in the pattern name and variable names
        JPanelFixture fancyEditor = patternEditorDialog.panel("FancyPatternModelEditor");
        fancyEditor.textBox("Pattern name").robot.settings().delayBetweenEvents(1);        
        fancyEditor.textBox("Pattern name").enterText("pattern_with_'_and_\"_characters");
        fancyEditor.textBox("Pattern").enterText("This is pattern with ' and \" characters");
        fancyEditor.textBox("Pattern name").robot.settings().delayBetweenEvents(60);
        
        // Add the pattern and close the dialog
        patternEditorDialog.button("Add pattern button").click();
        
        // Close down the chart editor
        dialogFixture.close();
        
        // Shutdown the frontend and re-open it
        dsl.shutdown();
        
        // Reset the finder to search the new instance only
        finder = BasicComponentFinder.finderWithNewAwtHierarchy();
        
        dsl = SwingFrontEndDSL.createDSL(configuration);
        dsl.openChartEditor();

        // Find the new dialog window, there must be an easier way...
        dialog = finder.findByName("Charting Editor", JDialog.class);
        dialogFixture = new DialogFixture(dsl.getFrameFixture().robot, dialog);

        // Get hold of the patterns node and bring up the context menu
        tree = dialogFixture.tree("ChartingTreeEditorView.tree");
        tree.clickPath("Patterns");

        // Double click the existing pattern
        // jshaw - hack to make the test pass on mac
        dsl.sleep(1000);
        tree.doubleClickRow(1);
        dsl.sleep(1000);
        tree.doubleClickRow(1);

        patternEditorDialog = dsl.getFrameFixture().dialog("Pattern Editor");

        // Click on the variable items in the fancy editor
        fancyEditor = patternEditorDialog.panel("FancyPatternModelEditor");

        // Make sure the pattern has the correct values
        assertThat(fancyEditor.textBox("Pattern name").target.getText(), is("pattern_with_'_and_\"_characters"));
        assertThat(fancyEditor.textBox("Pattern").target.getText(), is("This is pattern with ' and \" characters"));
        
        patternEditorDialog.close();
        dialogFixture.close();
    }
    
}
