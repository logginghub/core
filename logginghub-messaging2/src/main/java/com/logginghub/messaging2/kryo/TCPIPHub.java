package com.logginghub.messaging2.kryo;

import java.net.InetSocketAddress;

public class TCPIPHub {

    private InetSocketAddress address;

    public TCPIPHub(InetSocketAddress address) {
        super();
        this.address = address;
    }

    public InetSocketAddress getAddress() {
        return address;        
    }

}
