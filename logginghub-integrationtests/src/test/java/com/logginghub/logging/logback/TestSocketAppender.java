package com.logginghub.logging.logback;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.InetSocketAddress;
import java.util.EnumSet;

import org.junit.Test;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.levelsetting.InstanceFilter;
import com.logginghub.logging.api.levelsetting.LevelSetting;
import com.logginghub.logging.api.levelsetting.LevelSettingsConfirmation;
import com.logginghub.logging.api.levelsetting.LevelSettingsGroup;
import com.logginghub.logging.api.levelsetting.MultipleResultListener;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.logback.SocketAppender;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.modules.BaseHub;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Result;

public class TestSocketAppender extends BaseHub {

    @Test public void test_level_setting() throws ConnectorException {

        // Fire up the hub
        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions));
        SocketHub hub = hubFixture.start();

        // Create a logger and configure it programmatically
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("testLogger");
        logger.setLevel(ch.qos.logback.classic.Level.INFO);

        SocketAppender connector = new SocketAppender("logback test handler");
        connector.addConnectionPoint(new InetSocketAddress(hub.getPort()));

        // Typically the appender doesn't connect until it logs something for the first time
        connector.getAppenderHelper().getSocketClient().connect();
        connector.start();
        logger.addAppender(connector);

        // Create a socket client we'll use to set the log levels
        SocketClient clientA = fixture.createClientAutoSubscribe("clientA", hub);

        Bucket<LogEvent> events = fixture.createEventBucketFor(clientA);

        int sent = connector.getAppenderHelper().getSocketClient().getSent();
        logger.debug("Hello world");

        assertThat(logger.getLevel().toString(), is("INFO"));

        // Make sure we didn't dispatch that event
        assertThat(connector.getAppenderHelper().getSocketClient().getSent(), is(sent));

        // Use the API from clientA to set the level on our logger
        final Bucket<Result<LevelSettingsConfirmation>> results = new Bucket<Result<LevelSettingsConfirmation>>();
        InstanceFilter filter = new InstanceFilter();
        LevelSettingsGroup settings = new LevelSettingsGroup();
        settings.add(new LevelSetting("testLogger", "FINE"));

        MultipleResultListener<LevelSettingsConfirmation> listener = new MultipleResultListener<LevelSettingsConfirmation>() {
            @Override public void onResult(Result<LevelSettingsConfirmation> result) {
                results.add(result);
            }
        };

        clientA.getLevelSettingAPI().setLevels(filter, settings, listener);

        results.waitForMessages(1);
        assertThat(results.get(0).isSuccessful(), is(true));

        logger.debug("Hello world");

        events.waitForMessages(1);

        assertThat(events.get(0).getMessage(), is("Hello world"));

    }

    @Test public void test_level_setting_instance_filter() throws ConnectorException {

        // Fire up the hub
        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions));
        SocketHub hub = hubFixture.start();

        // Create a logger and configure it programmatically
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("testLogger");
        logger.setLevel(ch.qos.logback.classic.Level.INFO);

        SocketAppender connector = new SocketAppender("log4j. test handler");
        connector.addConnectionPoint(new InetSocketAddress(hub.getPort()));

        // Typically the appender doesn't connect until it logs something for the first time
        connector.getAppenderHelper().getSocketClient().connect();
        connector.start();
        logger.addAppender(connector);

        // Create a socket client we'll use to set the log levels
        SocketClient clientA = fixture.createClientAutoSubscribe("clientA", hub);

        // Use the API from clientA to set the level on our logger
        final Bucket<Result<LevelSettingsConfirmation>> results = new Bucket<Result<LevelSettingsConfirmation>>();
        InstanceFilter filter = new InstanceFilter();
        filter.setHostFilter("anotherhost");

        LevelSettingsGroup settings = new LevelSettingsGroup();
        settings.add(new LevelSetting("testLogger", "FINE"));

        MultipleResultListener<LevelSettingsConfirmation> listener = new MultipleResultListener<LevelSettingsConfirmation>() {
            @Override public void onResult(Result<LevelSettingsConfirmation> result) {
                results.add(result);
            }
        };

        clientA.getLevelSettingAPI().setLevels(filter, settings, listener);
        results.waitForMessages(1);
        assertThat(results.get(0).isSuccessful(), is(false));

    }

}
