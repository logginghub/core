package com.logginghub.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColourInterpolation {

    // Stores the colour ranges. The double is a value between 0 and 1 that this colour is good for
    private List<Pair<Double, Color>> colourRanges = new ArrayList<Pair<Double, Color>>();
    private Color[] colours;

    public ColourInterpolation(Color... colours) {
        this.colours = colours;
        double factor = 0;
        double perItem = 1 / (double) (colours.length - 1);

        for (int i = 0; i < colours.length; i++) {
            this.colourRanges.add(new Pair<Double, Color>(factor, colours[i]));
            factor += perItem;
        }
    }
    
    public Color[] getColours() {
        return colours;
    }

    public Color interpolate(double value) {

        Pair<Double, Color> previous = null;
        Color result = null;

        for (int i = 0; i < this.colourRanges.size() && result == null; i++) {

            Pair<Double, Color> thisItem = colourRanges.get(i);

            double itemValue = thisItem.getA();
            if (itemValue >= value) {
                // Gone far enough
                if (previous == null) {
                    // In the first item
                    result = thisItem.getB();
                }
                else {
                    // In between two items - work out how far
                    double startRange = previous.getA();
                    double endRange = itemValue;

                    double factor = (value - startRange) / (endRange - startRange);

                    Color start = previous.getB();
                    Color end = thisItem.getB();

                    int r = colour(factor, start.getRed(), end.getRed());
                    int g = colour(factor, start.getGreen(), end.getGreen());
                    int b = colour(factor, start.getBlue(), end.getBlue());

                    result = new Color(r, g, b);
                }
            }

            previous = thisItem;

        }

        if (result == null) {
            // Value is after the final colour
            result = previous.getB();
        }

        return result;

    }

    public int colour(double factor, int start, int end) {
        return (int) (start + (factor * (end - start)));
    }

}
