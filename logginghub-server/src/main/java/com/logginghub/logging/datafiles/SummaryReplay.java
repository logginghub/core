package com.logginghub.logging.datafiles;

import com.logginghub.utils.Destination;
import com.logginghub.utils.Out;

import java.io.File;

/**
 * Created by james on 18/09/15.
 */
public class SummaryReplay {
    public static void main(String[] args) {
        SummaryBuilder.replay(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp",
                                       "bats.aggregatedsummary.binary.log"), new Destination<SummaryTimeElement>() {
            @Override
            public void send(SummaryTimeElement summaryTimeElement) {
                Out.out(summaryTimeElement);
            }
        });
    }
}
