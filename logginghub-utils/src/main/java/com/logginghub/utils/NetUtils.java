package com.logginghub.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NetUtils {

    public static InetSocketAddress toInetSocketAddress(String hostPortPair, int defaultPort) {
        String[] split = hostPortPair.split(":");

        String hostname = split[0];
        int port;
        if (split.length > 1) {
            port = Integer.parseInt(split[1]);
        }
        else {
            port = defaultPort;
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        return inetSocketAddress;
    }

    /**
     * Parses a comma separated list of ip/host:port pairs. If a port isn't
     * specified, the default port parameter is used instead.
     * 
     * @param list
     * @param defaultPort
     * @return
     */
    public static List<InetSocketAddress> toInetSocketAddressList(String addresses, int defaultPort) {
        String[] split = addresses.split(",");
        return toInetSocketAddressList(split, defaultPort);
    }

    public static List<InetSocketAddress> toInetSocketAddressList(String[] hostPortPairs, int defaultPort) {
        List<InetSocketAddress> addressList = new ArrayList<InetSocketAddress>();

        for (String hostPortPair : hostPortPairs) {
            addressList.add(toInetSocketAddress(hostPortPair.trim(), defaultPort));
        }

        return addressList;

    }

    public static int findFreePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to find free local port to bind the agent to", e);
        }
    }

    public static String getLocalIP() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostAddress = localHost.getHostAddress();
            return hostAddress;
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalHostname() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostName = localHost.getHostName();
            return hostName;
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPortOpen(InetSocketAddress connectionPoint) {
        return isPortOpen(connectionPoint, 100);
    }

    public static boolean isPortOpen(InetSocketAddress connectionPoint, int timeoutMillis) {

        boolean isOpen;

        try {
            Socket socket = new Socket();
            socket.connect(connectionPoint, timeoutMillis);
            try {
                socket.close();
            }
            catch (IOException e) {

            }
            isOpen = true;
        }
        catch (IOException e) {
            isOpen = false;
        }

        return isOpen;
    }

    public static boolean isPortOpen(int port) {
        return isPortOpen(new InetSocketAddress("localhost", port));
    }

    public static void send(String message, InetSocketAddress connectionPoint) {
        try {
            Socket socket = new Socket(connectionPoint.getAddress(), connectionPoint.getPort());
            byte[] bytes = message.getBytes();
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(bytes);

            try {
                outputStream.close();
                socket.close();
            }
            catch (IOException e) {

            }
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to send message to {}", connectionPoint), e);
        }

    }

    public static class PingResult {
        public String host;
        public String replyFrom = "";
        public int size;
        public int time;
        public int ttl;
        
        @Override public String toString() {
            return "PingResult [host=" + host + ", replyFrom=" + replyFrom + ", size=" + size + ", time=" + time + ", ttl=" + ttl + "]";
        }
    }

    public static List<PingResult> tracert(String host) throws UnknownHostException {
        
        InetAddress[] allByName = InetAddress.getAllByName(host);
                
        List<PingResult> results = new ArrayList<NetUtils.PingResult>();
        
        int ttl = 1;
        boolean done = false;
        while(!done) {
            
            PingResult ping = ping(host, ttl);
            results.add(ping);
            Out.out("{}", ping);
            
            ttl++;
            
            for (InetAddress inetAddress : allByName) {
                if(ping.replyFrom.equals(inetAddress.getHostAddress())) {
                    done = true;
                    break;
                }    
            }
        }
        
        return results;
    }
    
    public static PingResult ping(String host) {
        return ping(host, 255);
    }
    
    public static PingResult ping(String host, int ttl) {
        String cmd = "";
        int count = 1;
        if (System.getProperty("os.name").startsWith("Windows")) {
            // For Windows
            cmd = StringUtils.format("ping -n {} -i {} {}", count, ttl, host);            
        }
        else {
            // For Linux and OSX
            cmd = StringUtils.format("ping -c {} -t {} {}", count, ttl, host);
        }

        ProcessWrapper wrapper = ProcessUtils.launch(cmd);
        StringBufferInputStreamReaderThreadListener outputHandler = (StringBufferInputStreamReaderThreadListener) wrapper.getOutputHandler();
        StringBufferInputStreamReaderThreadListener errorHandler = (StringBufferInputStreamReaderThreadListener) wrapper.getErrorHandler();

        wrapper.waitFor();

        String string = outputHandler.toString();

        PingResult result = new PingResult();
        result.host = host;

        List<String> splitIntoLineList = StringUtils.splitIntoLineList(string);
        for (String line : splitIntoLineList) {
            String trim = line.trim();
            if (trim.startsWith("Reply from")) {

                List<String> words = StringUtils.splitIntoWords(trim);
                result.replyFrom = StringUtils.trimFromEnd(words.get(2), 1);

                List<Pair<String, String>> pairs = StringUtils.splitIntoKeyValuePairs(trim);
                for (Pair<String, String> pair : pairs) {
                    if (pair.getA().equals("time")) {
                        result.time = Integer.parseInt(StringUtils.before(pair.getB(), "ms"));
                    }
                    else if (pair.getA().equals("TTL")) {
                        result.ttl = Integer.parseInt(pair.getB());
                    }
                    else if (pair.getA().equals("bytes")) {
                        result.size= Integer.parseInt(pair.getB());
                    }
                }
            }
        }

        return result;
    }

}
