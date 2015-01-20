package com.logginghub.logging.frontend;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import com.logginghub.logging.LogEvent;

public class Utils {
    private static String[] m_colourHex = new String[] { "#6B8ADF", "#92B8E4", "#A0CCFF", "#F7F7FE", "#E2E2FE", "#F9F915", "#FF484C" };

    private static Color[] m_colours = null;

    static {
        int colours = m_colourHex.length;
        m_colours = new Color[colours];
        for (int i = 0; i < colours; i++) {
            m_colours[i] = Color.decode(m_colourHex[i]);
        }
    }

    private static DateFormat m_dateFormatWithMillis = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
    private static DateFormat m_dateFormat = DateFormat.getDateTimeInstance();
    private static ThreadLocal<Date> m_dates = new ThreadLocal<Date>();

    public static String formatTime(long localCreationTimeMillis) {
        Date date = getTempDate();
        date.setTime(localCreationTimeMillis);
        return m_dateFormatWithMillis.format(date);
    }

    public static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = Utils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        }
        else {
            throw new RuntimeException("Failed to create image icon for path " + path);
        }
    }

    /*
     * public static Image createImage(String path, String description) { java.net.URL imgURL =
     * Utils.class.getResource(path); if(imgURL != null) { return new ImageIcon(imgURL,
     * description); } else { System.err.println("Couldn't find file: " + path); return null; } }
     */

    private static Date getTempDate() {
        Date date = m_dates.get();
        if (date == null) {
            date = new Date();
            m_dates.set(date);
        }
        return date;
    }

    public static Color getBackgroundColourForEvent(LogEvent event) {
        int levelValue = event.getLevel();
        return getBackgroundColourForLevel(levelValue);
    }

    public static Color getBackgroundColourForLevel(int levelValue) {
        Color background = null;

        if (levelValue == Level.SEVERE.intValue()) {
            background = m_colours[6];
        }
        else if (levelValue == Level.WARNING.intValue()) {
            background = m_colours[5];
        }
        else if (levelValue == Level.CONFIG.intValue()) {
            background = m_colours[4];
        }
        else if (levelValue == Level.INFO.intValue()) {
            background = m_colours[3];
        }
        else if (levelValue == Level.FINE.intValue()) {
            background = m_colours[2];
        }
        else if (levelValue == Level.FINER.intValue()) {
            background = m_colours[1];
        }
        else if (levelValue == Level.FINEST.intValue()) {
            background = m_colours[0];
        }
        else {
            background = m_colours[3];
        }

        return background;
    }
}
