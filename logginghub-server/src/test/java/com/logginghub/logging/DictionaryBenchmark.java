package com.logginghub.logging;

import java.util.HashMap;

import com.logginghub.utils.Benchmarker;
import com.logginghub.utils.Dictionary;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.Benchmarker.Approach;

public class DictionaryBenchmark {

    public final static class ToDictionaryEvent extends Approach {
        DefaultLogEvent logEvent = LogEventBuilder.start().setMessage("This is a log message").toLogEvent();
        Dictionary dictionary = new Dictionary();

        @Override public void iterate() throws Exception {
            DictionaryLogEvent dictionaryEvent = DictionaryLogEvent.fromLogEvent(logEvent, dictionary);
        }
        
    }
    
    public final static class FromDictionaryEvent extends Approach {
        DefaultLogEvent logEvent = LogEventBuilder.start().setMessage("This is a log message").toLogEvent();
        Dictionary dictionary = new Dictionary();
        DictionaryLogEvent dictionaryEvent = DictionaryLogEvent.fromLogEvent(logEvent, dictionary);

        @Override public void iterate() throws Exception {
           dictionaryEvent.toLogEvent(dictionary);
        }
        
    }
    
    public final static class HundredThousand5LetterWords extends Approach {

        private HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
        private int count = 0;
        private String search;

        public HundredThousand5LetterWords() {
            int words = 1000000;
            int characters = 5;
            for (int i = 0; i < words; i++) {
                dictionary.put(StringUtils.randomString(characters), i);
            }
            
            search = StringUtils.randomString(characters);
        }

        @Override public void iterate() throws Exception {
            Integer integer = dictionary.get(search);
            if (integer != null) {
                count += integer;
            }
        }

    }

    public static void main(String[] args) {
        Benchmarker.benchmark(1000, DictionaryBenchmark.class);
    }

}
