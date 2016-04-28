package com.logginghub.utils.logging;

import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class SingleLineStreamFormatter implements LogEventFormatter {
    private final static String format = "{0,date} {0,time,HH:mm:ss.SSS}";
    String formatString = "%s | %7s | %20.20s | %-30.30s | %10.10s | ";
    Date dat = new Date();
    private MessageFormat formatter;
    private boolean stripClass;

    private Object args[] = new Object[1];

    private String lineSeparator = (String) System.getProperty("line.separator");

    private PatternMetadataContainer patternMetadataContainer = null;

    public SingleLineStreamFormatter() {
        int levelWidth = Integer.getInteger("singleLineFormatter.levelWidth", 7);
        int threadWidth = Integer.getInteger("singleLineFormatter.threadWidth", 20);
        int methodWidth = Integer.getInteger("methodLineFormatter.levelWidth", 30);
        int threadContextWidth = Integer.getInteger("methodLineFormatter.threadContextWidth", 10);

        setWidths(levelWidth, threadWidth, methodWidth, threadContextWidth);

        if (System.getProperty("stripClass") == null) {
            stripClass = true;
        } else {
            stripClass = Boolean.getBoolean("stripClass");
        }
    }

    public void setWidths(int level, int thread, int sourceMethod, int threadContext) {
        formatString = String.format("%%s | %%%ds | %%%d.%ds | %%-%d.%ds | %%%d.%ds | ",
                                     level,
                                     thread,
                                     thread,
                                     sourceMethod,
                                     sourceMethod,
                                     threadContext,
                                     threadContext);
    }

    public void setPatternMetadataContainer(PatternMetadataContainer patternMetadataContainer) {
        this.patternMetadataContainer = patternMetadataContainer;
    }

    public void setStripClass(boolean stripClass) {
        this.stripClass = stripClass;
    }

    public boolean willStripClass() {
        return stripClass;
    }

    private String stripClass(String source) {
        int lastDot = source.lastIndexOf('.');
        if (lastDot != -1) {
            return source.substring(lastDot + 1, source.length());
        } else {
            return source;
        }
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

        String name = record.getThreadName();

        String context = record.getThreadContext();
        if (context == null) {
            context = "";
        }

        sb.append(String.format(formatString,
                                text,
                                Logger.getLevelName(record.getLevel(), false).toUpperCase(),
                                name,
                                source + "." + method,
                                context));


        List<String> lines = StringUtils.splitIntoLineList(message);
        if(lines.size() == 1) {
            sb.append(message);
        }else{
            int length = sb.length() - 2 /* for the space and pipe symbol */;
            String padding = StringUtils.repeat(" ", length);

            sb.append(lines.get(0)).append(lineSeparator);
            for(int i = 1; i < lines.size(); i++) {
                sb.append(padding).append("| ").append(lines.get(i)).append(lineSeparator);
            }
        }

        sb.append(lineSeparator);

        if (record.getThrowable() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrowable().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        }

        return sb.toString();

    }

    @Override
    public String format(EventContext event) {

        String formatted;
        if (patternMetadataContainer != null) {

            int patternId = event.getPatternId();

            PatternMetadata metadataForPattern = patternMetadataContainer.getMetadataForPattern(patternId);

            String pattern = metadataForPattern.getPattern();
            List<Type> types = metadataForPattern.getTypes();

            final ByteBuffer buffer = event.getBuffer();
            Object[] values = new Object[types.size()];
            for (int i = 0; i < values.length; i++) {

                Type type = types.get(i);

                if (type == Integer.TYPE) {
                    values[i] = buffer.getInt();
                } else if (type == Long.TYPE) {
                    values[i] = buffer.getLong();
                } else if (type == Float.TYPE) {
                    values[i] = buffer.getFloat();
                } else if (type == Boolean.TYPE) {
                    byte value = buffer.get();
                    values[i] = value == 1 ? Boolean.TRUE : Boolean.FALSE;
                } else if (type == String.class) {
                    int length = buffer.getInt();
                    byte[] data = new byte[length];
                    buffer.get(data);
                    values[i] = new String(data, Charset.forName("UTF-8"));
                } else {
                    throw new NotImplementedException("Don't know how to process type '{}'", type);
                }

            }

            LogEvent logEvent = new LogEvent();
            logEvent.setMessage(StringUtils.format(pattern, values));
            logEvent.setSourceClassName(event.getSourceClassName());
            logEvent.setSourceMethodName(event.getSourceMethodName());
            formatted = format(logEvent);
        } else {
            formatted = "??";
        }

        return formatted;
    }


}
