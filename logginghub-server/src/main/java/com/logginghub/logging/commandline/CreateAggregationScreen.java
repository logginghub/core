package com.logginghub.logging.commandline;

import java.awt.event.KeyEvent;
import java.io.Console;

import jline.console.ConsoleReader;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.Result;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;

public class CreateAggregationScreen implements Screen {

    private static final char ESC = 27;

    private CommandLineController commandLineController;
    private ScreenController screenController;

    private String patternIDField = "";
    private String captureLabelIndexField = "";
    private String typeField = "";
    private String intervalField = "";
    private String groupByField = "";

    private String status = "";

    private int selectedLine = 0;

    public CreateAggregationScreen(CommandLineController commandLineController, ScreenController screenController) {
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

        c.writer().println("--------------------------");
        c.writer().println("Enter aggregation details:");
        c.writer().println("--------------------------");
        c.writer().println("");
        c.writer().println("Pattern ID    : " + patternIDField);
        c.writer().println("Interval      : " + intervalField);
        c.writer().println("Type          : " + typeField);
        c.writer().println("Capture index : " + captureLabelIndexField);
        c.writer().println("Group by      : " + groupByField);
        c.writer().println("");
        c.writer().println(status);
        c.writer().println("");
        c.writer().println("(use arrows to move between fields, enter to create, escape to cancel");

        // move the cursor to the correct spot
        int x = 17;
        int y = 5 + selectedLine;

        switch (selectedLine) {
            case 0:
                x += patternIDField.length();
                break;
            case 1:
                x += intervalField.length();
                break;
            case 2:
                x += typeField.length();
                break;
            case 3:
                x += captureLabelIndexField.length();
                break;
            case 4:
                x += groupByField.length();
                break;
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
                if (selectedLine == 4) {
                    if (createAggregation()) {
                        screenController.back();
                    }
                }
                else {
                    nextLine();
                }
                break;
            }
            case KeyEvent.VK_UP: {
                selectedLine--;
                if (selectedLine < 0) {
                    selectedLine = 4;
                }
                break;
            }
            case KeyEvent.VK_TAB:
            case KeyEvent.VK_DOWN: {
                nextLine();
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                switch (selectedLine) {
                    case 0:
                        patternIDField = StringUtils.removeLast(patternIDField, 1);
                        break;
                    case 1:
                        intervalField = StringUtils.removeLast(intervalField, 1);
                        break;
                    case 2:
                        typeField = StringUtils.removeLast(typeField, 1);
                        break;
                    case 3:
                        captureLabelIndexField = StringUtils.removeLast(captureLabelIndexField, 1);
                        break;
                    case 4:
                        groupByField = StringUtils.removeLast(groupByField, 1);
                        break;
                }
                break;
            }
            default: {
                switch (selectedLine) {
                    case 0:
                        patternIDField += t.getChar();
                        break;
                    case 1:
                        intervalField += t.getChar();
                        break;
                    case 2:
                        typeField += t.getChar();
                        break;
                    case 3:
                        captureLabelIndexField += t.getChar();
                        break;
                    case 4:
                        groupByField += t.getChar();
                        break;
                }

            }
        }
    }

    private void nextLine() {
        selectedLine++;
        if (selectedLine > 5) {
            selectedLine = 0;
        }
    }

    private boolean createAggregation() {
        boolean success;

        Aggregation template = new Aggregation();

        try {
            template.setPatternID(Integer.parseInt(patternIDField));
            template.setCaptureLabelIndex(Integer.parseInt(captureLabelIndexField));
            template.setGroupBy(groupByField);
            template.setInterval(TimeUtils.parseInterval(intervalField));
            template.setType(AggregationType.valueOf(typeField));
        }
        catch (RuntimeException e) {
            template = null;
            status = StringUtils.format("{}[31;1m looks like one of those paramters failed to parse : {}{}[0m", ESC, e.getMessage(), ESC);
        }

        if (template != null) {
            status = StringUtils.format("{}[36;1m Making request...{}[0m", ESC, ESC);

            Result<Aggregation> result = commandLineController.createAggregation(template);

            success = result.isSuccessful();

            if (!success) {

                if (result.isFailure()) {
                    status = StringUtils.format("{}[31;1m createAggregation failed : {}{}[0m", ESC, result.getInternalReason(), ESC);
                }
                else if (result.isUnsuccessful()) {
                    status = StringUtils.format("{}[31;1m createAggregation request timed out : {}{}[0m", ESC, result.getInternalReason(), ESC);
                }
                else if (result.isUnsuccessful()) {
                    status = StringUtils.format("{}[33;1m createAggregation was unsuccessful : {}{}[0m", ESC, result.getInternalReason(), ESC);
                }
            }
            else {
                status = StringUtils.format("Aggregation created (id {})", result.getValue().getPatternID());
            }
        }
        else {
            success = false;
        }

        return success;
    }
}
