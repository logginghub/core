package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.logging.Logger;

@RunWith(CustomRunner.class) public class TestSocketTextReader extends BaseHub {

    @Test public void test_post() throws IOException, ConnectorException, LoggingMessageSenderException {

        fixture.getSocketTextReaderConfiguration().getTemplate().setChannel("events/text");
        fixture.getSocketTextReaderConfiguration().getTemplate().setLevel("WARNING");
        fixture.getSocketTextReaderConfiguration().getTemplate().setMessage("Socket text : ${message} : reader");
        fixture.getSocketTextReaderConfiguration().getTemplate().setLoggerName("source logger");
        fixture.getSocketTextReaderConfiguration().getTemplate().setSourceAddress("123.123.123.123");
        fixture.getSocketTextReaderConfiguration().getTemplate().setSourceApplication("source application");
        fixture.getSocketTextReaderConfiguration().getTemplate().setSourceClassName("source class");
        fixture.getSocketTextReaderConfiguration().getTemplate().setSourceHost("source host");
        fixture.getSocketTextReaderConfiguration().getTemplate().setSourceMethodName("source method");
        fixture.getSocketTextReaderConfiguration().getTemplate().setThreadName("source thread");

        SocketHub hub = fixture.getSocketHubA();
        fixture.getSocketTextReaderModule().waitUntilBound();

        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<LogEvent> events = fixture.createEventBucketFor(client);

        NetUtils.send("test message", new InetSocketAddress(fixture.getSocketTextReaderConfiguration().getPort()));

        events.waitForMessages(1);
        assertThat(events.get(0).getChannel(), is("events/text"));
        assertThat(events.get(0).getFormattedException(), is(nullValue()));
        assertThat(events.get(0).getFormattedObject(), is(nullValue()));
        assertThat(events.get(0).getLevel(), is(Logger.warning));
        assertThat(events.get(0).getOriginTime(), is(greaterThan(0L)));
        assertThat(events.get(0).getLoggerName(), is("source logger"));
        assertThat(events.get(0).getMessage(), is("Socket text : test message : reader"));
        assertThat(events.get(0).getPid(), is(greaterThan(0)));
        // jshaw - this increments depending on how many other tests we've run!
        // assertThat(events.get(0).getSequenceNumber(), is(0L));
        assertThat(events.get(0).getSourceAddress(), is("123.123.123.123"));
        assertThat(events.get(0).getSourceApplication(), is("source application"));
        assertThat(events.get(0).getSourceClassName(), is("source class"));
        assertThat(events.get(0).getSourceHost(), is("source host"));
        assertThat(events.get(0).getSourceMethodName(), is("source method"));
        assertThat(events.get(0).getThreadName(), is("source thread"));

    }

    @Test public void test_defaults() throws IOException, ConnectorException, LoggingMessageSenderException {

        SocketHub hub = fixture.getSocketHubA();
        fixture.getSocketTextReaderModule().waitUntilBound();

        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<LogEvent> events = fixture.createEventBucketFor(client);

        NetUtils.send("test message", new InetSocketAddress(fixture.getSocketTextReaderConfiguration().getPort()));

        events.waitForMessages(1);
        assertThat(events.get(0).getChannel(), is("events"));
        assertThat(events.get(0).getFormattedException(), is(nullValue()));
        assertThat(events.get(0).getFormattedObject(), is(nullValue()));
        assertThat(events.get(0).getLevel(), is(Logger.info));
        assertThat(events.get(0).getOriginTime(), is(greaterThan(0L)));
        assertThat(events.get(0).getLoggerName(), is(""));
        assertThat(events.get(0).getMessage(), is("test message"));
        assertThat(events.get(0).getPid(), is(greaterThan(0)));
        // jshaw - this increments depending on how many other tests we've run!
        // assertThat(events.get(0).getSequenceNumber(), is(0L));
        assertThat(events.get(0).getSourceAddress(), is(NetUtils.getLocalIP()));
        assertThat(events.get(0).getSourceApplication(), is(""));
        assertThat(events.get(0).getSourceClassName(), is(""));
        assertThat(events.get(0).getSourceHost(), is(NetUtils.getLocalHostname()));
        assertThat(events.get(0).getSourceMethodName(), is(""));
        assertThat(events.get(0).getThreadName(), is(""));

    }

}
