package com.logginghub.logging.datafiles;

import com.logginghub.logging.datafiles.combiner.Combiner;
import com.logginghub.logging.datafiles.aggregation.PatternAggregation;
import com.logginghub.utils.Destination;
import com.logginghub.utils.TimeUtils;

import java.io.File;

/**
 * Created by james on 17/09/15.
 */
public class HigherGranularity {
    public static void main(String[] args) {

        int patternId = 0;

        final Combiner combiner = new Combiner(patternId, TimeUtils.minutes);

        BinaryPatternAggregatedFileStream.replay(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp",
                                                          "bats.aggregatedpattern.0.binary.log"), new Destination<PatternAggregation>() {
            @Override
            public void send(PatternAggregation aggregation) {
                combiner.send(aggregation.getSortedTimeAggregations());
            }
        });

        combiner.dump();

    }
}
