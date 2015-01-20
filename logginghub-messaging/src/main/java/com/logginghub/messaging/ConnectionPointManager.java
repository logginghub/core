package com.logginghub.messaging;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionPointManager
{
    private List<InetSocketAddress> m_connectionPoints = new CopyOnWriteArrayList<InetSocketAddress>();
    private List<InetSocketAddress> m_currentConnectionList = new ArrayList<InetSocketAddress>();

    private InetSocketAddress m_currentConnectionPoint = null;

    private InetSocketAddress m_defaultConnectionPoint = null;

    /**
     * Provider a default connection point (typically localhost) in case no
     * connection points are provided
     * 
     * @param defaultConnectionPoint
     */
    public void setDefaultConnectionPoint(InetSocketAddress defaultConnectionPoint)
    {
        m_defaultConnectionPoint = defaultConnectionPoint;
    }

    public InetSocketAddress getDefaultConnectionPoint()
    {
        return m_defaultConnectionPoint;
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress)
    {
        m_connectionPoints.add(inetSocketAddress);
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress)
    {
        m_connectionPoints.remove(inetSocketAddress);
    }

    public void clearConnectionPoints()
    {
        m_connectionPoints.clear();
    }

    public List<InetSocketAddress> getConnectionPoints()
    {
        return Collections.unmodifiableList(m_connectionPoints);
    }

    public InetSocketAddress getNextConnectionPoint()
    {
        if(m_connectionPoints.isEmpty())
        {
            if(m_defaultConnectionPoint == null)
            {
                throw new RuntimeException("There are no connection points specified, please use addConnectionPoint to add at least one.");
            }
            else
            {
                return m_defaultConnectionPoint;
            }
        }

        if(m_currentConnectionList.isEmpty())
        {
            m_currentConnectionList.addAll(m_connectionPoints);
        }

        InetSocketAddress remove = m_currentConnectionList.remove(0);
        m_currentConnectionPoint = remove;
        return remove;
    }

    public InetSocketAddress getCurrentConnectionPoint()
    {
        return m_currentConnectionPoint;
    }

    public void addConnectionPoints(List<InetSocketAddress> newConnectionPointList)
    {
        for(InetSocketAddress inetSocketAddress : newConnectionPointList)
        {
            addConnectionPoint(inetSocketAddress);
        }

    }
}
