package com.logginghub.logging.frontend.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD) public class ChartingConfiguration {

    @XmlElementWrapper(name = "pages") @XmlElement(name = "page") private List<PageConfiguration> pages = new ArrayList<PageConfiguration>();
    @XmlElement(name = "parsers") private ParserBlockConfiguration parserConfiguration = new ParserBlockConfiguration();

    public List<PageConfiguration> getPages() {
        return pages;
    }

    public void setPages(List<PageConfiguration> pages) {
        this.pages = pages;
    }

    public ParserBlockConfiguration getParserConfiguration() {
        return parserConfiguration;
    }

}
