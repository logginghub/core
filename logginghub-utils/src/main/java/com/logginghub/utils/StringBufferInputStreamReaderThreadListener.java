package com.logginghub.utils;

public class StringBufferInputStreamReaderThreadListener implements InputStreamReaderThreadListener {
    private StringBuffer buffer = new StringBuffer();
    
    public void onCharacter(char c) {
        buffer.append(c);
    }

    
    public void onLine(String line) {}
    
    
    public StringBuffer getBuffer() {
        return buffer;
    }
    
    public String toString() {
        return buffer.toString();
         
    }
}
