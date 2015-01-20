package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.transaction.configuration.LogEventTemplateConfiguration;

@XmlAccessorType(XmlAccessType.FIELD) public class SQLExtractQueryConfiguration {
    @XmlAttribute private String database;
    @XmlAttribute private String sql;
    @XmlAttribute private String pattern;
    @XmlAttribute private String initialDelay = "10 seconds";
    @XmlAttribute private String repeatDelay = "1 minute";

    @XmlElement private LogEventTemplateConfiguration template = new LogEventTemplateConfiguration();
    
    public LogEventTemplateConfiguration getTemplate() {
        return template;
    }
    
    public void setTemplate(LogEventTemplateConfiguration template) {
        this.template = template;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(String initialDelay) {
        this.initialDelay = initialDelay;
    }

    public String getRepeatDelay() {
        return repeatDelay;
    }
    
    public void setRepeatDelay(String repeatDelay) {
        this.repeatDelay = repeatDelay;
    }

}
