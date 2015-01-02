package com.logginghub.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class JSONWriter {

    private PrintWriter out;
    private String div = "";
    private StringWriter stringWriter;
    private boolean isSingleLine = true;
    
    private String newLine = "";
    private String elementDiv = ":";

    private String arrayStart = "[";
    private String arrayEnd = "]";

    private String objectStart = "{";
    private String objectEnd = "}";

    public JSONWriter(PrintWriter out) {
        this.out = out;
    }

    public JSONWriter() {
        stringWriter = new StringWriter();
        this.out = new PrintWriter(stringWriter);
    }

    @Override public String toString() {
        return stringWriter.toString();
    }

    public JSONWriter startArray() {
        out.print(arrayStart);
        div = "";
        return this;
    }

    public JSONWriter endArray() {
        out.print(arrayEnd);
        div = "";
        return this;
    }

    public JSONWriter writeProperty(String name, Object value) {
        out.print(div);
        if (value instanceof Integer || value instanceof Boolean) {
            out.print("\"" + name + "\"" + elementDiv + value);
        }
        else {
            out.print("\"" + name + "\"" + elementDiv + "\"" + value + "\"");
        }
        div = ",";
        return this;
    }

    public JSONWriter startArrayElement() {
        out.print(div);
        out.print(objectStart);
        div = "";
        return this;
    }

    public JSONWriter endArrayElement() {
        out.print(objectEnd);
        div = ",";
        return this;
    }

    public JSONWriter endElement() {
        out.print(objectEnd);
        out.print(newLine);
        return this;
    }

    public JSONWriter startElement() {
        out.print(div);
        out.print(objectStart);
        out.print(newLine);
        div = "";
        return this;
    }

    public JSONWriter startArrayProperty(String name) {
        out.print(div);
        out.print(newLine);
        out.print("\"" + name + "\"");
        out.print(elementDiv);
        out.print(arrayStart);
        out.print(newLine);
        div = "";
        return this;
    }

    public JSONWriter endArrayProperty() {
        out.print(arrayEnd);
        out.print(newLine);
        div = ",";
        return this;
    }

    public JSONWriter setSingleLine(boolean isSingleLine) {
        this.isSingleLine = isSingleLine;
        if (isSingleLine) {
            this.newLine = "";
        }
        else {
            this.newLine = String.format("%n");
        }
        return this;
    }

    public JSONWriter startElementProperty(String name) {
        out.print(div);
        out.print(newLine);
        out.print("\"" + name + "\"");
        out.print(elementDiv);
        out.print(objectStart);
        out.print(newLine);
        div = "";
        return this;
    }

    public JSONWriter endElementProperty() {
        out.print(objectEnd);
        out.print(newLine);
        div = "";
        return this;
    }

    public JSONWriter writeString(String json) {
        out.print(json);
        return this;
    }

    public void write(int[] intArray) {
        String div = "";
        out.print(arrayStart);
        for (int i : intArray) {
            out.print(div);
            out.print(i);
            div = ",";
        }
        out.print(arrayEnd);
    }

    public void write(Object[] strings) {
        String div = "";
        out.print(arrayStart);
        for (Object object : strings) {
            out.print(div);
            out.print("\"");
            out.print(object.toString());
            out.print("\"");
            div = ",";
        }
        out.print(arrayEnd);
    }
    

    public static String jsonify(String input) {
        String output = input;
        output = output.replace("\\", "\\\\");
        output = output.replace("\"", "\\\"");
        output = output.replace("\b", "\\b");
        output = output.replace("\t", "\\t");
        output = output.replace("\f", "\\f");
        output = output.replace("\r", "\\r");
        output = output.replace("\n", "\\n");        
        return output;
    }

}
