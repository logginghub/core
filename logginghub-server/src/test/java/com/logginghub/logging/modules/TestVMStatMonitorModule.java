package com.logginghub.logging.modules;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.modules.configuration.VMStatMonitorConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Keys;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

@Ignore // Can't get the threads to die cleanly
public class TestVMStatMonitorModule {

	@Test public void test_linux() {
		if(OSUtils.isNixVariant()) {
//			Logger.setLevel(Logger.trace, VMStatTelemetryGenerator.class);

	        final Bucket<DataStructure> data = new Bucket<DataStructure>();
	        
//	        TelemetryInterface telemetryInterface = new TelemetryInterface() {
//	            public void publishTelemetry(DataStructure dataStructure) {
//	            	System.out.println(dataStructure);
//	                data.add(dataStructure);
//	            }
//	        }; 
	                        
	        
            ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
            discovery.bind(Destination.class, LogEvent.class, new Multiplexer<LogEvent>());
	        
	        VMStatMonitorConfiguration config = new VMStatMonitorConfiguration();
            VMStatMonitorModule generator = new VMStatMonitorModule();
            generator.setProcessName("processName");
            generator.configure(config, discovery);
            
            generator.getDataStructureMultiplexer().addDestination(data);
	        generator.start();
	        
	        
	        
//	        Timeout.defaultTimeout.setTime(100000);
	        data.waitForMessages(2);
	        
	        DataStructure dataStructure1 = data.get(0);
	        DataStructure dataStructure2 = data.get(1);
	        	        
	        assertThat(dataStructure1.getStringKey(Keys.processName), is("processName"));
	        assertThat(dataStructure2.getStringKey(Keys.processName), is("processName"));
	        
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_Processes_Run_Queue), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_Processes_Blocking), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_Memory_Swap), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_Memory_Free), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_Memory_Buffers), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_Memory_Cache), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_Swap_In), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_Swap_Out), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_IO_Blocks_In), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_IO_Blocks_Out), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_System_Interupts), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_System_Context_Switches), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_CPU_User), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_CPU_System), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_CPU_Idle), is(true));
	        assertThat(dataStructure1.containsValue(Values.VMSTAT_CPU_Waiting), is(true));

	        assertThat(dataStructure2.containsValue(Values.VMSTAT_Processes_Run_Queue), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_Processes_Blocking), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_Memory_Swap), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_Memory_Free), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_Memory_Buffers), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_Memory_Cache), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_Swap_In), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_Swap_Out), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_IO_Blocks_In), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_IO_Blocks_Out), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_System_Interupts), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_System_Context_Switches), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_CPU_User), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_CPU_System), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_CPU_Idle), is(true));
	        assertThat(dataStructure2.containsValue(Values.VMSTAT_CPU_Waiting), is(true));
	        
	        generator.stop();
	       	     
	        // Manual check to see if the process was actually killed
	        //ThreadUtils.sleep(5000);
		}
	}
	
    @Test public void test_simulator() throws Exception {
//        Logger.setLevel(Logger.trace, VMStatTelemetryGenerator.class);

        final Bucket<DataStructure> data = new Bucket<DataStructure>();
        
//        TelemetryInterface telemetryInterface = new TelemetryInterface() {
//            public void publishTelemetry(DataStructure dataStructure) {
//                data.add(dataStructure);
//            }
//        }; 
        
        
        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.bind(Destination.class, LogEvent.class, new Multiplexer<LogEvent>());
                        
        VMStatMonitorConfiguration config = new VMStatMonitorConfiguration();
        VMStatMonitorModule generator = new VMStatMonitorModule();
        generator.setProcessName("processName");
        generator.configure(config, discovery);
        generator.setSimulator(true);
        
        generator.getDataStructureMultiplexer().addDestination(data);
        
        
        generator.start();
        
        data.waitForMessages(2);
        
        DataStructure dataStructure1 = data.get(0);
        DataStructure dataStructure2 = data.get(1);
        
        
        assertThat(dataStructure1.getStringKey(Keys.processName), is("processName"));
        assertThat(dataStructure2.getStringKey(Keys.processName), is("processName"));

        // 0  1 449804  61296   4532  71168    0    1     0     8  504 1280  1  3 95  1
        // 1  0 449803  61295   4531  71176    1    0     1    32  497 1450  2  1 97  0
        
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_Processes_Run_Queue), is(0));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_Processes_Blocking), is(1));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_Memory_Swap), is(449804));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_Memory_Free), is(61296));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_Memory_Buffers), is(4532));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_Memory_Cache), is(71168));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_Swap_In), is(0));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_Swap_Out), is(1));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_IO_Blocks_In), is(0));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_IO_Blocks_Out), is(8));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_System_Interupts), is(504));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_System_Context_Switches), is(1280));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_CPU_User), is(1));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_CPU_System), is(3));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_CPU_Idle), is(95));
        assertThat(dataStructure1.getIntValue(Values.VMSTAT_CPU_Waiting), is(1));

        assertThat(dataStructure2.getIntValue(Values.VMSTAT_Processes_Run_Queue), is(1));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_Processes_Blocking), is(0));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_Memory_Swap), is(449803));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_Memory_Free), is(61295));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_Memory_Buffers), is(4531));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_Memory_Cache), is(71176));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_Swap_In), is(1));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_Swap_Out), is(0));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_IO_Blocks_In), is(1));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_IO_Blocks_Out), is(32));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_System_Interupts), is(497));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_System_Context_Switches), is(1450));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_CPU_User), is(2));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_CPU_System), is(1));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_CPU_Idle), is(97));
        assertThat(dataStructure2.getIntValue(Values.VMSTAT_CPU_Waiting), is(0));
        
        generator.stop();
    }


}
