package com.logginghub.utils.execution;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RemoteExecutingServer implements Compute
{
    public final static int defaultPort = 12345;
    public final static String serviceName = "remoteExector";

    public RemoteExecutingServer()
    {
        super();
    }

    public <T> T executeTask(Task<T> t)
    {
        return t.run();
    }

    public static void main(String[] args)
    {
        try
        {
            Registry registry = LocateRegistry.createRegistry(defaultPort);
            
            Compute engine = new RemoteExecutingServer();
            Compute stub = (Compute) UnicastRemoteObject.exportObject(engine, 0);

            registry.rebind(serviceName, stub);
            System.out.println("RemoteExecutingServer bound");
        }
        catch (Exception e)
        {
            System.err.println("RemoteExecutingServer exception:");
            e.printStackTrace();
        }
    }
}
