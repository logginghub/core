package com.logginghub.logging.servers;

import com.logginghub.logging.servers.DispatchQueue.DispatchQueueConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.Source;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Throttler;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Configures;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DispatchQueue<T> implements Module<DispatchQueueConfiguration>, Source<T>, Destination<T> {

    private static final Logger logger = Logger.getLoggerFor(DispatchQueue.class);
    private DispatchQueueConfiguration configuration;
    private ThreadPoolExecutor pool;
    private BlockingQueue<Runnable> queue;
    private Multiplexer<T> multiplexer = new Multiplexer<T>();
    private Throttler throttler;
    private Throttler discardThrottler;
    private Throttler diskSpaceThrottler;

    @Override public void configure(DispatchQueueConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        throttler = new Throttler(10, TimeUnit.SECONDS);
        discardThrottler = new Throttler(10, TimeUnit.SECONDS);
        diskSpaceThrottler = new Throttler(10, TimeUnit.SECONDS);
    }

    @Override public void start() {
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        queue = pool.getQueue();
    }

    @Override public void stop() {
        if (pool != null) {
            pool.shutdownNow();
            pool = null;
        }
    }

    @Override public void send(final T t) {
        if (queue != null) {
            int size = queue.size();
            if (size < configuration.asynchronousQueueDiscardSize && pool != null) {
                pool.execute(new Runnable() {
                    public void run() {
                        try {
                            multiplexer.send(t);
                        }
                        catch (Exception e) {
                            if (e.getCause() != null &&
                                e.getCause() instanceof IOException &&
                                e.getCause().getMessage().contains("There is not enough space on the disk")) {
                                if (diskSpaceThrottler.isOkToFire()) {
                                    logger.warn("Failed to write log event to the '{}' log file : you've run out of disk space (this message will repeat every 10 seconds until the problem is resolved)",
                                                configuration.name);
                                }
                            }
                            else {
                                logger.warn(e, "Failed to write to the '{}' : {}", configuration.name, e.getMessage());
                            }
                        }
                    }
                });
            }
            else {
                if (discardThrottler.isOkToFire()) {
                    logger.warn("The asynchronous queue writing to '{}' has reached {} elements; we are discarding all new entries until the issue is resolved",
                                configuration.name,
                                size);
                }
            }

            if (size > configuration.asynchronousQueueWarningSize && throttler.isOkToFire()) {
                logger.warn("The asynchronous queue writing to '{}' has reached  {} elements; it doesn't look like the downstream handlers are keeping up",
                            configuration.name,
                            size);
            }
        }
    }

    @Override public void addDestination(Destination<T> listener) {
        multiplexer.addDestination(listener);
    }

    @Override public void removeDestination(Destination<T> listener) {
        multiplexer.removeDestination(listener);
    }

    public void waitForQueueToDrain() {
        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return queue.size() == 0;

            }
        });
    }

    @Configures(DispatchQueue.class) public static class DispatchQueueConfiguration {
        @XmlAttribute int asynchronousQueueDiscardSize = 10000;
        @XmlAttribute public int asynchronousQueueWarningSize = 30000;
        @XmlAttribute public String name = "<queue name>";

        public int getAsynchronousQueueDiscardSize() {
            return asynchronousQueueDiscardSize;
        }

        public void setAsynchronousQueueDiscardSize(int asynchronousQueueDiscardSize) {
            this.asynchronousQueueDiscardSize = asynchronousQueueDiscardSize;
        }

        public int getAsynchronousQueueWarningSize() {
            return asynchronousQueueWarningSize;
        }

        public void setAsynchronousQueueWarningSize(int asynchronousQueueWarningSize) {
            this.asynchronousQueueWarningSize = asynchronousQueueWarningSize;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
