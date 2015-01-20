package com.logginghub.logging.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;

public class CommandHistory {

    private File file;

    private List<String> history = new ArrayList<String>();

    public CommandHistory() {
        file = new File(StringUtils.format("{}/.logginghub/hubconsole.history", System.getProperty("user.home")));
        Out.out("history at {}", file.getAbsolutePath());
        file.getParentFile().mkdirs();
    }

    public void load() {
        if (file.exists()) {
            history = FileUtils.readAsStringList(file);
        }
    }

    public void save() {
        FileUtils.writeAsStringList(file, history);
    }

    public void add(String line) {
//        if (!history.contains(line)) {
            history.add(line);
            FileUtils.appendLine(line, file);
//        }
    }

    public CommandHistoryPointer getPointer() {
        CommandHistoryPointer pointer = new CommandHistoryPointer(this);
        return pointer;
    }

    public final static class CommandHistoryPointer {

        private CommandHistory commandHistory;
        private int index = 0;

        public CommandHistoryPointer(CommandHistory commandHistory) {
            this.commandHistory = commandHistory;
            index = commandHistory.history.size() - 1;
        }

        public String next() {
            String value;
            if (commandHistory.history.isEmpty()) {
                value = "";
            }
            else {
                value = commandHistory.history.get(index);
                index++;
                if (index == commandHistory.history.size()) {
                    index = 0;
                }
            }
            return value;
        }

        public String previous() {
            String value;
            if (commandHistory.history.isEmpty()) {
                value = "";
            }
            else {
                value = commandHistory.history.get(index);
                index--;
                if (index == -1) {
                    index = commandHistory.history.size() - 1;
                }
            }
            return value;
        }

    }
}
