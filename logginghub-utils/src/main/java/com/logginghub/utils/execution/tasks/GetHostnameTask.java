package com.logginghub.utils.execution.tasks;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.logginghub.utils.execution.Task;

public class GetHostnameTask implements Task<String>
{
    public String run()
    {
	
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }        
    }
}
