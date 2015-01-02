package com.logginghub.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Makes closing things down easier - register closers and then just call close to do everything
 * 
 */
public class Closer {

    private List<Closeable> closers = new CopyOnWriteArrayList<Closeable>();
    private ExceptionHandler exceptionHandler = ExceptionHandler.noop;

    public synchronized void register(Closeable c) {
        closers.add(c);
    }

    public synchronized void close() throws IOException {
        for (Closeable c : closers) {
            c.close();
        }
        closers.clear();
    }

    public synchronized void closeQuietly() {
        for (Closeable c : closers) {
            try {
                c.close();
            }
            catch (IOException e) {
                exceptionHandler.handleException("Closing closable : " + c, e);
            }
        }
        closers.clear();
    }
    
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

}
