package com.logginghub.logging.hub.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.logginghub.logging.modules.TimestampFixedRollingFileLogger;
import com.logginghub.logging.modules.TimestampVariableRollingFileLogger;
import com.logginghub.utils.module.Configures;

/**
 * Variation on the {@link RollingFileLoggerConfiguration} to support the
 * configuration properties for the newer {@link TimestampVariableRollingFileLogger}
 * 
 * @author James
 * 
 */
@Configures(TimestampFixedRollingFileLogger.class)
@XmlAccessorType(XmlAccessType.FIELD) public class TimestampFixedRollingFileLoggerConfiguration extends BaseFileLoggerConfiguration implements FileLogConfiguration {
 
}
