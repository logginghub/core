package com.logginghub.messaging.fixture;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HelloProvider implements HelloService {

    private List<HelloListener> listeners = new CopyOnWriteArrayList<HelloListener>();

    public String hello(String name) {
        String result = "Hello " + name;
        notify(name);
        return result;
    }

    protected void notify(String name) {
        for (HelloListener helloListener : listeners) {
            helloListener.onHello(name);
        }
    }

    public void addListener(HelloListener listener) {
        listeners.add(listener);
    }

    public void removeListener(HelloListener listener) {
        listeners.remove(listener);
    }

}
