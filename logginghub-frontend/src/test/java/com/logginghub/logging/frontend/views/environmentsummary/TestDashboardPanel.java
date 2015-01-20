package com.logginghub.logging.frontend.views.environmentsummary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JLabelFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.ObservableArrayList;
import com.logginghub.logging.frontend.model.ObservableList;
import com.logginghub.logging.frontend.views.detail.DetailedLogEventTable;
import com.logginghub.logging.frontend.views.environmentsummary.DashboardPanel;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.swing.TestFrame;

@Ignore // works fine when run by itself :/
public class TestDashboardPanel {

    private DashboardPanel panel;
    private FrameFixture frameFixture;
    
    @BeforeClass public static void setupFEST() {
        FailOnThreadViolationRepaintManager.install();
        System.setProperty(DetailedLogEventTable.useDefaultColumnPropertiesKey, "true");
    }

    @Before public void setup() {
    
        
        JFrame dashboard = GuiActionRunner.execute(new GuiQuery<JFrame>() {
            @Override protected JFrame executeInEDT() throws Throwable {
                panel = new DashboardPanel();                               
                return TestFrame.show(panel, 800, 600);
            }
        });

        frameFixture = new FrameFixture(dashboard);
    }
    
    @After public void cleanup() {
        frameFixture.cleanUp();
    }
    
    @Test public void test() {
        final EnvironmentModel pricingModel = new EnvironmentModel();
        final EnvironmentModel tradingModel = new EnvironmentModel();       
        final EnvironmentModel riskModel = new EnvironmentModel();
        
        pricingModel.setName("Pricing");
        tradingModel.setName("Trading");
        riskModel.setName("Risk");
        
        final ObservableList<EnvironmentModel> environments = new ObservableArrayList<EnvironmentModel>();
        environments.add(pricingModel);
        environments.add(tradingModel);
        environments.add(riskModel);        
                
        SwingUtilities.invokeLater(new Runnable() {           
            @Override public void run() {
                panel.bind(environments);
                panel.revalidate();
            }
        });        
                
        pricingModel.getEventController().add(LogEventBuilder.start().setLevel(Level.WARNING.intValue()).toLogEvent(),
                                              new Filter<LogEvent>() {
                                                @Override public boolean passes(LogEvent event) {
                                                    return true;
                                                }
                                            }, true);
       
        pricingModel.updateEachSecond();
        
        final JLabelFixture label = frameFixture.panel("DashboardPanel").panel("EnvironmentSummaryPanel-Pricing").panel("warningIndicatorResizingLabel").label("valueResizingLabel");
        ThreadUtils.untilTrue(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return label.target.getText().equals("1 (+1)");
            }
        });
        assertThat(label.target.getText(), is("1 (+1)"));
        
        pricingModel.updateEachSecond();
        
        pricingModel.updateEachSecond();
        
        ThreadUtils.untilTrue(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return label.target.getText().equals("1");
            }
        });
        assertThat(label.target.getText(), is("1"));
        
    }

}

