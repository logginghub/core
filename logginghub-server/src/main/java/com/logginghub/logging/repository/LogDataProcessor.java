package com.logginghub.logging.repository;

import java.io.File;

import com.logginghub.logging.listeners.LogEventListener;

/**
 * Implement this to partake in the ancient art of log event processing.
 * Instances are instantiated at runtime from the configuration of the
 * DataFileProcessor, and passed any log events that are decoded during the
 * batch run.
 * 
 * @author James
 * 
 */
public interface LogDataProcessor extends LogEventListener {

    /**
     * Signals the start of processing to the processor, and provides a folder
     * in which the processor can generate any artifacts it needs to.
     * 
     * @param resultsFolder
     */
    void processingStarted(File resultsFolder);

    /**
     * Signals that processing has finished so that the processor can complete
     * any end-of-batch activities.
     */
    void processingEnded();

}
