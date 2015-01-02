package com.logginghub.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.NumberFormat;

public class HTMLBuilder {

    private BufferedWriter writer;
    private StringWriter stringWriter;

    public HTMLBuilder(Writer writer) {
        this.writer = new BufferedWriter(writer);
    }

    public HTMLBuilder() {
        this.stringWriter = new StringWriter();
        this.writer = new BufferedWriter(stringWriter);
    }

    @Override public String toString() {
        if (stringWriter != null) {
            return stringWriter.toString();
        }
        else {
            return this.toString();
        }
    }

    public void close() {
        FileUtils.closeQuietly(writer);
    }

    public HTMLBuilder table() {
        appendLine("<table border='1'>");
        return this;
    }

    public HTMLBuilder tableNoBorder() {
        appendLine("<table border='0'>");
        return this;
    }

    public HTMLBuilder endTable() {
        appendLine("</table>");
        return this;
    }

    public HTMLBuilder html() {
        appendLine("<html>");
        return this;
    }

    public HTMLBuilder endHtml() {
        appendLine("</html>");
        return this;
    }

    public HTMLBuilder head() {
        appendLine("<head>");
        return this;
    }

    public HTMLBuilder endHead() {
        appendLine("</head>");
        return this;
    }

    public HTMLBuilder body() {
        appendLine("<body>");
        return this;
    }

    public HTMLBuilder endBody() {
        appendLine("</body>");
        return this;
    }

    public HTMLBuilder tr() {
        appendLine("<tr>");
        return this;
    }

    public HTMLBuilder td() {
        appendLine("<td>");
        return this;
    }

    public HTMLBuilder endTr() {
        appendLine("</tr>");
        return this;
    }

    public HTMLBuilder endTd() {
        appendLine("</td>");
        return this;
    }

    public HTMLBuilder th(Object content) {
        appendLine("<th>" + content + "</th>");
        return this;
    }

    public HTMLBuilder endTh() {
        appendLine("</th>");
        return this;
    }

    public HTMLBuilder tdFormat(double value) {
        NumberFormat instance = NumberFormat.getInstance();
        instance.setMaximumFractionDigits(2);
        instance.setMinimumFractionDigits(2);
        return td(instance.format(value));
    }

    public HTMLBuilder td(Object value) {
        appendLine("<td nowrap='nowrap'>" + value + "</td>");
        return this;
    }

    public HTMLBuilder td(String content, Object... values) {
        appendLine("<td nowrap='nowrap'>" + StringUtils.format(content, values) + "</td>");
        return this;
    }

    public void appendLine(String string) {
        try {
            writer.append(string);
            writer.newLine();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HTMLBuilder appendLine(String format, Object... values) {
        try {
            writer.append(StringUtils.format(format, values));
            writer.newLine();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    public HTMLBuilder append(String format, Object... values) {
        try {
            writer.append(StringUtils.format(format, values));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }


    public HTMLBuilder thead() {
        appendLine("<thead>");
        return this;
    }

    public HTMLBuilder endThead() {
        appendLine("</thead>");
        return this;
    }

    public HTMLBuilder p() {
        appendLine("<p>");
        return this;
    }

    public HTMLBuilder endP() {
        appendLine("</p>");
        return this;
    }

    public HTMLBuilder div(String string, Object... params) {
        appendLine("<div>" + string + "</div>", params);
        return this;
    }
    
    public HTMLBuilder span(String string, Object... params) {
        appendLine("<span>" + string + "</span>", params);
        return this;
    }

    public HTMLBuilder pre(String string, Object... params) {
        appendLine("<pre>" + string + "</pre>", params);
        return this;
    }

    public HTMLBuilder link(String link) {
        appendLine("<a href='{}'>{}</a>", link, link);
        return this;
    }

    public HTMLBuilder link(String linkFormat, String variable) {
        appendLine("<a href='{}'>{}</a>", StringUtils.format(linkFormat, variable), variable);
        return this;
    }

    public HTMLBuilder link(String text, String urlFormat, Object... variables) {
        appendLine("<a href='{}'>{}</a>", StringUtils.format(urlFormat, variables), text);
        return this;
    }

    public HTMLBuilder endForm() {
        appendLine("</form>");
        return this;
    }

    public HTMLBuilder formPost(String action) {
        appendLine("<form action=\"{}\" method=\"POST\">", action);
        return this;
    }
    
    public HTMLBuilder formGet(String action) {
        appendLine("<form action=\"{}\" method=\"GET\">", action);
        return this;
    }
    
    public HTMLBuilder form() {
        appendLine("<form>");
        return this;
    }

    public HTMLBuilder checkbox(String group, String value, String text) {
        appendLine("<input type=\"checkbox\" name=\"{}\" value=\"{}\">{}</input><br/>", group, value, text);
        return this;
    }

    public HTMLBuilder radioButton(String group, String value, String text) {
        appendLine("<input type=\"radio\" name=\"{}\" value=\"{}\">{}</input><br/>", group, value, text);
        return this;
    }

    public HTMLBuilder submit(String text) {
        appendLine("<input type=\"submit\" value=\"{}\">", text);
        return this;
    }

    public HTMLBuilder input(String name, String initial) {
        appendLine("<input type=\"text\" name=\"{}\" value=\"{}\"/>", name, initial);
        return this;
    }

    public HTMLBuilder text(String string, Object... objects) {
        append(string, objects);
        return this;
    }

    public HTMLBuilder br() {
        appendLine("<br/>");
        return this;
    }

    public HTMLBuilder fieldset(String string) {
        appendLine("<fieldset/>");
        appendLine("<legend>{}</legend>", string);
        return this;
    }
    
    public HTMLBuilder endFieldset() {
        appendLine("</fieldset>");
        return this;
    }

    

    

}
