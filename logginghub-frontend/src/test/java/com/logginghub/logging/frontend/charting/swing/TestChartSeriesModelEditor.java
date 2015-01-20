package com.logginghub.logging.frontend.charting.swing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import javax.swing.JFrame;

import org.fest.swing.data.TableCell;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JTableFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.charting.swing.ChartSeriesModelEditor;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.SystemTimeProvider;

public class TestChartSeriesModelEditor {

    private FrameFixture frameFixture;
    private ChartSeriesModel chartSeriesModel;
    private NewChartingModel chartingModel;
    private SystemTimeProvider timeProvider;
    private NewChartingController controller;
    private ChartSeriesModelEditor editor;
    private JFrame frame;

    @BeforeClass public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @After public void teardown() {
        frameFixture.cleanUp();
        frame.dispose();
    }

    @Before public void setup() throws Exception {

        editor = GuiActionRunner.execute(new GuiQuery<ChartSeriesModelEditor>() {
            @Override protected ChartSeriesModelEditor executeInEDT() throws Throwable {
                ChartSeriesModelEditor editor = new ChartSeriesModelEditor();
                return editor;
            }
        });

        frame = GuiActionRunner.execute(new GuiQuery<JFrame>() {
            @Override protected JFrame executeInEDT() throws Throwable {
                JFrame frame = new JFrame();
                frame.getContentPane().add(editor);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(1024, 768);
                frame.setVisible(true);
                return frame;
            }
        });

        frameFixture = new FrameFixture(frame);

        chartSeriesModel = new ChartSeriesModel();
        chartingModel = new NewChartingModel();
        timeProvider = new SystemTimeProvider();
        controller = new NewChartingController(chartingModel, timeProvider);

        PatternModel patternModel1 = new PatternModel();
        patternModel1.getPatternID().set(5);
        patternModel1.getPattern().set("This is {a} pattern [with] some {labels}");
        patternModel1.getName().set("patternModel1");

        chartingModel.getPatternModels().add(patternModel1);

        PatternModel patternModel2 = new PatternModel();
        patternModel2.getPatternID().set(8);
        patternModel2.getPattern().set("Another [totally] different {pattern} with [three] labels");
        patternModel2.getName().set("patternModel2");

        chartingModel.getPatternModels().add(patternModel2);
    }

    @Test public void test_default_values_selected() throws InterruptedException {

        GuiActionRunner.execute(new GuiTask() {
            @Override protected void executeInEDT()  {
                editor.bind(controller, chartSeriesModel, chartingModel);
            }
        });

        JComboBoxFixture patternComboBox = frameFixture.comboBox("Pattern Combo");
        JComboBoxFixture labelComboBox = frameFixture.comboBox("Label Combo");
        JComboBoxFixture typeComboBox = frameFixture.comboBox("Type Combo");
        JComboBoxFixture groupByComboBox = frameFixture.comboBox("Group by");

        patternComboBox.requireSelection("patternModel1");
        labelComboBox.requireSelection("a");
        typeComboBox.requireSelection("Mean");
        groupByComboBox.requireSelection("");

        assertThat(chartSeriesModel.getPatternID().get(), is(5));
        assertThat(chartSeriesModel.getLabelIndex().get(), is(0));
        assertThat(chartSeriesModel.getGroupBy().get(), is(nullValue()));
        assertThat(chartSeriesModel.getType().get(), is("Mean"));
    }

    @Test public void test_selected_defaults_from_model() throws InterruptedException {

        chartSeriesModel.getPatternID().set(8);
        chartSeriesModel.getLabelIndex().set(1);
        chartSeriesModel.getGroupBy().set("{three}");
        chartSeriesModel.getType().set("Count");

        GuiActionRunner.execute(new GuiTask() {
            @Override protected void executeInEDT()  {
                editor.bind(controller, chartSeriesModel, chartingModel);
            }
        });

        JComboBoxFixture patternComboBox = frameFixture.comboBox("Pattern Combo");
        JComboBoxFixture labelComboBox = frameFixture.comboBox("Label Combo");
        JComboBoxFixture typeComboBox = frameFixture.comboBox("Type Combo");
        JComboBoxFixture groupByComboBox = frameFixture.comboBox("Group by");

        patternComboBox.requireSelection("patternModel2");
        labelComboBox.requireSelection("pattern");
        typeComboBox.requireSelection("Count");
        groupByComboBox.requireSelection("{three}");
    }

    @Test public void test_change_values() throws InterruptedException {

        chartSeriesModel.getPatternID().set(8);
        chartSeriesModel.getLabelIndex().set(1);
        chartSeriesModel.getGroupBy().set("{three}");
        chartSeriesModel.getType().set("Count");

        GuiActionRunner.execute(new GuiTask() {
            @Override protected void executeInEDT()  {
                editor.bind(controller, chartSeriesModel, chartingModel);
            }
        });

        JComboBoxFixture patternComboBox = frameFixture.comboBox("Pattern Combo");
        JComboBoxFixture labelComboBox = frameFixture.comboBox("Label Combo");
        JComboBoxFixture typeComboBox = frameFixture.comboBox("Type Combo");
        JComboBoxFixture groupByComboBox = frameFixture.comboBox("Group by");

        patternComboBox.selectItem("patternModel1");

        patternComboBox.requireSelection("patternModel1");
        labelComboBox.requireSelection("a");
        typeComboBox.requireSelection("Count");
        groupByComboBox.requireSelection("");

        assertThat(chartSeriesModel.getPatternID().get(), is(5));
        assertThat(chartSeriesModel.getLabelIndex().get(), is(0));
        assertThat(chartSeriesModel.getGroupBy().get(), is(""));
        assertThat(chartSeriesModel.getType().get(), is("Count"));
    }

    @Test public void test_white_black_lists() throws InterruptedException {

        chartSeriesModel.getPatternID().set(8);
        chartSeriesModel.getLabelIndex().set(1);
        chartSeriesModel.getGroupBy().set("{three}");
        chartSeriesModel.getType().set("Count");

        GuiActionRunner.execute(new GuiTask() {
            @Override protected void executeInEDT()  {
                editor.bind(controller, chartSeriesModel, chartingModel);
            }
        });

        JComboBoxFixture patternComboBox = frameFixture.comboBox("Pattern Combo");
        JComboBoxFixture labelComboBox = frameFixture.comboBox("Label Combo");
        JComboBoxFixture typeComboBox = frameFixture.comboBox("Type Combo");
        JComboBoxFixture groupByComboBox = frameFixture.comboBox("Group by");

        JButtonFixture addFilterButton = frameFixture.button("Add filter");

        JTableFixture filterTable = frameFixture.table("Filter table");

        filterTable.robot.settings().delayBetweenEvents(20);

        addFilterButton.click();

        filterTable.requireRowCount(1);

        filterTable.requireCellValue(TableCell.row(0).column(0), "true");
        filterTable.requireCellValue(TableCell.row(0).column(1), "totally");
        filterTable.requireCellValue(TableCell.row(0).column(2), "");
        filterTable.requireCellValue(TableCell.row(0).column(3), "");

        filterTable.enterValue(TableCell.row(0).column(2), "a,b,c");
        filterTable.enterValue(TableCell.row(0).column(3), "d,e,f");

        assertThat(chartSeriesModel.getFilters().get(0).getVariableIndex().get(), is(0));
        assertThat(chartSeriesModel.getFilters().get(0).getBlacklist().get(), is("d,e,f"));
        assertThat(chartSeriesModel.getFilters().get(0).getWhitelist().get(), is("a,b,c"));

        addFilterButton.click();

        filterTable.requireRowCount(2);

        filterTable.requireCellValue(TableCell.row(1).column(0), "true");
        filterTable.requireCellValue(TableCell.row(1).column(1), "totally");
        filterTable.requireCellValue(TableCell.row(1).column(2), "");
        filterTable.requireCellValue(TableCell.row(1).column(3), "");

        filterTable.enterValue(TableCell.row(1).column(1), "three");
        filterTable.enterValue(TableCell.row(1).column(2), "g,h,i");
        filterTable.enterValue(TableCell.row(1).column(3), "j,k,l");

        assertThat(chartSeriesModel.getFilters().get(1).getVariableIndex().get(), is(2));
        assertThat(chartSeriesModel.getFilters().get(1).getBlacklist().get(), is("j,k,l"));
        assertThat(chartSeriesModel.getFilters().get(1).getWhitelist().get(), is("g,h,i"));

    }


}
