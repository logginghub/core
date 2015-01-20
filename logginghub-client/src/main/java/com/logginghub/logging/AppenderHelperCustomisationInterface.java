package com.logginghub.logging;


public interface AppenderHelperCustomisationInterface {
    HeapLogger createHeapLogger();
    CpuLogger createCPULogger();
    GCFileWatcher createGCWatcher();
}
