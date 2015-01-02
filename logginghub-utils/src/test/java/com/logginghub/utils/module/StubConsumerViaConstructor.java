package com.logginghub.utils.module;

public class StubConsumerViaConstructor {

    private StubService service;

    public StubConsumerViaConstructor(StubService service) {
        this.service = service;        
    }
    
    public StubService getService() {
        return service;
    }
    
}
