package com.logginghub.logging.modules;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.modules.configuration.IOStatMonitorConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Keys;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

public class TestIOStatMonitorModule {

	@Test public void test_linux() {
		if(OSUtils.isNixVariant()) {
//			Logger.setLevel(Logger.trace, IOStatTelemetryGenerator.class);

	        final Bucket<DataStructure> data = new Bucket<DataStructure>();
	        
//	        TelemetryInterface telemetryInterface = new TelemetryInterface() {
//	            public void publishTelemetry(DataStructure dataStructure) {
//	            	System.out.println(dataStructure);
//	                data.add(dataStructure);
//	            }
////	            public void publishTelemetry(TelemetryData data) {}
//	        }; 
	        
	        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
	        discovery.bind(Destination.class, LogEvent.class, new Multiplexer<LogEvent>());
	        
	        IOStatMonitorConfiguration config = new IOStatMonitorConfiguration();
            IOStatMonitorModule generator = new IOStatMonitorModule();
            generator.setProcessName("processName");
            generator.configure(config, discovery);            
            generator.getDataStructureMultiplexer().addDestination(data);
	        generator.start();
	        
//	        Timeout.defaultTimeout.setTime(100000);
	        data.waitForMessages(2);
	        
	        DataStructure dataStructure1 = data.get(0);
	        DataStructure dataStructure2 = data.get(1);
	        	        
	        assertThat(dataStructure1.getStringKey(Keys.processName), is("processName"));
	        assertThat(dataStructure1.getStringKey(Keys.device), is("sda"));
	        
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Reads_Requested), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Writes_Requested), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Reads_Completed), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Writes_Completed), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Read_Amount), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Write_Amount), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Average_Request_Size), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Average_Request_Queue_Length), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Request_Served_Time), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Read_Requests_Served_Time), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Write_Requests_Served_Time), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Service_Time), is(true));
	        assertThat(dataStructure1.containsValue(Values.IOSTAT_Device_Utilisation), is(true));

	        assertThat(dataStructure2.getStringKey(Keys.processName), is("processName"));
	        assertThat(dataStructure2.getStringKey(Keys.device), is("sdb"));
	        
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Reads_Requested), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Writes_Requested), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Reads_Completed), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Writes_Completed), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Read_Amount), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Write_Amount), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Average_Request_Size), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Average_Request_Queue_Length), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Request_Served_Time), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Read_Requests_Served_Time), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Write_Requests_Served_Time), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Service_Time), is(true));
	        assertThat(dataStructure2.containsValue(Values.IOSTAT_Device_Utilisation), is(true));
	        
	        generator.stop();
	       	     
	        // Manual check to see if the process was actually killed
	        //ThreadUtils.sleep(5000);
		}
	}
	
    @Test public void test_simulator() throws Exception {
//        Logger.setLevel(Logger.trace, IOStatTelemetryGenerator.class);

        final Bucket<DataStructure> data = new Bucket<DataStructure>();
        
//        TelemetryInterface telemetryInterface = new TelemetryInterface() {
//            public void publishTelemetry(DataStructure dataStructure) {
//                data.add(dataStructure);
//            }
////            public void publishTelemetry(TelemetryData data) {}
//        }; 
                        
        IOStatMonitorConfiguration config = new IOStatMonitorConfiguration();
        config.setSimulating(true);
        IOStatMonitorModule generator = new IOStatMonitorModule();
        generator.setProcessName("processName");
        
        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.bind(Destination.class, LogEvent.class, new Multiplexer<LogEvent>());
        
        generator.configure(config, discovery);
        
        generator.getDataStructureMultiplexer().addDestination(data);
        
        generator.start();
        
        data.waitForMessages(4);
        
        DataStructure dataStructure1 = data.get(0);
        DataStructure dataStructure2 = data.get(1);
        	        
        assertThat(dataStructure1.getStringKey(Keys.processName), is("processName"));
        assertThat(dataStructure1.getStringKey(Keys.device), is("sda"));
        
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Reads_Requested), is(1.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Writes_Requested), is(2.02));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Reads_Completed), is(3.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Writes_Completed), is(4.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Read_Amount), is(5.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Write_Amount), is(6.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Average_Request_Size), is(7.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Average_Request_Queue_Length), is(8.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Request_Served_Time), is(9.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Read_Requests_Served_Time), is(10.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Write_Requests_Served_Time), is(11.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Service_Time), is(12.00));
        assertThat(dataStructure1.getDoubleValue(Values.IOSTAT_Device_Utilisation), is(13.00));

        assertThat(dataStructure2.getStringKey(Keys.processName), is("processName"));
        assertThat(dataStructure2.getStringKey(Keys.device), is("sdb"));
        
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Reads_Requested), is(14.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Writes_Requested), is(15.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Reads_Completed), is(16.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Writes_Completed), is(17.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Read_Amount), is(18.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Write_Amount), is(19.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Average_Request_Size), is(20.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Average_Request_Queue_Length), is(21.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Request_Served_Time), is(22.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Read_Requests_Served_Time), is(23.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Write_Requests_Served_Time), is(24.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Service_Time), is(25.0));
        assertThat(dataStructure2.getDoubleValue(Values.IOSTAT_Device_Utilisation), is(26.0));
        
        DataStructure dataStructure3 = data.get(2);
        	        
        assertThat(dataStructure3.getStringKey(Keys.processName), is("processName"));
        assertThat(dataStructure3.getStringKey(Keys.device), is("sda"));
        assertThat(dataStructure3.getDoubleValue(Values.IOSTAT_Reads_Requested), is(27.00));
        
        generator.stop();
    }


}
