package com.logginghub.messaging;

import java.io.IOException;
import java.io.Serializable;

public interface MessagingSender
{
    <T extends Serializable> T makeRequest(Serializable request,
                                           int destinationID)
                    throws IOException;

    <T extends Serializable> T makeRequest(Serializable request)
                    throws IOException;


}
