package com.logginghub.logging.frontend.views.logeventdetail;

import java.awt.Color;
import java.awt.Font;

public class HighlightSettings {

    private Color foreground;
    private Color background;
    private Font font;
    private Color border;
    private int borderThickness;
    private Color borderColour;

    public HighlightSettings(Color foreground, Color background) {
        setForeground(foreground);
        setBackground(background);
    }

    public Font getFont() {
        return font;
    }

    public Color getBorder() {
        return border;
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public Color getBackground() {
        return background;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setBorderColour(Color borderColour) {
        this.borderColour = borderColour;
    }

    public Color getBorderColour() {
        return borderColour;
    }

    public void setBorderThickness(int borderWidth) {
        this.borderThickness = borderWidth;
    }

}
