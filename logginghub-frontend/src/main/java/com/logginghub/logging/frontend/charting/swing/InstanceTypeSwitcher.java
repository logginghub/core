package com.logginghub.logging.frontend.charting.swing;

import java.util.HashMap;
import java.util.Map;

public class InstanceTypeSwitcher {

    public interface Handler<T> {
        public void handle(T t);
    }
    
    private Map<Class, Handler> handlers = new HashMap<Class, Handler>();
    
    public <T> void registerType(Class<T> clazz, Handler<T> runnable) {
        handlers.put(clazz, runnable);
    }
    
    public void invoke(Object obj) {
        Class<? extends Object> class1 = obj.getClass();
        Handler runnable = handlers.get(class1);
        if(runnable != null) {
            runnable.handle(obj);
        }
    }
    
}
