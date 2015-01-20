package com.logginghub.logging.commandline;

import java.awt.event.KeyEvent;
import java.io.Console;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jline.console.ConsoleReader;

import com.logginghub.logging.commandline.AnsiColourHelper.AnsiColour;
import com.logginghub.logging.commandline.CommandHistory.CommandHistoryPointer;
import com.logginghub.utils.StringUtils;

public class DefaultScreen implements Screen {

    private static final int FIXED_LINES = 3;
    private static final char ESC = 27;
    private List<String> lines = new ArrayList<String>();
    private int firstVisibleLine = 0;
    private String commandHelperLine = "help";
    private String statusLine = "status";
    private int consoleHeight = 0;
    private StringBuilder currentBuffer = new StringBuilder();

    private boolean proposalMode = false;
    private String proposalStem = "";
    private List<String> currentProposals;
    private int currentProposalIndex = -1;

    private CommandHistory commandHistory = new CommandHistory();
    private CommandHistoryPointer pointer;
    private CommandLineController commandLineController;
    private ScreenController screenController;

    private volatile boolean dirty = true;

    public DefaultScreen(final CommandLineController commandLineController, final ScreenController screenController, int consoleHeight) {
        this.commandLineController = commandLineController;
        this.screenController = screenController;
        this.consoleHeight = consoleHeight;

        commandHistory.load();
        pointer = commandHistory.getPointer();

        commandLineController.registerHandler("mkpattern", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                screenController.changeScreen(new CreatePatternScreen(commandLineController, screenController));
            }
        });

        commandLineController.registerHandler("mkaggregation", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                screenController.changeScreen(new CreateAggregationScreen(commandLineController, screenController));
            }
        });

        commandLineController.registerHandler("editfilter", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                screenController.changeScreen(new EditFilterScreen(commandLineController, screenController));
            }
        });

        commandLineController.registerHandler("clear", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                clear();
            }
        });

        commandLineController.registerHandler("historical", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                clear();
                commandLineController.makeHistoricalEventRequest();
            }
        });

        commandLineController.registerHandler("patternhistory", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                clear();
                commandLineController.makeHistoricalPatternisedEventRequest();
            }
        });
        
        commandLineController.registerHandler("patternhistoryhealth", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                clear();
                commandLineController.makeHistoricalPatternisedHealthRequest();
            }
        });
        
        commandLineController.registerHandler("aggregatedhistoryhealth", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                clear();
                commandLineController.makeHistoricalAggregatedHealthRequest();
            }
        });
        
        commandLineController.registerHandler("historicalhealth", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                clear();
                commandLineController.makeHistoricalEventHealthRequest();
            }
        });

        commandLineController.registerHandler("aggregatedhistory", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, PrintWriter out, String command, String[] arguments) {
                lines.clear();
                commandLineController.makeHistoricalAggregatedEventRequest();
            }
        });
    }

    @Override public void render(Console c, ConsoleReader reader, boolean full) {
        consoleHeight = reader.getTerminal().getHeight();
        int consoleWidth = reader.getTerminal().getWidth();

        if (dirty) {
            if (full) {
                dirty = false;
                // clear screen only the first time
                c.writer().print(ESC + "[2J");
                c.flush();

                // reposition the cursor to 1|1
                c.writer().print(ESC + "[1;1H");
                c.flush();

                int maximumLines = getItemsOnScreen();

                synchronized (lines) {
                    int linesAvailable = lines.size() - firstVisibleLine;
                    int linesToRender = Math.min(linesAvailable, maximumLines);
                    for (int i = 0; i < linesToRender; i++) {
                        if (firstVisibleLine > 0) {
                            int b = 0;
                        }
                        int viewIndex = i + firstVisibleLine;
                        String line = this.lines.get(viewIndex);

                        if (line.length() < consoleWidth) {
                            line = StringUtils.padRight(line, consoleWidth);
                        }

                        if (viewIndex % 2 == 0) {
                            c.writer().println(AnsiColourHelper.format(AnsiColour.None, AnsiColour.Black, false, line));
                        }
                        else {
                            c.writer().println(line);
                        }
                    }
                }
            }

            c.writer().print(ESC + StringUtils.format("[{};{}H", consoleHeight - 2, 1));

            c.writer().println(StringUtils.paddingString("", consoleWidth, '-', full));

            // c.writer().println(statusLine);
            c.writer().println(StringUtils.padRight(StringUtils.format("{} : {} : {} : {}",
                                                                       consoleHeight,
                                                                       firstVisibleLine,
                                                                       lines.size(),
                                                                       commandHelperLine), consoleWidth));
            c.writer().print(StringUtils.padRight("prompt>" + currentBuffer.toString(), consoleWidth));
            c.writer().flush();

        }
        // c.writer().print(ESC + StringUtils.format("[{};{}H", lines, 20));
    }

    public void processKey(ConsoleKeyEvent t) {

        switch (t.getKeycode()) {
            case KeyEvent.VK_TAB: {

                if (proposalMode) {
                    // Need to deal with subproposals
                }
                else {

                    proposalMode = true;

                    String partial = currentBuffer.toString();

                    currentProposals = commandLineController.getPotentialCommands(partial);
                    currentProposalIndex = 0;

                    if (currentProposals.size() == 0) {
                        commandHelperLine = "No commands found that start with '" + partial + "'";
                    }
                    else {
                        commandHelperLine = AnsiColourHelper.format(AnsiColour.Cyan, AnsiColour.None, true, currentProposals.toString());
                        currentBuffer = new StringBuilder(currentProposals.get(currentProposalIndex) + " ");
                    }
                }

                break;
            }
            case KeyEvent.VK_UP: {
                if (proposalMode) {
                    currentProposalIndex++;
                    if (currentProposalIndex == currentProposals.size()) {
                        currentProposalIndex = 0;
                    }
                    currentBuffer = new StringBuilder(currentProposals.get(currentProposalIndex) + " ");
                }
                else {
                    if (t.isControl()) {
                        scrollUp();
                    }
                    else {
                        previousCommandHistory();
                    }
                }
                break;
            }
            case KeyEvent.VK_DOWN: {
                if (proposalMode) {
                    currentProposalIndex--;
                    if (currentProposalIndex == -1) {
                        currentProposalIndex = currentProposals.size() - 1;
                    }
                    currentBuffer = new StringBuilder(currentProposals.get(currentProposalIndex) + " ");
                }
                else {
                    if (t.isControl()) {
                        scrollDown();
                    }
                    else {
                        nextCommandHistory();
                    }
                }
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                if (t.isControl()) {
                    scrollPageUp();
                }
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                if (t.isControl()) {
                    scrollPageDown();
                }
                break;
            }
            case KeyEvent.VK_RIGHT: {
                break;
            }
            case KeyEvent.VK_LEFT: {
                break;
            }
            case KeyEvent.VK_ESCAPE: {
                break;
            }
            case KeyEvent.VK_ENTER: {

                if (proposalMode) {
                    proposalMode = false;
                    proposalStem = "";
                }

                if (currentBuffer.length() > 0) {
                    String command = currentBuffer.toString();
                    currentBuffer = new StringBuilder();

                    commandLineController.processCommand(command);
                    commandHistory.add(command);

                    // Move the history pointer forwards
                    pointer.next();
                }
                statusLine = "Last char was a new line";
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {

                if (proposalMode) {
                    proposalMode = false;
                    currentBuffer = new StringBuilder(proposalStem);
                    proposalStem = "";
                }
                else {
                    if (currentBuffer.length() > 0) {
                        currentBuffer = new StringBuilder(currentBuffer.substring(0, currentBuffer.length() - 1));
                    }
                }
                break;
            }
            default: {
                currentBuffer.append(t.getChar());
                break;
            }
        }

        dirty = true;
    }

    private void scrollPageDown() {
        firstVisibleLine += getItemsOnScreen();
        lockScrollBounds();
    }

    private void scrollPageUp() {
        firstVisibleLine -= getItemsOnScreen();
        lockScrollBounds();
    }

    private void nextCommandHistory() {
        currentBuffer = new StringBuilder(pointer.next());
    }

    private void previousCommandHistory() {
        currentBuffer = new StringBuilder(pointer.previous());
    }

    private void scrollDown() {
        firstVisibleLine++;
        lockScrollBounds();
    }

    private void lockScrollBounds() {

        if (firstVisibleLine < 0) {
            firstVisibleLine = 0;
        }
        else {
            int itemsOnScreen = getItemsOnScreen();
            int items = lines.size();
            int lastItemForFullScreen = items - itemsOnScreen + 1;

            if (lastItemForFullScreen < 0) {
                // No point scrolling, everything fits on one screen anyway
                firstVisibleLine = 0;
            }
            else if (firstVisibleLine > lastItemForFullScreen) {
                // No point scrolling past the last item
                firstVisibleLine = lastItemForFullScreen;
            }
        }
    }

    private int getItemsOnScreen() {
        int itemsOnScreen = consoleHeight - FIXED_LINES;
        return itemsOnScreen;
    }

    private void scrollUp() {
        firstVisibleLine--;
        lockScrollBounds();
    }

    public void append(String line, Object... args) {
        synchronized (lines) {

            lines.add(StringUtils.format(line, args));

            if (lines.size() > getItemsOnScreen()) {
                scrollDown();
            }
        }
        dirty = true;
    }

    private void clear() {
        firstVisibleLine = 0;
        lines.clear();
        dirty = true;
    }
}
