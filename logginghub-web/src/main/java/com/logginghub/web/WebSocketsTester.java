package com.logginghub.web;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.logginghub.utils.Out;
import com.logginghub.utils.WorkerThread;

public class WebSocketsTester {

    public static void main(String[] args) throws UnknownHostException, IOException {

        Socket socket = new Socket("localhost", 8080);
        Out.out("Connected");

        final InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        final WorkerThread thread = new WorkerThread("WebsocketsTester-Reader") {
            private byte[] readBuffer = new byte[1024 * 1024];

            @Override protected void onRun() throws Throwable {
                try {
                    int read = inputStream.read(readBuffer);

                    if (read == -1) {
                        Out.out("End of stream");
                        dontRunAgain();
                    }
                    else {
                        String string = new String(readBuffer, 0, read);
                        System.out.println(string);
                    }

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
        BufferedWriter writer = new BufferedWriter(streamWriter);

        write(writer, "GET / HTTP/1.1\r\n");
        write(writer, "Host: 127.0.0.1:8080\r\n");
        write(writer, "Upgrade: websocket\r\n");
        write(writer, "Connection: Upgrade\r\n");
        write(writer, "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n");
        write(writer, "Origin: http://localhost:8080\r\n");
        write(writer, "Sec-WebSocket-Protocol: chat\r\n");
        write(writer, "Sec-WebSocket-Version: 13\r\n");
        write(writer, "\r\n");
        
//        write(writer, "GET / HTTP/1.1\r\n");
//        write(writer, "Content-Length: 0\r\n");
//        write(writer, "Host: localhost:8080\r\n");
//        write(writer, "Cookie: sessionID=33495503-d355-4da5-9748-579f5cb87431; view=prices\r\n");
//        writer.newLine();
//        write(writer, "");
//        write(writer, "");
//        write(writer, "");
//        write(writer, "");
//        write(writer, "");
//        write(writer, "");
        
        writer.flush();

    }

    private static void write(BufferedWriter writer, String string) throws IOException {
        
        String show = string;
        show = show.replace("\r", "\\r");
        show = show.replace("\n", "\\n");
        
        Out.out(" > {}", show);
        writer.write(string);
    }

}
