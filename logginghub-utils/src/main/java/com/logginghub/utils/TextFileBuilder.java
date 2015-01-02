package com.logginghub.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class TextFileBuilder {

    private BufferedWriter writer;
    
    public TextFileBuilder(Writer writer) {
        this.writer = new BufferedWriter(writer);
    }

    public void close() {
        FileUtils.closeQuietly(writer);
    }

    public TextFileBuilder table() {
        appendLine("<table border='1'>");        
        return this;         
    }
    
    public TextFileBuilder endTable() {
        appendLine("</table>");        
        return this;         
    }
    
    public TextFileBuilder html() {
        appendLine("<html>");        
        return this;         
    }
    
    public TextFileBuilder endHtml() {
        appendLine("</html>");        
        return this;         
    }
    
    public TextFileBuilder head() {
        appendLine("<head>");        
        return this;         
    }
    
    public TextFileBuilder endHead() {
        appendLine("</head>");        
        return this;         
    }
    
    public TextFileBuilder body() {
        appendLine("<body>");        
        return this;         
    }
    
    public TextFileBuilder endBody() {
        appendLine("</body>");        
        return this;         
    }

    public TextFileBuilder tr() {
        appendLine("<tr>");        
        return this;         
    }
    
    public TextFileBuilder endTr() {
        appendLine("</tr>");        
        return this;         
    }
    
    public TextFileBuilder th(Object content) {
        appendLine("<th>" + content +"</th>");        
        return this;         
    }
    
    public TextFileBuilder endTh() {
        appendLine("</th>");        
        return this;         
    }
    
    public TextFileBuilder td(Object content) {
        appendLine("<td nowrap='nowrap'>" + content + "</td>");        
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

    public TextFileBuilder thead() {
        appendLine("<thead>");
        return this;
    }
    
    public TextFileBuilder endThead() {
        appendLine("</thead>");
        return this;
    }

    public TextFileBuilder p() {
        appendLine("<p>");
        return this;
    }
    

    public TextFileBuilder endP() {
        appendLine("</p>");
        return this;
    }
}
