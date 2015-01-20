package com.logginghub.logging.frontend.analysis;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public interface HighlightableRenderer {
    void setHighlightedSeries(int seriesIndex);
    XYLineAndShapeRenderer getRenderer();
}
