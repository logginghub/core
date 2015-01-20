package com.logginghub.logging.logeventformatters.log4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;

import com.logginghub.logging.LogEvent;

public class Log4jPatternParser {

    private static long startTime = System.currentTimeMillis();

    private static final char ESCAPE_CHAR = '%';

    private static final int LITERAL_STATE = 0;
    private static final int CONVERTER_STATE = 1;
    private static final int MINUS_STATE = 2;
    private static final int DOT_STATE = 3;
    private static final int MIN_STATE = 4;
    private static final int MAX_STATE = 5;

    static final int FULL_LOCATION_CONVERTER = 1000;
    static final int METHOD_LOCATION_CONVERTER = 1001;
    static final int CLASS_LOCATION_CONVERTER = 1002;
    static final int LINE_LOCATION_CONVERTER = 1003;
    static final int FILE_LOCATION_CONVERTER = 1004;

    static final int SOURCE_APPLICATION = 1005;
    static final int SOURCE_HOST = 1006;
    static final int SOURCE_ADDRESS = 1007;

    static final int RELATIVE_TIME_CONVERTER = 2000;
    static final int THREAD_CONVERTER = 2001;
    static final int LEVEL_CONVERTER = 2002;
    static final int NDC_CONVERTER = 2003;
    static final int MESSAGE_CONVERTER = 2004;

    int state;
    protected StringBuffer currentLiteral = new StringBuffer(32);
    protected int patternLength;
    protected int i;
    Log4jPatternConvertor head;
    Log4jPatternConvertor tail;
    protected Log4jFormattingInfo formattingInfo = new Log4jFormattingInfo();
    protected String pattern;

    public Log4jPatternParser(String pattern) {
        this.pattern = pattern;
        patternLength = pattern.length();
        state = LITERAL_STATE;
    }

    private void addToList(Log4jPatternConvertor pc) {
        if (head == null) {
            head = tail = pc;
        }
        else {
            tail.next = pc;
            tail = pc;
        }
    }

    protected String extractOption() {
        if ((i < patternLength) && (pattern.charAt(i) == '{')) {
            int end = pattern.indexOf('}', i);
            if (end > i) {
                String r = pattern.substring(i + 1, end);
                i = end + 1;
                return r;
            }
        }
        return null;
    }

    /**
     * The option is expected to be in decimal and positive. In case of error,
     * zero is returned.
     */
    protected int extractPrecisionOption() {
        String opt = extractOption();
        int r = 0;
        if (opt != null) {
            try {
                r = Integer.parseInt(opt);
                if (r <= 0) {
                    LogLog.error("Precision option (" + opt + ") isn't a positive integer.");
                    r = 0;
                }
            }
            catch (NumberFormatException e) {
                LogLog.error("Category option \"" + opt + "\" not a decimal integer.", e);
            }
        }
        return r;
    }

    public Log4jPatternConvertor parse() {
        char c;
        i = 0;
        while (i < patternLength) {
            c = pattern.charAt(i++);
            switch (state) {
                case LITERAL_STATE:
                    // In literal state, the last char is always a literal.
                    if (i == patternLength) {
                        currentLiteral.append(c);
                        continue;
                    }
                    if (c == ESCAPE_CHAR) {
                        // peek at the next char.
                        switch (pattern.charAt(i)) {
                            case ESCAPE_CHAR:
                                currentLiteral.append(c);
                                i++; // move pointer
                                break;
                            case 'n':
                                currentLiteral.append(Layout.LINE_SEP);
                                i++; // move pointer
                                break;
                            default:
                                if (currentLiteral.length() != 0) {
                                    addToList(new LiteralPatternConverter(currentLiteral.toString()));
                                    // LogLog.debug("Parsed LITERAL converter: \""
                                    // +currentLiteral+"\".");
                                }
                                currentLiteral.setLength(0);
                                currentLiteral.append(c); // append %
                                state = CONVERTER_STATE;
                                formattingInfo.reset();
                        }
                    }
                    else {
                        currentLiteral.append(c);
                    }
                    break;
                case CONVERTER_STATE:
                    currentLiteral.append(c);
                    switch (c) {
                        case '-':
                            formattingInfo.leftAlign = true;
                            break;
                        case '.':
                            state = DOT_STATE;
                            break;
                        default:
                            if (c >= '0' && c <= '9') {
                                formattingInfo.min = c - '0';
                                state = MIN_STATE;
                            }
                            else finalizeConverter(c);
                    } // switch
                    break;
                case MIN_STATE:
                    currentLiteral.append(c);
                    if (c >= '0' && c <= '9') formattingInfo.min = formattingInfo.min * 10 + (c - '0');
                    else if (c == '.') state = DOT_STATE;
                    else {
                        finalizeConverter(c);
                    }
                    break;
                case DOT_STATE:
                    currentLiteral.append(c);
                    if (c >= '0' && c <= '9') {
                        formattingInfo.max = c - '0';
                        state = MAX_STATE;
                    }
                    else {
                        LogLog.error("Error occured in position " + i + ".\n Was expecting digit, instead got char \"" + c + "\".");
                        state = LITERAL_STATE;
                    }
                    break;
                case MAX_STATE:
                    currentLiteral.append(c);
                    if (c >= '0' && c <= '9') formattingInfo.max = formattingInfo.max * 10 + (c - '0');
                    else {
                        finalizeConverter(c);
                        state = LITERAL_STATE;
                    }
                    break;
            } // switch
        } // while
        if (currentLiteral.length() != 0) {
            addToList(new LiteralPatternConverter(currentLiteral.toString()));
            // LogLog.debug("Parsed LITERAL converter: \""+currentLiteral+"\".");
        }
        return head;
    }

    protected void finalizeConverter(char c) {
        Log4jPatternConvertor pc = null;
        switch (c) {
            case 'c':
                pc = new CategoryPatternConverter(formattingInfo, extractPrecisionOption());
                currentLiteral.setLength(0);
                break;
            case 'C':
                pc = new ClassNamePatternConverter(formattingInfo, extractPrecisionOption());
                currentLiteral.setLength(0);
                break;
            case 'd':
                String dateFormatStr = AbsoluteTimeDateFormat.ISO8601_DATE_FORMAT;
                DateFormat df;
                String dOpt = extractOption();
                if (dOpt != null) dateFormatStr = dOpt;

                if (dateFormatStr.equalsIgnoreCase(AbsoluteTimeDateFormat.ISO8601_DATE_FORMAT)) df = new ISO8601DateFormat();
                else if (dateFormatStr.equalsIgnoreCase(AbsoluteTimeDateFormat.ABS_TIME_DATE_FORMAT)) df = new AbsoluteTimeDateFormat();
                else if (dateFormatStr.equalsIgnoreCase(AbsoluteTimeDateFormat.DATE_AND_TIME_DATE_FORMAT)) df = new DateTimeDateFormat();
                else {
                    try {
                        df = new SimpleDateFormat(dateFormatStr);
                    }
                    catch (IllegalArgumentException e) {
                        LogLog.error("Could not instantiate SimpleDateFormat with " + dateFormatStr, e);
                        df = (DateFormat) OptionConverter.instantiateByClassName("org.apache.log4j.helpers.ISO8601DateFormat", DateFormat.class, null);
                    }
                }
                pc = new DatePatternConverter(formattingInfo, df);
                currentLiteral.setLength(0);
                break;
            case 'F':
                pc = new LocationPatternConverter(formattingInfo, FILE_LOCATION_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 'l':
                pc = new LocationPatternConverter(formattingInfo, FULL_LOCATION_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 'L':
                pc = new LocationPatternConverter(formattingInfo, LINE_LOCATION_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 'm':
                pc = new BasicPatternConverter(formattingInfo, MESSAGE_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 'M':
                pc = new LocationPatternConverter(formattingInfo, METHOD_LOCATION_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 'p':
                pc = new BasicPatternConverter(formattingInfo, LEVEL_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 'r':
                pc = new BasicPatternConverter(formattingInfo, RELATIVE_TIME_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 't':
                pc = new BasicPatternConverter(formattingInfo, THREAD_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 'x':
                pc = new BasicPatternConverter(formattingInfo, NDC_CONVERTER);
                currentLiteral.setLength(0);
                break;
            case 'a':
                pc = new BasicPatternConverter(formattingInfo, SOURCE_APPLICATION);
                currentLiteral.setLength(0);
                break;
            case 'h':
                pc = new BasicPatternConverter(formattingInfo, SOURCE_HOST);
                currentLiteral.setLength(0);
                break;
            case 'i':
                pc = new BasicPatternConverter(formattingInfo, SOURCE_ADDRESS);
                currentLiteral.setLength(0);
                break;
            case 'X':
                String xOpt = extractOption();
                pc = new MDCPatternConverter(formattingInfo, xOpt);
                currentLiteral.setLength(0);
                break;
            default:
                LogLog.error("Unexpected char [" + c + "] at position " + i + " in conversion patterrn.");
                pc = new LiteralPatternConverter(currentLiteral.toString());
                currentLiteral.setLength(0);
        }

        addConverter(pc);
    }

    protected void addConverter(Log4jPatternConvertor pc) {
        currentLiteral.setLength(0);
        addToList(pc);
        state = LITERAL_STATE;
        formattingInfo.reset();
    }

    // ---------------------------------------------------------------------
    // PatternConverters
    // ---------------------------------------------------------------------

    private static class BasicPatternConverter extends Log4jPatternConvertor {
        int type;

        BasicPatternConverter(Log4jFormattingInfo formattingInfo, int type) {
            super(formattingInfo);
            this.type = type;
        }

        public String convert(LogEvent event) {
            switch (type) {
                case RELATIVE_TIME_CONVERTER: {
                    return (Long.toString(event.getOriginTime() - startTime));
                }
                case THREAD_CONVERTER: {
                    return event.getThreadName();
                }
                case LEVEL_CONVERTER: {
                    return event.getLevelDescription();
                }
                case NDC_CONVERTER: {
                    return getNDC(event);
                }
                case MESSAGE_CONVERTER: {
                    return event.getMessage();
                }
                case SOURCE_APPLICATION: {
                    return event.getSourceApplication();
                }
                case SOURCE_HOST: {
                    return event.getSourceHost();
                }
                case SOURCE_ADDRESS: {
                    return event.getSourceAddress();
                }

                default:
                    return null;
            }
        }

    }

    private static class LiteralPatternConverter extends Log4jPatternConvertor {
        private String literal;

        LiteralPatternConverter(String value) {
            literal = value;
        }

        public final void format(StringBuffer sbuf, LogEvent event) {
            sbuf.append(literal);
        }

        public String convert(LogEvent event) {
            return literal;
        }
    }

    private static class DatePatternConverter extends Log4jPatternConvertor {
        private DateFormat df;
        private Date date;

        DatePatternConverter(Log4jFormattingInfo formattingInfo, DateFormat df) {
            super(formattingInfo);
            date = new Date();
            this.df = df;
        }

        public String convert(LogEvent event) {
            date.setTime(event.getOriginTime());
            String converted = null;
            try {
                converted = df.format(date);
            }
            catch (Exception ex) {
                LogLog.error("Error occured while converting date.", ex);
            }
            return converted;
        }
    }

    private static class MDCPatternConverter extends Log4jPatternConvertor {
        private String key;

        MDCPatternConverter(Log4jFormattingInfo formattingInfo, String key) {
            super(formattingInfo);
            this.key = key;
        }

        public String convert(LogEvent event) {
            String mdc;

            if (event.getFormattedObject() == null) {
                mdc = "";
            }
            else {
                mdc = Arrays.toString(event.getFormattedObject());
            }
            return mdc;
        }
    }

    private class LocationPatternConverter extends Log4jPatternConvertor {
        int type;

        LocationPatternConverter(Log4jFormattingInfo formattingInfo, int type) {
            super(formattingInfo);
            this.type = type;
        }

        public String convert(LogEvent event) {
            switch (type) {
                case FULL_LOCATION_CONVERTER: {
                    return event.getSourceClassName() + "::" + event.getSourceMethodName();
                }
                case METHOD_LOCATION_CONVERTER: {
                    return event.getSourceMethodName();
                }
                case LINE_LOCATION_CONVERTER: {
                    return "<line number not supported>";
                }
                case FILE_LOCATION_CONVERTER: {
                    return "<file name not supported>";
                }
                default:
                    return null;
            }
        }
    }

    private static abstract class NamedPatternConverter extends Log4jPatternConvertor {
        int precision;

        NamedPatternConverter(Log4jFormattingInfo formattingInfo, int precision) {
            super(formattingInfo);
            this.precision = precision;
        }

        abstract String getFullyQualifiedName(LogEvent event);

        public String convert(LogEvent event) {
            String n = getFullyQualifiedName(event);
            if (precision <= 0) return n;
            else {
                int len = n.length();

                // We substract 1 from 'len' when assigning to 'end' to avoid
                // out of
                // bounds exception in return r.substring(end+1, len). This can
                // happen if
                // precision is 1 and the category name ends with a dot.
                int end = len - 1;
                for (int i = precision; i > 0; i--) {
                    end = n.lastIndexOf('.', end - 1);
                    if (end == -1) return n;
                }
                return n.substring(end + 1, len);
            }
        }
    }

    private class ClassNamePatternConverter extends NamedPatternConverter {

        ClassNamePatternConverter(Log4jFormattingInfo formattingInfo, int precision) {
            super(formattingInfo, precision);
        }

        String getFullyQualifiedName(LogEvent event) {
            return event.getSourceClassName();
        }
    }

    private class CategoryPatternConverter extends NamedPatternConverter {

        CategoryPatternConverter(Log4jFormattingInfo formattingInfo, int precision) {
            super(formattingInfo, precision);
        }

        String getFullyQualifiedName(LogEvent event) {
            return event.getLoggerName();
        }
    }

    private static String getNDC(LogEvent event) {
        if (event.getFormattedObject() != null) {
            return Arrays.toString(event.getFormattedObject());
        }
        else {
            return "";
        }
    }
}
