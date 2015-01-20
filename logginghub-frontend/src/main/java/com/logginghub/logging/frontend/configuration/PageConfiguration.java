package com.logginghub.logging.frontend.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD) public class PageConfiguration {
    @XmlElement(name = "chart") private List<ChartConfiguration> charts = new ArrayList<ChartConfiguration>();
    @XmlAttribute private String title = "<no title set>";
    @XmlAttribute private int rows = 3;
    @XmlAttribute private int columns = 3;

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ChartConfiguration> getCharts() {
        return charts;
    }

    public void setCharts(List<ChartConfiguration> charts) {
        this.charts = charts;
    }
}
