package com.logginghub.logging.logeventformatters;
import java.util.logging.Level;

import com.logginghub.logging.BaseFormatter;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFormatter;
import com.logginghub.logging.natives.Win32ConsoleAPI;

/**
 * This formatter doesn't return a string to output, but deals completely with
 * the output itself
 * 
 * @author admin
 * 
 */
public class WindowsColouredConsoleFormatter extends BaseFormatter implements LogEventFormatter
{
    //private JavaHowTo jht = new JavaHowTo();
    private Win32ConsoleAPI m_consoleAPI = new Win32ConsoleAPI();

    /**
     * Format the given LogRecord.
     * 
     * @param record
     *            the log record to be formatted.
     * @return a formatted log record
     */
    public String format(LogEvent record)
    {       
        m_consoleAPI.keepColors();        
        
        m_consoleAPI.setColor((short)(Win32ConsoleAPI.FOREGROUND_WHITE), (short)0);
        
        StringBuffer text = formatDateTime(record.getOriginTime());
        output(text.toString());
        
        m_consoleAPI.setColor((short)(Win32ConsoleAPI.FOREGROUND_GREY), (short)0);
        
        output(record.getSourceHost().toString());
        
        output(record.getSourceApplication());

        output(record.getThreadName());
        
        String a;

        if(record.getSourceClassName() != null)
        {
            a = trimClassName(record.getSourceClassName());
        }
        else
        {
            a = trimClassName(record.getLoggerName());
        }

        output(a);

        if(record.getSourceMethodName() != null)
        {
            output(record.getSourceMethodName());
        }

        output(record.getLevelDescription());
        
        setColourForLevel(record);
        
        output(record.getMessage());

        if(record.getFormattedException() != null)
        {
            output(record.getFormattedException());
        }

        String[] formattedObject = record.getFormattedObject();
        if(formattedObject != null)
        {
            int i = 0;
            outputLine("");
            for(String string : formattedObject)
            {                
                outputLine("Object " + i);
                outputLine("{");
                outputLine(string);
                outputLine("}");
                i++;
            }
        }
        
        //System.out.println();
        m_consoleAPI.restoreColors();
        
        return "";

        // return sb.toString();
    }

    private void setColourForLevel(LogEvent record)
    {
        if(record.getJavaLevel() == Level.SEVERE)
        {
            m_consoleAPI.setColor((short)(m_consoleAPI.FOREGROUND_LIGHT_RED), (short)0);
        }
        else if(record.getJavaLevel() == Level.WARNING)
        {
            m_consoleAPI.setColor((short)(m_consoleAPI.FOREGROUND_LIGHT_YELLOW), (short)0);
        }
        else if(record.getJavaLevel() == Level.CONFIG)
        {
            m_consoleAPI.setColor((short)(m_consoleAPI.FOREGROUND_WHITE), (short)0);
        }        
        else if(record.getJavaLevel() == Level.INFO)
        {
            m_consoleAPI.setColor((short)(m_consoleAPI.FOREGROUND_LIGHT_GREEN), (short)0);
        }
        else if(record.getJavaLevel() == Level.FINE)
        {
            m_consoleAPI.setColor((short)(m_consoleAPI.FOREGROUND_LIGHT_AQUA), (short)0);
        }
        else if(record.getJavaLevel() == Level.FINER)
        {
            m_consoleAPI.setColor((short)(m_consoleAPI.FOREGROUND_LIGHT_BLUE), (short)0);
        }
        else if(record.getJavaLevel() == Level.FINEST)
        {
            m_consoleAPI.setColor((short)(m_consoleAPI.FOREGROUND_GREY), (short)0);
        }
    }

    private void output(String text)
    {
        System.out.print(text);
        System.out.print(' ');
    }
    
    private void outputLine(String text)
    {
        System.out.println(text);
    }
}
