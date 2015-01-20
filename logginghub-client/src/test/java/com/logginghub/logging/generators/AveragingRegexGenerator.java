package com.logginghub.logging.generators;


import java.util.Timer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;



public class AveragingRegexGenerator extends AbstractGenerator implements TimeBasedGenerator
{
    private String m_regex;
    private Pattern m_pattern;
    private int m_count;
    private String m_message;
    private long m_interval;
    private float m_total;

    public AveragingRegexGenerator()
    {

    }

    public AveragingRegexGenerator(String regex, String message, Timer timer, long interval)
    {
        setRegex(regex);
        setMessage(message);
        setInterval(interval);
    }

    public void setInterval(long interval)
    {
        m_interval = interval;
    }

    public String getRegex()
    {
        return m_regex;
    }

    public void setRegex(String regex)
    {
        m_regex = regex;
        m_pattern = Pattern.compile(m_regex);
    }

    public Pattern getPattern()
    {
        return m_pattern;
    }

    public void setPattern(Pattern pattern)
    {
        m_pattern = pattern;
    }

    public int getCount()
    {
        return m_count;
    }

    public String getMessage()
    {
        return m_message;
    }

    public void setMessage(String message)
    {
        m_message = message;
    }

    public void createNewEvent()
    {
        float average;

        if(m_count > 0)
        {
            average = m_total / m_count;
        }
        else           
        {
            average = 0;
        }

        String result = String.format(m_message, average);

        DefaultLogEvent event = new DefaultLogEvent();

        event.setLevel(Level.INFO.intValue());
        event.setFormattedException(null);
        event.setFormattedObject(null);
        event.setLocalCreationTimeMillis(System.currentTimeMillis());
        event.setLoggerName("");
        event.setMessage(result);
        event.setSequenceNumber(0);
        event.setSourceApplication("");
        event.setSourceClassName("");
        // event.setSourceHost("");
        event.setSourceMethodName("");
        event.setThreadName("");

        event.setMessage(result);
        fireNewLogEvent(event);
        m_count = 0;
        m_total = 0;
    }

    @Override
    public void onNewLogEvent(LogEvent event)
    {
        String message = event.getMessage();

        Matcher matcher = m_pattern.matcher(message);
        if(matcher.matches())
        {
            String group = matcher.group(1);

            float value = Float.parseFloat(group);
            m_total += value;
            m_count++;
        }
    }

    public long getInterval()
    {
        return m_interval;
    }

    public void onTimerFired()
    {
        createNewEvent();
    }
}
