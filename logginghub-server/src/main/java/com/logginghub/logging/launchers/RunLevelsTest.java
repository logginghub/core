package com.logginghub.logging.launchers;

import java.util.logging.Logger;

import com.logginghub.logging.utils.LoggingUtils;

public class RunLevelsTest
{
    public static void main(String[] args)
    {
        LoggingUtils.loadLoggingConfiguration("/com/logginghub/logging/launchers/RunMockShopLogging.properties");
        Logger logger = Logger.getLogger(RunLevelsTest.class.getName());
        LoggingUtils.outputSampleLogging(logger);
    }
}
