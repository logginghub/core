package com.logginghub.logging.commandline;

import java.io.Console;

import jline.console.ConsoleReader;

public interface Screen {

    void render(Console c, ConsoleReader reader, boolean full);
    void processKey(ConsoleKeyEvent t);

}
