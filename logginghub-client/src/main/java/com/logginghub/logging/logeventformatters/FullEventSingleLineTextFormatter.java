package com.logginghub.logging.logeventformatters;

import com.logginghub.logging.BaseFormatter;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFormatter;

public class FullEventSingleLineTextFormatter extends BaseFormatter implements LogEventFormatter {
    private int sourceHostLength = 15;
    private boolean trimSourceHost = false;

    private int sourceAddressLength = 15;
    private boolean trimSourceAddress = false;

    private int pidLength = 7;
    private boolean trimPid = false;

    private int sourceApplicationLength = 15;
    private boolean trimSourceApplication = false;

    private int sourceClassNameLength = 10;
    private boolean trimSourceClassName = false;

    private int sourceMethodLength = 10;
    private boolean trimSourceMethod = false;

    private int threadNameLength = 15;
    private boolean trimThreadName = false;

    private int levelLength = 10;
    private boolean trimLevel = false;

    public String format(LogEvent record) {
        StringBuffer sb = new StringBuffer();

        StringBuffer text = formatDateTime(record.getOriginTime());
        sb.append(text);
        sb.append(' ');

        if (sourceHostLength > 0) {
            appendPadded(sb, record.getSourceHost(), sourceHostLength, trimSourceHost);
            sb.append(' ');
        }

        if (sourceAddressLength > 0) {
            appendPadded(sb, record.getSourceAddress(), sourceAddressLength, trimSourceAddress);
            sb.append(' ');
        }
        
        if (pidLength > 0) {
            appendPadded(sb, Integer.toString(record.getPid()), pidLength, trimPid);
            sb.append(' ');
        }
        
        if (sourceApplicationLength > 0) {
            appendPadded(sb, record.getSourceApplication(), sourceApplicationLength, trimSourceApplication);
            sb.append(' ');
        }

        if (sourceClassNameLength > 0) {
            if (record.getSourceClassName() != null) {
                appendPadded(sb, trimClassName(record.getSourceClassName()), sourceClassNameLength, trimSourceClassName);
            }
            else {
                appendPadded(sb, trimClassName(record.getLoggerName()), sourceClassNameLength, trimSourceClassName);
            }
        }

        if (sourceMethodLength > 0) {
            if (record.getSourceMethodName() != null) {
                sb.append('.');
                appendPadded(sb, record.getSourceMethodName(), sourceMethodLength, trimSourceMethod);
                sb.append("()");
            }
        }

        if (threadNameLength > 0) {
            if (record.getThreadName() != null) {
                sb.append(' ');
                appendPadded(sb, record.getThreadName(), threadNameLength, trimThreadName);
            }
        }

        if (levelLength > 0) {
            sb.append(' ');
            appendPadded(sb, record.getLevelDescription(), levelLength, trimLevel);
            sb.append(' ');
        }

        String message = record.getMessage();
        sb.append(' ');
        sb.append(message);

        if (record.getFormattedException() != null) {
            sb.append(' ');
            sb.append('{');
            sb.append(record.getFormattedException());
            sb.append('}');
        }
        return sb.toString();
    }
}
