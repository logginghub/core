package com.logginghub.utils.module;

import com.logginghub.utils.module.Inject;

public class StubConsumerViaInjectAnnotation {

    private StubService service;

    public StubConsumerViaInjectAnnotation() {
    }
    
    @Inject
    public void setService(StubService service) {
        this.service = service;
    }
    
    public StubService getService() {
        return service;
    }
    
}
