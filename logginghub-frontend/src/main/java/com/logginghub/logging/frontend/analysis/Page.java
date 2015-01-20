package com.logginghub.logging.frontend.analysis;

import java.util.ArrayList;
import java.util.List;


public class Page
{
    private List<ComponentProvider> charts = new ArrayList<ComponentProvider>();
    private String title;
    private int rows = 1;
    private int columns = 1;
    
    public int getRows()
    {
        return rows;
    }
    public void setRows(int rows)
    {
        this.rows = rows;
    }
    public int getColumns()
    {
        return columns;
    }
    public void setColumns(int columns)
    {
        this.columns = columns;
    }
    public List<ComponentProvider> getCharts()
    {
        return charts;
    }
    public void setCharts(List<ComponentProvider> charts)
    {
        this.charts = charts;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
}
