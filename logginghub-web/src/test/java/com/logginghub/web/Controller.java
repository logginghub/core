package com.logginghub.web;

@WebController(staticFiles = "/static/") public class Controller {

    public int simpleMethodCalls = 0;

    public void simpleMethod() {
        simpleMethodCalls++;
    }

    public String simpleMethodThatReturns() {
        return "Return string";
    }

    public String simpleMethodThatReturnsWithParam(String param) {
        return "Return string : " + param;
    }
    
    @EndPoint(path = "get/something", mime="text/plain") public String methodThatGetsSomething() {
        return "something";
    }

    public Boolean simpleMethodThatReturnsObject() {
        return Boolean.TRUE;
    }

}
