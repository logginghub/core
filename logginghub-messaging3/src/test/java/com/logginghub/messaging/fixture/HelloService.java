package com.logginghub.messaging.fixture;

public interface HelloService {

    String hello(String name);
    
    void addListener(HelloListener listener);
    void removeListener(HelloListener listener);
    
}
