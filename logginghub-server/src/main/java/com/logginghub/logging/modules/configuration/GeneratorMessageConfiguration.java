package com.logginghub.logging.modules.configuration;

import com.logginghub.logging.transaction.configuration.LogEventTemplateConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD) public class GeneratorMessageConfiguration {

    @XmlAttribute private double rateMin = 10;
    @XmlAttribute private double rateMax = 20;
    @XmlAttribute private int trendMin = 10;
    @XmlAttribute private int trendMax = 20;
    @XmlAttribute private boolean random = false;
    @XmlAttribute private String pattern;
    @XmlAttribute private String patternFile;
    @XmlElement private LogEventTemplateConfiguration template = new LogEventTemplateConfiguration();

    @XmlElement private List<VariableConfiguration> variable = new ArrayList<VariableConfiguration>();
    @XmlAttribute private String level = "INFO";

    public String getPatternFile() {
        return patternFile;
    }

    public String getPattern() {
        return pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    
    public boolean isRandom() {
        return random;
    }
    
    public LogEventTemplateConfiguration getTemplate() {
        return template;
    }

    public double getRateMin() {
        return rateMin;
    }

    public void setRateMin(int rateMin) {
        this.rateMin = rateMin;
    }

    public double getRateMax() {
        return rateMax;
    }

    public void setRateMax(int rateMax) {
        this.rateMax = rateMax;
    }

    public int getTrendMin() {
        return trendMin;
    }

    public void setTrendMin(int trendMin) {
        this.trendMin = trendMin;
    }

    public int getTrendMax() {
        return trendMax;
    }

    public void setTrendMax(int trendMax) {
        this.trendMax = trendMax;
    }

    public List<VariableConfiguration> getVariables() {
        return variable;
    }

    public String getLevel() {
        return level;
         
    }
    


}

