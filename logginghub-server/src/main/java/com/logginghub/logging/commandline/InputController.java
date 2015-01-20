package com.logginghub.logging.commandline;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import com.logginghub.utils.Stream;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WorkerThread;

public class InputController {

    enum State {
        Normal,
        Escape,
        KeyCombination
    }

    private Stream<ConsoleKeyEvent> keyEventStream = new Stream<ConsoleKeyEvent>();
    private volatile State state = State.Normal;

    private boolean alt = false;
    private boolean control = false;
    private boolean shift = false;
    private WorkerThread escapeDetectorThread;

    private boolean pageUp = false;
    private boolean pageDown = false;

    public Stream<ConsoleKeyEvent> getKeyEventStream() {
        return keyEventStream;
    }

    public void update(char read) {

        ConsoleKeyEvent event = null;
        switch (state) {
            case Normal:
                switch (read) {
                    case 13:
                        event = new ConsoleKeyEvent(KeyEvent.VK_ENTER, read, alt, control, shift);
                        break;
                    case 27: {
                        // Escape code - or is it an escape character?!?!
                        state = State.Escape;

                        // Hacky... if this is an escape code, there will be more stuff on the
                        // buffer, which will be processed almost instantly as the next char (thread
                        // schdulding apart...) so if we start a timer now we can pluck out escape
                        // key presses in isolation
                        if (escapeDetectorThread != null) {
                            escapeDetectorThread.stop();
                        }

                        escapeDetectorThread = WorkerThread.executeIn("escape-thread", 1, TimeUnit.MILLISECONDS, new Runnable() {
                            @Override public void run() {
                                ConsoleKeyEvent event = new ConsoleKeyEvent(KeyEvent.VK_ESCAPE, (char) 27, alt, control, shift);
                                keyEventStream.send(event);
                                state = State.Normal;
                            }
                        });

                        break;
                    }
                    case 9: {
                        // Tab for auto complete
                        event = new ConsoleKeyEvent(KeyEvent.VK_TAB, read, alt, control, shift);
                        break;
                    }
                    case 127: {
                        // Backspace
                        event = new ConsoleKeyEvent(KeyEvent.VK_BACK_SPACE, read, alt, control, shift);
                        break;
                    }
                    default: {
                        // TODO : this is a 1.7 call!!
                       // event = new ConsoleKeyEvent(KeyEvent.getExtendedKeyCodeForChar(read), read, alt, control, shift);
                    }
                }
                break;
                
            case Escape:
                // Kill the escape key by itself detector thread
                if (escapeDetectorThread != null) {
                    escapeDetectorThread.stop();
                }

                switch (read) {
                    case '[':
                        break; // CSI - control sequence indicator, ignore it
                    case ';': // CSI - separator, indicates a combination
                        state = State.KeyCombination;
                        break;
                    case 'A': // 65 : // up arrow
                        event = new ConsoleKeyEvent(KeyEvent.VK_UP, read, alt, control, shift);
                        state = State.Normal;
                        break;
                    case 'B': // 66 : // down arrow
                        event = new ConsoleKeyEvent(KeyEvent.VK_DOWN, read, alt, control, shift);
                        state = State.Normal;
                        break;
                    case 'C': // 67 : // right arrow
                        event = new ConsoleKeyEvent(KeyEvent.VK_RIGHT, read, alt, control, shift);
                        state = State.Normal;
                        break;
                    case 'D': // 68 : // left arrow
                        event = new ConsoleKeyEvent(KeyEvent.VK_LEFT, read, alt, control, shift);
                        state = State.Normal;
                        break;
                    case '~': // 126 : // Page up/down
                        if (pageUp) {
                            event = new ConsoleKeyEvent(KeyEvent.VK_PAGE_UP, read, alt, control, shift);
                        }

                        if (pageDown) {
                            event = new ConsoleKeyEvent(KeyEvent.VK_PAGE_DOWN, read, alt, control, shift);
                        }

                        pageDown = false;
                        pageUp = false;

                        state = State.Normal;
                        break;
                    case '5': // 52 : // if the next char is a tilde this will be page up
                        pageUp = true;
                        break;
                    case '6': // 53 : // if the next char is a tilde this will be page down
                        pageDown = true;
                        break;
                    case '3': // 51 : // more to come (for the delete key currently)
                        state = State.KeyCombination;
                        break;
                    case '1': // 49 : // more to come (for alt/ctrl/shift modifiers)
                        state = State.KeyCombination;
                        break;
                }

                alt = false;
                control = false;
                shift = false;

                break;

            case KeyCombination:
                switch (read) {
                    case '~': // 126 : delete key
                        state = State.Normal;
                        break;
                    case ';': // Divider
                        break;
                    case '2': // Shift
                        shift = true;
                        state = State.Escape;
                        break;
                    case '5': // Control
                        control = true;
                        state = State.Escape;
                        break;
                    case '3': // Alt
                        alt = true;
                        state = State.Escape;
                        break;
                }

                break;
        }

        if (event != null) {
            keyEventStream.send(event);
        }
    }

    public String getDebugState() {
        return StringUtils.format("state={} shift={} control={} alt={}", state, shift, control, alt);

    }
}
