package com.logginghub.logging.telemetry;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.MultiProcCpu;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.data.DataStructure;

public class SigarMachineTelemetryGenerator implements Asynchronous, MachineTelemetryGenerator {

    private WorkerThread timer;

    private Sigar sigar;
    private CpuPerc cpu;

    private DataStructure dataStructure = new DataStructure(DataStructure.Types.Telemetry);
    private String[] netInterfaceList;

    private long lastSent = 0;
    private long lastReceived = 0;
    private long pid;

    private String interval = "1 seconds";
    
    private Multiplexer<DataStructure> dataStructureMultiplexer = new Multiplexer<DataStructure>();

    private String host = NetUtils.getLocalHostname();
    private String ip = NetUtils.getLocalIP();
    
    public SigarMachineTelemetryGenerator() {

        try {
            sigar = new Sigar();
            cpu = sigar.getCpuPerc();
            pid = sigar.getPid();

            netInterfaceList = sigar.getNetInterfaceList();
        }
        catch (SigarException e) {
            throw new RuntimeException(e);
        }
        catch(NoClassDefFoundError noClassDefFoundError) {
            // jshaw - sigar has failed to load
        }


        dataStructure.addKey(DataStructure.Keys.host, host);
        dataStructure.addKey(DataStructure.Keys.ip, ip);
        dataStructure.addKey(DataStructure.Keys.pid, pid);
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getInterval() {
        return interval;
    }

    public void start() {
        stop();
        
        timer = WorkerThread.executeDaemonOngoing("MachineTelemetryGenerator", TimeUtils.parseInterval(interval), new Runnable() {
            public void run() {
                updateTelemetry();
                dataStructureMultiplexer.send(dataStructure);
            }
        });
    }
    
    public Multiplexer<DataStructure> getDataStructureMultiplexer() {
        return dataStructureMultiplexer;
    }

    protected void updateTelemetry() {

        if(sigar != null) {
            try {
                cpu = sigar.getCpuPerc();
                Mem mem = sigar.getMem();

                long totalSent = 0;
                long totalReceived = 0;
                for (String string : netInterfaceList) {
                    NetInterfaceStat netInterfaceStat = sigar.getNetInterfaceStat(string);

                    totalSent += netInterfaceStat.getTxBytes();
                    totalReceived += netInterfaceStat.getRxBytes();
                }

                if (lastSent > 0) {
                    long deltaSent = totalSent - lastSent;
                    long deltaReceived = totalReceived - lastReceived;

                    dataStructure.addValue(DataStructure.Values.SIGAR_OS_Network_Bytes_Sent, deltaSent);
                    dataStructure.addValue(DataStructure.Values.SIGAR_OS_Network_Bytes_Received, deltaReceived);
                }

                lastReceived = totalReceived;
                lastSent = totalSent;

                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Cpu_User_Time, 100d * cpu.getUser());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Cpu_System_Time, 100d * cpu.getSys());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Cpu_Wait_Time, 100d * cpu.getWait());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Cpu_Idle_Time, 100d * cpu.getIdle());

                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Memory_Actual_Free, mem.getActualFree());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Memory_Actual_Used, mem.getActualUsed());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Memory_Free, mem.getFree());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Memory_Free_Percent, mem.getFreePercent());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Memory_Ram, mem.getRam());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Memory_Total, mem.getTotal());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Memory_Used, mem.getUsed());
                dataStructure.addValue(DataStructure.Values.SIGAR_OS_Memory_Used_Percent, mem.getUsedPercent());
            } catch (SigarException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public long getCpuTime() {
        return new MultiProcCpu().getTotal();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

}
