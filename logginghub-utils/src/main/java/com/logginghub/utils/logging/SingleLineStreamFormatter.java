package com.logginghub.utils.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Level;

public class SingleLineStreamFormatter implements LogEventFormatter {
    String formatString = "%s | %7s | %20.20s | %-30.30s | %10.10s | %s";

    Date dat = new Date();
    private final static String format = "{0,date} {0,time,HH:mm:ss.SSS}";
    private MessageFormat formatter;
    private boolean stripClass;

    private Object args[] = new Object[1];

    private String lineSeparator = (String) System.getProperty("line.separator");

    public SingleLineStreamFormatter() {
        int levelWidth = Integer.getInteger("singleLineFormatter.levelWidth", 7);
        int threadWidth = Integer.getInteger("singleLineFormatter.threadWidth", 20);
        int methodWidth = Integer.getInteger("methodLineFormatter.levelWidth", 30);
        int threadContextWidth = Integer.getInteger("methodLineFormatter.threadContextWidth", 10);

        setWidths(levelWidth, threadWidth, methodWidth, threadContextWidth);

        if (System.getProperty("stripClass") == null) {
            stripClass = true;
        }
        else {
            stripClass = Boolean.getBoolean("stripClass");
        }
    }

    public void setWidths(int level, int thread, int sourceMethod, int threadContext) {
        formatString = String.format("%%s | %%%ds | %%%d.%ds | %%-%d.%ds | %%%d.%ds | %%s", level, thread, thread, sourceMethod, sourceMethod, threadContext, threadContext);
    }

    private String stripClass(String source) {
        int lastDot = source.lastIndexOf('.');
        if (lastDot != -1) {
            return source.substring(lastDot + 1, source.length());
        }
        else {
            return source;
        }
    }

    public void setStripClass(boolean stripClass) {
        this.stripClass = stripClass;
    }

    public boolean willStripClass() {
        return stripClass;
    }

    public String format(LogEvent record) {
        StringBuilder sb = new StringBuilder();

        dat.setTime(record.getOriginTime());
        args[0] = dat;
        StringBuffer text = new StringBuffer();
        if (formatter == null) {
            formatter = new MessageFormat(format);
        }
        formatter.format(args, text, null);
        String message = record.getMessage();

        String source;

        source = record.getSourceClassName();

        if (stripClass) {
            source = stripClass(source);
        }

        String method = "";
        if (record.getSourceMethodName() != null) {
            method = record.getSourceMethodName();
        }

        // Bare-faced assumption that logging is being written synchronously
        String name = record.getThreadName();

        String context = record.getThreadContext();
        if(context == null) {
            context = "";
        }

        sb.append(String.format(formatString, text, Level.parse("" + record.getLevel()), name, source + "." + method, context, message));

        sb.append(lineSeparator);

        if (record.getThrowable() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrowable().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch (Exception ex) {}
        }

        // if (record.getParameters() != null) {
        // Object[] parameters = record.getParameters();
        // for (Object object : parameters) {
        // sb.append("{").append(lineSeparator);
        // sb.append(object.toString());
        // sb.append("}").append(lineSeparator);
        // }
        // }

        return sb.toString();

    }
}
