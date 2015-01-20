package com.logginghub.logging.messages;

import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.utils.Benchmarker;
import com.logginghub.utils.Benchmarker.Approach;

public class ChannelParseBenchmark {
    
    public final static class Custom extends Approach {
        @Override public void iterate() throws Exception {
            ChannelMessage.parseChannel("this/is/a/longish/channel");
        }
    }
    
    public final static class Split extends Approach {
        @Override public void iterate() throws Exception {
            ChannelMessage.parseChannelSplit("this/is/a/longish/channel");
        }
    }
    
    public static void main(String[] args) {
        Benchmarker.benchmark(1000, ChannelParseBenchmark.class);
    }
}
