package com.logginghub.logging.repository;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.logginghub.utils.FileDateFormat;

/**
 * Data model that can produce 'realistic' time series data for any time.
 * @author James
 *
 */
public class StubDataModel {

    private Calendar calendar = new GregorianCalendar();
    private FileDateFormat fileDateFormat = new FileDateFormat();
    
    private long startTime = fileDateFormat.parseHelper("20100101.000000");
    private int startYear = 2010;
    
    private double[] dayOfTheWeekFactor = new double[] { -1, 0.2, 1.3, 1.2, 0.9, 1.4, 1.6, 0.4 };
    private double perYearFactor = 0.2;
    
    public double getValueForTime(long time){
        
        calendar.setTimeInMillis(time);
        
        double value;
        // Lets start things off from a sensible start date
        if(time < startTime){
            value = 0;
        }else{
            int yearsIntoSeries = calendar.get(Calendar.YEAR) - startYear;
        }
        
        return 0;
        
    }
    
}
