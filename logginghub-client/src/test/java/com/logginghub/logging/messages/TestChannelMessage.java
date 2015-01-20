package com.logginghub.logging.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.messages.ChannelMessage;

public class TestChannelMessage {

    @Test public void testParseChannel() throws Exception {
        assertThat(ChannelMessage.parseChannel("a/b/c"), is(new String[] { "a", "b", "c" }));
        assertThat(ChannelMessage.parseChannel("aaa/bbb/ccc"), is(new String[] { "aaa", "bbb", "ccc" }));
        assertThat(ChannelMessage.parseChannel("   a   /   b   /   c   "), is(new String[] { "a", "b", "c" }));
        assertThat(ChannelMessage.parseChannel("aaaaaaaaaaaa"), is(new String[] { "aaaaaaaaaaaa" }));
        assertThat(ChannelMessage.parseChannel(""), is(new String[] { }));
        assertThat(ChannelMessage.parseChannel(" "), is(new String[] { }));
    }

}
