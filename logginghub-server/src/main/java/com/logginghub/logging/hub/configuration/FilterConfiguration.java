package com.logginghub.logging.hub.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class FilterConfiguration {
    
    @XmlAttribute private String type;
    @XmlAttribute private String field;
    @XmlAttribute private String pattern;

    public FilterConfiguration() {}

    public FilterConfiguration(String type, String field, String pattern) {
        super();
        this.type = type;
        this.field = field;
        this.pattern = pattern;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public static FilterConfiguration contains(String string) {
        return new FilterConfiguration("contains", "message", string);
    }

    public static FilterConfiguration regex(String string) {
        return new FilterConfiguration("regex", "message", string);
    }

    public static FilterConfiguration startsWith(String string) {
        return new FilterConfiguration("startsWith", "message", string);
    }
    
//    public static FilterConfiguration startsWith(String string) {
//        FilterConfiguration configuration = new FilterConfiguration();
//        configuration.setStartsWith(string);
//        return configuration;
//    }
//
//    public static FilterConfiguration contains(String string) {
//        FilterConfiguration configuration = new FilterConfiguration();
//        configuration.setContains(string);
//        return configuration;
//         
//    }
//    
//    public static FilterConfiguration regex(String string) {
//        FilterConfiguration configuration = new FilterConfiguration();
//        configuration.setRegex(string);
//        return configuration;
//         
//    }
}
