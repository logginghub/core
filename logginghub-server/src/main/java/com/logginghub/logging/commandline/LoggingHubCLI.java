package com.logginghub.logging.commandline;

import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;

import com.logginghub.utils.Destination;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.StacktraceUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WorkerThread;

public class LoggingHubCLI {

    private boolean debug = false;
    private Stack<String> promptStack = new Stack<String>();
    private ConsoleReader reader;

    private InputController inputController = new InputController();
    private ScreenController screenController = new ScreenController();

    private CommandLineController controller = new CommandLineController() {
        @Override public void append(String line, Object... args) {
            LoggingHubCLI.this.append(line, args);
        }
    };

    private DefaultScreen defaultScreen;

    public LoggingHubCLI() throws IOException {

        reader = new ConsoleReader();
        int consoleHeight = reader.getTerminal().getHeight();
        
        defaultScreen = new DefaultScreen(controller, screenController, consoleHeight);
        screenController.changeScreen(defaultScreen);
        
        String[] art = ResourceUtils.readLines("com/logginghub/logging/commandline/art1.txt");
        for (String string : art) {
            append(string);
        }

        promptStack.push("logginghub");

        inputController.getKeyEventStream().addDestination(new Destination<ConsoleKeyEvent>() {
            @Override public void send(ConsoleKeyEvent t) {
                
                if(debug) {
                    append(t.toString());
                }
                
                screenController.processKey(t);
            }
        });

        controller.registerHandler("debug", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                debug = !debug;
            }
        });
        
        
    }

    public void append(String line, Object... args) {
        defaultScreen.append(line, args);
    }
    
    public void run() {
        try {
            List<Completer> completors = new LinkedList<Completer>();
            completors.add(new CommandCompleter());

            for (Completer c : completors) {
                reader.addCompleter(c);
            }

            final Console c = System.console();

            WorkerThread.every("ScreenUpdater", 100, TimeUnit.MILLISECONDS, new Runnable() {
                @Override public void run() {
                    renderScreen(reader, c, true);
                }
            });

            while (true) {
                try {
                    char read = (char) reader.readCharacter();
                                       
                    inputController.update(read);
                    
                    renderScreen(reader, c, false);
                    
                    if (debug) {
                        append("Last char was '{}' '{}' [{}]", (int)read, read, inputController.getDebugState());
                    }
                }
                catch (RuntimeException e) {
                    String stackTraceAsString = StacktraceUtils.getStackTraceAsString(e);
                    List<String> splitIntoLineList = StringUtils.splitIntoLineList(stackTraceAsString);
                    for (String string : splitIntoLineList) {
                        append(string);
                    }
                }
            }

        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private synchronized void renderScreen(ConsoleReader reader, Console c, boolean full) {
        screenController.render(c, reader, full);
    }

    public static void main(String[] args) throws IOException {
        LoggingHubCLI cli = new LoggingHubCLI();
        cli.run();
    }
}
