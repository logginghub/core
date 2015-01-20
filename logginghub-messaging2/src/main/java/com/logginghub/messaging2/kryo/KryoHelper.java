package com.logginghub.messaging2.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.logginghub.messaging2.messages.BasicMessage;
import com.logginghub.messaging2.messages.RequestMessage;
import com.logginghub.messaging2.messages.ResponseMessage;
import com.logginghub.messaging2.proxy.CallbackPlaceholder;
import com.logginghub.messaging2.proxy.MethodInvocationRequestMessage;
import com.logginghub.messaging2.proxy.MethodInvocationResponseMessage;

public class KryoHelper {
    public static void registerClasses(Kryo kryo) {
        kryo.register(String[].class);        
        kryo.register(Object[].class);
        kryo.register(BasicMessage.class);
        kryo.register(ConnectionMessage.class);
        kryo.register(ConnectedMessage.class);
        kryo.register(BasicMessage.class);
        kryo.register(RequestMessage.class);
        kryo.register(ResponseMessage.class);
        kryo.register(MethodInvocationRequestMessage.class);
        kryo.register(MethodInvocationResponseMessage.class);
        kryo.register(SubscribeMessage.class);
        kryo.register(UnsubscribeMessage.class);
        kryo.register(CallbackPlaceholder.class);
    }
}

