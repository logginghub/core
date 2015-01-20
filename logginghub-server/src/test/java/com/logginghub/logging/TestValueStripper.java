package com.logginghub.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.logginghub.logging.ValueStripper;

public class TestValueStripper
{
    @Test
    public void test1()
    {
        String string = "Order 1233432 processed in 2,345 ms";

        ValueStripper stripper = new ValueStripper();

        String regex = stripper.getRegex(string);

        assertEquals("Order [0-9,\\.]+ processed in [0-9,\\.]+ ms", regex);
        assertTrue(stripper.matches(string, regex));

        String replacementString = stripper.getReplacementString(string);
        assertEquals("Order $1 processed in $2 ms", replacementString);
        
        String[] values = stripper.getValues(string, replacementString);

        assertEquals(2, values.length);
        assertEquals("1233432", values[0]);
        assertEquals("2,345", values[1]);
    }
    
    @Test
    public void testComma()
    {
        String string = "Order processed, and it was really, really quick.";

        String regex = ValueStripper.getRegex(string);
        assertEquals(string, regex);
        assertTrue(ValueStripper.matches(string, regex));

        String replacementString = ValueStripper.getReplacementString(string);
        assertEquals(string, replacementString);
    }
    
    
    @Test
    public void test2()
    {
        String[] string = new String[]
        {
            "Order processed, status changed from [new] to [accepted] in 243 ms",
            "Order processed, status changed from [accepted] to [dispatched] in 2,343 ms",
            "Something totally different",
            "Order processed, but was rejected in 2,343 ms",
            "Order processed, status changed from [new] to [rejected] in 345 ms",
        };

        ValueStripper stripper = new ValueStripper();

        stripper.process(string[0]);
        stripper.process(string[1]);
        stripper.process(string[2]);
        stripper.process(string[3]);
        stripper.process(string[4]);

        String[] replacementStrings = stripper.getCurrentReplacementStrings();

        assertEquals(1, replacementStrings.length);
        assertEquals("Order processed, status changed from $1 to $2 in $3 ms",
                     replacementStrings[0]);

    }
}
