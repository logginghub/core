package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class HighlighterConfiguration {
    
    @XmlAttribute private String colourHex;
    @XmlAttribute private String phrase;

    public String getColourHex() {
        return colourHex;
    }
    
    public String getPhrase() {
        return phrase;
    }
    
    public void setColourHex(String colourHex) {
        this.colourHex = colourHex;
    }
    
    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
    
}
