package com.logginghub.utils.logging;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import com.logginghub.utils.Stream;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.WorkerThread;

public class UDPListener extends WorkerThread {

    public UDPListener() {
        super("UDPListener");
    }

    public int port = VLPorts.getSocketHubUDPPort();
    private DatagramSocket serverSocket;

    public static class PatternisedUDPData {
        public int patternID;
        public String[] parameters;
        public String host;
        public String hostIP;
        public int pid;
        public String applicationName;

        public PatternisedUDPData(String host, String ip, int pid, String applicationName, int patternID, String[] parameters) {
            super();
            this.applicationName = applicationName;
            this.pid = pid;
            this.host = host;
            this.hostIP = ip;
            this.patternID = patternID;
            this.parameters = parameters;
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public Stream<PatternisedUDPData> getEventStream() {
        return eventStream;
    }

    private Stream<PatternisedUDPData> eventStream = new Stream<UDPListener.PatternisedUDPData>();

    @Override protected void onRun() throws Throwable {
        if (serverSocket == null) {
            createSocket();
        }
        else {
            byte[] receiveData = new byte[576];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            byte[] data = receivePacket.getData();
            ByteBuffer buffer = ByteBuffer.wrap(data);

            int version = buffer.get();
            if (version == 1) {
                int pid = buffer.getInt();

                String applicationName = decodeString(buffer);
                int patternID = buffer.getShort();
                int count = buffer.getShort();

                String[] parameters = new String[count];
                for (int i = 0; i < count; i++) {
                    parameters[i] = decodeString(buffer);
                }

                eventStream.send(new PatternisedUDPData(receivePacket.getAddress().getHostName(), receivePacket.getAddress().getHostAddress(), pid, applicationName, patternID, parameters));
            }

        }
    }

    public void close() {
        serverSocket.close();
        stop();
    }
    
    private String decodeString(ByteBuffer buffer) {
        int length = buffer.getShort();
        byte[] stringData = new byte[length];
        buffer.get(stringData);
        return new String(stringData);
    }

    private void createSocket() throws SocketException {
        serverSocket = new DatagramSocket(port);
    }
}
