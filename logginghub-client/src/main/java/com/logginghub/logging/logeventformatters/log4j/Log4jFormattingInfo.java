package com.logginghub.logging.logeventformatters.log4j;

public class Log4jFormattingInfo {
    int min = -1;
    int max = 0x7FFFFFFF;
    boolean leftAlign = false;

    void reset() {
        min = -1;
        max = 0x7FFFFFFF;
        leftAlign = false;
    }

    void dump() {
        System.out.println("min=" + min + ", max=" + max + ", leftAlign=" + leftAlign);
    }
}
