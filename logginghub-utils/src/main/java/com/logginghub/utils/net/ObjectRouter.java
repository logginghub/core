package com.logginghub.utils.net;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ObjectRouter
{
    private Map<Class, ObjectHandler> m_handlers = new HashMap<Class, ObjectHandler>();
    private static Logger logger = Logger.getLogger(ObjectRouter.class.getName());

    public <T> void addHandler(ObjectHandler<T> handler, Class<? extends T> c)
    {
        m_handlers.put(c, handler);
    }

    public <T> void route(Object object, ObjectSocketHandler source)
    {
        logger.info(String.format("Attempting to route object type %s", object.getClass()));
        
        Class<? extends Object> c = object.getClass();
        ObjectHandler<T> objectHandler = m_handlers.get(c);
        if (objectHandler != null)
        {
            T cast = (T) c.cast(object);
            logger.info(String.format("Routing found, passing to handler %s", cast));
            
            objectHandler.handle(cast, source);
        }
        else
        {
            logger.info(String.format("No routing found, ignoring"));
        }
    }
}
