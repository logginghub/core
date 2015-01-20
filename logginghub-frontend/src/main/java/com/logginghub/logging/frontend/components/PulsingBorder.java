package com.logginghub.logging.frontend.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class PulsingBorder extends AbstractBorder {
    private static final long serialVersionUID = 1L;

    protected int thickness;
    protected Color lineColor;
    protected boolean roundedCorners;

    public PulsingBorder(Color color) {
        this(color, 1, false);
    }

    public PulsingBorder(Color color, int thickness) {
        this(color, thickness, false);
    }

    public PulsingBorder(Color color, int thickness, boolean roundedCorners) {
        lineColor = color;
        this.thickness = thickness;
        this.roundedCorners = roundedCorners;
    }


    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        int i;

        g.setColor(lineColor);
        for (i = 0; i < thickness; i++) {
            if (!roundedCorners) {
                g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
            }
            else {
                g.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i - 1, thickness, thickness);
            }
        }
        g.setColor(oldColor);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = thickness;
        return insets;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public int getThickness() {
        return thickness;
    }

    public boolean getRoundedCorners() {
        return roundedCorners;
    }

    public boolean isBorderOpaque() {
        return !roundedCorners;
    }

}
