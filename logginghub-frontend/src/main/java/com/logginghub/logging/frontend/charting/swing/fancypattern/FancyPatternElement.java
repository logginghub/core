package com.logginghub.logging.frontend.charting.swing.fancypattern;

import java.awt.Rectangle;

public class FancyPatternElement {

    public enum ElementState {
        raw,
        numeric,
        nonnumeric
    }

    private String text;
    private Rectangle bounds;
    private boolean mouseOver;
    private ElementState elementState = ElementState.raw;
    private boolean selected;
    private int group = -1;

    public FancyPatternElement(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setMouseOver(boolean b) {
        this.mouseOver = b;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }

    @Override public String toString() {
        return "Element [text=" + text + ", bounds=" + bounds + ", mouseOver=" + mouseOver + "]";
    }

    public void cycleState(boolean isNumeric) {

        if (isNumeric) {
            switch (elementState) {
                case numeric:
                    elementState = ElementState.nonnumeric;
                    break;
                case nonnumeric:
                    elementState = ElementState.raw;
                    break;
                case raw:
                    elementState = ElementState.numeric;
                    break;
            }

        }
        else {
            switch (elementState) {
                case nonnumeric:
                    elementState = ElementState.raw;
                    break;
                case raw:
                    elementState = ElementState.nonnumeric;
                    break;
            }
        }
    }

    public ElementState getElementState() {
        return elementState;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getGroup() {
        return group;
    }

}
