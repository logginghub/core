package com.logginghub.logging.utils;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.TimeUtils.TimeDetails;


public class LapCountingStopwatch
{
    private List<Stopwatch> m_laps = new ArrayList<Stopwatch>();
    public static String newline = System.getProperty("line.separator");
    private Stopwatch m_currentLap = null;

    public void start()
    {
        m_currentLap = Stopwatch.start("");
    }

    public void lap()
    {
        endCurrentLap();
        start();
    }

    private void endCurrentLap()
    {
        m_currentLap.stop();
        m_laps.add(m_currentLap);
    }

    public void stop()
    {
        endCurrentLap();
    }

    public float getAverageLapTimeNanos()
    {
        float total = 0;
        float count = m_laps.size();

        for(Stopwatch stopwatch : m_laps)
        {
            total += stopwatch.getDurationNanos();
        }

        float average = total / count;
        return average;
    }

    public static LapCountingStopwatch start(String string)
    {
        LapCountingStopwatch stopwatch = new LapCountingStopwatch();
        stopwatch.start();
        return stopwatch;
    }
    
    public String dump()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(m_laps.size() + " laps : ");
        builder.append(newline);
        
        for(Stopwatch stopwatch : m_laps)
        {
            builder.append(stopwatch.getFormattedDuration());
            builder.append(newline);
        }

        return builder.toString();
    }

    public String getFormattedAverageLapTime()
    {
        float averageLapTimeNanos = getAverageLapTimeNanos();
        TimeDetails details = TimeUtils.makeNice(averageLapTimeNanos);        
        return String.format("%.2f %s", details.getValue(), details.getAbbriviatedUnits());
    }
}
