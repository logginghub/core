package com.logginghub.logging.messages;

public class EncodeBuilder {

    public EncodeBuilder version(int version) {
        return this;
         
    }

    public EncodeBuilder encode(long value) {
        return this;
         
    }

    public EncodeBuilder encode(double value) {
        return this;
         
    }

    public EncodeBuilder encode(String value) {
        return this;
    }

    public EncodeBuilder encode(String field, double value) {
        return this;
         
    }

    public EncodeBuilder encode(String field, String value) {
        return this;
    }

    
    public EncodeBuilder encode(String field, long value) {
        return null;
         
    }
    
    public EncodeBuilder encode(int field, double value) {
        return this;
         
    }

    public EncodeBuilder encode(int field, String value) {
        return this;
    }

    
    public EncodeBuilder encode(int field, long value) {
        return null;
         
    }

}
