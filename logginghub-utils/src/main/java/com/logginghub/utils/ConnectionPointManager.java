package com.logginghub.utils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.logging.Logger;

public class ConnectionPointManager {
    private static final Logger logger = Logger.getLoggerFor(ConnectionPointManager.class);
    private List<InetSocketAddress> connectionPoints = new CopyOnWriteArrayList<InetSocketAddress>();
    private List<InetSocketAddress> currentConnectionList = new ArrayList<InetSocketAddress>();
    private InetSocketAddress currentConnectionPoint = null;
    private InetSocketAddress defaultConnectionPoint = null;

    /**
     * Provider a default connection point (typically localhost) in case no
     * connection points are provided
     * 
     * @param defaultConnectionPoint
     */
    public void setDefaultConnectionPoint(InetSocketAddress defaultConnectionPoint) {
        this.defaultConnectionPoint = defaultConnectionPoint;
    }

    public InetSocketAddress getDefaultConnectionPoint() {
        return defaultConnectionPoint;
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        connectionPoints.add(inetSocketAddress);
        logger.trace("Connection point added : {}", inetSocketAddress);
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        connectionPoints.remove(inetSocketAddress);
    }

    public void clearConnectionPoints() {
        connectionPoints.clear();
    }

    public List<InetSocketAddress> getConnectionPoints() {
        return Collections.unmodifiableList(connectionPoints);
    }

    public InetSocketAddress getNextConnectionPoint() {
        if (connectionPoints.isEmpty()) {
            if (defaultConnectionPoint == null) {
                throw new RuntimeException("There are no connection points specified, please use addConnectionPoint to add at least one.");
            }
            else {
                return defaultConnectionPoint;
            }
        }

        if (currentConnectionList.isEmpty()) {
            currentConnectionList.addAll(connectionPoints);
        }

        InetSocketAddress remove = currentConnectionList.remove(0);
        currentConnectionPoint = remove;
        return remove;
    }

    public InetSocketAddress getCurrentConnectionPoint() {
        return currentConnectionPoint;
    }

    public void addConnectionPoints(List<InetSocketAddress> newConnectionPointList) {
        for (InetSocketAddress inetSocketAddress : newConnectionPointList) {
            addConnectionPoint(inetSocketAddress);
        }
    }
}
