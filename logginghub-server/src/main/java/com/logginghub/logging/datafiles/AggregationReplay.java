package com.logginghub.logging.datafiles;

import com.logginghub.logging.datafiles.aggregation.PatternAggregation;
import com.logginghub.utils.Destination;

import java.io.File;

/**
 * Created by james on 17/09/15.
 */
public class AggregationReplay {
    public static void main(String[] args) {


        BinaryPatternAggregatedFileStream.replay(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp",
                                                          "bats.aggregatedpattern.0.binary.log"), new Destination<PatternAggregation>() {
            @Override
            public void send(PatternAggregation aggregation) {
                aggregation.dump();
            }
        });
    }
}
