package com.logginghub.analytics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * Controls the line format used for chart series to ensure the same formats can
 * be used for equivalent series in different charts.
 * 
 * @author James
 * 
 */
public class LineFormatController {
    
    private Map<String, Paint> seriesPaints = new HashMap<String, Paint>();
    private Map<String, Stroke> seriesStrokes = new HashMap<String, Stroke>();
    private DefaultDrawingSupplier defaultDrawingSupplier = new DefaultDrawingSupplier();
    
    public synchronized Paint allocateColour(String seriesName){
        Paint paint = seriesPaints.get(seriesName);
        if(paint == null){
            paint = defaultDrawingSupplier.getNextPaint();
            seriesPaints.put(seriesName, paint);
        }
        
        return paint;
    }
    
   
    public void setPaint(String seriesName, Paint paint) {
        seriesPaints.put(seriesName, paint);
    }

    public void setStroke(String seriesName, BasicStroke stroke) {
        seriesStrokes.put(seriesName, stroke);
    }

    public Stroke getStroke(String label) {
        Stroke stroke = seriesStrokes.get(label);
        if(stroke == null){
            stroke = new BasicStroke(1);
            seriesStrokes.put(label, stroke);
        }
        return stroke;
         
    }
    
}
