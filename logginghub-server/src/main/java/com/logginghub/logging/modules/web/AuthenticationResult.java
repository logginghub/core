package com.logginghub.logging.modules.web;

public class AuthenticationResult implements SessionToken {

    private String userName;

    public AuthenticationResult(String userName) {
        this.userName = userName;
    }
    
    public void setUsername(String userName) {
        this.userName = userName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    @Override public String getSessionInfo() {
        return userName;
    }

}
