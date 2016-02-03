package matt;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Created by james on 03/02/2016.
 */
public class HTMLTestLocal {

    public static void main(String[] args) {

        // HTML support
        JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);
        String img = "file:./chart.png";
        jEditorPane.setText("<html><head></head><body><h1>Test</h1><img src=\"" + img + "\"></img></body></html>");

        JScrollPane scrollPane = new JScrollPane(jEditorPane);

        JFrame frame = new JFrame("HTML test");
        frame.getContentPane().add(scrollPane);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

}
