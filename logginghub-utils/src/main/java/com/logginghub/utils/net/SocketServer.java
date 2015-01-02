package com.logginghub.utils.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.WorkerThread;

public class SocketServer {
    private static Logger logger = Logger.getLogger(SocketServer.class.getName());
    private WorkerThread m_acceptorThread;
    private int m_port;
    private ServerSocket m_serverSocket;
    private ExceptionHandler m_exceptionHandler;

    private List<SocketServerListener> m_listeners = new CopyOnWriteArrayList<SocketServerListener>();
    private boolean m_shuttingDown = false;

    private CountDownLatch m_boundLatch = new CountDownLatch(1);

    public SocketServer(int port, ExceptionHandler exceptionHandler) {
        m_port = port;
        m_exceptionHandler = exceptionHandler;
    }

    public void addServerSocketConnectorListener(SocketServerListener listener) {
        m_listeners.add(listener);
    }

    public void removeServerSocketConnectorListener(SocketServerListener listener) {
        m_listeners.remove(listener);
    }

    public int getPort() {
        return m_port;
    }

    public void start() {
        if (m_acceptorThread != null) {
            throw new RuntimeException("You've started the server socket connector acceptor thread already.");
        }

        m_acceptorThread = new WorkerThread("ServerSocketConnectorAcceptorThread") {
            @Override protected void onRun() throws IOException {
                ensureBound();
                accept();
            }
        };

        m_acceptorThread.start();
    }

    private synchronized void ensureBound() throws IOException {
        if (m_serverSocket == null) {
            m_serverSocket = new ServerSocket(getPort());
            logger.info(String.format("Successfully bound to port %d", getPort()));

            m_boundLatch.countDown();

        }
    }

    private void accept() {
        try {
            Socket socket = m_serverSocket.accept();

            List<SocketServerListener> listeners = m_listeners;
            for (SocketServerListener socketServerListener : listeners) {
                socketServerListener.onAccepted(socket);
            }
        }
        catch (IOException e) {
            if (!m_shuttingDown) {
                m_exceptionHandler.handleException("Exception caught from accept call or listener onAccepted notification", e);
            }
        }
    }

    public void stop() {
        logger.info(String.format("SocketServer stopping"));

        m_shuttingDown = true;
        FileUtils.closeQuietly(m_serverSocket);
        m_acceptorThread.stop();
    }

    public void waitUntilBound() {
        try {
            m_boundLatch.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(String.format("Thread interupted waiting for bound latch"), e);
        }
    }
}
