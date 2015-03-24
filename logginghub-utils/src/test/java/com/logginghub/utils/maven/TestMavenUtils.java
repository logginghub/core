package com.logginghub.utils.maven;

import com.logginghub.utils.OSUtils;
import com.logginghub.utils.VersionNumber;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class TestMavenUtils {

    // jshaw - seriously?
    @Ignore
    @Test
    public void testGetJarArtifactForKey() throws Exception {

        VersionedMavenKey key = new VersionedMavenKey("com.miglayout", "miglayout", VersionNumber.parse("3.7.3.1"));
        File jarArtifactForKey = MavenUtils.getJarArtifactForKey(key);
        if (OSUtils.isNixVariant()) {
            assertThat(jarArtifactForKey.getAbsolutePath(), is("/home/james/.m2/repository/com/miglayout/miglayout/3.7.3.1/miglayout-3.7.3.1.jar"));
        } else {
            fail("OS not supported yet - implement me");
        }

        key.setClassifier("swing");
        jarArtifactForKey = MavenUtils.getJarArtifactForKey(key);
        if (OSUtils.isNixVariant()) {
            assertThat(jarArtifactForKey.getAbsolutePath(),
                       is("/home/james/.m2/repository/com/miglayout/miglayout/3.7.3.1/miglayout-3.7.3.1-swing.jar"));
        } else {
            fail("OS not supported yet - implement me");
        }
    }

}



