package com.logginghub.logging.frontend.charting.swing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.swing.JFrame;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTreeFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.LineChartModel;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.charting.model.PageModel;
import com.logginghub.logging.frontend.charting.swing.ChartingTreeEditorView;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.SystemTimeProvider;

public class TestChartingTreeEditorView {

    private FrameFixture frameFixture;
    private ChartSeriesModel chartSeriesModel;
    private NewChartingModel chartingModel;
    private SystemTimeProvider timeProvider;
    private NewChartingController controller;
    private ChartingTreeEditorView view;
    private JFrame frame;

    @BeforeClass public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @After public void teardown() {
        frameFixture.cleanUp();
        frame.dispose();
    }

    @Before public void setup() throws Exception {

        view = GuiActionRunner.execute(new GuiQuery<ChartingTreeEditorView>() {
            @Override protected ChartingTreeEditorView executeInEDT() throws Throwable {
                ChartingTreeEditorView editor = new ChartingTreeEditorView();
                return editor;
            }
        });

        frame = GuiActionRunner.execute(new GuiQuery<JFrame>() {
            @Override protected JFrame executeInEDT() throws Throwable {
                JFrame frame = new JFrame();
                frame.getContentPane().add(view);
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

    @Test public void test_pattern_name_resolution() {

        chartSeriesModel.getPatternID().set(8);
        chartSeriesModel.getLabelIndex().set(1);
        chartSeriesModel.getGroupBy().set("{three}");
        chartSeriesModel.getType().set("Count");

        // Build the objects required for the chart
        PageModel pageModel = new PageModel();
        pageModel.getName().set("Page1");

        LineChartModel lineChartModel = new LineChartModel();
        lineChartModel.getMatcherModels().add(chartSeriesModel);

        pageModel.getChartingModels().add(lineChartModel);

        chartingModel.getPages().add(pageModel);

        GuiActionRunner.execute(new GuiTask() {
            @Override protected void executeInEDT() {
                view.bind(controller);
            }
        });

        final JTreeFixture tree = frameFixture.tree("ChartingTreeEditorView.tree");

        GuiActionRunner.execute(new GuiTask() {
            @Override protected void executeInEDT() {
                tree.target.expandRow(5);
            }
        });
        
        assertThat(tree.node(6).value(), is("patternModel2/pattern/Count"));

    }
}
