package com.logginghub.utils.module;

public class StubProvider implements StubService{

    private String id;

    public String getId() {
        return id;
    }
    
    
    
    @Override public String toString() {
        return "StubProvider [id=" + id + "]";
    }



    public String reverse(String string) {
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        return sb.reverse().toString();
    }

}
