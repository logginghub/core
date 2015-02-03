package com.logginghub.logging.frontend.views.logeventdetail.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.swing.JFrame;

import org.fest.swing.core.Robot;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.GenericComponentFixture;
import org.fest.swing.fixture.JLabelFixture;
import org.junit.Test;

import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.swing.TestFrame;

public class TestTimeView {

    private TimeView timeView;
    private FrameFixture frameFixture;
    private TimeModel model = new TimeModel();
    private LogEventContainerController eventController = new LogEventContainerController();
    private TimeController timeFilterController = new TimeController(model, eventController);

    class TimeViewFixture extends GenericComponentFixture<TimeViewComponent> {
        public TimeViewFixture(Robot robot, TimeViewComponent target) {
            super(robot, target);
        }
    }

    @Test public void test_one_step() {

        Logger.setLevel(TimeViewComponent.class, Logger.fine);
        
        final JFrame frame = GuiActionRunner.execute(new GuiQuery<JFrame>() {
            @Override protected JFrame executeInEDT() throws Throwable {
                timeView = new TimeView();
                timeView.bind(timeFilterController);
                JFrame frame = TestFrame.show(timeView, 1400, 100);
                return frame;
            }
        });

        frameFixture = new FrameFixture(frame);

        HistoricalIndexResponse response = new HistoricalIndexResponse();
        int items = 1000;

        HistoricalIndexElement[] hie = new HistoricalIndexElement[items];
        for (int i = 0; i < items; i++) {
            hie[i] = new HistoricalIndexElement(i * 1000, 1000, 10, 2, 1, 1);
        }

        response.setElements(hie);

        timeFilterController.processUpdate(response);
        model.moveToEarliestTime(TimeUtils.parseInterval("2 minutes"));

        JLabelFixture label = frameFixture.label("TimeView.intervalLabel");
        assertThat(label.text(), is("1 sec"));

        TimeViewFixture timeViewFixture = new TimeViewFixture(frameFixture.robot, timeView.getTimeViewComponent());

        timeViewFixture.robot.moveMouse(timeView.getTimeViewComponent());

        // Scroll up the intervals
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("10 sec"));
        
        long delay = 0;
        ThreadUtils.sleep(delay);
        
        frameFixture.cleanUp();

    }

    
    @Test public void test_full_scrolling() {

        final JFrame frame = GuiActionRunner.execute(new GuiQuery<JFrame>() {
            @Override protected JFrame executeInEDT() throws Throwable {
                timeView = new TimeView();
                timeView.bind(timeFilterController);

                JFrame frame = TestFrame.show(timeView, 1400, 100);
                return frame;
            }
        });

        frameFixture = new FrameFixture(frame);

        HistoricalIndexResponse response = new HistoricalIndexResponse();
        int items = 1000;

        HistoricalIndexElement[] hie = new HistoricalIndexElement[items];
        for (int i = 0; i < items; i++) {
            hie[i] = new HistoricalIndexElement(i * 1000, 1000, 10, 2, 1, 1);
        }

        response.setElements(hie);

        timeFilterController.processUpdate(response);
        model.moveToEarliestTime(TimeUtils.parseInterval("2 minutes"));

        JLabelFixture label = frameFixture.label("TimeView.intervalLabel");
        assertThat(label.text(), is("1 sec"));

        TimeViewFixture timeViewFixture = new TimeViewFixture(frameFixture.robot, timeView.getTimeViewComponent());

        timeViewFixture.robot.moveMouse(timeView.getTimeViewComponent());

        long delay = 0;
        
        // Scroll up the intervals
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("10 sec"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("30 sec"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("1 min"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("10 min"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("30 min"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("1 hour"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("6 hour"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("12 hour"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("1 day"));
        ThreadUtils.sleep(delay);
        
        // This is the last option, it should lock here
        timeViewFixture.robot.rotateMouseWheel(-1);
        assertThat(label.text(), is("1 day"));
        ThreadUtils.sleep(delay);
        
        // And then back down the intervals
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("12 hour"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("6 hour"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("1 hour"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("30 min"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("10 min"));
        ThreadUtils.sleep(delay);       
        
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("1 min"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("30 sec"));
        ThreadUtils.sleep(delay);
        
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("10 sec"));
        ThreadUtils.sleep(delay);
                
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("1 sec"));
        ThreadUtils.sleep(delay);
        
        // This is the last option, it should lock here
        timeViewFixture.robot.rotateMouseWheel(1);
        assertThat(label.text(), is("1 sec"));
        ThreadUtils.sleep(delay);

        frameFixture.cleanUp();

    }

    @Test public void test_full_scrolling_with_clicks() {

        final JFrame frame = GuiActionRunner.execute(new GuiQuery<JFrame>() {
            @Override protected JFrame executeInEDT() throws Throwable {
                timeView = new TimeView();
                timeView.bind(timeFilterController);

                JFrame frame = TestFrame.show(timeView, 1400, 100);
                return frame;
            }
        });

        frameFixture = new FrameFixture(frame);

        HistoricalIndexResponse response = new HistoricalIndexResponse();
        int items = 1000;

        HistoricalIndexElement[] hie = new HistoricalIndexElement[items];
        for (int i = 0; i < items; i++) {
            hie[i] = new HistoricalIndexElement(i * 1000, 1000, 10, 2, 1, 1);
        }

        response.setElements(hie);

        timeFilterController.processUpdate(response);
        model.moveToEarliestTime(TimeUtils.parseInterval("2 minutes"));

        JLabelFixture label = frameFixture.label("TimeView.intervalLabel");
        assertThat(label.text(), is("1 sec"));

        TimeViewFixture timeViewFixture = new TimeViewFixture(frameFixture.robot, timeView.getTimeViewComponent());

        timeViewFixture.robot.moveMouse(timeView.getTimeViewComponent());

        long delay = 0;
        
        
        // Scroll up the intervals
        label.click();        
        assertThat(label.text(), is("10 sec"));
        ThreadUtils.sleep(delay);
        
        label.click();
        assertThat(label.text(), is("30 sec"));
        ThreadUtils.sleep(delay);
        
        label.click();
        assertThat(label.text(), is("1 min"));
        ThreadUtils.sleep(delay);
        
        label.click();
        assertThat(label.text(), is("10 min"));
        ThreadUtils.sleep(delay);
        
        label.click();
        assertThat(label.text(), is("30 min"));
        ThreadUtils.sleep(delay);
        
        label.click();
        assertThat(label.text(), is("1 hour"));
        ThreadUtils.sleep(delay);
        
        label.click();
        assertThat(label.text(), is("6 hour"));
        ThreadUtils.sleep(delay);
        
        label.click();
        assertThat(label.text(), is("12 hour"));
        ThreadUtils.sleep(delay);
        
        label.click();
        assertThat(label.text(), is("1 day"));
        ThreadUtils.sleep(delay);
        
        // This is the last option, it should lock here
        label.click();
        assertThat(label.text(), is("1 day"));
        ThreadUtils.sleep(delay);
        
        // And then back down the intervals
        label.rightClick();
        assertThat(label.text(), is("12 hour"));
        ThreadUtils.sleep(delay);
        
        label.rightClick();
        assertThat(label.text(), is("6 hour"));
        ThreadUtils.sleep(delay);
        
        label.rightClick();
        assertThat(label.text(), is("1 hour"));
        ThreadUtils.sleep(delay);
        
        label.rightClick();
        assertThat(label.text(), is("30 min"));
        ThreadUtils.sleep(delay);
        
        label.rightClick();
        assertThat(label.text(), is("10 min"));
        ThreadUtils.sleep(delay);       
        
        label.rightClick();
        assertThat(label.text(), is("1 min"));
        ThreadUtils.sleep(delay);
        
        label.rightClick();
        assertThat(label.text(), is("30 sec"));
        ThreadUtils.sleep(delay);
        
        label.rightClick();
        assertThat(label.text(), is("10 sec"));
        ThreadUtils.sleep(delay);
                
        label.rightClick();
        assertThat(label.text(), is("1 sec"));
        ThreadUtils.sleep(delay);
        
        // This is the last option, it should lock here
        label.rightClick();
        assertThat(label.text(), is("1 sec"));
        ThreadUtils.sleep(delay);

        frameFixture.cleanUp();

    }
    
}
