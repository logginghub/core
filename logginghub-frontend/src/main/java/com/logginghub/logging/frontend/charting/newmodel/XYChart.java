package com.logginghub.logging.frontend.charting.newmodel;

import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

/**
 * Created by james on 31/01/15.
 */
public class XYChart extends BindableChartBase {
    @Override protected AbstractXYItemRenderer getRenderer() {
        return new XYLineAndShapeRenderer();
    }
}