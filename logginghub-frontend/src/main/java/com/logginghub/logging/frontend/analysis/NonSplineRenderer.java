package com.logginghub.logging.frontend.analysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.StringUtils;

public class NonSplineRenderer extends XYLineAndShapeRenderer implements HighlightableRenderer {

    private static final BasicStroke thickerStroke = new BasicStroke(3);

    private static final long serialVersionUID = 1L;

    private Paint[] _paints;
    private int nextPaint = 0;

    private int seriesHighlight = -1;

    // private int highlightColumn = -1;

    private Map<String, LineFormat> formatsBySeries = new HashMap<String, LineFormat>();

    class LineFormat {
        Paint color;
        Stroke stroke;
        public BasicStroke highlightedStroke;

    }

    public NonSplineRenderer(boolean lines, boolean shapes) {
        super(lines, shapes);

        _paints = new Paint[] { ColourUtils.parseColor("#FF0000"),
                               ColourUtils.parseColor("#0000FF"),
                               ColourUtils.parseColor("#00FF00"),
                               ColourUtils.parseColor("#FFFF00"),
                               ColourUtils.parseColor("#FF00FF"),
                               ColourUtils.parseColor("#FF8080"),
                               ColourUtils.parseColor("#808080"),
                               ColourUtils.parseColor("#800000"),
                               ColourUtils.parseColor("#FF8000"),

        };

        String formats = ResourceUtils.readOrNull("seriesformat.txt");
        if (StringUtils.isNotNullOrEmpty(formats)) {
            String[] splitIntoLines = StringUtils.splitIntoLines(formats);
            for (String string : splitIntoLines) {

                String[] split = string.split(",");

                String series = split[0].trim();
                String colourString = split[1].trim();
                String thinknessString = split[2].trim();

                String dashString = null;
                if (split.length > 3) {
                    dashString = split[3].trim();
                }

                LineFormat format = new LineFormat();
                format.color = ColourUtils.parseColor(colourString);

                if (StringUtils.isNotNullOrEmpty(dashString)) {

                    format.stroke = new BasicStroke(Float.parseFloat(thinknessString),
                                                    BasicStroke.CAP_BUTT,
                                                    BasicStroke.JOIN_MITER,
                                                    1f,
                                                    new float[] { Float.parseFloat(dashString) },
                                                    10f);

                    format.highlightedStroke = new BasicStroke(Float.parseFloat(thinknessString) + 3,
                                                               BasicStroke.CAP_BUTT,
                                                               BasicStroke.JOIN_MITER,
                                                               1f,
                                                               new float[] { Float.parseFloat(dashString) },
                                                               10f);
                }
                else {
                    format.stroke = new BasicStroke(Float.parseFloat(thinknessString));
                    format.highlightedStroke = new BasicStroke(Float.parseFloat(thinknessString) + 3);
                }

                formatsBySeries.put(series, format);
            }
        }

    }
    
    public Paint lookupSeriesPaint(int series) {
        XYDataset dataset = getPlot().getDataset();
        String key = dataset.getSeriesKey(series).toString();
        LineFormat lineFormat = getLineFormat(key);
        return lineFormat.color;
    }
    
    

    public synchronized LineFormat getLineFormat(String key) {
        LineFormat lineFormat = formatsBySeries.get(key);
        if (lineFormat == null) {
            lineFormat = new LineFormat();
            lineFormat.color = _paints[nextPaint++ % _paints.length];
            lineFormat.stroke = new BasicStroke(1);
            lineFormat.highlightedStroke = new BasicStroke(3);
            formatsBySeries.put(key, lineFormat);
        }
        return lineFormat;
    }

    @Override public Stroke getItemStroke(int series, int item) {
        XYDataset dataset = getPlot().getDataset();
        String key = dataset.getSeriesKey(series).toString();

        LineFormat lineFormat = getLineFormat(key);
        lineFormat = getLineFormat(key);

        if (series == seriesHighlight) {
            return lineFormat.highlightedStroke;
        }
        else {
            return lineFormat.stroke;
        }
    }

    @Override public Paint getItemPaint(int series, int col) {

        XYDataset dataset = getPlot().getDataset();
        String key = dataset.getSeriesKey(series).toString();

        boolean hasSelection = seriesHighlight != -1;
        boolean isSelected = series == seriesHighlight;

        LineFormat lineFormat = getLineFormat(key);

        if (isSelected) {
            return lineFormat.color;
        }
        else {

            if (!hasSelection) {
                // No selection, return standard
                return lineFormat.color;
            }
            else {
                // Has a selection, but its not us, so go heavy alpha
                Color c = (Color) lineFormat.color;
                Color a = new Color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 0.25f);
                return a;
            }
        }

    }

    public Shape lookupLegendShape(int series) {
        return new Rectangle(15, 15);
    }

    public void setHighlightedSeries(int seriesIndex) {
        this.seriesHighlight = seriesIndex;
        notifyListeners(new RendererChangeEvent(this));
    }

    @Override public XYLineAndShapeRenderer getRenderer() {
        return this;         
    }

}