package com.logginghub.logging.commandline.editscreen;

import java.awt.event.KeyEvent;
import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import jline.console.ConsoleReader;

import com.logginghub.logging.commandline.ConsoleKeyEvent;
import com.logginghub.logging.commandline.Screen;
import com.logginghub.utils.StringUtils;

public class EditorScreen<T> implements Screen {

    private static final char ESC = 27;

    // private CommandLineController commandLineController;
    // private ScreenController screenController;

    private String patternName = "";
    private String pattern = "";

    private String status = "";

    private int selectedLine = 0;

    interface RenderableRow {

        String render();

        void append(char c);

        void backspace();

    }

    class EditableItemRow implements RenderableRow {
        String label;
        EditorGetter<T> editorGetter;
        EditorSetter<T> editorSetter;

        @Override public String render() {
            return label + " : " + editorGetter.get(item);
        }

        @Override public void append(char c) {
            String currentValue = editorGetter.get(item);
            editorSetter.set(currentValue + c, item);
        }

        @Override public void backspace() {
            String currentValue = editorGetter.get(item);
            String newValue = StringUtils.removeLast(currentValue, 1);
            editorSetter.set(newValue, item);
        }

    }

    static class DividerRow implements RenderableRow {
        @Override public String render() {
            return "";
        }

        @Override public void append(char c) {}

        @Override public void backspace() {}
    }

    private List<RenderableRow> rows = new ArrayList<EditorScreen.RenderableRow>();

    private T item;

    private String title = "";

    public EditorScreen() {
        // this.commandLineController = commandLineController;
        // this.screenController = screenController;
    }

    public void addEditableItem(String label, EditorGetter<T> editorGetter, EditorSetter<T> editorSetter) {

        EditorScreen<T>.EditableItemRow row = new EditableItemRow();
        row.label = label;
        row.editorGetter = editorGetter;
        row.editorSetter = editorSetter;

        rows.add(row);

    }

    public void addDivider() {
        rows.add(new DividerRow());
    }

    @Override public void render(Console c, ConsoleReader reader, boolean full) {
        // clear screen only the first time
        c.writer().print(ESC + "[2J");
        c.flush();

        // reposition the cursor to 1|1
        c.writer().print(ESC + "[1;1H");
        c.flush();

        // FilterValues filterValues = commandLineController.getFilterValues();

        c.writer().println("--------------------");
        c.writer().println(title);
        c.writer().println("--------------------");
        c.writer().println("");

        for (RenderableRow renderableRow : rows) {
            c.writer().println(renderableRow.render());
        }

        // c.writer().println("Level              : " + filterValues.getLevel());
        // c.writer().println("Message            : " + filterValues.getMessage());
        // c.writer().println("Source application : " + filterValues.getSourceApplication());
        // c.writer().println("Source host        : " + filterValues.getSourceHost());
        // c.writer().println("Source address     : " + filterValues.getSourceAddress());
        // c.writer().println("");
        // c.writer().println("Earliest time          : " + filterValues.getStartTime());
        // c.writer().println("Latest time            : " + filterValues.getEndTime());
        c.writer().println("");
        c.writer().println(status);
        c.writer().println("");
        c.writer().println("(use arrows to move between fields, enter to create, escape to cancel");

        // move the cursor to the correct spot
        int x = 1;
        int y = 5 + selectedLine;

        RenderableRow row = rows.get(selectedLine);
        x += row.render().length();

        c.writer().print(StringUtils.format("{}[{};{}H", ESC, y, x));
        c.flush();
    }

    @Override public void processKey(ConsoleKeyEvent t) {
        switch (t.getKeycode()) {
            case KeyEvent.VK_ESCAPE: {
                onEscape();
                break;
            }
            case KeyEvent.VK_ENTER: {
                if (selectedLine == rows.size() -1) {
                    onFinished(item);
                }
                else {
                    down();
                }
                break;
            }
            case KeyEvent.VK_UP: {
                up();
                break;
            }
            case KeyEvent.VK_TAB:
            case KeyEvent.VK_DOWN: {
                down();
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                RenderableRow row = rows.get(selectedLine);
                row.backspace();
                break;
            }
            default: {
                RenderableRow row = rows.get(selectedLine);
                row.append(t.getChar());
            }
        }
    }

    protected void onFinished(T item) {}

    protected void onEscape() {}

    private void down() {
        selectedLine++;
        
        if (selectedLine == rows.size()) {
            selectedLine = 0;
        }
        
        if(rows.get(selectedLine) instanceof DividerRow) {
            down();
        }
    }

    private void up() {
        selectedLine--;
        if (selectedLine < 0) {
            selectedLine = rows.size() - 1;
        }
        
        if(rows.get(selectedLine) instanceof DividerRow) {
            up();
        }
    }

    // Sigar OS - mfp={memoryfree} mup={memoryused} mt={memorytotal} mu={memoryused}
    // neti={networkin} neto={networkout} cpu={cpu-used} us={cpu-user} sy={cpu-sys} id={cpu-idle}
    // wa={cpu-waiting}
    // private boolean createPattern() {
    //
    // Result<Pattern> result = commandLineController.createPattern(patternName, pattern);
    //
    // boolean success = result.isSuccessful();
    //
    // if (!success) {
    // status = StringUtils.format("{}[31;1m createPattern request failed : {}{}[0m", ESC,
    // result.getExternalReason(), ESC);
    // }
    // else {
    // status = StringUtils.format("Pattern created (id {})", result.getValue().getPatternID());
    // }
    //
    // return success;
    // }

    public void setItem(T t) {
        this.item = t;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
