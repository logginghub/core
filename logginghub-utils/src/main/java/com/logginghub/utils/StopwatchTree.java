package com.logginghub.utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.logginghub.utils.StringUtils.StringUtilsBuilder;

public class StopwatchTree {

    private Stack<Element> elementStack = new Stack<Element>();
    private Element root;

    public static class Element {
        public long time;
        public String event;
        public List<Element> children = new ArrayList<Element>(0);

        public void dump(long parentTime, StringUtilsBuilder builder) {
            NumberFormat instance = NumberFormat.getInstance();
            instance.setMaximumFractionDigits(2);
            
            builder.appendLine("{} : {}", instance.format((time - parentTime) * 1e-9d), event);
            for (Element child : children) {
                child.dump(time, builder);
            }
        }
    }

    public StopwatchTree(String event, Object... objects) {
        root = new Element();
        root.time = System.nanoTime();
        root.event = StringUtils.format(event, objects);
        elementStack.push(root);
    }

    public Element event(String event, Object... objects) {
        Element element = new Element();
        element.time = System.nanoTime();
        element.event = StringUtils.format(event, objects);
        elementStack.peek().children.add(element);
        return element;
    }

    public void subEvent(String event) {
        Element element = event(event);
        elementStack.push(element);
    }

    public void endSubEvent() {
        elementStack.pop();
    }

    @Override public String toString() {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        root.dump(root.time, builder);
        return builder.toString();
    }

}
