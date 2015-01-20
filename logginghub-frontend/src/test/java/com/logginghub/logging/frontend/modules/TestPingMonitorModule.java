package com.logginghub.logging.frontend.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.utils.StringUtils;

public class TestPingMonitorModule {

    @Test public void test_regex() throws Exception {

        String regex = "(\\d*) bytes from ([^ (]*)[ ]*(.*): icmp_seq=(\\d*) ttl=(\\d*) time=([\\d\\.]*) ms";

        String input1 = "64 bytes from hosting (95.172.2.226): icmp_seq=3 ttl=47 time=24.5 ms";
        assertThat(StringUtils.matchGroupsArray(input1, regex), is(new String[] { "64", "hosting", "(95.172.2.226)", "3", "47", "24.5" }));

        String input2 = "64 bytes from 54.171.42.240: icmp_seq=3 ttl=52 time=21.5 ms";
        assertThat(StringUtils.matchGroupsArray(input2, regex), is(new String[] { "64", "54.171.42.240", "", "3", "52", "21.5" }));
    }

}
