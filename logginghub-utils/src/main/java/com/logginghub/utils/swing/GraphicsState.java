package com.logginghub.utils.swing;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;

/**
 * Captures the state of various aspects of the Graphics2D object to make it
 * easy to reset the state back to how you found it.
 * 
 * @author James
 */
public class GraphicsState
{
    private Stroke stroke;
    private Color color;
    private Paint paint;
    private Font font;
    private Composite composite;
    private static Graphics2D g2d;

    public static GraphicsState capture(Graphics2D g2d)
    {
        GraphicsState.g2d = g2d;
        GraphicsState state = new GraphicsState();
        state.stroke = g2d.getStroke();
        state.color = g2d.getColor();
        state.paint = g2d.getPaint();
        state.font = g2d.getFont();
        state.composite = g2d.getComposite();
        return state;
    }

    public void restore()
    {
        g2d.setFont(font);
        g2d.setColor(color);
        g2d.setPaint(paint);
        g2d.setComposite(composite);
        g2d.setStroke(stroke);
    }
}
