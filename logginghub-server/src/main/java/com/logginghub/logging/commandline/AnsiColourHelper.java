package com.logginghub.logging.commandline;

import com.logginghub.utils.StringUtils;

public class AnsiColourHelper {

    private static final char ESC = 27;

    public enum AnsiColour {
        None,
        Black,
        Red,
        Green,
        Yellow,
        Blue,
        Magenta,
        Cyan,
        White
    }

    public static String format(AnsiColour foreground, AnsiColour background, boolean bold, String format, Object... params) {

        StringBuilder builder = new StringBuilder();

        builder.append(ESC).append("[");

        String div = "";
        if (background != AnsiColour.None) {
            // Setup the background colour
            builder.append("4").append(getColourCodeChar(background));
            div = ";";
        }

        if (foreground != AnsiColour.None) {
            // Setup the foreground colour
            builder.append(div).append("3").append(getColourCodeChar(foreground));
            div = ";";
        }

        if (bold) {
            builder.append(div).append("1");
        }

        builder.append("m");

        builder.append(StringUtils.format(format, params));
        builder.append(getResetCode());
        return builder.toString();

    }

    public static String red(String format, Object... params) {
        return format(AnsiColour.Red, format, params);
    }

    public static String format(AnsiColour colour, String format, Object... params) {
        StringBuilder builder = new StringBuilder();
        builder.append(getBoldColourCode(colour));
        builder.append(StringUtils.format(format, params));
        builder.append(getResetCode());
        return builder.toString();
    }

    private static String getResetCode() {
        return ESC + "[0m";
    }

    private static String getBoldColourCode(AnsiColour colour) {
        char colourCode = getColourCodeChar(colour);
        return ESC + "[3" + colourCode + ";1m";
    }

    private static String getColourCode(AnsiColour colour) {
        char colourCode = getColourCodeChar(colour);
        return ESC + "[3" + colourCode + "m";
    }

    private static char getColourCodeChar(AnsiColour colour) {
        char colourCode = '7';

        switch (colour) {
            case Black:
                colourCode = '0';
                break;
            case Red:
                colourCode = '1';
                break;
            case Green:
                colourCode = '2';
                break;
            case Yellow:
                colourCode = '3';
                break;
            case Blue:
                colourCode = '4';
                break;
            case Magenta:
                colourCode = '5';
                break;
            case Cyan:
                colourCode = '6';
                break;
            case White:
                colourCode = '7';
                break;
        }
        return colourCode;
    }

}
