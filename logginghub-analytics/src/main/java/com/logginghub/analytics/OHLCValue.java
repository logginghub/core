package com.logginghub.analytics;

import com.logginghub.utils.SinglePassStatisticsDoublePrecision;
import com.logginghub.utils.logging.Logger;

public class OHLCValue {

    public long time;
    public double open = Double.NaN;
    public double high = Double.NaN;
    public double low = Double.NaN;
    public double close = Double.NaN;
    public double total;
    public int count;
    public SinglePassStatisticsDoublePrecision stats;

    public OHLCValue(long time, double open, double high, double low, double close) {
        super();
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public OHLCValue() {}

    @Override public String toString() {
        return "OHLCValue [time=" + Logger.toDateString(time) + ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close + "]";
    }

    public double getMean() {
        return total / count;
    }

    public void update(double value) {

        if (Double.isNaN(open)) {
            open = value;
        }

        close = value;

        if (Double.isNaN(high)) {
            high = value;
        }
        else {
            high = Math.max(high, value);
        }
        
        if (Double.isNaN(low)) {
            low = value;
        }
        else {
            low = Math.min(low, value);
        }


    }

}
