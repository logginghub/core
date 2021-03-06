package com.logginghub.logging.frontend.views.logeventdetail;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.DemoLogEventProducer;
import com.logginghub.logging.DummyLogEventProducer;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.filters.MessageContainsFilter;
import com.logginghub.logging.frontend.ComponentKeys;
import com.logginghub.logging.frontend.CountingEventQueue;
import com.logginghub.logging.frontend.DetailedLogEventPanelListener;
import com.logginghub.logging.frontend.ReflectionDispatchActionListener;
import com.logginghub.logging.frontend.binary.ImportController;
import com.logginghub.logging.frontend.components.QuickFilterHistoryController;
import com.logginghub.logging.frontend.configuration.RowFormatConfiguration;
import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.frontend.images.Icons.IconIdentifier;
import com.logginghub.logging.frontend.model.ColumnSettingsModel;
import com.logginghub.logging.frontend.model.CustomDateFilterModel;
import com.logginghub.logging.frontend.model.CustomQuickFilterModel;
import com.logginghub.logging.frontend.model.EnvironmentController;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.EventTableColumnModel;
import com.logginghub.logging.frontend.model.FilterBookmarkModel;
import com.logginghub.logging.frontend.model.HubConnectionModel;
import com.logginghub.logging.frontend.model.LevelNamesModel;
import com.logginghub.logging.frontend.model.LogEventContainer;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.frontend.model.LogEventContainerListener;
import com.logginghub.logging.frontend.model.QuickFilterController;
import com.logginghub.logging.frontend.model.QuickFilterModel;
import com.logginghub.logging.frontend.model.RowFormatModel;
import com.logginghub.logging.frontend.views.logeventdetail.time.TimeController;
import com.logginghub.logging.frontend.views.logeventdetail.time.TimeModel;
import com.logginghub.logging.frontend.views.logeventdetail.time.TimeView;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.RequestResponseMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.modules.TimestampVariableRollingFileLogger;
import com.logginghub.logging.repository.LocalDiskRepository;
import com.logginghub.logging.repository.config.LocalDiskRepositoryConfiguration;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.MemorySnapshot;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.MovingAverage;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Throttler;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.VisualStopwatchController;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ProxyServiceDiscovery;
import com.logginghub.utils.observable.BindableToModel;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Second attempt - this one aims to be faster when there are a lot of events coming in
 *
 * @author James
 */
public class DetailedLogEventTablePanel extends JPanel implements LogEventListener, BindableToModel<EnvironmentModel> {

    private static final int dummyEventsDelayMS = 100;
    private static final Logger logger = Logger.getLoggerFor(DetailedLogEventTablePanel.class);
    private static final long serialVersionUID = 1L;
    private final static long quickFilterTimeout = 200;
    private final static int eventBatchSizeWarningLevel = 100000;
    private static int nextDummyApplicationIndex;

    private final Object incommingEventBatchLock = new Object();
    private JPanel statusBar;
    private int eastPanelIcons;
    private JLabel addQuickFilterButton;
    private JLabel clearButton;
    private JPanel eastPanel;
    private MouseWheelListener mouseWheelPauser;
    private boolean autoScroll = true;
    private JLabel autoScrollButton;
    private Metadata dynamicSettings;
    private EnvironmentModel environmentModel;
    private JMenuBar menuBar;
    private LogEventContainerController eventController = new LogEventContainerController();
    private EventDetailPanel eventDetailPanel;
    private int eventsReceived = 0;
    private TimerTask executeQuickFilter = null;
    private int filterCount = 1;
    private volatile LogEventContainer incommingEventBatch = new LogEventContainer();
    private String lastSearchTerm = "";
    private List<DetailedLogEventPanelListener> listeners = new CopyOnWriteArrayList<DetailedLogEventPanelListener>();
    private MemorySnapshot memorySnapshot = MemorySnapshot.createSnapshot();
    private MovingAverage movingAverage = new MovingAverage(5);
    private int nextFilterID = 0;
    private TimestampVariableRollingFileLogger outputLogger;
    private LogEventContainerListener outputLogListener = new LogEventContainerListener() {
        @Override
        public void onAdded(LogEvent event) {
        }

        @Override
        public void onPassedFilter(LogEvent event) {
            logger.fine("Event has passed filter and writeOutputLog is turned on, writing event '{}'", event);
            writeOutputLog(event);
        }

        @Override
        public void onRemoved(LogEvent removed) {
        }

        @Override
        public void onCleared() {
        }
    };
    private ImageIcon pauseIcon;
    private ImageIcon playIcon;
    private boolean playing = true;
    private JPanel quickFilterContainerPanel;
    private List<QuickFilterRowPanel> quickFilterRowPanels = new CopyOnWriteArrayList<QuickFilterRowPanel>();
    private Timer quickFilterTimer = new Timer("QuickFilterTimerThread", true);
    private LevelHighlighter rowHighlighter = new LevelHighlighter(RowFormatModel.fromConfiguration(new RowFormatConfiguration()));
    private JLabel statusText;
    private DetailedLogEventTable table;
    private DetailedLogEventTableModel tableModel;
    private JScrollPane tableScrollPane;
    private boolean writeOutputLog = false;
    private QuickFilterController quickFilterController;
    private JLabel timeTravelButton;
    private TimeController timeController;
    private JSplitPane timeSplitter;
    private int previousTimeSplitterLocation;
    private Throttler eventBatchWarningThrottle = new Throttler(1, TimeUnit.SECONDS);
    private MouseAdapter autoPauser;
    private Binder2 timeModelBinder = new Binder2();
    private LocalDiskRepository binaryExporter;
    private DummyLogEventProducer dummyLogEventProducerx;
    private DemoLogEventProducer demoLogEventProducer;
    private boolean historicalView = false;
    private TimeView timeView;
    private JSplitPane eventDetailsSplitPane;
    private LevelNamesModel levelNamesModel;
    private EnvironmentController environmentController;

    public DetailedLogEventTablePanel(JMenuBar menuBar,
                                      String propertiesName,
                                      EventTableColumnModel eventTableColumnModel,
                                      LevelNamesModel levelNamesModel,
                                      ColumnSettingsModel columnSettingsModel,
                                      final LogEventContainerController eventController,
                                      TimeProvider timeProvider,
                                      boolean showHeapSlider) {
        this();
        this.menuBar = menuBar;
        this.levelNamesModel = levelNamesModel;

        this.eventController = eventController;
        this.eventDetailPanel = new EventDetailPanel();

        tableModel = new DetailedLogEventTableModel(eventTableColumnModel, levelNamesModel, eventController);
        table = new DetailedLogEventTable(tableModel, rowHighlighter, propertiesName, columnSettingsModel);

        table.setName("logEventTable");
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tableScrollPane = new JScrollPane(table);

        mouseWheelPauser = new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                pause();
            }
        };
        tableScrollPane.addMouseWheelListener(mouseWheelPauser);

        autoPauser = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                pause();
            }
        };

        try {
            Component topButton = tableScrollPane.getVerticalScrollBar().getComponent(0);
            Component bottomButton = tableScrollPane.getVerticalScrollBar().getComponent(1);
            tableScrollPane.getVerticalScrollBar().addMouseListener(autoPauser);
            topButton.addMouseListener(autoPauser);
            bottomButton.addMouseListener(autoPauser);
        } catch (Exception e) {
            // This goes bang on macs - need an alternative approach
            autoPauser = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    pause();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pause();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    pause();
                }
            };
            tableScrollPane.getVerticalScrollBar().addMouseListener(autoPauser);
        }

        JPanel filtersAndButtonsPanel = new JPanel();

        timeTravelButton = new JLabel();
        timeTravelButton.setName("timeTravel");
        timeTravelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleTimeViewer();
            }
        });
        timeTravelButton.setToolTipText("Hide or show the time viewer");
        timeTravelButton.setIcon(Icons.get(IconIdentifier.Clock));

        autoScrollButton = new JLabel();
        clearButton = new JLabel();
        clearButton.setName("clear");

        pauseIcon = Icons.get(IconIdentifier.Pause);
        playIcon = Icons.get(IconIdentifier.Play);
        ImageIcon clearIcon = Icons.get(IconIdentifier.Clear);

        clearButton.setIcon(clearIcon);
        clearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clearEvents();
            }
        });
        clearButton.setToolTipText("Clear the current events");

        autoScrollButton.setIcon(pauseIcon);
        autoScrollButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                togglePlaying();
            }
        });
        autoScrollButton.setToolTipText("Pause or resume the event stream");

        addQuickFilterButton = new JLabel();
        addQuickFilterButton.setIcon(Icons.get(IconIdentifier.AddCircleSmall));
        addQuickFilterButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addQuickFilter();
            }
        });
        addQuickFilterButton.setToolTipText("Add another quick filter");
        addQuickFilterButton.setName("addQuickFilter");

        GridLayout gridLayout = new GridLayout();
        eastPanel = new JPanel(gridLayout);
        eastPanel.add(timeTravelButton);
        eastPanel.add(addQuickFilterButton);
        eastPanel.add(autoScrollButton);
        eastPanel.add(clearButton);
        eastPanelIcons = 4;
        updateEastPanelSize();

        filtersAndButtonsPanel.setLayout(new BorderLayout());
        filtersAndButtonsPanel.add(eastPanel, BorderLayout.EAST);

        quickFilterContainerPanel = new JPanel(new MigLayout("gap 2, ins 2", "[fill, grow]", "[fill, grow]"));

        QuickFilterRowPanel quickFilterRowPanel = createQuickFilterRow();
        quickFilterRowPanel.setAndOrVisible(false);
        quickFilterContainerPanel.add(quickFilterRowPanel, "wrap");
        quickFilterRowPanels.add(quickFilterRowPanel);

        table.setDefaultEditor(Date.class, new TableCellEditor() {

            private Date value;
            private JTextField textField = new JTextField();
            private SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            public Object getCellEditorValue() {
                return value;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.value = (Date) value;
                if (this.value != null) {
                    textField.setText(f.format(this.value));
                } else {
                    textField.setText("");
                }
                return textField;

            }            @Override
            public boolean isCellEditable(EventObject anEvent) {
                return true;
            }

            @Override
            public boolean shouldSelectCell(EventObject anEvent) {
                return true;
            }

            @Override
            public boolean stopCellEditing() {
                String inputData = textField.getText();
                try {
                    Date newDate = f.parse(inputData);
                    // Make this our new value; this gets picked up and applied back into the model
                    this.value = newDate;
                } catch (ParseException e) {
                    // Don't do anything, revert back to the current value
                }
                return true;
            }

            @Override
            public void cancelCellEditing() {
                // Don't do anything
            }

            @Override
            public void addCellEditorListener(CellEditorListener l) {

            }

            @Override
            public void removeCellEditorListener(CellEditorListener l) {

            }


        });

        filtersAndButtonsPanel.add(quickFilterContainerPanel, BorderLayout.CENTER);

        eventDetailsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        eventDetailsSplitPane.setName("eventDetailsSplitPane");
        eventDetailsSplitPane.setLeftComponent(tableScrollPane);
        eventDetailsSplitPane.setDividerSize(2);
        eventDetailsSplitPane.setDividerLocation(300);

        eventDetailsSplitPane.setRightComponent(eventDetailPanel);

        final TimeModel model = new TimeModel();
        timeController = new TimeController(model, eventController);
        long now = System.currentTimeMillis();
        model.getViewStart().set(TimeUtils.before(now, "1 minute"));

        timeView = new TimeView();
        timeView.setTimeProvider(timeProvider);
        timeView.bind(timeController);
        timeSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        timeSplitter.setDividerSize(2);
        timeSplitter.setLeftComponent(timeView);
        timeSplitter.setRightComponent(eventDetailsSplitPane);
        timeSplitter.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                logger.fine("Time split pane divider has moved, saving change : {}", timeSplitter.getDividerLocation());
                if (dynamicSettings != null) {
                    dynamicSettings.set("timeSplitterPosition", timeSplitter.getDividerLocation());
                }
            }
        });

        add(timeSplitter, BorderLayout.CENTER);

        add(filtersAndButtonsPanel, BorderLayout.NORTH);


        statusBar = new JPanel();
        statusBar.setBorder(BorderFactory.createBevelBorder(1));
        statusText = new JLabel("Status bar");
        statusText.setName(ComponentKeys.StatusText.name());
        statusBar.add(statusText);

        if (showHeapSlider) {
            final JSlider slider = new JSlider();
            slider.setMaximum(100);
            slider.setValue((int) (100 * (eventController.getThreshold() / (double) Runtime.getRuntime().maxMemory())));
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int value = slider.getValue();
                    float percentage = (float) value / 100f;
                    eventController.setThreshold((long) (Runtime.getRuntime().maxMemory() * percentage));
                }
            });
            statusBar.add(new JLabel(" | Heap for events"));
            statusBar.add(slider);
        }

        add(statusBar, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    final LogEvent logEventAtRow = tableModel.getLogEventAtRow(selectedRow);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            eventDetailPanel.update(logEventAtRow);
                        }
                    });
                }
            }
        });

        TimerUtils.every("StatusTextUpdater", 1000, TimeUnit.MILLISECONDS, new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateStatusText();
                    }
                });
            }
        });

        logger.trace("Starting table updater");

        TimerUtils.every("LogEventTableUpdater", 50, TimeUnit.MILLISECONDS, new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        Stopwatch start = Stopwatch.start("Updating table with new batch");
                        LogEventContainer newEvents;
                        synchronized (incommingEventBatchLock) {
                            if (incommingEventBatch.size() > 0) {
                                newEvents = incommingEventBatch;
                                incommingEventBatch = new LogEventContainer();
                            } else {
                                newEvents = null;
                            }
                        }

                        if (newEvents != null) {
                            tableModel.onNewLogEvent(newEvents);
                            start.stop();
                            VisualStopwatchController.getInstance().add(start);

                            if (playing && autoScroll) {
                                // logger.info("Scrolling to bottom : {} events", newEvents.size());
                                start = Stopwatch.start("Scrolling to bottom");
                                scrollToBottom();
                                start.stop();
                                VisualStopwatchController.getInstance().add(start);
                            }
                        }
                    }
                });
            }
        });

    }

    public DetailedLogEventTablePanel() {
        setLayout(new BorderLayout());
    }

    protected void pause() {
        tableModel.pause();
        playing = false;
        autoScrollButton.setIcon(playIcon);
    }

    protected void toggleTimeViewer() {
        logger.fine("Toggling the time viewer visibility : previousTimeSplitterLocation is '{}' and current location is '{}'",
                    previousTimeSplitterLocation,
                    timeSplitter.getDividerLocation());
        if (isTimeViewerVisible()) {
            previousTimeSplitterLocation = timeSplitter.getDividerLocation();
            timeSplitter.setDividerLocation(1);
        } else {
            timeSplitter.setDividerLocation(previousTimeSplitterLocation);
            previousTimeSplitterLocation = 0;
        }

        if (dynamicSettings != null) {
            dynamicSettings.set("previousTimeSplitterLocation", previousTimeSplitterLocation);
        }
    }

    public void clearEvents() {
        eventController.clear();
        eventDetailPanel.clear();
        tableModel.fireTableDataChanged();
        System.gc();
        updateStatusText();
    }

    public void togglePlaying() {
        if (playing) {
            pause();
        } else {
            play();
        }
    }

    protected void addQuickFilter() {

        if (filterCount == 1) {
            // Convert the undecorated filter into a row with a delete button
            QuickFilterRowPanel existing = (QuickFilterRowPanel) quickFilterContainerPanel.getComponent(0);
            JPanel existingWrapped = createWrapperPanel(existing);
            quickFilterContainerPanel.removeAll();
            quickFilterContainerPanel.add(existingWrapped, "wrap");

            // Add the and/or control
            existing.setAndOrVisible(true);
        }

        QuickFilterModel quickFilterModel = new QuickFilterModel();
        environmentModel.getQuickFilterModels().add(quickFilterModel);
        QuickFilterRowPanel quickFilterRowPanel = createQuickFilterRow();

        QuickFilterHistoryController quickFilterHistoryController = new QuickFilterHistoryController(environmentModel.getQuickFilterHistoryModel(),
                                                                                                     environmentModel);
        quickFilterRowPanel.bind(quickFilterHistoryController, quickFilterModel, quickFilterController.getIsAndFilter());

        final JPanel wrapperPanel = createWrapperPanel(quickFilterRowPanel);

        filterCount++;

        quickFilterContainerPanel.add(wrapperPanel, "wrap");
        quickFilterContainerPanel.validate();

        quickFilterModel.getLevelFilter().get().getSelectedLevel().addListener(new ObservablePropertyListener<Level>() {
            @Override
            public void onPropertyChanged(Level oldValue, Level newValue) {
                notifyLevelChanges();
            }
        });
    }

    private void updateEastPanelSize() {
        eastPanel.setPreferredSize(new Dimension(32 * eastPanelIcons, 35));
    }

    private QuickFilterRowPanel createQuickFilterRow() {
        QuickFilterRowPanel quickFilterRowPanel = new QuickFilterRowPanel(levelNamesModel);
        quickFilterRowPanel.setName("quickFilter-" + nextFilterID++);
        return quickFilterRowPanel;
    }

    public void updateStatusText() {

        movingAverage.addValue(eventsReceived);
        eventsReceived = 0;

        memorySnapshot.refreshSnapshot();

        int eventQueueCount;
        int eventQueueProcessed;

        EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        if (systemEventQueue instanceof CountingEventQueue) {
            CountingEventQueue eventQueue = (CountingEventQueue) systemEventQueue;
            eventQueueCount = eventQueue.getCount();
            eventQueueProcessed = eventQueue.getProcessedSinceLastCheck();
        } else {
            eventQueueCount = 0;
            eventQueueProcessed = 0;
        }

        boolean detailed = Boolean.getBoolean("detailedLogEventTablePanel.detailedStatus");

        if (detailed) {
            statusText.setText(String.format(
                    "%d events | %d filtered | event buffer %.1f %%  | memory usage %.1f %%  | %.0f events/sec | %d events in next batch | Awt " +
                    "queue size %d | Awt processed %d",
                    eventController.getLiveEventsThatPassFilter().size(),
                    eventController.getLiveEventsThatFailedFilter().size(),
                    eventController.getUsedPercentage(),
                    memorySnapshot.getAvailableMemoryPercentage(),
                    movingAverage.calculateMovingAverage(),
                    incommingEventBatch.size(),
                    eventQueueCount,
                    eventQueueProcessed));
        } else {

            if(environmentModel.getShowFullStatusDetails().get()) {

                statusText.setText(String.format("%d events | %d filtered | %.0f events/sec | event buffer %.1f %%  | memory usage %.1f %%",
                                                 eventController.getLiveEventsThatPassFilter().size(),
                                                 eventController.getLiveEventsThatFailedFilter().size(),
                                                 movingAverage.calculateMovingAverage(),
                                                 eventController.getUsedPercentage(),
                                                 memorySnapshot.getAvailableMemoryPercentage()));
            }else{
                statusText.setText(String.format("%d events | %d filtered",
                                                 eventController.getLiveEventsThatPassFilter().size(),
                                                 eventController.getLiveEventsThatFailedFilter().size()));
            }
        }
    }

    private void scrollToBottom() {
        int row = table.getRowCount() - 1;
        Rectangle cellRect = table.getCellRect(row, 0, true);
        table.scrollRectToVisible(cellRect);
    }

    private boolean isTimeViewerVisible() {
        return previousTimeSplitterLocation == 0;
    }

    protected void play() {
        tableModel.play();
        playing = true;
        autoScrollButton.setIcon(pauseIcon);
    }

    private JPanel createWrapperPanel(final QuickFilterRowPanel quickFilterRowPanel) {
        final JPanel wrapperPanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, fill][shrink, fill]", "[grow,fill]"));

        JLabel removeButton = new JLabel();
        removeButton.setIcon(Icons.get(IconIdentifier.Delete));
        removeButton.setToolTipText("Remove this quick filter");
        removeButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                quickFilterContainerPanel.remove(wrapperPanel);
                quickFilterContainerPanel.validate();
                filterCount--;

                environmentModel.getQuickFilterModels().remove(quickFilterRowPanel.getModel());

                if (filterCount == 1) {
                    updateFirstFilterState();
                }
            }
        });

        removeButton.setName(quickFilterRowPanel.getName() + ".remove");

        wrapperPanel.add(quickFilterRowPanel);
        wrapperPanel.add(removeButton);
        return wrapperPanel;
    }

    protected void notifyLevelChanges() {

        int lowestLevel = Level.SEVERE.intValue();
        ObservableList<QuickFilterModel> quickFilterModels = environmentModel.getQuickFilterModels();
        for (QuickFilterModel model : quickFilterModels) {
            lowestLevel = Math.min(lowestLevel, model.getLevelFilter().get().getSelectedLevel().get().intValue());
        }

        for (DetailedLogEventPanelListener detailedLogEventPanelListener : listeners) {
            detailedLogEventPanelListener.onLevelFilterChanged(lowestLevel);
        }

        saveQuickFilterLevel(Level.parse("" + lowestLevel));
    }

    protected void updateFirstFilterState() {
        if (filterCount == 1) {
            JComponent remainingWrapper = (JComponent) quickFilterContainerPanel.getComponent(0);
            QuickFilterRowPanel existing = (QuickFilterRowPanel) remainingWrapper.getComponent(0);

            // Forceably re-enabled the filter
            existing.getModel().getIsEnabled().set(true);

            // Chop off the and/or button
            existing.setAndOrVisible(false);

            // Clear and re-add the undecorated panel
            quickFilterContainerPanel.removeAll();
            quickFilterContainerPanel.add(existing, "wrap");
            quickFilterContainerPanel.validate();

        }
    }

    protected void saveQuickFilterLevel(Level selectedLevel) {
        if (dynamicSettings != null) {
            dynamicSettings.put("detailedLogEventTablePanel.quickFilterLevel", selectedLevel.getName());
        }
    }

    public synchronized void activateDemoSource() {
        logger.info("Activating demo source");
        if (demoLogEventProducer == null) {
            demoLogEventProducer = new DemoLogEventProducer();
            demoLogEventProducer.produceEventsOnTimer(1, dummyEventsDelayMS, 5);
            demoLogEventProducer.addLogEventListener(environmentModel);
        } else {
            demoLogEventProducer.produceEventsOnTimer(1, dummyEventsDelayMS, 5);
        }
    }

    public synchronized void activateDummySource() {
        logger.info("Activating dummy source");
        if (dummyLogEventProducerx == null) {

            String appName = "TestApplication-" + nextDummyApplicationIndex++;
            dummyLogEventProducerx = new DummyLogEventProducer(appName);
            dummyLogEventProducerx.produceEventsOnTimer(1, dummyEventsDelayMS, 5);

            dummyLogEventProducerx.addLogEventListener(environmentModel);
        } else {
            dummyLogEventProducerx.produceEventsOnTimer(1, dummyEventsDelayMS, 5);
        }
    }

    public void addChartCreationRequestListener(DetailedLogEventPanelListener listener) {
        listeners.add(listener);
    }

    private void addLevelFilterMenuOption(JMenu levelMenu, final Level level, int key) {

        String name = levelNamesModel.getLevelName(level.intValue());

        JMenuItem levelItem = new JMenuItem(name);
        levelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getFirstQuickFilter().getModel().getLevelFilter().get().getSelectedLevel().set(level);
                // quickLevelFilterCombo.setSelectedItem(level);
            }
        });
        levelItem.setAccelerator(KeyStroke.getKeyStroke(key, InputEvent.CTRL_MASK));
        levelMenu.add(levelItem);
    }

    @Override
    public synchronized void addMouseMotionListener(MouseMotionListener l) {
        super.addMouseMotionListener(l);
        table.addMouseMotionListener(l);
        tableScrollPane.addMouseMotionListener(l);
    }

    public void bind(EnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;
        this.environmentController = new EnvironmentController(environmentModel);
        tableModel.setEnvironmentController(environmentController);

        environmentController.loadSearches();

        eventDetailPanel.bind(environmentModel);

        setupMenuBar(menuBar);

        QuickFilterModel quickFilterModel = new QuickFilterModel();

        // Build any custom filters from the configuration
        ObservableList<CustomQuickFilterModel> customFilters = environmentModel.getCustomFilters();
        for (CustomQuickFilterModel customFilter : customFilters) {
            quickFilterModel.getCustomFilters().add(customFilter);
        }

        ObservableList<CustomDateFilterModel> customDateFilters = environmentModel.getCustomDateFilters();
        for (CustomDateFilterModel customDateFilter : customDateFilters) {
            quickFilterModel.getCustomDateFilters().add(customDateFilter);
        }

        environmentModel.getQuickFilterModels().add(quickFilterModel);

        quickFilterModel.getLevelFilter().get().getSelectedLevel().addListener(new ObservablePropertyListener<Level>() {
            @Override
            public void onPropertyChanged(Level oldValue, Level newValue) {
                notifyLevelChanges();
            }
        });

        quickFilterController = new QuickFilterController(environmentModel);
        tableModel.addFilter(quickFilterController.getFilter(), null);
        tableModel.addFilter(timeController.getFilter(), null);

        QuickFilterHistoryController quickFilterHistoryController = new QuickFilterHistoryController(environmentModel.getQuickFilterHistoryModel(),
                                                                                                     environmentModel);
        getFirstQuickFilter().bind(quickFilterHistoryController, quickFilterModel, quickFilterController.getIsAndFilter());

        environmentModel.getFilterUpdateCount().addListener(new ObservablePropertyListener<Integer>() {
            @Override
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                logger.info("Environment filter models have changed, starting refresh timer...");
                updateQuickFilterTimer();
            }
        });

        TimeModel timeModel = timeController.getModel();

        timeModelBinder.attachPropertyListener(timeModel.getSelectionChanged(), new ObservablePropertyListener<Integer>() {
            @Override
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                tableModel.refreshFilters(null);
                scrollToCenter(table, 0, 0);
            }
        });

        // Bind to the time click selector to jump to a particular time
        timeModelBinder.attachPropertyListener(timeModel.getClickedTime(), new ObservablePropertyListener<Long>() {
            @Override
            public void onPropertyChanged(Long oldValue, Long newValue) {
                if (newValue != Long.MAX_VALUE) {
                    if (playing) {
                        pause();
                    }
                    navigateToTime(newValue);
                }
            }
        });

        // Bind to the clear selection from the time selector to clear and start playing again
        timeModelBinder.attachPropertyListener(timeModel.getSelectionCleared(), new ObservablePropertyListener<Integer>() {
            @Override
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                if (!playing) {
                    togglePlaying();
                }
            }
        });


        environmentModel.getEventDetailsSeparatorHorizontalOrientiation().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                setDetailPaneOrientation(newValue);
            }
        });

        environmentModel.getEventDetailsSeparatorLocation().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, final String newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setSplitPaneLocation(newValue);
                    }
                });
            }
        });


        if (environmentModel.isDisableAutoScrollPauser()) {
            disableScrollerAutoPause();
        }

        environmentModel.getShowClearEvents().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    eastPanel.remove(clearButton);
                    eastPanelIcons--;
                    updateEastPanelSize();
                }
            }
        });

        environmentModel.getShowTimeControl().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    eastPanel.remove(timeTravelButton);
                    eastPanelIcons--;
                    updateEastPanelSize();
                }
            }
        });

        environmentModel.getShowAddFilter().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    eastPanel.remove(addQuickFilterButton);
                    eastPanelIcons--;
                    updateEastPanelSize();
                }
            }
        });


        environmentModel.getShowTimeSelectionView().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if(newValue) {
                    add(timeSplitter, BorderLayout.CENTER);
                }else{
                    add(eventDetailsSplitPane, BorderLayout.CENTER);
                }
            }
        });

        environmentModel.getPanelBackgroundColour().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, String newValue) {
                if(StringUtils.isNotNullOrEmpty(newValue)) {
                    DetailedLogEventTablePanel.this.setOpaque(true);
                    DetailedLogEventTablePanel.this.setBackground(ColourUtils.parseColor(newValue));
                }else{
                    DetailedLogEventTablePanel.this.setOpaque(false);
                    DetailedLogEventTablePanel.this.setBackground(Color.white);
                }
            }
        });

        environmentModel.getDetailViewBackgroundColour().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, String newValue) {
                if(StringUtils.isNotNullOrEmpty(newValue)) {
                    eventDetailPanel.setOpaque(true);
                    eventDetailPanel.setBackground(ColourUtils.parseColor(newValue));
                }else{
                    eventDetailPanel.setOpaque(false);
                    eventDetailPanel.setBackground(Color.white);
                }
            }
        });

        environmentModel.getTableBackgroundColour().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, String newValue) {
                if(StringUtils.isNotNullOrEmpty(newValue)) {
                    table.setOpaque(true);
                    table.setBackground(ColourUtils.parseColor(newValue));

                    tableScrollPane.getViewport().setOpaque(true);
                    tableScrollPane.getViewport().setBackground(ColourUtils.parseColor(newValue));
                }else{
                    table.setOpaque(false);
                    table.setBackground(Color.white);
                }
            }
        });

        environmentModel.getSummaryBarBackgroundColour().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, String newValue) {
                if(StringUtils.isNotNullOrEmpty(newValue)) {
                    statusBar.setOpaque(true);
                    statusBar.setBackground(ColourUtils.parseColor(newValue));
                }else{
                    statusBar.setOpaque(false);
                    statusBar.setBackground(Color.white);
                }
            }
        });


    }

    @Override
    public void unbind(EnvironmentModel environmentModel) {

    }

    /**
     * Binds an import handler to this panel, enalbing the time view to control the import of historical data
     *
     * @param importHandler The import handler to bind to; this will be the only source of events displayed in this panel
     */
    public void bind(final ImportController importHandler) {
        final TimeModel timeModel = timeController.getModel();

        historicalView = true;

        // Unbind anything that might have been setup before
        timeModelBinder.unbind();

        // Stop anything from triggering pause from the scrollbars
        disableScrollerAutoPause();

        // Stop the time controller from updating based on which events are in the filter
        timeController.detachControllerListener();

        // We dont use auto scroll in this mode, so high it completely
        autoScrollButton.setVisible(false);
        autoScrollButton.getParent().remove(autoScrollButton);

        // Detach the time filter so we just use the time view for loading data
        tableModel.removeFilter(timeController.getFilter(), null);

        timeModelBinder.attachPropertyListener(timeModel.getSelectionChanged(), new ObservablePropertyListener<Integer>() {
            @Override
            public void onPropertyChanged(Integer oldValue, Integer newValue) {

                logger.fine("Time filter selection changed, from '{}' to '{}",
                            Logger.toDateString(timeModel.getSelectionStart().longValue()),
                            Logger.toDateString(timeModel.getSelectionEnd().longValue()));

                clearEvents();

                // Spin off a thread to do the heavy lifting
                WorkerThread exector = new WorkerThread("DataImport") {
                    @Override
                    protected void onRun() throws Throwable {
                        importHandler.loadData(timeModel.getSelectionStart().longValue(), timeModel.getSelectionEnd().longValue(), environmentModel);
                        // Only run once
                        stop();
                    }

                    @Override
                    protected void beforeStop() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                tableModel.refreshFilters(null);
                                scrollToCenter(table, 0, 0);
                            }
                        });
                    }
                };

                exector.start();
            }
        });

        // Bind to the time click selector to jump to a particular time
        timeModelBinder.attachPropertyListener(timeModel.getClickedTime(), new ObservablePropertyListener<Long>() {
            @Override
            public void onPropertyChanged(Long oldValue, Long newValue) {
                // if (newValue != Long.MAX_VALUE) {
                // if (playing) {
                // pause();
                // }
                // navigateToTime(newValue);
                // }
            }
        });

        // Bind to the clear selection from the time selector to clear and start playing again
        timeModelBinder.attachPropertyListener(timeModel.getSelectionCleared(), new ObservablePropertyListener<Integer>() {
            @Override
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                if (environmentModel != null) {
                    environmentModel.getEventController().clear();
                }
                clearEvents();
            }
        });
    }

    private void disableScrollerAutoPause() {

        tableScrollPane.removeMouseWheelListener(mouseWheelPauser);

        try {
            Component topButton = tableScrollPane.getVerticalScrollBar().getComponent(0);
            Component bottomButton = tableScrollPane.getVerticalScrollBar().getComponent(1);
            topButton.removeMouseListener(autoPauser);
            bottomButton.removeMouseListener(autoPauser);

            tableScrollPane.getVerticalScrollBar().removeMouseListener(autoPauser);
        } catch (Exception e) {
            // The first bit goes bang on macs - need an alternative approach
            tableScrollPane.getVerticalScrollBar().removeMouseListener(autoPauser);
        }
    }

    public static void scrollToCenter(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport) table.getParent();
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
        Rectangle viewRect = viewport.getViewRect();
        rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);

        int centerX = (viewRect.width - rect.width) / 2;
        int centerY = (viewRect.height - rect.height) / 2;
        if (rect.x < centerX) {
            centerX = -centerX;
        }
        if (rect.y < centerY) {
            centerY = -centerY;
        }
        rect.translate(centerX, centerY);
        viewport.scrollRectToVisible(rect);
    }

    public void close() {
    }

    public void closeOutputLog() {
        if (outputLogger != null) {
            outputLogger.close();
        }
    }

    public synchronized void deactivateDemoSource() {
        if (demoLogEventProducer != null) {
            demoLogEventProducer.stop();
            demoLogEventProducer = null;
        }
    }

    public synchronized void deactivateDummySource() {
        if (dummyLogEventProducerx != null) {
            dummyLogEventProducerx.stop();
            dummyLogEventProducerx = null;
        }
    }

    public void deleteSearch() {

        ObservableList<FilterBookmarkModel> filterBookmarks = environmentModel.getFilterBookmarks();
        String[] selectionValues = new String[filterBookmarks.size()];
        int index = 0;
        for (FilterBookmarkModel filterBookmark : filterBookmarks) {
            selectionValues[index++] = filterBookmark.getName().get();
        }

        Object result = JOptionPane.showInputDialog(null,
                                                    "Please choose a search to delete",
                                                    "Delete Search",
                                                    JOptionPane.QUESTION_MESSAGE,
                                                    null,
                                                    selectionValues,
                                                    null);

        String searchName = result.toString();

        if (searchName != null) {
            environmentController.deleteSearch(searchName);
        }
    }

    protected void executeQuickFilter() {
        tableModel.refreshFilters(null);
    }

    public synchronized void exportBinaryFile() {
        final JFileChooser fc = new JFileChooser();

        String settingsKey = getBinaryFileSettingsKey();
        String binaryFolder = dynamicSettings.getString(settingsKey, ".");

        fc.setCurrentDirectory(new File(binaryFolder));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            dynamicSettings.set(settingsKey, file.getAbsolutePath());

            LocalDiskRepositoryConfiguration config = new LocalDiskRepositoryConfiguration();
            config.setDataFolder(file.getAbsolutePath());
            config.setPrefix(environmentModel.getName().get() + ".");
            config.setFileDurationMilliseconds(10 * TimeUtils.minutes);

            binaryExporter = new LocalDiskRepository(config);

            getEnvironmentModel().addLogEventListener(binaryExporter);
        }
    }

    public String getBinaryFileSettingsKey() {
        return "binaryFolder-" + environmentModel.getName().get();
    }

    public EnvironmentModel getEnvironmentModel() {
        return environmentModel;
    }

    public void setEnvironmentModel(EnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;
        loadFromModel(environmentModel);
    }

    private void loadFromModel(EnvironmentModel model) {
        List<com.logginghub.logging.frontend.model.HighlighterModel> highlighters = model.getHighlighters();
        for (com.logginghub.logging.frontend.model.HighlighterModel highlighterModel : highlighters) {
            addHighlighter(highlighterModel);
        }

        setAutoLockWarning(model.getAutoLocking().get());

        TimestampVariableRollingFileLoggerConfiguration outputLogConfiguration = model.getOutputLogConfiguration();
        if (outputLogConfiguration != null) {
            setupOutputLog(outputLogConfiguration);
        }

        setWriteOutputLog(model.isWriteOutputLog());
    }

    public void addHighlighter(com.logginghub.logging.frontend.model.HighlighterModel phraseHighlighterModel) {
        final PhraseHighlighter highlighter = new PhraseHighlighter(phraseHighlighterModel.getPhrase().get());
        Color background = ColourUtils.parseColor(phraseHighlighterModel.getColourHex().get());
        highlighter.setHighlightBackgroundColour(background);
        table.addHighlighter(highlighter);
    }

    public void setAutoLockWarning(boolean selected) {
        environmentModel.setAutoLockWarning(selected);
    }

    private void setupOutputLog(TimestampVariableRollingFileLoggerConfiguration outputLogConfiguration) {
        outputLogger = new TimestampVariableRollingFileLogger();
        outputLogger.configure(outputLogConfiguration, new ProxyServiceDiscovery());
    }

    public void setWriteOutputLog(boolean selected) {
        if (writeOutputLog != selected) {
            writeOutputLog = selected;

            if (writeOutputLog) {
                eventController.addLogEventContainerListener(outputLogListener);
            } else {
                eventController.removeLogEventContainerListener(outputLogListener);
            }
        }
    }

    public void findBackwardsAgain() {
        if (lastSearchTerm.length() > 0) {
            findBackwards(lastSearchTerm);
        } else {
            findBackwards();
        }
    }

    private void findBackwards(String term) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || selectedRow == 0) {
            selectedRow = 1;
        }

        MessageContainsFilter filter = new MessageContainsFilter(term);

        int row = tableModel.findPreviousEvent(selectedRow - 1, filter);
        if (row != -1) {
            table.changeSelection(row, -1, false, false);
        }

        lastSearchTerm = term;
    }

    public void findBackwards() {
        String term = JOptionPane.showInputDialog(getTopLevelAncestor(), "Find backwards", lastSearchTerm);
        if (term != null) {
            findBackwards(term);
        }
    }

    public void findForwardsAgain() {
        if (lastSearchTerm.length() > 0) {
            findForwards(lastSearchTerm);
        } else {
            findForwards();
        }
    }

    private void findForwards(String term) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            selectedRow = 0;
        }

        MessageContainsFilter filter = new MessageContainsFilter(term);

        int row = tableModel.findNextEvent(selectedRow + 1, filter);
        if (row != -1) {
            table.changeSelection(row, -1, false, false);
        }

        lastSearchTerm = term;
    }

    public void findForwards() {
        String term = JOptionPane.showInputDialog(getTopLevelAncestor(), "Find forwards", lastSearchTerm);
        if (term != null) {
            findForwards(term);
        }
    }

    public int getCurrentBatchSize() {
        return incommingEventBatch.size();
    }

    public int getLevelFilter() {
        return getFirstQuickFilter().getModel().getLevelFilter().get().getSelectedLevel().get().intValue();
    }

    public QuickFilterRowPanel getFirstQuickFilter() {
        return quickFilterRowPanels.get(0);
    }

    public TimestampVariableRollingFileLogger getOutputLogger() {
        return outputLogger;
    }

    public TimeController getTimeFilterController() {
        return timeController;
    }

    public TimeView getTimeView() {
        return timeView;
    }

    public synchronized boolean isDemoSourceActive() {
        return demoLogEventProducer != null;
    }

    public synchronized boolean isDummySourceActive() {
        return dummyLogEventProducerx != null;
    }

    public synchronized boolean isExportingBinary() {
        return binaryExporter != null;
    }

    public boolean isHistoricalView() {
        return historicalView;
    }

    protected void navigateToTime(long time) {
        int row = tableModel.findFirstTime(time);
        logger.fine("Navigating to time '{}' - found row index '{}'", Logger.toDateString(time), row);
        if (row != -1) {
            table.changeSelection(row, -1, false, false);
            Rectangle cellRect = table.getCellRect(row, 0, true);
            logger.fine("CellRect is '{}'", cellRect);

            scrollToCenter(table, row, 0);
        }
    }

    //    public DetailedLogEventTableModel getTableModel() {
    //        return tableModel;
    //    }

    public void onNewLogEvent(final LogEvent event) {
        logger.fine("New event received in logeventdetail panel '{}'", event);
        Stopwatch sw = Stopwatch.start("DetailedLogEventPanel.onNewLogEvent");
        eventsReceived++;

        synchronized (environmentModel.getHighestLevelSinceLastSelected()) {
            environmentModel.getHighestLevelSinceLastSelected().setIfGreater(event.getLevel());
        }

        synchronized (incommingEventBatchLock) {
            // jshaw : not sure what I was trying to do here, its just discarding events if they
            // arrive to quickly. This should be a) configurable, and b) done somewhere higher up
            // the chain?
            // if (incommingEventBatch.size() < 10000) {
            // incommingEventBatch.add(event);
            // }

            incommingEventBatch.add(event);
            if (incommingEventBatch.size() > eventBatchSizeWarningLevel && eventBatchWarningThrottle.isOkToFire()) {
                logger.warn("The incoming event batch contains '{}' items (over warning level of '{}')",
                            incommingEventBatch.size(),
                            eventBatchSizeWarningLevel);
            }
        }

        sw.stop();
        VisualStopwatchController.getInstance().add(sw);
    }

    public void openBinaryFolder() {
        String settingsKey = getBinaryFileSettingsKey();
        String binaryFolder = dynamicSettings.getString(settingsKey, new File("").getAbsolutePath());

        File file = new File(binaryFolder);
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                logger.warn(e, "Failed to use Desktop to explore path '{}'", file.getAbsolutePath());
            }
        }
    }

    //    public void gotoBookmark(Integer bookmark) {
    //        table.gotoBookmark(bookmark.intValue());
    //    }

    public String saveSearch() {
        String searchName = JOptionPane.showInputDialog("Saved search name", "");
        if (searchName != null) {
            environmentController.saveSearch(searchName);
        }
        return searchName;
    }

    public void selectSearch(String name) {
        environmentController.selectSearch(name);
    }

    public void sendHistoricalDataRequest(String autoRequestHistory) {

        ObservableList<HubConnectionModel> hubConnectionModels = environmentModel.getHubConnectionModels();
        for (HubConnectionModel hubConnectionModel : hubConnectionModels) {
            SocketClientManager socketClientManager = hubConnectionModel.getSocketClientManager();
            final SocketClient client = socketClientManager.getClient();

            final HistoricalDataRequest request = new HistoricalDataRequest();

            long start;
            long end;

            if (autoRequestHistory.equalsIgnoreCase("all")) {
                start = 0;
                end = System.currentTimeMillis();
            } else {
                start = TimeUtils.before(System.currentTimeMillis(), autoRequestHistory);
                end = System.currentTimeMillis();
            }

            request.setStart(start);
            request.setEnd(end);
            request.setLevelFilter(Level.ALL.intValue());
            request.setQuickfilter("");
            request.setMostRecentFirst(false);
            request.setCorrelationID(client.getNextCorrelationID());

            LoggingMessageListener listener = new LoggingMessageListener() {
                @Override
                public void onNewLoggingMessage(LoggingMessage message) {
                    if (message instanceof RequestResponseMessage) {
                        RequestResponseMessage requestResponseMessage = (RequestResponseMessage) message;
                        if (requestResponseMessage.getCorrelationID() == request.getCorrelationID()) {

                            HistoricalDataResponse response = (HistoricalDataResponse) requestResponseMessage;
                            logger.fine("Streaming event data received : {} events", response.getEvents().length);
                            final DefaultLogEvent[] events = response.getEvents();

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    for (DefaultLogEvent defaultLogEvent : events) {
                                        if (defaultLogEvent != null) {
                                            environmentModel.onNewLogEvent(defaultLogEvent);
                                        } else {
                                            logger.warn("Null log event returned");
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            };

            try {
                client.addLoggingMessageListener(listener);
                client.send(request);
            } catch (LoggingMessageSenderException e) {
                throw new FormattedRuntimeException(e, "Failed to send message");
            }

        }
    }

    public void sendHistoricalIndexRequest() {

        ObservableList<HubConnectionModel> hubConnectionModels = environmentModel.getHubConnectionModels();
        for (HubConnectionModel hubConnectionModel : hubConnectionModels) {
            SocketClientManager socketClientManager = hubConnectionModel.getSocketClientManager();
            final SocketClient client = socketClientManager.getClient();
            client.addLoggingMessageListener(new LoggingMessageListener() {
                @Override
                public void onNewLoggingMessage(LoggingMessage message) {
                    if (message instanceof HistoricalIndexResponse) {
                        HistoricalIndexResponse response = (HistoricalIndexResponse) message;
                        timeController.processHistoricalIndex(response);
                    }
                }
            });

            final long viewStart = timeController.getModel().getViewStart().longValue();
            final long viewEnd = timeView.getViewEndTime();

            WorkerThread.execute("Index loader", new Runnable() {
                @Override
                public void run() {
                    try {
                        client.sendHistoricalIndexRequest(viewStart, viewEnd);
                    } catch (Exception e) {
                        logger.info(e, "Failed to request historical index");
                    }
                }
            });

        }
    }

    public void setAutoScroll(boolean selected) {
        autoScroll = selected;
    }

    public void setDetailPaneOrientation(final boolean horizontal) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (horizontal) {
                    eventDetailsSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                } else {
                    eventDetailsSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                }

                setSplitPaneLocation(environmentModel.getEventDetailsSeparatorLocation().get());
            }
        });

    }

    private void setDividerLocationRelative(final double finalValue) {
        // Thanks to swing, setting based on a double value will only work once the component has been laid out, so we have to hack it
        ThreadUtils.untilTrue(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                logger.info("Setting divider location to '{}'", finalValue);
                eventDetailsSplitPane.setDividerLocation(finalValue);
                int dividerLocation = eventDetailsSplitPane.getDividerLocation();
                return dividerLocation > 0;
            }
        });
    }

    public void setDynamicSettings(Metadata dynamicSettings) {
        this.dynamicSettings = dynamicSettings;
        initialiseFromSettings(dynamicSettings);
    }

    private void initialiseFromSettings(Metadata dynamicSettings) {
        String level = dynamicSettings.getString("detailedLogEventTablePanel.quickFilterLevel", Level.INFO.getName());
        setQuickLevelFilter(Level.parse(level));
        // filterRowPanel.getSelectedLevel().set(Level.parse(level));
        // quickLevelFilterCombo.setSelectedItem();

        int timeSplitterPosition = dynamicSettings.getInt("timeSplitterPosition", 40);
        previousTimeSplitterLocation = dynamicSettings.getInt("previousTimeSplitterLocation", 0);

        logger.fine("Initialising the time splitter : position is '{}' and previous location is '{}'",
                    timeSplitterPosition,
                    previousTimeSplitterLocation);
        timeSplitter.setDividerLocation(timeSplitterPosition);
    }

    public void setQuickLevelFilter(Level level) {
        getFirstQuickFilter().getModel().getLevelFilter().get().getSelectedLevel().set(level);
    }

    public void setSelectedRowFormat(RowFormatModel selectedRowFormat) {
        rowHighlighter.setSelectedRowFormat(selectedRowFormat);
    }

    private void setSplitPaneLocation(String newValue) {
        // This property supports a few different styles - integer number of pixels, double value factor, string percentage
        String trimmed = newValue.trim();
        if (trimmed.endsWith("%")) {
            String valueString = trimmed.substring(0, trimmed.length() - 1).trim();

            double value = Double.parseDouble(valueString);

            // Turn it into a factor
            value = value / 100d;

            final double finalValue = value;
            setDividerLocationRelative(finalValue);
        } else {

            try {
                int integerValue = Integer.parseInt(trimmed);
                if (integerValue != -1) {
                    logger.info("Setting divider location to '{}'", integerValue);
                    eventDetailsSplitPane.setDividerLocation(integerValue);
                } else {
                    eventDetailsSplitPane.setDividerLocation(0.5d);
                }
            } catch (NumberFormatException e) {
                double value = Double.parseDouble(trimmed);
                setDividerLocationRelative(value);
            }
        }
    }

    private void setupMenuBar(JMenuBar menuBar) {

        MenuElement[] subElements = menuBar.getSubElements();
        for (MenuElement menuElement : subElements) {
            Component component = menuElement.getComponent();
            if (component instanceof JMenu) {
                JMenu jMenu = (JMenu) component;
                if (jMenu.getText().equals("Search")) {
                    // Already have a menu attacked
                    return;
                }
            }
        }

        JMenu searchMenu = new JMenu("Search");
        menuBar.add(searchMenu);

        JMenuItem toggleScrolling = new JMenuItem("Toggle scrolling");
        toggleScrolling.addActionListener(new ReflectionDispatchActionListener("toggleScrolling", this));
        toggleScrolling.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        searchMenu.add(toggleScrolling);

        JMenuItem clearEvents = new JMenuItem("Clear events");
        clearEvents.addActionListener(new ReflectionDispatchActionListener("clearEvents", this));
        clearEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        searchMenu.add(clearEvents);
        searchMenu.addSeparator();

        JMenuItem findForwards = new JMenuItem("Find fowards");
        JMenuItem findForwardsAgain = new JMenuItem("Find forwards again");
        JMenuItem findBackwards = new JMenuItem("Find backwards");
        JMenuItem findBackwardsAgain = new JMenuItem("Find backwards again");

        findForwards.addActionListener(new ReflectionDispatchActionListener("findForwards", this));
        findForwardsAgain.addActionListener(new ReflectionDispatchActionListener("findForwardsAgain", this));
        findBackwards.addActionListener(new ReflectionDispatchActionListener("findBackwards", this));
        findBackwardsAgain.addActionListener(new ReflectionDispatchActionListener("findBackwardsAgain", this));

        findForwards.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        findForwardsAgain.setAccelerator(KeyStroke.getKeyStroke('f'));

        findBackwards.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));
        findBackwardsAgain.setAccelerator(KeyStroke.getKeyStroke('b'));

        searchMenu.add(findForwards);
        searchMenu.add(findForwardsAgain);
        searchMenu.add(findBackwards);
        searchMenu.add(findBackwardsAgain);
        searchMenu.addSeparator();

        int bookmarks = 9;
        for (int i = 1; i < bookmarks; i++) {
            JMenuItem bookmark = new JMenuItem("Add bookmark " + i);
            // new ReflectionDispatchActionListener("addBookmark", new Object[]{i}, this)
            final int finalI = i;
            bookmark.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addBookmark(finalI);
                }
            });
            bookmark.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, InputEvent.CTRL_MASK));
            searchMenu.add(bookmark);
        }

        searchMenu.addSeparator();

        for (int i = 1; i < bookmarks; i++) {
            JMenuItem bookmark = new JMenuItem("Go to bookmark " + i);
            final int finalI = i;
            bookmark.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gotoBookmark(finalI);
                }
            });
            bookmark.setAccelerator(KeyStroke.getKeyStroke((char) ('0' + i)));
            searchMenu.add(bookmark);
        }

        JMenu levelMenu = new JMenu("Levels");
        menuBar.add(levelMenu);

        addLevelFilterMenuOption(levelMenu, Level.SEVERE, KeyEvent.VK_F8);
        addLevelFilterMenuOption(levelMenu, Level.WARNING, KeyEvent.VK_F7);
        addLevelFilterMenuOption(levelMenu, Level.INFO, KeyEvent.VK_F6);
        addLevelFilterMenuOption(levelMenu, Level.CONFIG, KeyEvent.VK_F5);
        addLevelFilterMenuOption(levelMenu, Level.FINE, KeyEvent.VK_F4);
        addLevelFilterMenuOption(levelMenu, Level.FINER, KeyEvent.VK_F3);
        addLevelFilterMenuOption(levelMenu, Level.FINEST, KeyEvent.VK_F2);
        addLevelFilterMenuOption(levelMenu, Level.ALL, KeyEvent.VK_F1);

    }

    public void gotoBookmark(Integer bookmark) {
        table.gotoBookmark(bookmark.intValue());
    }

    public void addBookmark(Integer bookmark) {
        table.addBookmark(bookmark.intValue());
    }

    public synchronized void stopBinaryExport() {
        if (binaryExporter != null) {
            getEnvironmentModel().removeLogEventListener(binaryExporter);
            binaryExporter.close();
            binaryExporter = null;
        }
    }

    //    protected void showTimeTravelDialogue() {
    //
    //        JDialog dialog = new JDialog();
    //        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
    //
    //        TimeTravelPanel timeTravelPanel = new TimeTravelPanel(/* repositoryClient */);
    //        dialog.add(timeTravelPanel);
    //        dialog.setSize(800, 500);
    //        dialog.setLocationRelativeTo(this);
    //
    //        dialog.setVisible(true);
    //
    //    }

    protected synchronized void updateQuickFilterTimer() {
        logger.trace("Updating the quick filter timer...");

        // Kill off the existing task if it exists
        if (executeQuickFilter != null) {
            executeQuickFilter.cancel();
            executeQuickFilter = null;
        }

        // Create a new instance
        executeQuickFilter = new TimerTask() {
            @Override
            public void run() {
                try {
                    executeQuickFilter();
                } catch (Exception e) {
                    logger.warning(e, "Quick filter execution failed");
                }
            }
        };

        quickFilterTimer.schedule(executeQuickFilter, quickFilterTimeout);
    }

    protected void writeOutputLog(LogEvent event) {
        if (outputLogger == null) {
            JOptionPane.showMessageDialog(this,
                                          "You haven't provided an output log configuration, this should have been picked up earlier [this message " +
                                          "" + "will repeat for each event]");
        } else {
            try {
                outputLogger.write(event);
            } catch (LoggingMessageSenderException e) {
                throw new RuntimeException(e);
            }
        }

    }

    // for Matt
//        table.setDefaultEditor(Date.class, new DateEditor());
//    private static final class DateEditor extends AbstractCellEditor implements TableCellEditor {
//
//        private Date value;
//        private JTextField textField = new JTextField();
//        private SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
//
//        @Override
//        public Object getCellEditorValue() {
//            return value;
//        }
//
//        @Override
//        public boolean stopCellEditing() {
//            String inputData = textField.getText().trim();
//
//            if(inputData.isEmpty()) {
//                this.value = null;
//            }else {
//                try {
//                    Date newDate = f.parse(inputData);
//                    // Make this our new value; this gets picked up and applied back into the model
//                    this.value = newDate;
//                } catch (ParseException e) {
//                    // Don't do anything, revert back to the current value
//                }
//            }
//            super.stopCellEditing();
//            return true;
//        }
//
//        @Override
//        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//            this.value = (Date) value;
//            if (this.value != null) {
//                textField.setText(f.format(this.value));
//            } else {
//                textField.setText("");
//            }
//            return textField;
//
//        }
//
//    }

}
