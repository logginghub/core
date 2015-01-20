package com.logginghub.logging.modules;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.logginghub.logging.modules.configuration.ZeroCopyHubConfiguration;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class ZeroCopyHubModule implements Module<ZeroCopyHubConfiguration> {

    private ZeroCopyHubConfiguration configuration;
    private ServerSocketChannel listener;

    private static final Logger logger = Logger.getLoggerFor(ZeroCopyHubModule.class);
    private IntegerStat recv;

    @Override public void configure(ZeroCopyHubConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
    }

    @Override public void start() {

        final InetSocketAddress listenAddr = new InetSocketAddress(configuration.getPort());

        WorkerThread serverListener = new WorkerThread("LoggingHub::ServerListener") {
            @Override protected void onRun() throws Throwable {
                try {
                    listener = ServerSocketChannel.open();
                    ServerSocket ss = listener.socket();
                    ss.setReuseAddress(true);
                    ss.bind(listenAddr);
                    logger.info("Listening on port : " + listenAddr.toString());

                    readData();
                }
                catch (IOException e) {
                    logger.warning("Failed to bind, is port : " + listenAddr.toString() + " already in use ? Error Msg : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        StatBundle bundle = new StatBundle();
        recv = bundle.createIncremental("recv");
        bundle.startPerSecond(logger);
        
        serverListener.start();

    }

    private void readData() {
        ByteBuffer dst = ByteBuffer.allocate(4096);
        try {
            while (true) {
                SocketChannel conn = listener.accept();
                logger.info("Accepted : " + conn);
                conn.configureBlocking(true);

                int nread = 0;
                while (nread != -1) {
                    try {
                        nread = conn.read(dst);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        nread = -1;
                    }
                    
                    dst.flip();
//                    System.out.println(dst);

                    while (dst.hasRemaining()) {
                        int level = dst.getInt();
                        int patternID = dst.getInt();
                        long value = dst.getLong();
                        recv.increment();
//                        System.out.println(level + ":: Logging message " + value);
                    }
                    dst.rewind();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void stop() {}

}
