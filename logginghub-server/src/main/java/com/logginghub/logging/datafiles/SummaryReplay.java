package com.logginghub.logging.datafiles;

import com.logginghub.logging.modules.web.FirstCutLoggingHubDatabase;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;

import java.io.File;

/**
 * Created by james on 18/09/15.
 */
public class SummaryReplay {
    public static void main(String[] args) {


        File baseFolder = new File("data/database");
        FileUtils.deleteContents(baseFolder);
        final FirstCutLoggingHubDatabase firstCutLoggingHubDatabase = new FirstCutLoggingHubDatabase(baseFolder);

        SummaryBuilder.replay(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp",
                                       "bats.aggregatedsummary.binary.log"), new Destination<SummaryTimeElement>() {
            @Override
            public void send(SummaryTimeElement summaryTimeElement) {

                firstCutLoggingHubDatabase.updateFrom(summaryTimeElement);

//                Out.out(summaryTimeElement);
            }
        });

        firstCutLoggingHubDatabase.flushDirtyFiles();

    }
}
