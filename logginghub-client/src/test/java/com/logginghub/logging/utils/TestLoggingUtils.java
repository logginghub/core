package com.logginghub.logging.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class TestLoggingUtils {

    @Test public void testGetLog4jLevelDescriptionInt() throws Exception {
        
        assertThat(LoggingUtils.getLog4jLevelDescription(com.logginghub.utils.logging.Logger.finest), is("TRACE"));
        assertThat(LoggingUtils.getLog4jLevelDescription(com.logginghub.utils.logging.Logger.finer), is("TRACE"));
        assertThat(LoggingUtils.getLog4jLevelDescription(com.logginghub.utils.logging.Logger.fine), is("DEBUG"));
        assertThat(LoggingUtils.getLog4jLevelDescription(com.logginghub.utils.logging.Logger.config), is("INFO"));
        assertThat(LoggingUtils.getLog4jLevelDescription(com.logginghub.utils.logging.Logger.info), is("INFO"));
        assertThat(LoggingUtils.getLog4jLevelDescription(com.logginghub.utils.logging.Logger.warning), is("WARN"));
        assertThat(LoggingUtils.getLog4jLevelDescription(com.logginghub.utils.logging.Logger.severe), is("ERROR"));
       
    }

}
