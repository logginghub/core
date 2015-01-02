package com.logginghub.utils.net;

public abstract class ObjectRouterHandler extends ObjectRouter implements
                ObjectSocketListener, ObjectInputStreamReaderListener
{
    public void onObjectRead(Object object)
    {
        route(object, null);
    }

    public void onNewObject(Object object, ObjectSocketHandler source)
    {
        route(object, source);
    }
}
