package com.logginghub.messaging;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConnectionPoints
{
    private List<InetSocketAddress> connectionPoints = new ArrayList<InetSocketAddress>();
    private Iterator<InetSocketAddress> iterator;

    public void add(InetSocketAddress connectionPoint)
    {
        connectionPoints.add(connectionPoint);
        resetIterator();
    }

    private void resetIterator()
    {
        if (iterator != null)
        {
            iterator = connectionPoints.iterator();
        }
    }

    public InetSocketAddress getNextConnectionPoint()
    {
        if (iterator == null || !iterator.hasNext())
        {
            iterator = connectionPoints.iterator();
        }

        return iterator.next();
    }
}
