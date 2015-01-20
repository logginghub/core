package com.logginghub.logging.modules;

import java.text.NumberFormat;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.modules.configuration.TelemetryOutputConfiguration;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.utils.sof.SerialisableObject;

public class TelemetryOutputModule implements Module<TelemetryOutputConfiguration> {

    private TimeProvider timeProvider = new SystemTimeProvider();
    
    @Override public void configure(TelemetryOutputConfiguration configuration, ServiceDiscovery discovery) {
        
        final Destination<LogEvent> logEventDestination = discovery.findService(Destination.class, LogEvent.class, configuration.getEventDestinationRef());
        
        ChannelMessagingService channelMessagingService = discovery.findService(ChannelMessagingService.class);
        channelMessagingService.subscribe(Channels.telemetryUpdates, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                SerialisableObject payload = t.getPayload();
                if(payload instanceof DataStructure) {                    
                    DataStructure dataStructure = (DataStructure) payload;
                    process(dataStructure, logEventDestination);
                }
            }
        });
    }

    protected void process(DataStructure telemetry, Destination<LogEvent> logEventDestination) {

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumFractionDigits(1);

        // This could be machine or process telemetry...
        String host = telemetry.getKey(DataStructure.Keys.host).toString();
        String address = telemetry.getKey(DataStructure.Keys.ip).toString();
        Long pid = telemetry.getLongKey(DataStructure.Keys.pid);
        if (pid == null) {
            pid = -1L;
        }

        if (telemetry.containsValue(Values.Top)) {
            DefaultLogEvent event = formatTop(telemetry, host, address, pid);
            logEventDestination.send(event);
        }

        if (telemetry.containsValue(Values.Netstat)) {
            DefaultLogEvent event = formatNetstat(telemetry, host, address, pid);
            logEventDestination.send(event);
        }

        if (telemetry.containsValue(Values.NetstatStatistics)) {
            DefaultLogEvent event = formatNetstatStatistics(telemetry, host, address, pid);
            logEventDestination.send(event);
        }

        if (telemetry.getValue(DataStructure.Values.SIGAR_OS_Cpu_Idle_Time) != null) {
            DefaultLogEvent event = formatMachine(telemetry, nf, host, address, pid);
            logEventDestination.send(event);
        }

        if (telemetry.getValue(DataStructure.Values.JVM_Process_Memory_Maximum) != null) {
            DefaultLogEvent event = formatProcess(telemetry, nf, host, address, pid);
            logEventDestination.send(event);
        }

        if (telemetry.getValue(DataStructure.Values.VMSTAT_CPU_Idle) != null) {
            DefaultLogEvent event = formatVMStat(telemetry, nf, host, address);
            logEventDestination.send(event);
        }

    }

    private DefaultLogEvent formatTop(DataStructure telemetry, String host, String address, Long pid) {
        String top = telemetry.getValue(Values.Top).asString();
        DefaultLogEvent event = new DefaultLogEvent();
        event.setPid(pid.intValue());
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(Logger.info);
        event.setSourceHost(host);
        event.setSourceAddress(address);
        event.setSourceApplication("TelemetryAgent");
        event.setThreadName("telemetry");
        event.setSourceClassName("telemetry");
        event.setSourceMethodName("top");
        event.setMessage(top);
        event.setChannel("telemetry/top");
        return event;
    }

    private DefaultLogEvent formatNetstatStatistics(DataStructure telemetry, String host, String address, Long pid) {
        String top = telemetry.getValue(Values.NetstatStatistics).asString();
        DefaultLogEvent event = new DefaultLogEvent();
        event.setPid(pid.intValue());
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(Logger.info);
        event.setSourceHost(host);
        event.setSourceAddress(address);
        event.setSourceApplication("TelemetryAgent");
        event.setThreadName("telemetry");
        event.setSourceClassName("telemetry");
        event.setSourceMethodName("netstat-statistics");
        event.setMessage(top);
        event.setChannel("telemetry/netstat/statistics");
        return event;
    }

    private DefaultLogEvent formatNetstat(DataStructure telemetry, String host, String address, Long pid) {
        String top = telemetry.getValue(Values.Netstat).asString();
        DefaultLogEvent event = new DefaultLogEvent();
        event.setPid(pid.intValue());
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(Logger.info);
        event.setSourceHost(host);
        event.setSourceAddress(address);
        event.setSourceApplication("TelemetryAgent");
        event.setThreadName("telemetry");
        event.setSourceClassName("telemetry");
        event.setSourceMethodName("netstat");
        event.setMessage(top);
        event.setChannel("telemetry/netstat");
        return event;
    }

    private DefaultLogEvent formatMachine(DataStructure telemetry, NumberFormat nf, String host, String address, Long pid) {
        Double cpuUser = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Cpu_User_Time);
        Double cpuSystem = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Cpu_System_Time);
        Double cpuWait = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Cpu_Wait_Time);
        Double cpuIdle = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Cpu_Idle_Time);

        Double networkTx = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Network_Bytes_Sent);
        Double networkRx = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Network_Bytes_Received);

        Double memoryActualFree = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Memory_Actual_Free);
        Double memoryActualUsed = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Memory_Actual_Used);
        Double memoryFree = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Memory_Free);
        Double memoryFreePercent = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Memory_Free_Percent);
        Double memoryRam = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Memory_Ram);
        Double memoryTotal = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Memory_Total);
        Double memoryUsed = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Memory_Used);
        Double memoryUsedPercent = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Memory_Used_Percent);

        DefaultLogEvent event = new DefaultLogEvent();
        event.setPid(pid.intValue());
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(Logger.info);
        event.setSourceHost(host);
        event.setSourceAddress(address);
        event.setSourceApplication("TelemetryAgent");
        event.setThreadName("telemetry");
        event.setSourceClassName("telemetry");
        event.setSourceMethodName("machine");
        event.setMessage(StringUtils.format("sigar-os user={}% system={}% wait={}% idle={}% memactfree={} memactused={} memfree={} ({}%) memram={} memtotal={} memused={} ({}%) nettx={} netrx={}",
                                            nf.format(cpuUser),
                                            nf.format(cpuSystem),
                                            nf.format(cpuWait),
                                            nf.format(cpuIdle),
                                            ByteUtils.formatMB(memoryActualFree),
                                            ByteUtils.formatMB(memoryActualUsed),
                                            ByteUtils.formatMB(memoryFree),
                                            nf.format(memoryFreePercent),
                                            ByteUtils.formatMB(memoryRam),
                                            ByteUtils.formatMB(memoryTotal),
                                            ByteUtils.formatMB(memoryUsed),
                                            nf.format(memoryUsedPercent),
                                            ByteUtils.formatKB(networkTx),
                                            ByteUtils.formatKB(networkRx)));
        event.setChannel("telemetry");
        return event;
    }

    private DefaultLogEvent formatProcess(DataStructure telemetry, NumberFormat nf, String host, String address, Long pid) {
        String processName = telemetry.getKey(DataStructure.Keys.processName).asString();

        Double jvmMemoryMax = telemetry.getDoubleValue(DataStructure.Values.JVM_Process_Memory_Maximum);
        Double jvmMemoryTotal = telemetry.getDoubleValue(DataStructure.Values.JVM_Process_Memory_Total);
        Double jvmMemoryUsed = telemetry.getDoubleValue(DataStructure.Values.JVM_Process_Memory_Used);

        Double processCPUSystem = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Process_Cpu_System_Time);
        Double processCPUUser = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Process_Cpu_User_Time);

        Double processOSMemory = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Process_Memory_Size);
        Double processOSResident = telemetry.getDoubleValue(DataStructure.Values.SIGAR_OS_Process_Memory_Resident);

        DefaultLogEvent event = new DefaultLogEvent();
        event.setLevel(Logger.info);
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setSourceHost(host);
        event.setSourceAddress(address);
        event.setSourceApplication(processName);
        event.setThreadName("telemetry");
        event.setSourceClassName("telemetry");
        event.setSourceMethodName("process");
        event.setChannel("telemetry");
        // TODO : not sure I like the look of this?
        event.setPid(pid.intValue());

        double jvmMemoryUsedPercent;

        if (jvmMemoryUsed != null && jvmMemoryMax != null) {
            jvmMemoryUsedPercent = 100d * jvmMemoryUsed.doubleValue() / jvmMemoryMax.doubleValue();
        }
        else {
            jvmMemoryUsedPercent = -1;
        }

        event.setMessage(StringUtils.format("sigar-process user={}% system={}% jvmmemoryused={} ({}% of max) jvmmemorytotal={} jvmmemorymax={} osmemory={} osmemoryresident={}",
                                            nf.format(processCPUUser),
                                            nf.format(processCPUSystem),
                                            ByteUtils.formatMB(jvmMemoryUsed),
                                            nf.format(jvmMemoryUsedPercent),
                                            ByteUtils.formatMB(jvmMemoryTotal),
                                            ByteUtils.formatMB(jvmMemoryMax),
                                            ByteUtils.formatMB(processOSMemory),
                                            ByteUtils.formatMB(processOSResident)));
        return event;
    }

    private DefaultLogEvent formatVMStat(DataStructure telemetry, NumberFormat nf, String host, String address) {
        String processName = telemetry.getKey(DataStructure.Keys.processName).asString();

        int runQueue = telemetry.getIntValue(DataStructure.Values.VMSTAT_Processes_Run_Queue);
        int blocking = telemetry.getIntValue(DataStructure.Values.VMSTAT_Processes_Blocking);

        int free = telemetry.getIntValue(DataStructure.Values.VMSTAT_Memory_Free);

        int swapin = telemetry.getIntValue(DataStructure.Values.VMSTAT_Swap_In);
        int swapout = telemetry.getIntValue(DataStructure.Values.VMSTAT_Swap_Out);

        int ioin = telemetry.getIntValue(DataStructure.Values.VMSTAT_IO_Blocks_In);
        int ioout = telemetry.getIntValue(DataStructure.Values.VMSTAT_IO_Blocks_Out);

        int interupts = telemetry.getIntValue(DataStructure.Values.VMSTAT_System_Interupts);
        int contextswitches = telemetry.getIntValue(DataStructure.Values.VMSTAT_System_Context_Switches);

        int user = telemetry.getIntValue(DataStructure.Values.VMSTAT_CPU_User);
        int system = telemetry.getIntValue(DataStructure.Values.VMSTAT_CPU_System);
        int idle = telemetry.getIntValue(DataStructure.Values.VMSTAT_CPU_Idle);
        int wait = telemetry.getIntValue(DataStructure.Values.VMSTAT_CPU_Waiting);

        DefaultLogEvent event = new DefaultLogEvent();
        event.setLevel(Logger.info);
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setSourceHost(host);
        event.setSourceAddress(address);
        event.setSourceApplication(processName);
        event.setThreadName("telemetry");
        event.setSourceClassName("telemetry");
        event.setSourceMethodName("process");
        event.setChannel("telemetry/vmstat");

        event.setMessage(StringUtils.format("vmtstat r={}% b={}% free={} swapin={} swapout={} ioin={} ioout={} interupts={} cs={} us={} sy={} id={} wa={}",
                                            nf.format(runQueue),
                                            nf.format(blocking),
                                            ByteUtils.formatMB((double) free),
                                            nf.format(swapin),
                                            nf.format(swapout),
                                            nf.format(ioin),
                                            nf.format(ioout),
                                            nf.format(interupts),
                                            nf.format(contextswitches),
                                            user,
                                            system,
                                            idle,
                                            wait));
        return event;
    }
    
    @Override public void start() {}

    @Override public void stop() {}

}
