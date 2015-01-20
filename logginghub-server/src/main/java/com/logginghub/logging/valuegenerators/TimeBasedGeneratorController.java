package com.logginghub.logging.valuegenerators;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.ValueGenerator;

/**
 * If you want your value generators to generate values on a fixed schedule,
 * add them to an instance of the TimeBasedGeneratorController. Its timer
 * will call the generateValue method on each ValueGenerator at the interval
 * specific in the constructor. This makes each generator produce its value,
 * notify its listeners and then reset, ready for the next time interval.
 * @author James
 */
public class TimeBasedGeneratorController
{
    private Timer m_timer;
    private List<ValueGenerator<?>> m_generators = new CopyOnWriteArrayList<ValueGenerator<?>>();

    public TimeBasedGeneratorController()
    {
        this(1000);
    }
    
    public TimeBasedGeneratorController(long interval)
    {
        m_timer = new Timer("PerSecondValueGenerator", true);
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                generate();
            }
        }, interval, interval);
    }

    protected void generate()
    {
        List<ValueGenerator<?>> generators = m_generators;
        for(ValueGenerator<?> generator : generators)
        {
            generator.generateValue();
        }
    }

    public void addValueGenerator(final ValueGenerator<?> generator)
    {
        m_generators.add(generator);
    }

    public void removeValueGenerator(final ValueGenerator<?> generator)
    {
        m_generators.remove(generator);
    }
}
