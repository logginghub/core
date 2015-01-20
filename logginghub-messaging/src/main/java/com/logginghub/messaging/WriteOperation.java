package com.logginghub.messaging;

public class WriteOperation implements Runnable
{
    private final MessagingServerSocketHandler destination;
    private final Message message;
    private final MessagingServer server;

    /**
     * Construct a new write operation, encapulsating the act of sending this
     * message to this handler.
     * 
     * @param message
     * @param destination
     * @param server
     *            A reference to the server that owns this handler is provided
     *            in case the write fails and we need to notify the server so it
     *            can disconnect the client and tidy up
     */
    public WriteOperation(Message message,
                          MessagingServerSocketHandler destination,
                          MessagingServer server)
    {
        this.message = message;
        this.destination = destination;
        this.server = server;

    }

    public void run()
    {
        try
        {
            destination.sendMessage(message);
        }
        catch (MessagingRuntimeException mre)
        {
            server.asyncWriteOperationFailed(message, destination);
        }
    }
}
