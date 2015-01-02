package com.logginghub.utils.net.objectclient;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

/**
 * Simple object io socket client. It will continue trying to connect and
 * receive messages until you stop the worker thread.
 * 
 * @author James
 * 
 */
public class ObjectSocketClient extends WorkerThread {
    private final int m_port;
    private final String m_server;
    private Socket m_socket;
    private ObjectInputStream m_objectInputStream;
    private ObjectOutputStream m_objectOutputStream;
    private long m_reconnectionTimeout = 1000;

    private static final Logger logger = Logger.getLoggerFor(ObjectSocketClient.class);
    private List<ObjectSocketClientListener> m_listeners = new CopyOnWriteArrayList<ObjectSocketClientListener>();

    private Map<Status, CountDownLatch> m_statusLatches = new ConcurrentHashMap<Status, CountDownLatch>();

    private Status m_status = Status.Disconnected;

    public enum Status {
        Connecting,
        Connected,
        Disconnected;
    }

    public ObjectSocketClient(String server, int port) {
        super("SocketClient");
        m_server = server;
        m_port = port;
    }

    public void waitForStatus(Status status) throws InterruptedException {
        if (m_status != status) {
            CountDownLatch countDownLatch = m_statusLatches.get(status);
            if (countDownLatch == null) {
                countDownLatch = new CountDownLatch(1);
                m_statusLatches.put(status, countDownLatch);
            }
            countDownLatch.await();
        }
    }

    private void changeStatus(Status status) {
        if (m_status != status) {
            Status oldStatus = m_status;
            m_status = status;

            fireStatusChanged(oldStatus, status);

            CountDownLatch latch = m_statusLatches.remove(status);
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    @Override protected void onRun() throws IOException, ClassNotFoundException {
        try {
            ensureConnected();
        }
        catch (EOFException e) {
            // The socket was closed, this isn't an error
            disconnect();
        }
        catch (IOException e) {
            logger.fine("Connection failed, waiting {} ms before we try again", m_reconnectionTimeout);

            waitForReconnectionTime();
        }

        if (isConnected()) {
            try {
                waitForMessage();
            }
            catch (EOFException e) {
                // The socket was closed, this isn't an error
                disconnect();
            }
            catch (SocketException se) {
                // TODO : is there a better way of handling this?
                if (se.getMessage().equals("socket closed") || se.getMessage().equals("Connection reset")) {
                    // The socket was closed, this isn't an error
                    disconnect();
                }
                else {
                    throw se;
                }
            }
            catch (IOException e) {
                disconnect();
                throw e;
            }
            catch (ClassNotFoundException e) {
                disconnect();
                throw e;
            }
        }
    }

    private void waitForReconnectionTime() {
        try {
            Thread.sleep(m_reconnectionTimeout);
        }
        catch (InterruptedException e1) {}
    }

    private synchronized boolean isConnected() {
        return m_socket != null;
    }

    private synchronized void disconnect() {
        if (m_socket != null) {
            FileUtils.closeQuietly(m_objectInputStream);
            FileUtils.closeQuietly(m_objectOutputStream);
            FileUtils.closeQuietly(m_socket);
        }

        changeStatus(Status.Disconnected);
    }

    @Override protected void beforeStop() {
        super.beforeStop();
        disconnect();
    }

    private void waitForMessage() throws IOException, ClassNotFoundException {
        Object object = m_objectInputStream.readObject();
        fireNewObject(object);
    }

    public synchronized void sendObject(Object object) throws IOException {
        m_objectOutputStream.writeObject(object);
        m_objectOutputStream.flush();
    }

    private synchronized void ensureConnected() throws IOException {
        if (!isConnected()) {
            changeStatus(Status.Connecting);
            logger.fine("Attempting to connect to {}:{}", m_server, m_port);

            m_socket = new Socket(m_server, m_port);

            logger.fine("Successfully connected to {}:{}, initialising streams...", m_server, m_port);

            OutputStream outputStream = m_socket.getOutputStream();
            InputStream inputStream = m_socket.getInputStream();

            m_objectOutputStream = new ObjectOutputStream(outputStream);
            m_objectInputStream = new ObjectInputStream(inputStream);

            logger.fine("Succesfully connected");

            changeStatus(Status.Connected);
        }
    }

    public void addSocketClientListener(ObjectSocketClientListener listener) {
        m_listeners.add(listener);
    }

    public void removeSocketClientListener(ObjectSocketClientListener listener) {
        m_listeners.remove(listener);
    }

    private void fireNewObject(Object object) {
        for (ObjectSocketClientListener listener : m_listeners) {
            listener.onNewObject(object);
        }
    }

    private void fireStatusChanged(Status oldStatus, Status newStatus) {
        for (ObjectSocketClientListener listener : m_listeners) {
            listener.onStatusChanged(oldStatus, newStatus);
        }
    }

    public void setReconnectionTimeout(long i) {
        m_reconnectionTimeout = i;
    }

    public long getReconnectionTimeout() {
        return m_reconnectionTimeout;
    }

    public synchronized Status getStatus() {
        return m_status;
    }
}
