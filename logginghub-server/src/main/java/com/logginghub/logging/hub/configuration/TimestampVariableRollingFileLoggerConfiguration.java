package com.logginghub.logging.hub.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.logginghub.logging.modules.TimestampVariableRollingFileLogger;
import com.logginghub.utils.module.Configures;

/**
 * Variation on the {@link RollingFileLoggerConfiguration} to support the
 * configuration properties for the newer {@link TimestampVariableRollingFileLogger}
 * 
 * @author James
 * 
 */
@Configures(TimestampVariableRollingFileLogger.class)
@XmlAccessorType(XmlAccessType.FIELD) public class TimestampVariableRollingFileLoggerConfiguration extends BaseFileLoggerConfiguration implements FileLogConfiguration {

}
