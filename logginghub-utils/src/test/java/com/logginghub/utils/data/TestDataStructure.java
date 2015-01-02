package com.logginghub.utils.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.EOFException;
import java.io.IOException;

import org.junit.Test;

import com.logginghub.utils.data.DataElement;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Keys;
import com.logginghub.utils.data.DataStructure.Types;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofSerialiser;

public class TestDataStructure {

    @Test public void test() throws IOException {

        DataStructure dataStructure = new DataStructure(1);
        dataStructure.addKey(1, "hello");
        dataStructure.addValue(1, "world");

        assertThat((String) dataStructure.getKey(1).object, is("hello"));
        assertThat((String) dataStructure.getValue(1).object, is("world"));

        byte[] encoded = dataStructure.toByteArray();
//        HexDump.dump(encoded);

        DataStructure decoded = DataStructure.fromByteArray(encoded);

        assertThat((String) decoded.getKey(1).object, is("hello"));
        assertThat((String) decoded.getValue(1).object, is("world"));

    }

    @Test public void test_telemetry_structure() {
        
        //DataStructureBuilder.start(Types.Telemetry).key(DataStructure.Keys.Host, "localhost").key()
        
        DataStructure structure = new DataStructure(Types.Telemetry);
        
        structure.addKey(DataStructure.Keys.host, "localhost");
        structure.addValue(DataStructure.Values.JVM_Process_Memory_Maximum, 123);
        structure.addValue(DataStructure.Values.SIGAR_OS_Cpu_System_Time, 2);
        
//        System.out.println(structure);
        
    }

    
    @Test public void test_sof() throws SofException, EOFException {
        
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(DataStructure.class, 0);
        configuration.registerType(DataElement.class, 1);
        
        DataStructure structure = new DataStructure(Types.Telemetry);
        
        structure.addKey(DataStructure.Keys.host, "localhost");
        structure.addValue(DataStructure.Values.JVM_Process_Memory_Maximum, 123);
        structure.addValue(DataStructure.Values.SIGAR_OS_Cpu_System_Time, 2);

        
        byte[] bytes = SofSerialiser.toBytes(structure, configuration);
        DataStructure decoded = SofSerialiser.fromBytes(bytes, configuration);
        
        assertThat(decoded.getKey(Keys.host).asString(), is("localhost"));
        
    }
    
    
}

