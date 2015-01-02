package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.logginghub.utils.OSUtils;
import com.logginghub.utils.ReflectionEnvironmentVariableReplacer;

public class TestReflectionEnvironmentVariableReplacer {

    static class TopLevel {
        private String topString = "topString ${USERNAME} username";
        private MidLevel midLevel = new MidLevel();
        private int value = 1;
    }

    static class MidLevel {
        private String middleString = "middleString ${OS} ${COMPUTERNAME}";
        private BottomLevel bottomLevel = new BottomLevel();
        private boolean isSomething = false;
        private List<Float> list = new ArrayList<Float>();
    }

    static class BottomLevel {
        public String bottomString = "${PROCESSOR_ARCHITECTURE} ${Property}";
        public String nullString = null;
        private Map<String, String> map = new HashMap<String, String>();
    }

    private ReflectionEnvironmentVariableReplacer replacer = new ReflectionEnvironmentVariableReplacer();
    private TopLevel topLevel = new TopLevel();

    @Test public void testDoReplacements() throws IllegalArgumentException, IllegalAccessException {
        assertThat(topLevel.topString, is("topString ${USERNAME} username"));
        assertThat(topLevel.midLevel.middleString, is("middleString ${OS} ${COMPUTERNAME}"));
        assertThat(topLevel.midLevel.bottomLevel.bottomString, is("${PROCESSOR_ARCHITECTURE} ${Property}"));

        System.setProperty("Property", "sysPropertyValue");

        replacer.doReplacements(topLevel);

        Map<String, String> env = System.getenv();

        if (OSUtils.isWindows()) {
            String username = env.get("USERNAME");
            String os = env.get("OS");
            String computerName = env.get("COMPUTERNAME");
            String processor = env.get("PROCESSOR_ARCHITECTURE");

            assertThat(topLevel.topString, is("topString " + username + " username"));
            assertThat(topLevel.midLevel.middleString, is("middleString " + os + " " + computerName));
            assertThat(topLevel.midLevel.bottomLevel.bottomString, is(processor + " " + "sysPropertyValue"));
        }
        // TODO : something for the same on linux?
    }

}
