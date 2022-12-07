package com.logginghub.logging.telemetry;

//import java.nio.ByteBuffer;

import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Keys;
import com.logginghub.utils.data.DataStructure.Values;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

public class TestProcessTelemetryGeneratorTest {

    // Load the sigar libs
//    KryoTelemetryClient client = new KryoTelemetryClient();

//    @Test public void testKryo() {
        //
        // TelemetryData data = new TelemetryData();
        // data.setKey(TelemetryKeyElement.host, "host");
        // data.set(TelemetryDataType.OS_Cpu_System_Time, 0.4d);
        //
        // ByteBuffer buffer = ByteBuffer.allocate(10000);
        //
        // Kryo kryo = new Kryo();
        // TelemetryClassRegistrar tcr = new TelemetryClassRegistrar();
        // tcr.registerClasses(kryo);
        //
        // kryo.writeObject(buffer, data);
        // buffer.flip();
        //
        // TelemetryData readObject = kryo.readObject(buffer,
        // TelemetryData.class);
        // System.out.println(readObject);

//    }

    @Test public void testStart() {

        final Bucket<DataStructure> bucket = new Bucket<DataStructure>();

        if(SigarHelper.hasSigarSupport()) {
            SigarProcessTelemetryGenerator generator = new SigarProcessTelemetryGenerator("testProcess");
            generator.getDataStructureMultiplexer().addDestination(bucket);

            generator.start();
            bucket.waitForMessages(1);
            generator.stop();

            // System.out.println(bucket.get(0));

            assertThat(bucket.get(0).getKey(Keys.processName).asString(), is("testProcess"));
            assertThat(bucket.get(0).getKey(Keys.host).asString(), is(NetUtils.getLocalHostname()));
            assertThat(bucket.get(0).getKey(Keys.ip).asString(), is(NetUtils.getLocalIP()));
            assertThat(bucket.get(0).getValue(Values.SIGAR_OS_Process_Cpu_Percentage).asDouble(), is(greaterThanOrEqualTo(0d)));
            assertThat(bucket.get(0).getValue(Values.SIGAR_OS_Process_Cpu_System_Time).asDouble(), is(greaterThanOrEqualTo(0d)));
            assertThat(bucket.get(0).getValue(Values.JVM_Process_Memory_Maximum).asLong(), is(Runtime.getRuntime().maxMemory()));
            assertThat(bucket.get(0).getValue(Values.JVM_Process_Memory_Used).asDouble(), is(greaterThan(0d)));
            assertThat(bucket.get(0).getValue(Values.JVM_Process_Memory_Total).asDouble(), is(greaterThan(0d)));
        }
    }
}
