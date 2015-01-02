package com.logginghub.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class Histogram extends JPanel {

    private int maximumValues = 3 * 60;
    private static final long serialVersionUID = 1L;

    private List<List<Integer>> series = new ArrayList<List<Integer>>();
    private List<Color> colours = new ArrayList<Color>();

    private HistogramHighValueLinker highValueLinker;

    public Histogram(int seriesCount) {

        for(int i = 0; i < seriesCount; i++) {
            series.add(new ArrayList<Integer>());
        }
        
        colours.add(ColourUtils.parseColor("red"));
        colours.add(ColourUtils.parseColor("green"));
        colours.add(ColourUtils.parseColor("blue"));

    }

    public void clear() {
        series.clear();
    }

    public void setHighValueLinker(HistogramHighValueLinker highValueLinker) {
        this.highValueLinker = highValueLinker;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        Number highest = 0;
        if (highValueLinker != null) {
            highest = highValueLinker.getHighValue();
        }
        else {
            highest = getHighValue();
        }
        
        for (int i = 0; i < series.size(); i++) {
            List<Integer> data = series.get(i);
            Color colour = colours.get(i);
            render(g, colour, data, highest);
        }
    }

    private void render(Graphics g, Color color, List<Integer> series, Number highest) {

        int size = series.size();


        int x = 0;
        for (int i = 0; i < size; i++) {
            Number value = series.get(i);
            double relativeToHighest = value.doubleValue() / highest.doubleValue();
            int actualHeight = (int) (getHeight() * relativeToHighest);
            g.setColor(color);
            int y2 = getHeight() - actualHeight;
            g.drawLine(x, getHeight(), x, y2);
            // g.setColor(Color.yellow);
            // g.drawLine(x, y2, x, y2 - 1);

            x++;
        }
    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(maximumValues, 100);
    }

    public int getHighValue() {
        int highest = -1;

        for (int i = 0; i < series.size(); i++) {
            List<Integer> data = series.get(i);
            int size = data.size();
            for (int j = 0; j < size; j++) {
                int value = data.get(j);
                highest = Math.max(value, highest);
            }
        }

        return highest;
    }

    public void add(int seriesIndex, int value) {
        List<Integer> data = series.get(seriesIndex);
        data.add(value);

        if (highValueLinker != null) {
            highValueLinker.onNewValue(value);
        }

        if (data.size() > getWidth()) {
            int removed = data.remove(0);

            if (highValueLinker != null) {
                highValueLinker.onValueRemoved(removed);
            }
        }

        repaint();
    }
}