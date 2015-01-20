package com.logginghub.logging.commandline;

import java.io.Console;
import java.util.Stack;

import jline.console.ConsoleReader;

public class ScreenController {

    private Stack<Screen> screenStack = new Stack<Screen>();

    private Screen current = null;

    public void changeScreen(Screen newScreen) {
        screenStack.push(current);
        current = newScreen;
    }

    public void back() {
        if (!screenStack.isEmpty()) {
            current = screenStack.pop();
        }
    }
    
    public void render(Console c, ConsoleReader reader, boolean full) {
        current.render(c, reader, full);
    }

    public void processKey(ConsoleKeyEvent t) {
        current.processKey(t);
    }

}
