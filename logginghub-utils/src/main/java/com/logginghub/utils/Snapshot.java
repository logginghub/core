package com.logginghub.utils;

public class Snapshot
{
    private double lastSnapshotValue = 0;
    private double currentValue = 0;
    private double totalValue = 0;
    private long snapshotTime = 0;
    private long snapshotTimeDelta = 0;
    private double target = 0;
    
    private MovingAverage snapshotValuesMA = new MovingAverage(5);

    public Snapshot()
    {
        snapshotTime = System.currentTimeMillis();
    }

    public void update(double t)
    {
        lastSnapshotValue = currentValue;
        currentValue = t;
    }

    public boolean hasChanged()
    {
        return lastSnapshotValue > 0;
    }

    public void increment()
    {
        currentValue++;
    }

    public void snapshot()
    {
        lastSnapshotValue = currentValue;
        snapshotValuesMA.addValue(currentValue);
        
        totalValue += currentValue;

        long timeNow = System.currentTimeMillis();

        snapshotTimeDelta = timeNow - snapshotTime;
        snapshotTime = timeNow;

        currentValue = 0;
    }

    public double getTotal()
    {
        return totalValue;
    }

    public double getRate()
    {
        return lastSnapshotValue / ((double) snapshotTimeDelta / 1000f);
    }
    
    public double getRateMA()
    {
        return snapshotValuesMA.calculateMovingAverage();
    }

    public double getSnapshotValue()
    {
        return lastSnapshotValue;
    }

    public void increment(int bytesTransfered)
    {
        currentValue += bytesTransfered;
    }

    public void setTarget(double target)
    {
        this.target = target;
    }

    public double getPercentageComplete()
    {
        return 100.0 * totalValue / target;
    }

    public double getRemaining()
    {
        return target - totalValue;
    }
    
    public double getSecondsRemaining()
    {
        return getRemaining() / getRate();
    }
}
