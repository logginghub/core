package com.logginghub.messaging;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.Level2MessageSender;
import com.logginghub.messaging.netty.Level2Messaging;
import com.logginghub.messaging.netty.WrappedMessageListener;
import com.logginghub.utils.FactoryMapDecorator;

/**
 * Abstractions of common functionality between the level 2 client and server.
 * @author James
 *
 */
public class Level2Helper {

    private String homeChannel = "";
    
    private FactoryMapDecorator<String, List<WrappedMessageListener>> localChannelListeners = new FactoryMapDecorator<String, List<WrappedMessageListener>>(new HashMap<String, List<WrappedMessageListener>>()) {
        @Override protected List<WrappedMessageListener> createNewValue(String key) {
            return new CopyOnWriteArrayList<WrappedMessageListener>();
        }
    };

    private Level2Messaging level2Messaging;

    
    public Level2Helper(Level2Messaging level2Messaging) {
        this.level2Messaging = level2Messaging;
    }

    public String getHomeChannel() {
        return homeChannel;
    }
    
    public void setHomeChannel(String homeChannel) {
        this.homeChannel = homeChannel;
    }
    /**
     * Open a messaging stream to a server channel 
     */
    public MessagingChannel openChannel(String remoteChannel) {
        MessagingChannel channel = new MessagingChannel();
        channel.setRemoteNode("");
        channel.setRemoteNodeChannel(remoteChannel);
        channel.setLocalNode(homeChannel);
        channel.setLocalChannel(getNextDynamicChannel());
        channel.setSender(level2Messaging);
        return channel;
    }
    
    
    /**
     * Open a channel to the remote node - uses our home subscription and a
     * dynamic local channel
     * 
     * @param string
     * @param string2
     */
    public MessagingChannel openChannel(String remoteNode, String remoteChannel) {
        MessagingChannel channel = new MessagingChannel();
        channel.setRemoteNode(remoteNode);
        channel.setRemoteNodeChannel(remoteChannel);
        channel.setLocalNode(homeChannel);
        channel.setLocalChannel(getNextDynamicChannel());
        channel.setSender(level2Messaging);
        return channel;
    }

    private AtomicInteger nextDynamicChannel = new AtomicInteger();
    private String getNextDynamicChannel() {
        return "dynamicChannel-" + nextDynamicChannel.getAndIncrement();
    }

    public MessagingChannel openChannel(String remoteNode, String remoteChannel, String localNode, String localChannel) {
        MessagingChannel channel = new MessagingChannel();
        channel.setRemoteNode(remoteNode);
        channel.setRemoteChannel(remoteChannel);
        channel.setLocalNode(localNode);
        channel.setLocalChannel(localChannel);
        channel.setSender(level2Messaging);
        return channel;
    }

    public void dispatchToLocalListeners(ClientRequestContext requestContext, Level1MessageSender sender, MessageWrapper messageWrapper) {
        if (messageWrapper.getDeliverToLocalChannel() != null) {
            List<WrappedMessageListener> list = localChannelListeners.get(messageWrapper.getDeliverToChannel() + "/" + messageWrapper.getDeliverToLocalChannel());
            for (WrappedMessageListener wrappedMessageListener : list) {
                wrappedMessageListener.onNewMessage(messageWrapper, requestContext, sender);
            }
        }
    }
    

    public void addLocalChannelListener(String channel, String localChannel, WrappedMessageListener wrappedMessageListener) {        
        localChannelListeners.get(channel + "/" + localChannel).add(wrappedMessageListener);
        
    }

    public void removeLocalChannelListener(String channel, String localChannel, WrappedMessageListener wrappedMessageListener) {
        localChannelListeners.get(channel + "/" + localChannel).remove(wrappedMessageListener);
    }
}
