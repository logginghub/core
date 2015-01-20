package com.logginghub.messaging_experimental;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import com.logginghub.utils.ArrayUtils;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.WorkerThread;

import edu.emory.mathcs.backport.java.util.concurrent.BlockingDeque;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingDeque;

public class MessagingClient {
    private List<InetSocketAddress> connectionPoints = new CopyOnWriteArrayList<InetSocketAddress>();
    private int nextConnectionPoint = -1;
    private CountDownLatch readLatch = new CountDownLatch(1);
    private long reconnectionDelay = 5000;
    private ExceptionHandler exceptionHandler;
    private BlockingDeque writeQueue = new LinkedBlockingDeque();

    private MessageSerializer messageSerializer = new Version0MessageSerializer();
    private MessageSerializer[] messageSerializers = new MessageSerializer[] { messageSerializer };

    private Socket socket;

    private WorkerThread connectionThread = new WorkerThread("ConnectionThread") {
        @Override protected void onRun() throws Throwable {
            connectionThreadMain();
        }
    };

    private WorkerThread readThread = new WorkerThread("ReadThread") {
        @Override protected void onRun() throws Throwable {
            readThreadMain();
        }
    };

    public void addConnectionPoint(InetSocketAddress connectionPoint) {
        connectionPoints.add(connectionPoint);
        if (nextConnectionPoint == -1) {
            nextConnectionPoint = 0;
        }
    }

    public synchronized void start() {
        if (connectionPoints.isEmpty()) {
            throw new RuntimeException("Please add one or more connection points before you start the client, otherwise we dont know where to connect to.");
        }

        if (!connectionThread.isRunning()) {
            connectionThread.start();
            readThread.start();
        }
    }

    public synchronized void stop() {
        connectionThread.stop();
        readThread.stop();
    }

    protected void readThreadMain() {
        try {
            readLatch.await();
            runReadLoop();
        }
        catch (InterruptedException e) {}
    }

    protected void connectionThreadMain() {
        Socket socket = new Socket();
        InetSocketAddress address = getNextConnectionPoint();
        // TODO : set the socket settings
        try {
            socket.connect(address);
            handleSuccessfulConnect(socket);
        }
        catch (IOException e) {
            if (exceptionHandler != null) {
                exceptionHandler.handleException("Exception caught from connect", e);
            }
            connectionDelay();
        }
    }

    private void handleSuccessfulConnect(Socket socket) {
        this.socket = socket;
        readLatch.countDown();
        // Refresh the latch straight away in case we disconnect almost
        // instantly
        readLatch = new CountDownLatch(1);
        runWriteLoop();
    }

    private void runWriteLoop() {
        while (socket != null && connectionThread.isRunning())

            try {
                Message message = (Message) writeQueue.takeFirst();
                writeMessage(message);
            }
            catch (InterruptedException e) {
                // Not sure we care about this, only socket errors
            }
    }

    private void runReadLoop() {
        byte[] readBuffer = new byte[4 * 1024 * 10];
        int endMark = 0;

        try {
            InputStream inputStream = socket.getInputStream();
            BufferedInputStream bos = new BufferedInputStream(inputStream);

            while (socket != null && readThread.isRunning()) {
                try {
                    if (endMark == readBuffer.length) {
                        readBuffer = ArrayUtils.doubleSize(readBuffer);
                    }
                    int read = bos.read(readBuffer);
                    endMark += read;

                    attemptToDecode(readBuffer, endMark);
                }
                catch (IOException e) {
                    if (exceptionHandler != null) {
                        exceptionHandler.handleException("Exception caught from read or decode", e);
                    }
                    disconnect();
                }
            }
        }
        catch (IOException e) {
            if (exceptionHandler != null) {
                exceptionHandler.handleException("Exception caught initialising streams", e);
            }
            disconnect();
        }
    }

    private void attemptToDecode(byte[] readBuffer, int endMarker) {
        if (endMarker >= 1) {
            ByteBuffer wrapped = ByteBuffer.wrap(readBuffer);
            int version = (int) wrapped.get();

            MessageSerializer serializerForVersion = messageSerializers[version];
            serializerForVersion.attemptToDecode(wrapped);
        }
    }

    private void writeMessage(Message message) {
        ByteBuffer serialized = messageSerializer.serialize(message);

        try {
            socket.getOutputStream().write(serialized.array(), 0, serialized.remaining());
        }
        catch (IOException e) {
            if (exceptionHandler != null) {
                exceptionHandler.handleException("Exception caught writing message", e);
            }
            disconnect();
        }
    }

    private void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException e) {}
            socket = null;
        }
        readThread.interupt();
    }

    private void connectionDelay() {
        try {
            Thread.sleep(reconnectionDelay);
        }
        catch (InterruptedException e) {}
    }

    private InetSocketAddress getNextConnectionPoint() {
        InetSocketAddress inetSocketAddress = connectionPoints.get(nextConnectionPoint);
        nextConnectionPoint++;
        if (nextConnectionPoint == connectionPoints.size()) {
            nextConnectionPoint = 0;
        }
        return inetSocketAddress;
    }
}
