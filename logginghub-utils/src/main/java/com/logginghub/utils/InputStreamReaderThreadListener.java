package com.logginghub.utils;

public interface InputStreamReaderThreadListener {
    void onCharacter(char c);
    void onLine(String line);
}
