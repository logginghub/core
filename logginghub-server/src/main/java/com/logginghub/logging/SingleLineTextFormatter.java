package com.logginghub.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.logginghub.logging.BaseFormatter;

public class SingleLineTextFormatter extends Formatter
{
    

    // jshaw - because java logging doesn't have a formatter interface, we have
    // to extend Formatter, so we have to
    // use aggregation to get our shared code out of the base formatter
    private BaseFormatter baseFormatter = new BaseFormatter();

    private int threadWidth = 10;
    private int sourceMethodWidth = 20;
    
    public SingleLineTextFormatter()
    {

        Properties properties = new Properties();
        File file = new File("loggingColumnWidths.properties");
        if (file.exists())
        {
            try
            {
                properties.load(new FileInputStream(file));
                
                threadWidth = Integer.parseInt(properties.getProperty("threadWidth", "10"));
                sourceMethodWidth = Integer.parseInt(properties.getProperty("sourceMethodWidth", "10"));
                
            }
            catch (IOException e)
            {
            }            
        }
    }

    /**
     * Format the given LogRecord.
     * 
     * @param record
     *            the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record)
    {
        StringBuffer sb = new StringBuffer();

        StringBuffer text = baseFormatter.formatDateTime(record.getMillis());
        sb.append(text);
        sb.append(" ");

        baseFormatter.appendPadded(sb, record.getLevel().getLocalizedName(), 6);
        sb.append(" ");

        // TODO - jshaw : this wont work if there is a dispatch thread somewhere
        // in the logger
        // handler chain. But java logging does really crap things with the
        // threadID (some random number generated with LogRecord :/)
        baseFormatter.appendPadded(sb, Thread.currentThread().getName(), threadWidth);
        sb.append(" ");
        
        StringBuffer source = new StringBuffer();
        
        String classname = baseFormatter.getClassName(record);
        source.append(classname);

        if (record.getSourceMethodName() != null)
        {
            source.append(".").append(record.getSourceMethodName());            
        }
        
        baseFormatter.appendPadded(sb, source.toString(), sourceMethodWidth);

        sb.append(" ");

        String message = formatMessage(record);
        sb.append(" ");
        sb.append(message);
        sb.append(" ");
        sb.append(baseFormatter.lineSeparator);
        if (record.getThrown() != null)
        {
            sb.append("{");
            sb.append(baseFormatter.lineSeparator);
            try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch (Exception ex)
            {}
            sb.append("}");
            sb.append(baseFormatter.lineSeparator);
        }
        return sb.toString();
    }
}
