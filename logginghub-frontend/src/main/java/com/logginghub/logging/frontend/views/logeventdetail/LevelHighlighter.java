package com.logginghub.logging.frontend.views.logeventdetail;

import java.awt.Color;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.Utils;
import com.logginghub.logging.frontend.model.RowFormatModel;
import com.logginghub.utils.logging.Logger;

public class LevelHighlighter implements RowHighlighter {

    private static final Logger logger = Logger.getLoggerFor(LevelHighlighter.class);
    public static Color peachy = Color.decode("0xFFCC99");
    public static Color purple = Color.decode("0xCC99FF");

    private RowFormatModel selectedRowFormat;

    public LevelHighlighter(RowFormatModel formatConfiguration) {
        setSelectedRowFormat(formatConfiguration);
    }

    public void updateSettings(HighlightSettings settings, int rowIndex, int colIndex, boolean isSelected, boolean isBookmarked, LogEvent event) {

        Color background = Utils.getBackgroundColourForEvent(event);

        if (settings.getBackground() == null) {
            settings.setBackground(background);
        }

        if (settings.getForeground() == null) {
            settings.setForeground(Color.BLACK);
        }

        if (isBookmarked) {
            if (isSelected) {
                settings.setBackground(purple);
            }
            else {
                settings.setBackground(peachy);
            }
        }
        else {
            if (isSelected) {
                if (selectedRowFormat != null) {

                    Color overrideBackground = selectedRowFormat.getBackgroundColour().get();
                    if (overrideBackground != null) {
                        settings.setBackground(overrideBackground);
                    }

                    Color overrideForeground = selectedRowFormat.getForegroundColour().get();
                    if (overrideForeground != null) {
                        settings.setForeground(overrideForeground);
                    }

                    settings.setBorderColour(selectedRowFormat.getBorderColour().get());
                    settings.setBorderThickness(selectedRowFormat.getBorderWidth().get());
                    settings.setFont(selectedRowFormat.getFont().get());
                }
                else {
                    settings.setBackground(Color.cyan);
                }
            }
        }

        if (rowIndex % 2 == 0) {
            background = (aBitDarker(background));
        }

        if (colIndex == 0) {
            logger.fine("Background for row {} is {}", rowIndex, background);
        }
    }

    public static Color aBitDarker(Color background) {
        float factor = 0.95f;
        int darkerRed = (int) (background.getRed() * factor);
        int darkerGreen = (int) (background.getGreen() * factor);
        int darkerBlue = (int) (background.getBlue() * factor);

        Color darker = new Color(darkerRed, darkerGreen, darkerBlue);
        return darker;
    }

    public boolean isInterested(int rowIndex, int colIndex, boolean isSelected, boolean isBookmarked, LogEvent event) {
        return true;
    }

    public void setSelectedRowFormat(RowFormatModel selectedRowFormat) {
        this.selectedRowFormat = selectedRowFormat;
    }

}
