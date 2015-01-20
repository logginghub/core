package com.logginghub.messaging;

import com.logginghub.messaging.level3.ObjectController;
import com.logginghub.messaging.level3.ObjectControllerMessageBinder;
import com.logginghub.messaging.level3.ServiceRepository;
import com.logginghub.utils.logging.Logger;

public class Level3BlockingServer extends Level2BlockingServer {

    private ServiceRepository serviceRepository = new ServiceRepository();
    private static final Logger logger = Logger.getLoggerFor(Level3BlockingServer.class);

    private ObjectController objectController = new ObjectController(serviceRepository);
    private ObjectControllerMessageBinder messageBinder;

    public Level3BlockingServer() {
        
        messageBinder = new ObjectControllerMessageBinder(objectController, serviceRepository);
        
        addListener(messageBinder);
        
    }
 

    public void register(String string, Class<?> serviceInterface, Object serviceProvider) {
        serviceRepository.registerService(string, serviceInterface, serviceProvider);
    }

}
