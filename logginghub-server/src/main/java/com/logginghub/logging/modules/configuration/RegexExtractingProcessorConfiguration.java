package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@SuppressWarnings("restriction") @XmlAccessorType(XmlAccessType.FIELD) public class RegexExtractingProcessorConfiguration {

    @XmlAttribute private String name;
    @XmlAttribute private String expression;
    @XmlAttribute private long aggregationPeriod = 1000;
    @XmlAttribute private boolean countNameElements = true;
    @XmlAttribute private boolean allowNumericParseFailures = false;

    public RegexExtractingProcessorConfiguration() {}
    
    public RegexExtractingProcessorConfiguration(String name,
                                                 String expression,
                                                 long aggregationPeriod,
                                                 boolean countNameElements,
                                                 boolean allowNumericParseFailures) {
        super();
        this.name = name;
        this.expression = expression;
        this.aggregationPeriod = aggregationPeriod;
        this.countNameElements = countNameElements;
        this.allowNumericParseFailures = allowNumericParseFailures;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public long getAggregationPeriod() {
        return aggregationPeriod;
    }

    public void setAggregationPeriod(long aggregationPeriod) {
        this.aggregationPeriod = aggregationPeriod;
    }
    
    public void setAllowNumericParseFailures(boolean allowNumericParseFailures) {
        this.allowNumericParseFailures = allowNumericParseFailures;
    }
    public void setCountNameElements(boolean countNameElements) {
        this.countNameElements = countNameElements;
    }
    
    public boolean getAllowNumericParseFailures() {
        return allowNumericParseFailures;
    }
    
    public boolean getCountNameElements() {
        return countNameElements;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
