package com.logginghub.logging.valuegenerators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;

/**
 * Produces average values from the contents of the log event message. A regular
 * expression with at least one matching group is provided, which is matched
 * against the message field of each log event. If the message matches the
 * expression, the string value of the matching group provided at construction
 * time is converted into a floating point number. This is then added to a total
 * and the count increment, so future calls to getValue will return the average
 * of these values.
 * 
 * @author James
 */
public class RegexAverageValueGenerator extends AbstractValueGenerator<Float> implements
        LogEventListener
{
    private String m_regex;
    private Pattern m_pattern;
    private int m_group;
    private float m_total;
    private int m_count;

    public RegexAverageValueGenerator(String regex, int group)
    {
        m_regex = regex;
        m_pattern = Pattern.compile(regex);
        m_group = group;
    }

    public void onNewLogEvent(LogEvent event)
    {
        String message = event.getMessage();

        Matcher matcher = m_pattern.matcher(message);
        if(matcher.matches())
        {
            String string = matcher.group(m_group);

            float f = Float.parseFloat(string);

            m_total += f;
            m_count++;
        }
    }

    public void reset()
    {
        m_total = 0;
        m_count = 0;
    }

    public Float getValue()
    {
        float average = m_total / (float) m_count;
        return average;
    }
}