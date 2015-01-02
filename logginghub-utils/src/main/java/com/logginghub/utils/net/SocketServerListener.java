package com.logginghub.utils.net;

import java.net.Socket;

public interface SocketServerListener
{
    void onAccepted(Socket socket);
}
