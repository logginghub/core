package com.logginghub.integrationtests.loggingfrontend.helpers;

import java.awt.Frame;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.fixture.ContainerFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTableCellFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTable;

public abstract class BaseSwing {
    @BeforeClass public static void setup() {
        FailOnThreadViolationRepaintManager.install();
        System.setProperty(DetailedLogEventTable.useDefaultColumnPropertiesKey, "true");
    }

    private SwingFrontEnd swingFrontEnd;
    private FrameFixture frameFixture;
    private JTableCellFixture cell;
    protected SwingFrontEndDSL dsl;

    protected ContainerFixture<Frame> getFrameFixture() {
        return frameFixture;
    }

    @Before public void createFrontend() {
        dsl = createDSL(getConfiguration());
        swingFrontEnd = dsl.getSwingFrontEnd();
        frameFixture = dsl.getFrameFixture();
    }

    public static SwingFrontEndDSL createDSL(final LoggingFrontendConfiguration configuration) {
        return SwingFrontEndDSL.createDSL(configuration);
    }

    public static SwingFrontEndDSL createDSLWithRealMain(LoggingFrontendConfiguration configuration) {
        return SwingFrontEndDSL.createDSLWithRealMain(configuration);
    }
    
    protected LoggingFrontendConfiguration getConfiguration() {
        return new LoggingFrontendConfiguration();
    }

    @After public void after() {
        frameFixture.cleanUp();
        swingFrontEnd.close();
        swingFrontEnd.dispose();
    }

    public SwingFrontEnd getSwingFrontEnd() {
        return swingFrontEnd;
    }

    

}
