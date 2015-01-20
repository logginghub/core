package com.logginghub.logging.frontend.views.environmentsummary;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.logginghub.utils.Tracer;
import com.logginghub.utils.logging.Logger;

public class ResizingLabel extends JLabel {
    private static final long serialVersionUID = 1L;
    public static final int MIN_FONT_SIZE = 10;
    public static final int MAX_FONT_SIZE = 2000;

    private static final Logger logger = Logger.getLoggerFor(ResizingLabel.class);
    private Dimension minimumSize = new Dimension(1, 1);
    private Dimension maximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    private Dimension preferredSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    private boolean sizeOk = false;
    private Dimension textSize;
    private int descent;
    private FontMetrics fontMetrics;

    public ResizingLabel(String text) {
        super(text);
        setFont(Font.decode("Segoe UI-PLAIN-30"));
        init();
    }

    protected void init() {
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                // adaptLabelFont(ResizingLabel.this);
                sizeOk = false;
            }
        });
    }

    @Override public Dimension getMaximumSize() {
        return maximumSize;
    }

    @Override public Dimension getMinimumSize() {
        return minimumSize;
    }

    @Override public void setSize(Dimension d) {
        super.setSize(d);

        Tracer.trace("Someone has set the size to {}", d);
    }

    @Override public Dimension getPreferredSize() {
        return preferredSize;
    }

    @Override public void setSize(int width, int height) {
        super.setSize(width, height);
        Tracer.trace("Someone has set the size to {} x {}", width, height);
    }

    protected void adaptLabelFont(Graphics g) {
        Tracer.trace("Resizing label... {}", getSize());

        Font f = getFont();
        int fontSize = MIN_FONT_SIZE;

        Dimension size = this.getSize();

        int effectiveWidth = size.width - getInsets().left - getInsets().right;
        int effectiveHeight = size.height - getInsets().top - getInsets().bottom;

        Font deriveFont = null;
        while (fontSize < MAX_FONT_SIZE) {
            deriveFont = f.deriveFont(f.getStyle(), fontSize);
            fontMetrics = g.getFontMetrics(deriveFont);
            textSize = getTextSize(fontMetrics);

//            logger.info("Size {} : ascent {} descent {} height {}", fontSize, fontMetrics.getAscent(), fontMetrics.getDescent(), fontMetrics.getHeight());

            if (textSize.width > effectiveWidth) {
//                logger.trace("Size {} exceeds width", fontSize);
                break;
            }
            else if (fontMetrics.getAscent() - fontMetrics.getDescent() > effectiveHeight) {
                // else if (textSize.height > effectiveHeight) {
//                logger.info("Size {} exceeds height (our height is {}, text height is {})", fontSize, effectiveHeight, textSize.height);
                break;
            }
            fontSize++;
        }

        Tracer.trace("Font size is {}", fontSize);

        setFont(deriveFont);
        sizeOk = true;
    }

    private Dimension getTextSize(FontMetrics fm) {
        Dimension size = new Dimension();
        size.width = fm.stringWidth(getText());
        size.height = fm.getHeight();// - fm.getDescent();
        descent = fm.getDescent();
        return size;
    }

  

    @Override public void setText(String text) {
        if (text != null) {
            String currentText = getText();

            if (currentText.length() != text.length()) {
                sizeOk = false;
            }

            super.setText(text);
        }
    }

    protected void paintComponent(Graphics g) {
        if (!sizeOk) {
            adaptLabelFont(g);
        }

        Dimension size = getSize();
        Dimension textSize2 = textSize;

        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, size.width, size.height);
        }

        g.setColor(getForeground());
        g.setFont(getFont());
        int x = (size.width / 2) - (textSize2.width / 2);
        // int y = (size.height / 2) + ((textSize2.height-descent) / 2);
        int y = size.height - getInsets().bottom - (fontMetrics.getDescent() / 3);
//        logger.info("Drawing at {},{} - height is {}", x, y, size.height);
        g.drawString(getText(), x, y);
    }

    // @Override public Dimension getPreferredSize() {
    // Tracer.trace("Pref size {}", preferredSize);
    // return preferredSize;
    // }

    public static void main(String[] args) throws Exception {
        ResizingLabel label = new ResizingLabel("Some text");
        JFrame frame = new JFrame("Resize label font");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(label);

        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}