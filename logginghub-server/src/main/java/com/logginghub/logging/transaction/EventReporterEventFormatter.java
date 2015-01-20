package com.logginghub.logging.transaction;

import com.logginghub.logging.BaseFormatter;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFormatter;

public class EventReporterEventFormatter extends BaseFormatter implements LogEventFormatter {
    private int sourceHostLength = 15;
    private boolean trimSourceHost = false;

    private int sourceAddressLength = 15;
    private boolean trimSourceAddress = false;

    private int pidLength = 7;
    private boolean trimPid = false;

    private int sourceApplicationLength = 15;
    private boolean trimSourceApplication = false;

    private int threadNameLength = 18;
    private boolean trimThreadName = false;

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

        if (threadNameLength > 0) {
            if (record.getThreadName() != null) {
                sb.append(' ');
                appendPadded(sb, record.getThreadName(), threadNameLength, trimThreadName);
            }
        }

        String message = record.getMessage();
        sb.append(' ');
        sb.append(message);
        
        return sb.toString();
    }
}
