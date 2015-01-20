package com.logginghub.logging.utils;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Benchmarker;
import com.logginghub.utils.Benchmarker.Approach;

public class ValueStripperBenchmark {

    private static LogEvent eventMatches = LogEventBuilder.start().setMessage("Value one 123 value two 111 and finally value three 2234234").toLogEvent();
    private static LogEvent eventNotMatchesStart = LogEventBuilder.start().setMessage("Value foo one 123 value two 111 and finally value three 2234234").toLogEvent();
    private static LogEvent eventNotMatchesEnd = LogEventBuilder.start().setMessage("Value one 123 value two 111 and finally value three 2234234 foo").toLogEvent();
    
    public static class WithStartsWithMatching extends Approach {
        private ValueStripper2 stripper = new ValueStripper2("Value one {valueOne} value two {valueTwo} and finally value three {valueThree}");
        @Override public void iterate() throws Exception {
            stripper.onNewLogEvent(eventMatches);
        }
    }
    
    public static class WithStartsWithNotMatchingStart extends Approach {
        private ValueStripper2 stripper = new ValueStripper2("Value one {valueOne} value two {valueTwo} and finally value three {valueThree}");
        @Override public void iterate() throws Exception {
            stripper.onNewLogEvent(eventNotMatchesStart);
        }
    }
    
    public static class WithStartsWithNotMatchingEnd extends Approach {
        private ValueStripper2 stripper = new ValueStripper2("Value one {valueOne} value two {valueTwo} and finally value three {valueThree}");
        @Override public void iterate() throws Exception {
            stripper.onNewLogEvent(eventNotMatchesEnd);
        }
    }

    public static class WithoutStartsWithMatching extends Approach {

        private ValueStripper2 stripper = new ValueStripper2("Value one {valueOne} value two {valueTwo} and finally value three {valueThree}");
        public WithoutStartsWithMatching() {
            stripper.setStartsWith(null);
        }
        
        @Override public void iterate() throws Exception {
            stripper.onNewLogEvent(eventMatches);
        }

    }
    
    public static class WithoutStartsWithNotMatchingStart extends Approach {

        private ValueStripper2 stripper = new ValueStripper2("Value one {valueOne} value two {valueTwo} and finally value three {valueThree}");
        public WithoutStartsWithNotMatchingStart() {
            stripper.setStartsWith(null);
        }
        
        @Override public void iterate() throws Exception {
            stripper.onNewLogEvent(eventNotMatchesStart);
        }

    }
    
    public static class WithoutStartsWithNotMatchingEnd extends Approach {

        private ValueStripper2 stripper = new ValueStripper2("Value one {valueOne} value two {valueTwo} and finally value three {valueThree}");
        public WithoutStartsWithNotMatchingEnd() {
            stripper.setStartsWith(null);
        }
        
        @Override public void iterate() throws Exception {
            stripper.onNewLogEvent(eventNotMatchesEnd);
        }

    }

    public static void main(String[] args) {
        Benchmarker.benchmark(5000, ValueStripperBenchmark.class);
    }

}
