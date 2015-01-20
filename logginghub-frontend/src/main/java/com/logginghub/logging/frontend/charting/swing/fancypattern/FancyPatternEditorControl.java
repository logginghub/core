package com.logginghub.logging.frontend.charting.swing.fancypattern;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.logginghub.logging.frontend.charting.swing.EventSource;
import com.logginghub.logging.frontend.charting.swing.fancypattern.FancyPatternElement.ElementState;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtilsTokeniser;
import com.logginghub.utils.logging.Logger;

public class FancyPatternEditorControl extends JComponent {

    private static final Logger logger = Logger.getLoggerFor(FancyPatternEditorControl.class);
    private int nextGroupID = 0;
    private String text = "";

    private List<FancyPatternElement> elements = new ArrayList<FancyPatternElement>();

    private EventSource changedEvent = new EventSource("patternChanged");

    private FancyPatternElement mouseOverFancyPatternElement = null;
    private FancyPatternElement mousePressedFancyPatternElement = null;

    public FancyPatternEditorControl() {
        addMouseMotionListener(new MouseMotionListener() {
            @Override public void mouseMoved(MouseEvent e) {

            }

            @Override public void mouseDragged(MouseEvent e) {
                Point point = e.getPoint();

                FancyPatternElement elementAtPoint = getElementAtPoint(point);
                if (elementAtPoint != null) {

                    logger.fine("Mouse now over '{}'", elementAtPoint);

                    if (mousePressedFancyPatternElement != null) {
                        int startIndex = elements.indexOf(mousePressedFancyPatternElement);
                        int endIndex = elements.indexOf(elementAtPoint);

                        for (int i = 0; i < elements.size(); i++) {

                            if (i >= startIndex && i <= endIndex) {
                                elements.get(i).setSelected(true);
                            }
                            else {
                                elements.get(i).setSelected(false);
                            }
                        }

                    }

                    repaint();
                }

            }
        });

        addMouseListener(new MouseAdapter() {

            @Override public void mousePressed(MouseEvent e) {
                FancyPatternElement elementAtPoint = getElementAtPoint(e.getPoint());
                if (elementAtPoint != null) {
                    logger.fine("Mouse pressed on element '{}'", elementAtPoint);
                    mousePressedFancyPatternElement = elementAtPoint;
                    repaint();
                }
            }

            public void mouseReleased(MouseEvent e) {
                mousePressedFancyPatternElement = null;
            };

            @Override public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                FancyPatternElement elementAtPoint = getElementAtPoint(point);
                if (elementAtPoint != null) {
                    if (elementAtPoint.isSelected()) {
                        int group = nextGroupID++;

                        String fullGroup = buildSelectedString();
                        boolean isNumeric = StringUtils.isNumeric(fullGroup);

                        // Needs to start with a state other than raw
                        ElementState state = ElementState.numeric;

                        for (int i = 0; i < elements.size(); i++) {
                            FancyPatternElement element = elements.get(i);
                            if (element.isSelected()) {
                                logger.fine("Assigning element '{}' to group {} and cycling", element, group);
                                element.setGroup(group);
                                element.cycleState(isNumeric);
                                state = element.getElementState();
                            }
                        }

                        if (state == ElementState.raw) {
                            // Looks like we've just cycled the state back to raw, so we have to
                            // break the group up but leave it selected
                            for (int i = 0; i < elements.size(); i++) {
                                FancyPatternElement element = elements.get(i);
                                if (element.isSelected()) {
                                    element.setGroup(-1);
                                }
                            }   
                        }
                        
                        changedEvent.fireEvent(group);
                    }
                    else {
                        clearSelection();
                        elementAtPoint.setSelected(true);
                        if (elementAtPoint.getGroup() != -1) {
                            selectEntireGroup(elementAtPoint.getGroup());
                        }
                    }
                }

                repaint();
            }

            private String buildSelectedString() {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < elements.size(); i++) {
                    FancyPatternElement element = elements.get(i);
                    if (element.isSelected()) {
                        builder.append(element.getText());
                    }
                }
                return builder.toString();
            }

            private void clearSelection() {
                for (int i = 0; i < elements.size(); i++) {
                    FancyPatternElement element = elements.get(i);
                    element.setSelected(false);
                }
            }

            protected void selectEntireGroup(int group) {
                for (int i = 0; i < elements.size(); i++) {
                    FancyPatternElement element = elements.get(i);
                    if (element.getGroup() == group) {
                        element.setSelected(true);
                    }
                }
            }

        });

        buildModel();
    }
    
    public List<FancyPatternElement> getElements() {
        return elements;
    }

    public FancyPatternElement getElementAtPoint(Point point) {

        FancyPatternElement found = null;
        for (FancyPatternElement element : elements) {
            if (element.getBounds().contains(point)) {
                found = element;
                break;
            }
        }

        return found;
    }

    public void setEditedText(String newValue) {
        this.text = newValue;
        buildModel();
        repaint();
    }

    public EventSource getChangedEvent() {
        return changedEvent;
    }

    public void setRawText(String rawText) {
        this.text = rawText;
        buildModel();
    }

    public void buildModel() {
        elements.clear();

        StringUtilsTokeniser tokenise = StringUtils.tokenise(text);

        while (tokenise.hasMore()) {
            String element = tokenise.nextUpToCharacterTypeChange();
            elements.add(new FancyPatternElement(element));
        }

    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        FontRenderContext fontRenderContext = g2d.getFontRenderContext();

        Font font = g2d.getFont();

        LineMetrics lineMetrics = font.getLineMetrics(text, fontRenderContext);

        int ascent = (int) lineMetrics.getAscent();
        int decent = (int) lineMetrics.getDescent();
        int height = ascent + decent;

        FontMetrics fontMetrics = g.getFontMetrics();

        Insets insets = getInsets();

        int currentX = insets.left;
        int currentY = insets.top;
        int wordGap = 0; // dont need a word gap really now spaces are included

        int width = getWidth() - insets.right - insets.left;

        for (FancyPatternElement element : elements) {

            String elementstring = element.getText();

            Rectangle2D stringBounds = g2d.getFontMetrics().getStringBounds(elementstring, g2d);

            // Check to see if this word will exceed the line length
            if (currentX + stringBounds.getWidth() > width) {
                currentX = insets.left;
                currentY += height;
            }

            if (element.isSelected()) {
                g.setFont(font.deriveFont(Font.BOLD));
            }
            else {
                g.setFont(font);
            }

            ElementState elementState = element.getElementState();
            switch (elementState) {
                case raw:
                    g.setColor(Color.black);
                    break;
                case nonnumeric:
                    g.setColor(Color.green.darker());
                    break;
                case numeric:
                    g.setColor(Color.red.darker());
                    break;
            }

            g.drawString(elementstring, currentX, currentY + ascent);
            // g.drawRect(currentX, currentY, (int) stringBounds.getWidth(), (int)
            // stringBounds.getHeight());

            Rectangle bounds = new Rectangle(currentX, currentY, (int) stringBounds.getWidth(), (int) stringBounds.getHeight());
            element.setBounds(bounds);

            currentX += stringBounds.getWidth();
            currentX += wordGap;
        }

    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(400, 150);
    }
    
    @Override public Dimension getMinimumSize() {
        return new Dimension(400, 150);         
    }

}