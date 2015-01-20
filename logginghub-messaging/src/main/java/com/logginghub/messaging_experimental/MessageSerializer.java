package com.logginghub.messaging_experimental;

import java.nio.ByteBuffer;

public interface MessageSerializer
{
    ByteBuffer serialize(Message message);
    Message attemptToDecode(ByteBuffer wrapped);
}
