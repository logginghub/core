package com.logginghub.utils;


/**
 * Extends the WorkerThread to make it easy to wrap Runnable tasks with worker threads.
 * 
 * @author James
 * 
 */
public class RunnableWorkerThread extends WorkerThread
{
    private Runnable m_runnable;

    public RunnableWorkerThread(String name, Runnable runnable)
    {
        super(name);
        m_runnable = runnable;
    }
    
    public RunnableWorkerThread(Runnable runnable)
    {
        super("RunnableWorkerThread");
        m_runnable = runnable;
    }

    @Override protected void onRun()
    {
        m_runnable.run();
    }
}
