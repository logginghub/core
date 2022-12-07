package com.logginghub.logging.modules;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.PidHelper;
import com.logginghub.logging.modules.configuration.SocketTextReaderConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Stream;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class SocketTextReaderModule implements Module<SocketTextReaderConfiguration> {
    // private final int port;
    private WorkerThread thread;
    private ServerSocket socket;
    
    private static final Logger logger = Logger.getLoggerFor(SocketTextReaderModule.class);
    public static int defaultPort = 58780;
    // private final SocketHub hub;
    private CountDownLatch boundLatch = new CountDownLatch(1);
    private int pid = PidHelper.getPid();

//    private int level = Integer.getInteger("socketTextReader.level", Level.FINE.intValue());

//    private String startString = System.getProperty("socketTextReader.startString", "socketTextReader : ");
//    private String endString = System.getProperty("socketTextReader.endString", "");
    private SocketTextReaderConfiguration configuration;

    private Stream<LogEvent> logEventStream = new Stream<LogEvent>();
    
    public SocketTextReaderModule() {

    }

    public void start() {
        startThread();
    }

    private void startThread() {
        if (thread == null) {
            thread = new WorkerThread("LoggingHub-SocketTextReaderModule-acceptor") {
                @Override protected void onRun() throws Throwable {
                    run();
                }

                protected void beforeStop() {
                    if (socket != null) {
                        try {
                            socket.close();
                        }
                        catch (IOException e) {}
                    }
                };
            };

            thread.start();
        }
    }

    protected void run() {
        if (socket == null && thread.isRunning()) {
            try {
                socket = new ServerSocket();
                socket.bind(new InetSocketAddress(configuration.getBindAddress(), configuration.getPort()));
                logger.info("SocketTextReader bound to port {} on bind address {}", configuration.getPort(), configuration.getBindAddress());
                boundLatch.countDown();
            }
            catch (IOException e) {
                logger.info("Failed to bind to listening socket on port " + configuration.getPort() + ", waiting and then retrying");
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e1) {}
            }
        }

        if (socket != null && thread.isRunning()) {
            try {
                final Socket accept = socket.accept();

                Thread child = new Thread(new Runnable() {
                    public void run() {
                        try {
                            InputStream inputStream = accept.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sendLine(accept, line);
                            }

                            inputStream.close();
                            accept.close();
                        }
                        catch (IOException e) {
                            logger.info("Socket text connection abandoned from " + socket + " : " + e.getMessage());
                        }
                    }
                });

                child.start();
            }
            catch (IOException e) {

            }
        }
    }

    protected void sendLine(Socket source, String line) {       
        DefaultLogEvent event = configuration.getTemplate().createEvent();
        
        String template = configuration.getTemplate().getMessage();
        String actual = template.replace("${message}", line);
        
        event.setMessage(actual);
        event.setPid(pid);
        logEventStream.send(event);
    }

    public Stream<LogEvent> getLogEventStream() {
        return logEventStream;
    }
    
    public void stop() {
        if (thread != null) {
            thread.dontRunAgain();
            try {
                socket.close();
            }
            catch (IOException e) {
            }
            thread.stop();
        }
    }

    public void waitUntilBound() {
        try {
            boundLatch.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Thread interupted waiting for server socket to bind; we can't be sure what state the socket is in now");
        }
    }

    @Override public void configure(SocketTextReaderConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        
        @SuppressWarnings("unchecked") Destination<LogEvent> destination = discovery.findService(Destination.class,
                                                                                                 LogEvent.class,
                                                                                                 configuration.getEventDestinationRef());
        logEventStream.addDestination(destination);

    }

}
