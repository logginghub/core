package com.logginghub.utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColourUtils {

    public static final Color mildGreen = ColourUtils.attemptHex("C7FFCD");
    public static final Color mildRed = ColourUtils.attemptHex("F2BFCE");
    public static final Color mildOrange = ColourUtils.attemptHex("FCD195");
    public static final Color mildYellow = ColourUtils.attemptHex("FCFC95");
    public static final Color mildBlue = ColourUtils.attemptHex("95C0FC");
    public static final Color mildPurple = ColourUtils.attemptHex("CF95FC");
    public static final Color mildPink = ColourUtils.attemptHex("FC95EE");
    public static final Color mildGrey = ColourUtils.attemptHex("C2C2C2");

    private static Map<String, Color> htmlColours = new HashMap<String, Color>();

    static {
        setupHtmlColours();
    }

    public static Color parseColor(String colour) {

        Color parsed = attemptColorName(colour);
        if (parsed == null) {
            parsed = attemptHTML(colour);
            if (parsed == null) {
                parsed = attemptHex(colour);
                if (parsed == null) {
                    parsed = attemptSeparated(colour);
                }
            }
        }

        return parsed;
    }

    private static Color attemptHTML(String colour) {
        return htmlColours.get(colour.toLowerCase());
    }

    private static void setupHtmlColours() {
        htmlColours.put("AliceBlue".toLowerCase(), Color.decode("0xF0F8FF"));
        htmlColours.put("AntiqueWhite".toLowerCase(), Color.decode("0xFAEBD7"));
        htmlColours.put("Aqua".toLowerCase(), Color.decode("0x00FFFF"));
        htmlColours.put("Aquamarine".toLowerCase(), Color.decode("0x7FFFD4"));
        htmlColours.put("Azure".toLowerCase(), Color.decode("0xF0FFFF"));
        htmlColours.put("Beige".toLowerCase(), Color.decode("0xF5F5DC"));
        htmlColours.put("Bisque".toLowerCase(), Color.decode("0xFFE4C4"));
        htmlColours.put("Black".toLowerCase(), Color.decode("0x000000"));
        htmlColours.put("BlanchedAlmond".toLowerCase(), Color.decode("0xFFEBCD"));
        htmlColours.put("Blue".toLowerCase(), Color.decode("0x0000FF"));
        htmlColours.put("BlueViolet".toLowerCase(), Color.decode("0x8A2BE2"));
        htmlColours.put("Brown".toLowerCase(), Color.decode("0xA52A2A"));
        htmlColours.put("BurlyWood".toLowerCase(), Color.decode("0xDEB887"));
        htmlColours.put("CadetBlue".toLowerCase(), Color.decode("0x5F9EA0"));
        htmlColours.put("Chartreuse".toLowerCase(), Color.decode("0x7FFF00"));
        htmlColours.put("Chocolate".toLowerCase(), Color.decode("0xD2691E"));
        htmlColours.put("Coral".toLowerCase(), Color.decode("0xFF7F50"));
        htmlColours.put("CornflowerBlue".toLowerCase(), Color.decode("0x6495ED"));
        htmlColours.put("Cornsilk".toLowerCase(), Color.decode("0xFFF8DC"));
        htmlColours.put("Crimson".toLowerCase(), Color.decode("0xDC143C"));
        htmlColours.put("Cyan".toLowerCase(), Color.decode("0x00FFFF"));
        htmlColours.put("DarkBlue".toLowerCase(), Color.decode("0x00008B"));
        htmlColours.put("DarkCyan".toLowerCase(), Color.decode("0x008B8B"));
        htmlColours.put("DarkGoldenRod".toLowerCase(), Color.decode("0xB8860B"));
        htmlColours.put("DarkGray".toLowerCase(), Color.decode("0xA9A9A9"));
        htmlColours.put("DarkGreen".toLowerCase(), Color.decode("0x006400"));
        htmlColours.put("DarkKhaki".toLowerCase(), Color.decode("0xBDB76B"));
        htmlColours.put("DarkMagenta".toLowerCase(), Color.decode("0x8B008B"));
        htmlColours.put("DarkOliveGreen".toLowerCase(), Color.decode("0x556B2F"));
        htmlColours.put("Darkorange".toLowerCase(), Color.decode("0xFF8C00"));
        htmlColours.put("DarkOrchid".toLowerCase(), Color.decode("0x9932CC"));
        htmlColours.put("DarkRed".toLowerCase(), Color.decode("0x8B0000"));
        htmlColours.put("DarkSalmon".toLowerCase(), Color.decode("0xE9967A"));
        htmlColours.put("DarkSeaGreen".toLowerCase(), Color.decode("0x8FBC8F"));
        htmlColours.put("DarkSlateBlue".toLowerCase(), Color.decode("0x483D8B"));
        htmlColours.put("DarkSlateGray".toLowerCase(), Color.decode("0x2F4F4F"));
        htmlColours.put("DarkTurquoise".toLowerCase(), Color.decode("0x00CED1"));
        htmlColours.put("DarkViolet".toLowerCase(), Color.decode("0x9400D3"));
        htmlColours.put("DeepPink".toLowerCase(), Color.decode("0xFF1493"));
        htmlColours.put("DeepSkyBlue".toLowerCase(), Color.decode("0x00BFFF"));
        htmlColours.put("DimGray".toLowerCase(), Color.decode("0x696969"));
        htmlColours.put("DimGrey".toLowerCase(), Color.decode("0x696969"));
        htmlColours.put("DodgerBlue".toLowerCase(), Color.decode("0x1E90FF"));
        htmlColours.put("FireBrick".toLowerCase(), Color.decode("0xB22222"));
        htmlColours.put("FloralWhite".toLowerCase(), Color.decode("0xFFFAF0"));
        htmlColours.put("ForestGreen".toLowerCase(), Color.decode("0x228B22"));
        htmlColours.put("Fuchsia".toLowerCase(), Color.decode("0xFF00FF"));
        htmlColours.put("Gainsboro".toLowerCase(), Color.decode("0xDCDCDC"));
        htmlColours.put("GhostWhite".toLowerCase(), Color.decode("0xF8F8FF"));
        htmlColours.put("Gold".toLowerCase(), Color.decode("0xFFD700"));
        htmlColours.put("GoldenRod".toLowerCase(), Color.decode("0xDAA520"));
        htmlColours.put("Gray".toLowerCase(), Color.decode("0x808080"));
        htmlColours.put("Green".toLowerCase(), Color.decode("0x008000"));
        htmlColours.put("GreenYellow".toLowerCase(), Color.decode("0xADFF2F"));
        htmlColours.put("HoneyDew".toLowerCase(), Color.decode("0xF0FFF0"));
        htmlColours.put("HotPink".toLowerCase(), Color.decode("0xFF69B4"));
        htmlColours.put("IndianRed".toLowerCase(), Color.decode("0xCD5C5C"));
        htmlColours.put("Indigo".toLowerCase(), Color.decode("0x4B0082"));
        htmlColours.put("Ivory".toLowerCase(), Color.decode("0xFFFFF0"));
        htmlColours.put("Khaki".toLowerCase(), Color.decode("0xF0E68C"));
        htmlColours.put("Lavender".toLowerCase(), Color.decode("0xE6E6FA"));
        htmlColours.put("LavenderBlush".toLowerCase(), Color.decode("0xFFF0F5"));
        htmlColours.put("LawnGreen".toLowerCase(), Color.decode("0x7CFC00"));
        htmlColours.put("LemonChiffon".toLowerCase(), Color.decode("0xFFFACD"));
        htmlColours.put("LightBlue".toLowerCase(), Color.decode("0xADD8E6"));
        htmlColours.put("LightCoral".toLowerCase(), Color.decode("0xF08080"));
        htmlColours.put("LightCyan".toLowerCase(), Color.decode("0xE0FFFF"));
        htmlColours.put("LightGoldenRodYellow".toLowerCase(), Color.decode("0xFAFAD2"));
        htmlColours.put("LightGray".toLowerCase(), Color.decode("0xD3D3D3"));
        htmlColours.put("LightGreen".toLowerCase(), Color.decode("0x90EE90"));
        htmlColours.put("LightPink".toLowerCase(), Color.decode("0xFFB6C1"));
        htmlColours.put("LightSalmon".toLowerCase(), Color.decode("0xFFA07A"));
        htmlColours.put("LightSeaGreen".toLowerCase(), Color.decode("0x20B2AA"));
        htmlColours.put("LightSkyBlue".toLowerCase(), Color.decode("0x87CEFA"));
        htmlColours.put("LightSlateGray".toLowerCase(), Color.decode("0x778899"));
        htmlColours.put("LightSteelBlue".toLowerCase(), Color.decode("0xB0C4DE"));
        htmlColours.put("LightYellow".toLowerCase(), Color.decode("0xFFFFE0"));
        htmlColours.put("Lime".toLowerCase(), Color.decode("0x00FF00"));
        htmlColours.put("LimeGreen".toLowerCase(), Color.decode("0x32CD32"));
        htmlColours.put("Linen".toLowerCase(), Color.decode("0xFAF0E6"));
        htmlColours.put("Magenta".toLowerCase(), Color.decode("0xFF00FF"));
        htmlColours.put("Maroon".toLowerCase(), Color.decode("0x800000"));
        htmlColours.put("MediumAquaMarine".toLowerCase(), Color.decode("0x66CDAA"));
        htmlColours.put("MediumBlue".toLowerCase(), Color.decode("0x0000CD"));
        htmlColours.put("MediumOrchid".toLowerCase(), Color.decode("0xBA55D3"));
        htmlColours.put("MediumPurple".toLowerCase(), Color.decode("0x9370DB"));
        htmlColours.put("MediumSeaGreen".toLowerCase(), Color.decode("0x3CB371"));
        htmlColours.put("MediumSlateBlue".toLowerCase(), Color.decode("0x7B68EE"));
        htmlColours.put("MediumSpringGreen".toLowerCase(), Color.decode("0x00FA9A"));
        htmlColours.put("MediumTurquoise".toLowerCase(), Color.decode("0x48D1CC"));
        htmlColours.put("MediumVioletRed".toLowerCase(), Color.decode("0xC71585"));
        htmlColours.put("MidnightBlue".toLowerCase(), Color.decode("0x191970"));
        htmlColours.put("MintCream".toLowerCase(), Color.decode("0xF5FFFA"));
        htmlColours.put("MistyRose".toLowerCase(), Color.decode("0xFFE4E1"));
        htmlColours.put("Moccasin".toLowerCase(), Color.decode("0xFFE4B5"));
        htmlColours.put("NavajoWhite".toLowerCase(), Color.decode("0xFFDEAD"));
        htmlColours.put("Navy".toLowerCase(), Color.decode("0x000080"));
        htmlColours.put("OldLace".toLowerCase(), Color.decode("0xFDF5E6"));
        htmlColours.put("Olive".toLowerCase(), Color.decode("0x808000"));
        htmlColours.put("OliveDrab".toLowerCase(), Color.decode("0x6B8E23"));
        htmlColours.put("Orange".toLowerCase(), Color.decode("0xFFA500"));
        htmlColours.put("OrangeRed".toLowerCase(), Color.decode("0xFF4500"));
        htmlColours.put("Orchid".toLowerCase(), Color.decode("0xDA70D6"));
        htmlColours.put("PaleGoldenRod".toLowerCase(), Color.decode("0xEEE8AA"));
        htmlColours.put("PaleGreen".toLowerCase(), Color.decode("0x98FB98"));
        htmlColours.put("PaleTurquoise".toLowerCase(), Color.decode("0xAFEEEE"));
        htmlColours.put("PaleVioletRed".toLowerCase(), Color.decode("0xDB7093"));
        htmlColours.put("PapayaWhip".toLowerCase(), Color.decode("0xFFEFD5"));
        htmlColours.put("PeachPuff".toLowerCase(), Color.decode("0xFFDAB9"));
        htmlColours.put("Peru".toLowerCase(), Color.decode("0xCD853F"));
        htmlColours.put("Pink".toLowerCase(), Color.decode("0xFFC0CB"));
        htmlColours.put("Plum".toLowerCase(), Color.decode("0xDDA0DD"));
        htmlColours.put("PowderBlue".toLowerCase(), Color.decode("0xB0E0E6"));
        htmlColours.put("Purple".toLowerCase(), Color.decode("0x800080"));
        htmlColours.put("Red".toLowerCase(), Color.decode("0xFF0000"));
        htmlColours.put("RosyBrown".toLowerCase(), Color.decode("0xBC8F8F"));
        htmlColours.put("RoyalBlue".toLowerCase(), Color.decode("0x4169E1"));
        htmlColours.put("SaddleBrown".toLowerCase(), Color.decode("0x8B4513"));
        htmlColours.put("Salmon".toLowerCase(), Color.decode("0xFA8072"));
        htmlColours.put("SandyBrown".toLowerCase(), Color.decode("0xF4A460"));
        htmlColours.put("SeaGreen".toLowerCase(), Color.decode("0x2E8B57"));
        htmlColours.put("SeaShell".toLowerCase(), Color.decode("0xFFF5EE"));
        htmlColours.put("Sienna".toLowerCase(), Color.decode("0xA0522D"));
        htmlColours.put("Silver".toLowerCase(), Color.decode("0xC0C0C0"));
        htmlColours.put("SkyBlue".toLowerCase(), Color.decode("0x87CEEB"));
        htmlColours.put("SlateBlue".toLowerCase(), Color.decode("0x6A5ACD"));
        htmlColours.put("SlateGray".toLowerCase(), Color.decode("0x708090"));
        htmlColours.put("Snow".toLowerCase(), Color.decode("0xFFFAFA"));
        htmlColours.put("SpringGreen".toLowerCase(), Color.decode("0x00FF7F"));
        htmlColours.put("SteelBlue".toLowerCase(), Color.decode("0x4682B4"));
        htmlColours.put("Tan".toLowerCase(), Color.decode("0xD2B48C"));
        htmlColours.put("Teal".toLowerCase(), Color.decode("0x008080"));
        htmlColours.put("Thistle".toLowerCase(), Color.decode("0xD8BFD8"));
        htmlColours.put("Tomato".toLowerCase(), Color.decode("0xFF6347"));
        htmlColours.put("Turquoise".toLowerCase(), Color.decode("0x40E0D0"));
        htmlColours.put("Violet".toLowerCase(), Color.decode("0xEE82EE"));
        htmlColours.put("Wheat".toLowerCase(), Color.decode("0xF5DEB3"));
        htmlColours.put("White".toLowerCase(), Color.decode("0xFFFFFF"));
        htmlColours.put("WhiteSmoke".toLowerCase(), Color.decode("0xF5F5F5"));
        htmlColours.put("Yellow".toLowerCase(), Color.decode("0xFFFF00"));
        htmlColours.put("YellowGreen".toLowerCase(), Color.decode("0x9ACD32"));
    }

    private static Color attemptSeparated(String colour) {
        Color parsed = null;
        if (colour.contains(",")) {
            String[] split = colour.split(",");
            int red = Integer.decode(split[0]);
            int green = Integer.decode(split[1]);
            int blue = Integer.decode(split[2]);
            parsed = new Color(red, green, blue);
        }
        else if (colour.contains(" ")) {
            String[] split = colour.split(" ");
            int red = Integer.parseInt(split[0]);
            int green = Integer.parseInt(split[1]);
            int blue = Integer.parseInt(split[2]);
            parsed = new Color(red, green, blue);
        }
        else if (colour.length() == 6) {
            int red = Integer.parseInt(colour.substring(0, 2), 16);
            int green = Integer.parseInt(colour.substring(2, 4), 16);
            int blue = Integer.parseInt(colour.substring(4, 6), 16);
            parsed = new Color(red, green, blue);
        }

        return parsed;
    }

    public static Color newColourWithAlpha(Color color, double alpha) {
        Color result = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha));
        return result;
    }

    private static Color attemptHex(String colour) {
        Color resolved = null;
        if (!colour.startsWith("#")) {
            if (!colour.startsWith("0x")) {
                colour = "0x" + colour;
            }
        }

        try {
            resolved = Color.decode(colour);
        }
        catch (NumberFormatException nfe) {

        }
        return resolved;
    }

    private static Color attemptColorName(String colour) {

        Color resolved = null;
        try {
            resolved = (Color) Color.class.getField(colour).get(null);
        }
        catch (SecurityException e) {}
        catch (NoSuchFieldException e) {}
        catch (IllegalArgumentException e) {}
        catch (IllegalAccessException e) {}

        return resolved;

    }

    public static String toHex(Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String toHtmlHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

}