package com.logginghub.logging.logeventformatters;

import com.logginghub.logging.BaseFormatter;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFormatter;

public class SingleLineLogEventTextFormatter extends BaseFormatter implements LogEventFormatter {
    /**
     * Format the given LogRecord.
     * 
     * @param record
     *            the log record to be formatted.
     * @return a formatted log record
     */
    public String format(LogEvent record) {
        StringBuffer sb = new StringBuffer();

        StringBuffer text = formatDateTime(record.getOriginTime());
        sb.append(text);
        sb.append(' ');

        appendPadded(sb, record.getSourceApplication(), 15);
        sb.append(' ');

        if (record.getSourceClassName() != null) {
            appendPadded(sb, trimClassName(record.getSourceClassName()), 10);
        }
        else {
            appendPadded(sb, trimClassName(record.getLoggerName()), 10);
        }

        if (record.getSourceMethodName() != null) {
            sb.append('.');
            appendPadded(sb, record.getSourceMethodName(), 10);
            sb.append("()");
        }

        sb.append(' ');
        appendPadded(sb, record.getLevelDescription(), 10);
        sb.append(' ');

        String message = record.getMessage();
        sb.append(' ');
        sb.append(message);
        sb.append(' ');

        if (record.getFormattedException() != null) {
            sb.append('{');
            sb.append(record.getFormattedException());
            sb.append('}');
        }
        return sb.toString();
    }
}
