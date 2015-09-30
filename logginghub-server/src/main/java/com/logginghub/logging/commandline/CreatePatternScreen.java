package com.logginghub.logging.commandline;

import java.awt.event.KeyEvent;
import java.io.Console;

import jline.console.ConsoleReader;

import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.utils.Result;
import com.logginghub.utils.StringUtils;

public class CreatePatternScreen implements Screen {

    private static final char ESC = 27;

    private CommandLineController commandLineController;
    private ScreenController screenController;

    private String patternName = "";
    private String pattern = "";

    private String status = "";

    private int selectedLine = 0;

    public CreatePatternScreen(CommandLineController commandLineController, ScreenController screenController) {
        this.commandLineController = commandLineController;
        this.screenController = screenController;
    }

    @Override public void render(Console c, ConsoleReader reader, boolean full) {
        // clear screen only the first time
        c.writer().print(ESC + "[2J");
        c.flush();

        // reposition the cursor to 1|1
        c.writer().print(ESC + "[1;1H");
        c.flush();

        c.writer().println("---------------------");
        c.writer().println("Enter pattern details");
        c.writer().println("---------------------");
        c.writer().println("");
        c.writer().println("Name : " + patternName);
        c.writer().println("Pattern : " + pattern);
        c.writer().println("");
        c.writer().println(status);
        c.writer().println("");
        c.writer().println("(use arrows to move between fields, enter to create, escape to cancel");

        // move the cursor to the correct spot
        int x = 0;
        int y = 0;

        if (selectedLine == 0) {
            y = 5;
            x = "Name : ".length() + patternName.length() + 1;

        }
        else if (selectedLine == 1) {
            y = 6;
            x = "Pattern : ".length() + pattern.length() + 1;
        }

        c.writer().print(StringUtils.format("{}[{};{}H", ESC, y, x));
        c.flush();
    }

    @Override public void processKey(ConsoleKeyEvent t) {
        switch (t.getKeycode()) {
            case KeyEvent.VK_ESCAPE: {
                screenController.back();
                break;
            }
            case KeyEvent.VK_ENTER: {
                if (createPattern()) {
                    screenController.back();
                }
                break;
            }
            case KeyEvent.VK_UP: {
                selectedLine--;
                if (selectedLine < 0) {
                    selectedLine = 1;
                }
                break;
            }
            case KeyEvent.VK_TAB:
            case KeyEvent.VK_DOWN: {
                selectedLine++;
                if (selectedLine > 1) {
                    selectedLine = 0;
                }
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                if (selectedLine == 0) {
                    patternName = StringUtils.removeLast(patternName, 1);
                }
                else if (selectedLine == 1) {
                    pattern = StringUtils.removeLast(pattern, 1);
                }
                break;
            }
            default: {
                if (selectedLine == 0) {
                    patternName += t.getChar();
                }
                else if (selectedLine == 1) {
                    pattern += t.getChar();
                }

            }
        }
    }

    // Sigar OS - mfp={memoryfree} mup={memoryused} mt={memorytotal} mu={memoryused} neti={networkin} neto={networkout} cpu={cpu-used} us={cpu-user} sy={cpu-sys} id={cpu-idle} wa={cpu-waiting}
    private boolean createPattern() {

        Result<Pattern> result = commandLineController.createPattern(patternName, pattern);

        boolean success = result.isSuccessful();

        if (!success) {
            status = StringUtils.format("{}[31;1m createPattern request failed : {}{}[0m", ESC, result.getExternalReason(), ESC);
        }
        else {
            status = StringUtils.format("Pattern created (id {})", result.getValue().getPatternId());
        }

        return success;
    }
}
