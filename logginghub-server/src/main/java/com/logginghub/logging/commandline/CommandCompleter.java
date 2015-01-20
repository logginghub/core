package com.logginghub.logging.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jline.console.completer.Completer;

public class CommandCompleter implements Completer {

    public int complete(String buffer, final int cursor, final List<CharSequence> candidates) {

        List<String> commands = new ArrayList<String>();
        
        commands.add("connect");
        commands.add("exit");
        
        for (String string : commands) {
            if(string.startsWith(buffer)) {
                candidates.add(string);
            }
        }

        return 0;
    }

    protected CharSequence render(final File file, final CharSequence name) {
        return name;
    }
}
