package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;

@RunWith(CustomRunner.class)
public class TestResfulListener extends BaseHub {

    @Test public void test_post() throws IOException, ConnectorException, LoggingMessageSenderException {

        SocketHub hub = fixture.getSocketHubA();

        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<LogEvent> events = fixture.createEventBucketFor(client);

        WebClient webClient = new WebClient();

        WebRequestSettings requestSettings = new WebRequestSettings(new URL("http://localhost:" +
                                                                            fixture.getRestfulListenerConfiguration().getPort() +
                                                                            "/send"), HttpMethod.POST);

        requestSettings.setRequestParameters(new ArrayList());
        requestSettings.getRequestParameters().add(new NameValuePair("level", "WARNING"));
        requestSettings.getRequestParameters().add(new NameValuePair("sequence", "1"));
        requestSettings.getRequestParameters().add(new NameValuePair("sourceClass", "source class"));
        requestSettings.getRequestParameters().add(new NameValuePair("sourceMethod", "source method"));
        requestSettings.getRequestParameters().add(new NameValuePair("message", "restful message"));
        requestSettings.getRequestParameters().add(new NameValuePair("thread", "source thread"));
        requestSettings.getRequestParameters().add(new NameValuePair("time", "2"));
        requestSettings.getRequestParameters().add(new NameValuePair("logger", "source logger"));
        requestSettings.getRequestParameters().add(new NameValuePair("host", "source host"));
        requestSettings.getRequestParameters().add(new NameValuePair("hostip", "123.123.123.123"));
        requestSettings.getRequestParameters().add(new NameValuePair("application", "source application"));
        requestSettings.getRequestParameters().add(new NameValuePair("pid", "42"));
        requestSettings.getRequestParameters().add(new NameValuePair("exception", "exception stack trace"));
        requestSettings.getRequestParameters().add(new NameValuePair("object", "formatted object"));
        requestSettings.getRequestParameters().add(new NameValuePair("channel", "target/channel"));

        // Finally, we can get the page
        HtmlPage page = webClient.getPage(requestSettings);

        events.waitForMessages(1);
        assertThat(events.get(0).getChannel(), is("target/channel"));
        assertThat(events.get(0).getFormattedException(), is("exception stack trace"));
        assertThat(events.get(0).getFormattedObject()[0], is("formatted object"));
        assertThat(events.get(0).getLevel(), is(Logger.warning));
        assertThat(events.get(0).getOriginTime(), is(2L));
        assertThat(events.get(0).getLoggerName(), is("source logger"));
        assertThat(events.get(0).getMessage(), is("restful message"));
        assertThat(events.get(0).getPid(), is(42));
        assertThat(events.get(0).getSequenceNumber(), is(1L));
        assertThat(events.get(0).getSourceAddress(), is("123.123.123.123"));
        assertThat(events.get(0).getSourceApplication(), is("source application"));
        assertThat(events.get(0).getSourceClassName(), is("source class"));
        assertThat(events.get(0).getSourceHost(), is("source host"));
        assertThat(events.get(0).getSourceMethodName(), is("source method"));
        assertThat(events.get(0).getThreadName(), is("source thread"));

        webClient.closeAllWindows();
    }

    @Test public void test_get_params() throws IOException, ConnectorException, LoggingMessageSenderException {

        SocketHub hub = fixture.getSocketHubA();

        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<LogEvent> events = fixture.createEventBucketFor(client);

        WebClient webClient = new WebClient();

        int port = fixture.getRestfulListenerConfiguration().getPort();
        String url = StringUtils.format("http://localhost:{}/send?message=Test message&application=Source application", port);

        final HtmlPage page = webClient.getPage(url);
        
        events.waitForMessages(1);
        assertThat(events.get(0).getChannel(), is("events"));
        assertThat(events.get(0).getFormattedException(), is(nullValue()));
        assertThat(events.get(0).getFormattedObject(), is(nullValue()));
        assertThat(events.get(0).getLevel(), is(Logger.info));
        assertThat(events.get(0).getOriginTime(), is(greaterThan(0L)));
        assertThat(events.get(0).getLoggerName(), is(""));
        assertThat(events.get(0).getMessage(), is("Test message"));
        assertThat(events.get(0).getPid(), is(0));
        assertThat(events.get(0).getSequenceNumber(), is(0L));
        assertThat(events.get(0).getSourceAddress(), is(""));
        assertThat(events.get(0).getSourceApplication(), is("Source application"));
        assertThat(events.get(0).getSourceClassName(), is(""));
        assertThat(events.get(0).getSourceHost(), is(""));
        assertThat(events.get(0).getSourceMethodName(), is(""));
        assertThat(events.get(0).getThreadName(), is(""));
        
        webClient.closeAllWindows();
    }

}
