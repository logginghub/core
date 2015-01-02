package com.logginghub.utils.execution;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteExecutor
{
    private Compute m_remoteObject;

    public RemoteExecutor()
    {
        this("hosting", RemoteExecutingServer.defaultPort);
    }

    public RemoteExecutor(String host, int port)
    {
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }

        String remoteObjectName = RemoteExecutingServer.serviceName;
        try
        {
            Registry registry = LocateRegistry.getRegistry(host, port);
            m_remoteObject = (Compute) registry.lookup(remoteObjectName);
        }
        catch (RemoteException e)
        {
            throw new RuntimeException(String.format("Failed to bind to remote registry at %s:%d",
                                                     host,
                                                     port),
                                       e);
        }
        catch (NotBoundException e)
        {
            throw new RuntimeException(String.format("Connected to registry but failed to bind to remote object %s",
                                                     remoteObjectName),
                                       e);
        }
    }

    public <T> T run(Task<T> task)
    {
        try
        {
            T result = m_remoteObject.executeTask(task);
            return result;
        }
        catch (RemoteException e)
        {
            throw new RuntimeException(String.format("Failed to execute remote task"),
                                       e);
        }
    }

    public <T> T runLocal(Task<T> task)
    {
        return task.run();
    }
}
