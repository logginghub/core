package com.logginghub.logging.telemetry;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.MemorySnapshot;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;

public class SigarProcessTelemetryGenerator implements Asynchronous, ProcessTelemetryGenerator {

    private WorkerThread timer;

    private Sigar sigar;
    private long pid;
    private ProcCpu procCpu;
//    private TelemetryData telemetry = new TelemetryData();
    private DataStructure dataStructure = new DataStructure(DataStructure.Types.Telemetry);
    private ProcMem procMem;
    private int cores = Runtime.getRuntime().availableProcessors();
    private long lastTime;
    private long lastSys;
    private long lastUser;
    private String interval = "1 second";
    
    private String host = NetUtils.getLocalHostname();
    private String ip = NetUtils.getLocalIP();

    private Multiplexer<DataStructure> dataStructureMultiplexer = new Multiplexer<DataStructure>();
//    private Multiplexer<TelemetryData> telemetryDataMultiplexer = new Multiplexer<TelemetryData>();
    
    public SigarProcessTelemetryGenerator(String processName) {
        try {
            sigar = new Sigar();
            pid = sigar.getPid();
            procCpu = sigar.getProcCpu(pid);
            procMem = sigar.getProcMem(pid);
        }
        catch (SigarException e) {
            throw new RuntimeException(e);
        }catch(UnsatisfiedLinkError unsatisfiedLinkError) {
            // jshaw - no sigar support
        }

        dataStructure.addKey(DataStructure.Keys.host, host);
        dataStructure.addKey(DataStructure.Keys.ip, ip);
        dataStructure.addKey(DataStructure.Keys.pid, pid);
        dataStructure.addKey(DataStructure.Keys.processName, processName);
    }

    public Multiplexer<DataStructure> getDataStructureMultiplexer() {
        return dataStructureMultiplexer;
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

    protected void updateTelemetry() {

        if(sigar != null) {
            try {
                procCpu.gather(sigar, pid);
                procMem.gather(sigar, pid);
                double cpuPercent = procCpu.getPercent() / sigar.getCpuList().length;

                long now = System.currentTimeMillis();
                long elapsed = now - lastTime;
                long sysTimeMS = procCpu.getSys();
                long userTimeMS = procCpu.getUser();
                long totalTime = elapsed * cores;

                long deltaSys = sysTimeMS - lastSys;
                long deltaUsr = userTimeMS - lastUser;

                double sysPercentage = 100d * ((deltaSys) / (double) totalTime);
                double userPercentage = 100d * ((deltaUsr) / (double) totalTime);

                MemorySnapshot snapshot = MemorySnapshot.createSnapshot();

                dataStructure.addValue(Values.SIGAR_OS_Process_Cpu_Percentage, cpuPercent);
                dataStructure.addValue(Values.SIGAR_OS_Process_Cpu_System_Time, cpuPercent);
                dataStructure.addValue(Values.SIGAR_OS_Process_Cpu_User_Time, cpuPercent);

                dataStructure.addValue(Values.SIGAR_OS_Process_Memory_Size, cpuPercent);
                dataStructure.addValue(Values.SIGAR_OS_Process_Memory_Resident, cpuPercent);

                dataStructure.addValue(Values.JVM_Process_Memory_Maximum, snapshot.getMaxMemory());
                dataStructure.addValue(Values.JVM_Process_Memory_Total, snapshot.getTotalMemory());
                dataStructure.addValue(Values.JVM_Process_Memory_Used, snapshot.getUsed());

                lastTime = now;
                lastUser = userTimeMS;
                lastSys = sysTimeMS;
            } catch (SigarException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void setProcessName(String processName) {
        dataStructure.addKey(DataStructure.Keys.processName, processName);
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

}
