package com.logginghub.logging.frontend.views.historical;

import com.logginghub.logging.frontend.charting.model.TimeModel;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by james on 29/01/15.
 */
public class TimeField extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(TimeField.class);

    private JLabel dayLabel;
    private JLabel monthLabel;
    private JLabel yearLabel;

    private JLabel hourLabel;
    private JLabel minuteLabel;
    private JLabel secondLabel;
    private JLabel millisecondLabel;

    private int dayValue;
    private int monthValue;
    private int yearValue;

    private int hourValue;
    private int minuteValue;
    private int secondValue;
    private int millisecondValue;

    private int selectedField;
    private Point selectedLocation;
    //    private int selectedValue;
    //    private int newValue;

    private TimeModel timeModel;
    private GregorianCalendar calendar;
    private long originalTime;


    public TimeField() {
        setLayout(new MigLayout("gap 2", "", ""));

        setOpaque(true);
        setBackground(Color.WHITE);

        dayLabel = createResponsiveLabel("day", Calendar.DAY_OF_MONTH);
        monthLabel = createResponsiveLabel("month", Calendar.MONTH);
        yearLabel = createResponsiveLabel("year", Calendar.YEAR);
        hourLabel = createResponsiveLabel("hour", Calendar.HOUR_OF_DAY);
        minuteLabel = createResponsiveLabel("minute", Calendar.MINUTE);
        secondLabel = createResponsiveLabel("second", Calendar.SECOND);
        millisecondLabel = createResponsiveLabel("millis", Calendar.MILLISECOND);

        add(dayLabel, "cell 0 0");
        add(createLabel("-"), "cell 1 0");
        add(monthLabel, "cell 2 0");
        add(createLabel("-"), "cell 3 0");
        add(yearLabel, "cell 4 0");
        add(createLabel(" "), "cell 5 0");
        add(hourLabel, "cell 6 0");
        add(createLabel(":"), "cell 7 0");
        add(minuteLabel, "cell 8 0");
        add(createLabel(":"), "cell 9 0");
        add(secondLabel, "cell 10 0");
        add(createLabel(":"), "cell 11 0");
        add(millisecondLabel, "cell 12 0");

    }

    private JLabel createResponsiveLabel(String text, final int field) {
        final JLabel label = new JLabel(text);
        label.setBackground(Color.white);
        label.setOpaque(true);

        label.addMouseMotionListener(new MouseMotionListener() {
            @Override public void mouseDragged(MouseEvent e) {
                logger.info("Mouse dragged");

                Point location = e.getLocationOnScreen();

                int verticalDistance = -location.y + selectedLocation.y;
                int horizontalDistance = location.x - selectedLocation.x;

                int verticalAmount;
                int horizontalAmount;
                if (selectedField == Calendar.MILLISECOND) {
                    horizontalAmount = (int) (horizontalDistance * 1);
                    verticalAmount = (int) (verticalDistance * 1);
                } else if (selectedField == Calendar.YEAR) {
                    horizontalAmount = (int) (horizontalDistance / 50f);
                    verticalAmount = 1;
                }else {
                    horizontalAmount = (int) (horizontalDistance / 5f);
                    verticalAmount = (int) (verticalDistance / 15f);
                }

                if (verticalAmount == 0) {
                    verticalAmount = 1;
                }


                logger.info("Vertical distance {} horizontal distance {}", verticalAmount, horizontalAmount);

                calendar.setTimeInMillis(originalTime);
                int absVertical = Math.abs(verticalAmount);
                calendar.add(selectedField, horizontalAmount * absVertical);

                if (absVertical >= 2) {
                    if (selectedField == Calendar.MINUTE) {
                        int value = calendar.get(selectedField);

                        int roundAmount;
                        if(absVertical == 2) {
                            roundAmount = 10;
                        }else {
                            roundAmount = 30;
                        }

                        int rounded = roundTo(value, roundAmount);

                        int delta = rounded - value;

                        logger.info("Value is {} rounded is {} delta is {}", value, rounded, delta);
                        calendar.add(selectedField, delta);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    }
                }

                timeModel.getTime().set(calendar.getTimeInMillis());
            }

            @Override public void mouseMoved(MouseEvent e) {
            }
        });

        label.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
            }

            @Override public void mousePressed(MouseEvent e) {
                originalTime = calendar.getTimeInMillis();
                selectedField = field;
                selectedLocation = e.getLocationOnScreen();
                //                selectedValue = getValue(field);
                logger.info("Selected : {} at {}", selectedField, selectedLocation);

                timeModel.getEdited().set(true);
            }

            @Override public void mouseReleased(MouseEvent e) {
                logger.info("Released : {} at {}", selectedField, selectedLocation);

                Point releasedLocation = e.getLocationOnScreen();

                selectedField = -1;
                selectedLocation = null;


                label.setForeground(Color.black);
            }

            @Override public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.cyan.darker());
            }

            @Override public void mouseExited(MouseEvent e) {
                if (selectedField == field) {
                    // Dont change colour when the field is being dragged
                } else {
                    label.setForeground(Color.black);
                }
            }
        });

        return label;
    }

    public static int roundTo(int value, int nearest) {
        int halfWay = nearest / 2;
        return value % nearest >= halfWay ? ((value / nearest) * nearest) + nearest : (value / nearest) * nearest;
    }

    private JLabel createLabel(String s) {
        JLabel label = new JLabel(s);
        label.setBackground(Color.white);
        label.setOpaque(true);
        return label;
    }

    public void bind(TimeModel timeModel) {
        this.timeModel = timeModel;

        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timeModel.getTime().get());

        timeModel.getTime().addListenerAndNotifyCurrent(new ObservablePropertyListener<Long>() {
            @Override public void onPropertyChanged(Long oldValue, final Long newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        calendar.setTimeInMillis(newValue);
                        updateFromCalendar();
                    }
                });
            }
        });

    }

    private void updateFromCalendar() {
        dayLabel.setText(StringUtils.paddingString(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)),
                2,
                '0',
                true));
        monthLabel.setText(StringUtils.paddingString(Integer.toString(calendar.get(Calendar.MONTH) + 1), 2, '0', true));
        yearLabel.setText(Integer.toString(calendar.get(Calendar.YEAR)));
        hourLabel.setText(StringUtils.paddingString(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)),
                2,
                '0',
                true));
        minuteLabel.setText(StringUtils.paddingString(Integer.toString(calendar.get(Calendar.MINUTE)), 2, '0', true));
        secondLabel.setText(StringUtils.paddingString(Integer.toString(calendar.get(Calendar.SECOND)), 2, '0', true));
        millisecondLabel.setText(StringUtils.paddingString(Integer.toString(calendar.get(Calendar.MILLISECOND)),
                4,
                '0',
                true));

    }
}
