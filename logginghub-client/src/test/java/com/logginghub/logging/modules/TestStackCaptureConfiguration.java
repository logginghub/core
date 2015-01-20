package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.modules.StackCaptureConfiguration;

public class TestStackCaptureConfiguration {

    @Test public void testParseApplicationName() throws Exception {

        validate("instance.10", "instance", 10);
        validate("foo.instance.10", "foo.instance", 10);
        
        validate("instance10", "instance", 10);
        validate("instance10foo", "instance10foo", 1);

        validate("instance-10", "instance", 10);
        validate("foo-instance-10", "foo-instance", 10);        
    }

    private void validate(String input, String instanceType, int instanceNumber) {
        StackCaptureConfiguration configuration = new StackCaptureConfiguration();
        configuration.parseApplicationName(input);
        assertThat(configuration.getInstanceType(), is(instanceType));
        assertThat(configuration.getInstanceNumber(), is(instanceNumber));
    }

}
