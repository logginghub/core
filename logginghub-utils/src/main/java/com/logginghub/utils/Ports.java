package com.logginghub.utils;

/**
 * Because there are so many random servers and sevices in the codebase now, we
 * need something to track the default ports so nothing conflicts.
 * 
 * @author James
 * 
 */
public interface Ports
{
    public int JMSBroker = 10000;       
    public int RemotingServer = 10001;    
    public int MessagingServer = 10002;
    
    public int JMSBrokerTest = JMSBroker + 10000;
}
