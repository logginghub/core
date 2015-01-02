package com.logginghub.utils.net;

import java.net.Socket;

public interface SocketHandler
{
    void handleSocket(Socket socket);
    Socket getSocket();
}
