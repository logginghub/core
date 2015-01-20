package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import com.logginghub.logging.frontend.PathHelper;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.DelayedAutosavingFileBasedMetadata;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.OSUtils;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestReadOnlyFilesystem {

    private SwingFrontEndDSL dsl;

    @After public void cleanup() throws IOException {
        if (dsl != null && dsl.getFrameFixture() != null) {
            dsl.getFrameFixture().cleanUp();
        }
    }

    @Test public void test_cant_write_to_dynamic_properties() throws InterruptedException, IOException {

        // File lock is not exclusive on linux it would seem!
        if (OSUtils.isWindows()) {
            String propertiesName = "swingFrontEnd";
            File properties = PathHelper.getSettingsFile(propertiesName);
            FileOutputStream fos = new FileOutputStream(properties);

            try {
                java.nio.channels.FileLock lock = fos.getChannel().lock();
                try {

                    LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                                    .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                                    .toConfiguration();

                    dsl = BaseSwing.createDSLWithRealMain(configuration);

                    Metadata dynamicSettings = dsl.getSwingFrontEnd().getProxy().getDynamicSettings();
                    if (dynamicSettings instanceof DelayedAutosavingFileBasedMetadata) {
                        DelayedAutosavingFileBasedMetadata delayedAutosavingFileBasedMetadata = (DelayedAutosavingFileBasedMetadata) dynamicSettings;

                        final Bucket<Throwable> bucket = new Bucket<Throwable>();
                        delayedAutosavingFileBasedMetadata.getAction().setExceptionHandler(new ExceptionHandler() {
                            @Override public void handleException(String action, Throwable t) {
                                bucket.add(t);
                            }
                        });

                        dsl.getFrameFixture().moveTo(new Point(1, 1));
                        bucket.waitForMessages(1);

                        assertThat(bucket.size(), is(1));
                        Throwable throwable = bucket.get(0);
                        assertThat(throwable.getCause(), instanceOf(IOException.class));
                    }

                }
                finally {
                    lock.release();
                }
            }
            finally {
                fos.close();
            }
        }
    }

}
