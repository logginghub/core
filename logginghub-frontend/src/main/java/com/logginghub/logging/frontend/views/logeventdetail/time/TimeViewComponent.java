package com.logginghub.logging.frontend.views.logeventdetail.time;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.DateFormatFactory;
import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.Pair;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class TimeViewComponent extends JComponent {

    private static final Font FONT2 = new Font("Arial", Font.BOLD, 12);

    private static final Logger logger = Logger.getLoggerFor(TimeViewComponent.class);

    private static final int ONE_SECOND = 1000;
    private static final int TEN_SECOND = 10 * 1000;
    private static final int THIRTY_SECOND = 30 * 1000;

    private static final int ONE_MINUTE = 60 * 1000;
    private static final int TEN_MINUTE = 10 * 60 * 1000;
    private static final int THIRTY_MINUTE = 30 * 60 * 1000;

    private static final int ONE_HOUR = 60 * 60 * 1000;
    private static final int SIX_HOUR = 6 * 60 * 60 * 1000;
    private static final int TWELVE_HOUR = 12 * 60 * 60 * 1000;

    private static final int ONE_DAY = 24 * 60 * 60 * 1000;

    // private Image startImage = ResourceUtils.loadImage("/icons/greendown.png");
    // private Image endImage = ResourceUtils.loadImage("/icons/reddown.png");

    // private Rectangle startBounds = new Rectangle();
    // private Rectangle endBounds = new Rectangle();
    // private Rectangle dragTarget = null;

//    private TimeProvider timeProvider = new SystemTimeProvider();

    private Point filterDragStart = null;
    private long viewStartTimeAtDragStart = -1;

    private Cursor leftCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
    private Cursor rightCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    private boolean firstPaint = true;

    private DateFormat dateFormat = DateFormatFactory.getDateThenTimeWithMillis(DateFormatFactory.utc);

    private TimeModel model = new TimeModel();

    // private long startingDelta;
    // private int startingDistance;

    private long viewEndTime;

    private TimeController controller;

    private boolean dirty = true;

    public TimeViewComponent() {
        
        setName("TimeViewComponent");

        addMouseMotionListener(new MouseMotionListener() {

            @Override public void mouseMoved(MouseEvent e) {
                Point point = e.getPoint();
                long convertXToTime = convertXToTime(point.x);
                setToolTipText(Logger.toLocalDateString(convertXToTime).toString());
                setDirty(true);
            }

            @Override public void mouseDragged(MouseEvent e) {
                logger.fine("Mouse dragged : {}", e);

                Point point = e.getPoint();

                if (filterDragStart == null) {
                    logger.fine("Starting filter drag");
                    filterDragStart = point;
                    viewStartTimeAtDragStart = model.getViewStart().longValue();
                }
                else {
                    if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
                        processFilterDrag(filterDragStart, point, false);
                    }
                    else {
                        processScrollDrag(filterDragStart, point);
                    }
                }

                setDirty(true);
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            int delay;

            @Override public void mouseEntered(MouseEvent e) {
                delay = ToolTipManager.sharedInstance().getInitialDelay();
                ToolTipManager.sharedInstance().setInitialDelay(10);
                setDirty(true);
            }

            @Override public void mouseExited(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(delay);
                setDirty(true);
            }

            @Override public void mouseClicked(MouseEvent e) {
                logger.fine("Mouse clicked at time point '{}'", Logger.toDateString(convertXToTime(e.getPoint().x)));
                controller.clearSelection();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Point point = e.getPoint();
                    long convertXToTime = convertXToTime(point.x);
                    controller.timeClicked(convertXToTime);
                }
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    controller.clearAndPlay();
                }
                setDirty(true);
                repaint();
            }
            
            @Override public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                logger.fine("Mouse pressed '{}'", e);
                
                if (filterDragStart == null) {
                    logger.fine("Starting filter drag at '{}'", e.getPoint());
                    filterDragStart = e.getPoint();
                    viewStartTimeAtDragStart = model.getViewStart().longValue();
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                logger.fine("Mouse released '{}'", e);
                if (filterDragStart != null) {
                    Point endPoint = e.getPoint();

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        processFilterDrag(filterDragStart, endPoint, true);
                    }
                    else {
                        processScrollDrag(filterDragStart, endPoint);
                    }

                    filterDragStart = null;
                    viewStartTimeAtDragStart = -1;
                }
                setDirty(true);
                repaint();
            }
        });

    }

    public void updateWidth() {

        // if the first pixel on the left is now, what is the end time?
        int width = getWidth();

        long start = model.getViewStart().longValue();

        long interval = model.getInterval().longValue();

        long timeRage = width * interval;

        this.viewEndTime = start + timeRage;
    }

    public long convertXToTime(int x) {

        long start = model.getViewStart().longValue();
        long end = viewEndTime;

        long timeRange = end - start;
        int width = getWidth();

        double factor = x / (float) width;

        long time = (long) (start + (factor * timeRange));

        // Strip the millis off, thats a bit too precise
        time = TimeUtils.chunk(time, TimeUtils.seconds);

        return time;
    }

    public int convertTimeToX(long time) {

        long start = model.getViewStart().longValue();
        long end = viewEndTime;

        long timeRange = end - start;
        int width = getWidth();

        float factor = (time - start) / (float) timeRange;

        int x = (int) (width * factor);
        return x;
    }

    protected void processScrollDrag(Point start, Point end) {

        int startX = start.x;
        int endX = end.x;

        // TODO : some people might find it more intuitive to scroll one way or the other (like drag
        // scrolling on a touch screen, its counter intuitive to mouse dragging)
        boolean invertScrolling = false;
        int delta;
        if (invertScrolling) {
            delta = -endX + startX;
        }
        else {
            delta = +endX - startX;
        }

        // Speed up the scroll the further away the points are
        double acceleration = Math.log(Math.abs(delta));

        long time = (long) (model.getInterval().longValue() * delta * acceleration);

        model.getViewStart().set(time + viewStartTimeAtDragStart);

        logger.fine("Updating scroll drag {} vs {} : time range is '{}' pixels or {} ms : filter start was '{}', new view start '{}' end '{}'",
                    start,
                    end,
                    delta,
                    time,
                    Logger.toDateString(model.getViewStart().longValue()));

        // Update the scrolling point, otherwise wacky things happen
        // filterDragStart = end;

        // model.getSelectionStart().set(filterStart);
        // model.getSelectionEnd().set(filterEnd);
    }

    protected void processFilterDrag(Point start, Point end, boolean endEvent) {

        long filterStart = convertXToTime(start.x);
        long filterEnd = convertXToTime(end.x);

        logger.fine("Updating filter drag {} vs {} : start '{}' end '{}'",
                    start,
                    end,
                    Logger.toDateString(filterStart),
                    Logger.toDateString(filterEnd));

        if (endEvent) {
            controller.updateSelection(filterStart, filterEnd);
        }
        else {
            // Just update the visual markers without triggering the main change event
            model.getSelectionStart().set(filterStart);
            model.getSelectionEnd().set(filterEnd);
        }
        controller.clearClickedTime();
    }

    // protected void updateEnd() {
    // int startX = startBounds.x + startBounds.width / 2;
    // int endX = endBounds.x + endBounds.width / 2;
    //
    // int nowDistance = endX - startX;
    //
    // double ratio = nowDistance / (double) startingDistance;
    //
    // long now = (long) (ratio * startingDelta);
    //
    // long start = model.getStart().longValue();
    //
    // long newEnd = start + now;
    // model.getEnd().set(newEnd);
    // }

    protected void updateStart() {
        //
        // int startX = startBounds.x + startBounds.width / 2;
        // int endX = 100;//endBounds.x + endBounds.width / 2;
        //
        // int nowDistance = endX - startX;
        //
        // double ratio = nowDistance / (double) startingDistance;
        //
        // long now = (long) (ratio * startingDelta);
        //
        // long end = model.getEnd().longValue();
        //
        // long newStart = end - now;
        // model.getStart().set(newStart);

    }

    protected void updateValues() {

    }

    public TimeController getController() {
        return controller;
    }
    
    protected void bind(TimeController controller) {
        this.controller = controller;
        this.model = controller.getModel();

        model.getChanges().addListener(new ObservablePropertyListener<Integer>() {
            @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
//                logger.fine("Setting dirty, waiting for the repaint thread to draw us again...");
//                setDirty(true);
                repaint();
            }
        });

        // this.startingDelta = model.getEnd().longValue() - model.getStart().longValue();

        // TODO : bind to external changes

    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // jshaw - bit of a hack
        model.getViewWidth().set(getWidth());        
        
        // if(true) {
        // return;
        // }
        dirty = false;

        Graphics2D g2d = (Graphics2D) g;

        Insets insets = getInsets();

        int startX = insets.left;
        int startY = insets.top;

        int width = getWidth();
        int height = getHeight();

        int internalWidth = width - insets.left - insets.right;
        int internalHeight = height - insets.top - insets.bottom;

        int adjustedHeight = internalHeight - 16;

        if (firstPaint) {

            int defaultXStart = (int) (internalWidth * 0.5f);
            int defaultXEnd = (int) (internalWidth * 0.8f);

            // startBounds = new Rectangle(defaultXStart, 0, 32, 32);
            // endBounds = new Rectangle(defaultXEnd, 0, 32, 32);

            // startingDistance = defaultXEnd - defaultXStart;

            firstPaint = false;
        }

        int endX = width - insets.right;
        int endY = height - insets.bottom;

        // TODO : only really need to do this on a resize?
        updateWidth();

        // Do the Math
        long start = model.getViewStart().longValue();
        long end = viewEndTime;
        long interval = model.getInterval().longValue();

        long chunkedStart = TimeUtils.chunk(start, interval);
        long chunkedEnd = TimeUtils.chunk(end, interval);

        long selectionStart = model.getSelectionStart().longValue();
        long selectionEnd = model.getSelectionEnd().longValue();

        int selectionStartX = convertTimeToX(selectionStart);
        int selectionEndX = convertTimeToX(selectionEnd);

        logger.finest("Selection start x {} end x {}", selectionStartX, selectionEndX);

        logger.finest("Start is '{}' end is '{}' period is '{}'", Logger.toDateString(chunkedStart), Logger.toDateString(chunkedEnd), interval);

        HistoricalIndexElement max = model.getMaximumCount();
        int count = (int) ((chunkedEnd - chunkedStart) / interval);

        // Draw the outline
        g2d.setColor(Color.black);
        g2d.drawRect(startX, startY, internalWidth - 1, adjustedHeight);

        // Work out where the markers should appear for the different intervals
        long textInterval;
        long minor;
        switch ((int) interval) {
            case ONE_SECOND:
                textInterval = TimeUtils.minutes(1);
                minor = TimeUtils.seconds(10);
                break;
            case TEN_SECOND:
                textInterval = TimeUtils.minutes(10);
                minor = TimeUtils.minutes(1);
                break;
            case THIRTY_SECOND:
                textInterval = TimeUtils.minutes(30);
                minor = TimeUtils.minutes(5);
                break;
            case ONE_MINUTE:
                textInterval = TimeUtils.minutes(60);
                minor = TimeUtils.minutes(10);
                break;
            case TEN_MINUTE:
                textInterval = TimeUtils.hours(6);
                minor = TimeUtils.hours(1);
                break;
            case THIRTY_MINUTE:
                textInterval = TimeUtils.hours(24);
                minor = TimeUtils.hours(2);
                break;
            case ONE_HOUR:
                textInterval = TimeUtils.days(2);
                minor = TimeUtils.hours(12);
                break;
            case SIX_HOUR:
                textInterval = TimeUtils.days(14);
                minor = TimeUtils.days(1);
                break;
            case TWELVE_HOUR:
                textInterval = TimeUtils.days(28);
                minor = TimeUtils.days(2);
                break;
            case ONE_DAY:
                textInterval = TimeUtils.days(56);
                minor = TimeUtils.days(7);
                break;
            default:
                throw new NotImplementedException("" + interval + " : " + TimeUtils.formatIntervalMilliseconds(interval));

        }

        // ///// draw the date markers
        for (int i = 0; i < count; i++) {
            long chunk = chunkedStart + (i * interval);

            if (chunk % textInterval == 0) {
                g2d.setColor(Color.black);
                g2d.drawLine(i, 0, i, adjustedHeight);
            }
            else if (chunk % minor == 0) {
                g2d.setColor(Color.gray);
                g2d.drawLine(i, 0, i, adjustedHeight);
            }
        }

        // Use blending so we can still see the date markers
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        g2d.setComposite(alphaComposite);

        Map<Long, HistoricalIndexElement> counts = model.getCounts();
//        dumpCounts(counts);

        // /////// draw the waveform
        for (int i = 0; i < count; i++) {
            long chunk = chunkedStart + (i * interval);
            HistoricalIndexElement element = counts.get(chunk);
            if (element != null) {
                logger.finest("Chunk is {} : '{}' : {}", i, Logger.toDateString(chunk), element.getTotalCount());

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.setColor(Color.gray);
                float scale = element.getInfoCount() / (float) max.getInfoCount();
                int barHeight = (int) (scale * adjustedHeight);
                g2d.drawLine(i, adjustedHeight - barHeight, i, adjustedHeight);

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2d.setColor(Color.yellow);
                scale = 0.5f * element.getWarningCount() / (float) max.getWarningCount();
                barHeight = (int) (scale * adjustedHeight);
                if (barHeight > 0) {
                    // g2d.drawLine(i, adjustedHeight - barHeight, i, adjustedHeight);
                    g2d.fillRect(i, 1 + adjustedHeight - barHeight, 2, barHeight - 1);
                }

                g2d.setColor(Color.red);
                scale = 0.5f * element.getSevereCount() / (float) max.getSevereCount();
                barHeight = (int) (scale * adjustedHeight);
                if (barHeight > 0) {
                    g2d.drawLine(i, 1 + adjustedHeight - barHeight, i, adjustedHeight - 1);
                }
            }
            else {
                logger.finest("Chunk is {} : '{}' : no data", i, Logger.toDateString(chunk));
            }
        }

        // draw the filter selector
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g2d.setColor(Color.cyan.darker());
        g2d.fillRect(selectionStartX, 0, selectionEndX - selectionStartX, adjustedHeight);

        // Switch off blending
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // draw the time selector
        if (model.getClickedTime().longValue() != Long.MAX_VALUE) {
            int x = convertTimeToX(model.getClickedTime().longValue());
            g2d.setColor(Color.cyan);
            g2d.drawLine(x, 0, x, adjustedHeight);
        }

        // draw the Now indicator
        int x = convertTimeToX(model.getCurrentTime().longValue());
        g2d.setColor(Color.red);
        g2d.drawLine(x, 0, x, adjustedHeight);

        // ///// draw the time text
        g2d.setFont(FONT2);

        for (int i = 0; i < count; i++) {
            long chunk = chunkedStart + (i * interval);

            if (chunk % textInterval == 0) {
                g2d.setColor(Color.black);
                g2d.drawString(formatTime(chunk), i - 13, internalHeight - 3);
            }
        }
    }

    private void dumpCounts(Map<Long, HistoricalIndexElement> counts) {

        List<Pair<Long, HistoricalIndexElement>> list = new ArrayList<Pair<Long, HistoricalIndexElement>>();

        Set<Entry<Long, HistoricalIndexElement>> entrySet = counts.entrySet();
        for (Entry<Long, HistoricalIndexElement> entry : entrySet) {
            list.add(new Pair<Long, HistoricalIndexElement>(entry.getKey(), entry.getValue()));
        }

        Collections.sort(list, new Comparator<Pair<Long, HistoricalIndexElement>>() {
            @Override public int compare(Pair<Long, HistoricalIndexElement> o1, Pair<Long, HistoricalIndexElement> o2) {
                return CompareUtils.compare(o1.getA(), o2.getA());
            }
        });

        for (Pair<Long, HistoricalIndexElement> entry : list) {
            logger.info("{} : {}", entry.getA(), entry.getB());
        }

    }

    private String formatTime(long chunk) {
        return TimeUtils.formatJustTimeNoSeconds(chunk, TimeZone.getDefault());
    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(100, 30);
    }

    public void unbind() {}

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public long getViewEndTime() {
        return viewEndTime;
    }
}
