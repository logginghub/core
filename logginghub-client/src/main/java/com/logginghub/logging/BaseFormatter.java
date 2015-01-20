package com.logginghub.logging;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.LogRecord;

public class BaseFormatter {
    private Date date = new Date();
    private final static String format = "{0,date} {0,time,HH:mm:ss.SSS}";
    private MessageFormat messageFormat;

    private Object args[] = new Object[1];

    protected String lineSeparator = System.getProperty("line.separator");
    private boolean trimClassNames = true;

    public BaseFormatter() {
        messageFormat = new MessageFormat(format);
    }

    public void setTrimClassNames(boolean trimClassNames) {
        this.trimClassNames = trimClassNames;
    }

    public boolean getTrimClassNames() {
        return trimClassNames;
    }

    protected void appendPadded(StringBuffer buffer, String toAppend, int width) {
        this.appendPadded(buffer, toAppend, width, false);
    }

    protected void appendPadded(StringBuffer buffer, String toAppend, int width, boolean trimColumns) {
        if (toAppend != null) {
            boolean tooLongToFit = toAppend.length() > width;
            if (tooLongToFit && trimColumns) {
                // Trim it
                buffer.append(toAppend.substring(0, width));
            }
            else if (tooLongToFit && !trimColumns) {
                // Stick it in all together
                buffer.append(toAppend);
            }
            else {
                // Pad it out
                buffer.append(String.format("%-" + width + "s", toAppend));
            }
        }
        else {
            // Fill with blanks
            buffer.append(String.format("%-" + width + "s", ""));
        }
    }

    protected String getClassName(LogRecord record) {
        String classname;

        if (record.getSourceClassName() != null) {
            classname = record.getSourceClassName();
        }
        else {
            classname = record.getLoggerName();
        }

        if (trimClassNames) {
            classname = trimClassName(classname);
        }

        // classname = String.format("%-30s", classname);
        return classname;
    }

    protected String trimClassName(String sourceClassName) {
        String trimmed;

        if (sourceClassName != null) {

            int index = sourceClassName.lastIndexOf('.');
            if (index == -1) {
                trimmed = sourceClassName;
            }
            else {
                trimmed = sourceClassName.substring(index + 1, sourceClassName.length());
            }
        }
        else {
            trimmed = "";
        }

        return trimmed;
    }

    public StringBuffer formatDateTime(long time) {
        date.setTime(time);
        args[0] = date;
        StringBuffer text = new StringBuffer();

        messageFormat.format(args, text, null);
        return text;
    }

    /**
     * Localize and format the message string from a log record. This method is
     * provided as a convenience for Formatter subclasses to use when they are
     * performing formatting.
     * <p>
     * The message string is first localized to a format string using the
     * record's ResourceBundle. (If there is no ResourceBundle, or if the
     * message key is not found, then the key is used as the format string.) The
     * format String uses java.text style formatting.
     * <ul>
     * <li>If there are no parameters, no formatter is used.
     * <li>Otherwise, if the string contains "{0" then java.text.MessageFormat
     * is used to format the string.
     * <li>Otherwise no formatting is performed.
     * </ul>
     * <p>
     * 
     * @param record
     *            the log record containing the raw message
     * @return a localized and formatted message
     */
    public synchronized String formatMessage(LogRecord record) {
        String format = record.getMessage();
        java.util.ResourceBundle catalog = record.getResourceBundle();
        if (catalog != null) {
            // // We cache catalog lookups. This is mostly to avoid the
            // // cost of exceptions for keys that are not in the catalog.
            // if (catalogCache == null) {
            // catalogCache = new HashMap();
            // }
            // format = (String)catalogCache.get(record.essage);
            // if (format == null) {
            try {
                format = catalog.getString(record.getMessage());
            }
            catch (java.util.MissingResourceException ex) {
                // Drop through. Use record message as format
                format = record.getMessage();
            }
            // catalogCache.put(record.message, format);
            // }
        }
        // Do the formatting.
        try {
            Object parameters[] = record.getParameters();
            if (parameters == null || parameters.length == 0) {
                // No parameters. Just return format string.
                return format;
            }
            // Is is a java.text style format?
            // Ideally we could match with
            // Pattern.compile("\\{\\d").matcher(format).find())
            // However the cost is 14% higher, so we cheaply check for
            // 1 of the first 4 parameters
            if (format.indexOf("{0") >= 0 || format.indexOf("{1") >= 0 || format.indexOf("{2") >= 0 || format.indexOf("{3") >= 0) {
                return java.text.MessageFormat.format(format, parameters);
            }
            return format;

        }
        catch (Exception ex) {
            // Formatting failed: use localized format string.
            return format;
        }
    }
}
