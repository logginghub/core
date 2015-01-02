package com.logginghub.utils.module;

import com.logginghub.utils.module.Inject;
import com.logginghub.utils.module.Inject.Direction;

public class StubConsumerViaInjectAnnotationWithParentOnlyDirection {

    private StubService service;

    public StubConsumerViaInjectAnnotationWithParentOnlyDirection() {
    }
    
    @Inject(direction = Direction.Parent)
    public void setService(StubService service) {
        this.service = service;
    }
    
    public StubService getService() {
        return service;
    }
    
}
