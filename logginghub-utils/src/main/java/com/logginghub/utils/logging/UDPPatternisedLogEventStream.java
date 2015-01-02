package com.logginghub.utils.logging;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.logginghub.utils.VLPorts;

public class UDPPatternisedLogEventStream implements LoggerStream {

    private DatagramSocket datagramSocket;
    private InetAddress address;
    private int port = VLPorts.getSocketHubUDPPort();
    private String applicationName;
    private int pid;

    public UDPPatternisedLogEventStream(int pid, String applicationName, String destination) {
        this.pid = pid;
        this.applicationName = applicationName;
        try {
            datagramSocket = new DatagramSocket();
            address = InetAddress.getByName(destination);
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void onNewLogEvent(LogEvent event) {

        if (event.getPatternID() > 0) {
            byte[] buffer = new byte[576];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

            int version = 1;
            byteBuffer.put((byte) version);
            byteBuffer.putInt(pid);
            putString(byteBuffer, applicationName);
            byteBuffer.putShort((short) event.getPatternID());

            String[] parameters = event.getParameters();
            byteBuffer.putShort((short) parameters.length);
            for (String string : parameters) {
                putString(byteBuffer, string);
            }

            DatagramPacket packet = new DatagramPacket(buffer, byteBuffer.position(), address, port);

            try {
                datagramSocket.send(packet);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void putString(ByteBuffer byteBuffer, String string) {
        byte[] bytes = string.getBytes();
        byteBuffer.putShort((short) bytes.length);
        byteBuffer.put(bytes);
    }

    public static void main(String[] args) {
        GlobalLoggingParameters.applicationName = "appname";
        Logger.root().setupUDPLogging("localhost");
        Logger logger = Logger.getLoggerFor(UDPPatternisedLogEventStream.class);
        logger.info(1, "hello", "world");
    }

}
