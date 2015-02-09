package com.logginghub.logging.modules;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Keys;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Ignore
public class TestHubTelemetryOutput extends BaseHub {

    @Test public void test_hub_outputs_machine_telemetry() throws IOException, ConnectorException, LoggingMessageSenderException {

        SocketHub hub = fixture.getSocketHubA();

        DataStructure telemetry = new DataStructure();
        telemetry.addKey(Keys.host, "hostname");
        telemetry.addKey(Keys.ip, "123.123.123.123");
        telemetry.addValue(Values.SIGAR_OS_Network_Bytes_Sent, 1 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Network_Bytes_Received, 2 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Cpu_User_Time, 3);
        telemetry.addValue(Values.SIGAR_OS_Cpu_System_Time, 4);
        telemetry.addValue(Values.SIGAR_OS_Cpu_Wait_Time, 5);
        telemetry.addValue(Values.SIGAR_OS_Cpu_Idle_Time, 6);
        telemetry.addValue(Values.SIGAR_OS_Memory_Actual_Free, 7 * 1024 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Memory_Actual_Used, 8 * 1024 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Memory_Free, 9 * 1024 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Memory_Free_Percent, 10);
        telemetry.addValue(Values.SIGAR_OS_Memory_Ram, 11 * 1024 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Memory_Total, 12 * 1024 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Memory_Used, 13 * 1024 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Memory_Used_Percent, 14);

        // Send the telemetry into the hub
        SocketClient client = fixture.createClient("client", hub);
        Bucket<LogEvent> bucket = fixture.createEventBucketFor(client);
        client.send(new ChannelMessage(Channels.telemetryUpdates, telemetry));

        // See if there is a log event to go with it
        bucket.waitForMessages(1);
        assertThat(bucket.size(), is(1));
        LogEvent logEvent = bucket.get(0);

        assertThat(logEvent.getLevel(), is(Logger.info));
        assertThat(logEvent.getSourceHost(), is("hostname"));
        assertThat(logEvent.getSourceAddress(), is("123.123.123.123"));
        assertThat(logEvent.getSourceClassName(), is("telemetry"));
        assertThat(logEvent.getSourceMethodName(), is("machine"));
        assertThat(logEvent.getMessage(),
                   is("sigar-os user=3.0% system=4.0% wait=5.0% idle=6.0% memactfree=7.0 memactused=8.0 memfree=9.0 (10.0%) memram=11.0 memtotal=12.0 memused=13.0 (14.0%) nettx=1.0 netrx=2.0"));

    }

    @Test public void test_hub_outputs_process_telemetry() throws IOException, ConnectorException, LoggingMessageSenderException {

        SocketHub hub = fixture.getSocketHubA();

        DataStructure telemetry = new DataStructure();
        telemetry.addKey(Keys.host, "hostname");
        telemetry.addKey(Keys.ip, "123.123.123.123");
        telemetry.addKey(Keys.pid, "666");
        telemetry.addKey(Keys.processName, "process");

        telemetry.addValue(Values.JVM_Process_Memory_Maximum, 50 * 1024 * 1024);
        telemetry.addValue(Values.JVM_Process_Memory_Total, 20 * 1024 * 1024);
        telemetry.addValue(Values.JVM_Process_Memory_Used, 5 * 1024 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Process_Cpu_System_Time, 4);
        telemetry.addValue(Values.SIGAR_OS_Process_Cpu_User_Time, 5);
        telemetry.addValue(Values.SIGAR_OS_Process_Memory_Size, 6 * 1024 * 1024);
        telemetry.addValue(Values.SIGAR_OS_Process_Memory_Resident, 7 * 1024 * 1024);

        // Send the telemetry into the hub
        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<LogEvent> bucket = fixture.createEventBucketFor(client);
        client.send(new ChannelMessage(Channels.telemetryUpdates, telemetry));

        // See if there is a log event to go with it
        bucket.waitForMessages(1);
        assertThat(bucket.size(), is(1));
        LogEvent logEvent = bucket.get(0);

        assertThat(logEvent.getLevel(), is(Logger.info));
        assertThat(logEvent.getSourceApplication(), is("process"));
        assertThat(logEvent.getSourceClassName(), is("telemetry"));
        assertThat(logEvent.getSourceMethodName(), is("process"));
        assertThat(logEvent.getPid(), is(666));
        assertThat(logEvent.getSourceHost(), is("hostname"));
        assertThat(logEvent.getSourceAddress(), is("123.123.123.123"));
        assertThat(logEvent.getMessage(),
                   is("sigar-process user=5.0% system=4.0% jvmmemoryused=5.0 (10.0% of max) jvmmemorytotal=20.0 jvmmemorymax=50.0 osmemory=6.0 osmemoryresident=7.0"));

    }

}
