package com.logginghub.logging.frontend.views.stack;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import com.logginghub.utils.DateFormatFactory;
import com.logginghub.utils.Out;
import com.logginghub.utils.logging.Logger;

public class TreeMapComponent extends JComponent {

    private static final Font FONT2 = new Font("Arial", Font.BOLD, 12);

    private static final Logger logger = Logger.getLoggerFor(TreeMapComponent.class);

    // private Image startImage = ResourceUtils.loadImage("/icons/greendown.png");
    // private Image endImage = ResourceUtils.loadImage("/icons/reddown.png");

    // private Rectangle startBounds = new Rectangle();
    // private Rectangle endBounds = new Rectangle();
    // private Rectangle dragTarget = null;

    private Point filterDragStart = null;
    private long viewStartTimeAtDragStart = -1;

    private Cursor leftCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
    private Cursor rightCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    private boolean firstPaint = true;

    private DateFormat dateFormat = DateFormatFactory.getDateThenTimeWithMillis(DateFormatFactory.utc);

    private CountingTreeMap countingTreeMap;

    private Map<CountingTreeMap, Rectangle> bounds = new HashMap<CountingTreeMap, Rectangle>();

    private StackTraceController controller;

    public TreeMapComponent() {

        addMouseMotionListener(new MouseMotionListener() {

            @Override public void mouseMoved(MouseEvent e) {
                setToolTipText(getKeyAt(e.getPoint()));
            }

            @Override public void mouseDragged(MouseEvent e) {
                logger.fine("Mouse dragged : {}", e);

                Point point = e.getPoint();

                if (filterDragStart == null) {
                    logger.fine("Starting filter drag");
                    filterDragStart = point;
                }
                else {
                    if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
                        processFilterDrag(filterDragStart, point, false);
                    }
                    else {
                        processScrollDrag(filterDragStart, point);
                    }
                }

            }
        });

        addMouseListener(new MouseAdapter() {
            int delay;

            @Override public void mouseEntered(MouseEvent e) {
                delay = ToolTipManager.sharedInstance().getInitialDelay();
                ToolTipManager.sharedInstance().setInitialDelay(10);
            }

            @Override public void mouseExited(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(delay);
            }

            @Override public void mouseClicked(MouseEvent e) {}

            @Override public void mouseReleased(MouseEvent e) {}
        });

    }

    protected String getKeyAt(Point point) {

        int deepest = 0;
        String found = "";
        Set<Entry<CountingTreeMap, Rectangle>> entrySet = bounds.entrySet();
        for (Entry<CountingTreeMap, Rectangle> entry : entrySet) {

            Rectangle value = entry.getValue();

            if (value.contains(point)) {
                int depth = entry.getKey().getDepth();
                // value.getWidth() * value.getHeight();
                if (depth > deepest) {
                    deepest = depth;
                    found = entry.getKey().getPath();

                }
            }
        }

        return found;

    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        Rectangle bounds = new Rectangle(0, 0, getWidth() - 1, getHeight() - 1);
        g2d.setColor(Color.blue);
        // g2d.drawRect(0, 0, getWidth()-1, getHeight()-1);

        g2d.setColor(Color.red);
        g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        this.bounds.clear();

        if (countingTreeMap != null) {
            synchronized (countingTreeMap) {
                recurse(g2d, countingTreeMap, false, "", bounds);
            }
        }
    }

    private void recurse(Graphics2D g2d, CountingTreeMap parent, boolean verticalSplit, String indent, Rectangle parentBounds) {
        int currentXProgress = 0;
        int currentYProgress = 0;

        // Out.out("{} | Recursed into bounds {} x {} | {} x {}", indent, parentBounds.x,
        // parentBounds.y, parentBounds.width, parentBounds.height);

        Collection<CountingTreeMap> values = parent.getValues();
        Iterator<CountingTreeMap> iterator = values.iterator();
        while (iterator.hasNext()) {
            CountingTreeMap node = iterator.next();

            double factor = node.getCount() / (double) parent.getCount();

            int width;
            int height;

            if (verticalSplit) {
                width = (int) (factor * parentBounds.width);
                height = parentBounds.height;

                // Check to make sure we will all the space
                if (!iterator.hasNext()) {
                    width = parentBounds.width - currentXProgress;
                }
            }
            else {
                width = parentBounds.width;
                height = (int) (factor * parentBounds.height);

                // Check to make sure we will all the space
                if (!iterator.hasNext()) {
                    height = parentBounds.height - currentYProgress;
                }
            }

            // Out.out("{} | {} [{}] : {} x {} | {} x {}",
            // indent,
            // node.getKey(),
            // node.getCount(),
            // parentBounds.x + currentXProgress,
            // parentBounds.y + currentYProgress,
            // width,
            // height);

            g2d.drawRect(parentBounds.x + currentXProgress, parentBounds.y + currentYProgress, width, height);

            if (node.isLeaf()) {
                g2d.drawString(node.getKey() + " = " + node.getCount(), parentBounds.x + currentXProgress + (width / 2), parentBounds.y +
                                                                                                                         currentYProgress +
                                                                                                                         (height / 2));
            }

            Rectangle childBounds = new Rectangle(parentBounds.x + currentXProgress, parentBounds.y + currentYProgress, width, height);
            bounds.put(node, childBounds);

            recurse(g2d, node, !verticalSplit, indent + "  ", childBounds);

            if (verticalSplit) {
                currentXProgress += width;
            }
            else {
                currentYProgress += height;
            }
        }
    }

    private void recurse(CountingTreeMap node) {
        Collection<CountingTreeMap> values = node.getValues();
        for (CountingTreeMap countingTreeMap : values) {
            Out.out("{} : {}", countingTreeMap.getKey(), countingTreeMap.getCount());
            recurse(countingTreeMap);
        }
    }

    public void updateWidth() {

        // if the first pixel on the left is now, what is the end time?
        int width = getWidth();

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

    }

    protected void processFilterDrag(Point start, Point end, boolean endEvent) {

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

    @Override public Dimension getPreferredSize() {
        return new Dimension(100, 30);
    }

    public void bind(StackTraceController controller) {
        this.controller = controller;
        this.countingTreeMap = controller.getCountingTreeMap();        
    }

}
