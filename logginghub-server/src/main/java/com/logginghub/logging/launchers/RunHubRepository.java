package com.logginghub.logging.launchers;

import java.io.IOException;

import com.logginghub.logging.repository.SocketRepository;

public class RunHubRepository
{
    public static void main(String[] args) throws IOException
    {
        SocketRepository repository = new SocketRepository();        
        repository.start();        
    }
}
