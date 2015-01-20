package com.logginghub.logging.commandline;

import java.io.PrintWriter;

import jline.console.ConsoleReader;

public interface CommandHandler {
    void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments);
}
