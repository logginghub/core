package com.logginghub.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Register MessagePayloadHandlers with the class and they'll be called when
 * process is called with a message containing a matching payload. Avoids the
 * big if else instanceof code in a typical message listener.
 * 
 * @author James
 * 
 */
public class MessagePayloadProcessor
{
    private Map<Class<?>, MessagePayloadHandler<?>> m_handlers = new HashMap<Class<?>, MessagePayloadHandler<?>>();
    private static Logger logger = Logger.getLogger(MessagePayloadProcessor.class.getName());

    public <T> void addHandler(Class<? extends T> c,MessagePayloadHandler<T> handler)
    {
        m_handlers.put(c, handler);
    }

    @SuppressWarnings("unchecked") public <T> void process(Message message,
                                                           MessagingServerSocketHandler source)
    {
        Object object = message.getPayload();

        if (logger.isLoggable(Level.FINE))
        {
            logger.fine(String.format("Attempting to route object type %s",
                                      object.getClass()));
        }

        Class<? extends Object> c = object.getClass();
        MessagePayloadHandler<T> objectHandler = (MessagePayloadHandler<T>) m_handlers.get(c);
        if (objectHandler != null)
        {
            T cast = (T) c.cast(object);
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("Routing found, passing to handler %s",
                                          cast));
            }
            objectHandler.handle(cast, message, source);
        }
        else
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("No routing found for class [%s], no handlers to send it to",
                                             c.getName()));
            }
        }

    }
}
