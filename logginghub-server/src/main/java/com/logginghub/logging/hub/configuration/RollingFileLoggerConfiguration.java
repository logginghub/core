package com.logginghub.logging.hub.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.logginghub.logging.modules.RollingFileLogger;
import com.logginghub.utils.module.Configures;

@Configures(RollingFileLogger.class)
@XmlAccessorType(XmlAccessType.FIELD) public class RollingFileLoggerConfiguration extends BaseFileLoggerConfiguration {

}
