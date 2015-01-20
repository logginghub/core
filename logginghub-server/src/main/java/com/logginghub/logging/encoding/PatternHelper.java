package com.logginghub.logging.encoding;

import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;

public class PatternHelper {

    public static class Foo {
        private Foo[] elements = new Foo[27];
        private String triggerMessage;
        private int value;

        public Foo process(char charAt, String triggerMessage) {

            charAt = Character.toLowerCase(charAt);

            int offset = charAt - 'a';
            if (!Character.isLetter(charAt)) {
                charAt = ' ';
                offset = 26;
            }
            else {
                offset = charAt - 'a';
            }

            Foo foo = elements[offset];
            if (foo == null) {
                foo = new Foo();
                foo.triggerMessage = triggerMessage;
                elements[offset] = foo;
            }
            foo.value++;

            return foo;
        }

        public void dump(String parent, int parentScore) {

            for (int i = 0; i < elements.length; i++) {
                Foo foo = elements[i];
                if (foo != null) {
                    char c = (char) ('a' + i);
                    if (i == 26) {
                        c = ' ';
                    }
                    foo.dump(parent + c, value);
                }
            }

            if (value != parentScore) {
                Out.out("{}  | {}", StringUtils.padLeft("" + value, 10), StringUtils.first(triggerMessage, 200));
            }

        }
    }

    private Foo root = new Foo();

    public void dump() {
        root.dump("", 0);
    }

    public void process(String message) {
        int chars = 100;

        int actual = Math.min(chars, message.length());

        Foo pointer = root;

        for (int i = 0; i < actual; i++) {
            char charAt = message.charAt(i);

            pointer = pointer.process(charAt, message);

        }

    }

}
