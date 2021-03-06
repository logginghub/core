package com.logginghub.logging.frontend.charting.swing;

import com.logginghub.logging.LogEventMultiplexer;
import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.NewChartingView;
import com.logginghub.logging.frontend.charting.model.AggregationConfiguration;
import com.logginghub.logging.frontend.charting.model.ChartSeriesFilterModel;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.ExpressionConfiguration;
import com.logginghub.logging.frontend.charting.model.LineChartModel;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.charting.model.PageModel;
import com.logginghub.logging.frontend.charting.model.PieChartModel;
import com.logginghub.logging.frontend.charting.model.StreamConfiguration;
import com.logginghub.logging.frontend.charting.model.TableChartModel;
import com.logginghub.logging.frontend.charting.swing.config.CentredDialog;
import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.utils.KryoVersion1Decoder;
import com.logginghub.logging.utils.LogEventBlockElement;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Pointer;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.SwingHelper;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.Xml;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import com.logginghub.utils.swing.VLFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class ChartingTreeEditorView extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(ChartingTreeEditorView.class);
    private static final long serialVersionUID = 1L;
    private final DefaultMutableTreeNode aggregationsNode;
    private final DefaultMutableTreeNode expressionsNode;

    private DefaultMutableTreeNode patternsNode;
    // private DefaultMutableTreeNode streamsNode;
    private DefaultMutableTreeNode chartingNode;
    private NewChartingController controller;

    private ObservableList<ChartSeriesFilterModel> copyFilters;

    private AtomicInteger nextAggregationId = new AtomicInteger(0);
    private AtomicInteger nextExpressionId = new AtomicInteger(0);

    private NewChartingModel model;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private NewChartingView chartingPanel;
    private Counterparts<PageModel, DefaultMutableTreeNode> pageCounterparts;
    private Counterparts<PatternModel, DefaultMutableTreeNode> patternCounterparts;
    private Counterparts<AggregationConfiguration, DefaultMutableTreeNode> aggregationsCounterparts;
    private Counterparts<ExpressionConfiguration, DefaultMutableTreeNode> expressionCounterparts;
    private Counterparts<StreamConfiguration, DefaultMutableTreeNode> streamCounterparts;
    private Counterparts<LineChartModel, DefaultMutableTreeNode> lineChartCounterparts;
    private Counterparts<PieChartModel, DefaultMutableTreeNode> pieChartCounterparts;
    private Counterparts<TableChartModel, DefaultMutableTreeNode> tableChartCounterparts;
    private Counterparts<ChartSeriesModel, DefaultMutableTreeNode> seriesCounterparts;
    private DefaultMutableTreeNode root;

    public ChartingTreeEditorView() {
        setName("ChartingTreeEditorView");

        lineChartCounterparts = new Counterparts<LineChartModel, DefaultMutableTreeNode>();
        pieChartCounterparts = new Counterparts<PieChartModel, DefaultMutableTreeNode>();
        tableChartCounterparts = new Counterparts<TableChartModel, DefaultMutableTreeNode>();
        seriesCounterparts = new Counterparts<ChartSeriesModel, DefaultMutableTreeNode>();
        pageCounterparts = new Counterparts<PageModel, DefaultMutableTreeNode>();
        patternCounterparts = new Counterparts<PatternModel, DefaultMutableTreeNode>();
        streamCounterparts = new Counterparts<StreamConfiguration, DefaultMutableTreeNode>();
        aggregationsCounterparts = new Counterparts<AggregationConfiguration, DefaultMutableTreeNode>();
        expressionCounterparts = new Counterparts<ExpressionConfiguration, DefaultMutableTreeNode>();

        setLayout(new BorderLayout());

        root = new DefaultMutableTreeNode("root");

        patternsNode = new DefaultMutableTreeNode("Patterns");
        aggregationsNode = new DefaultMutableTreeNode("Aggregations");
        expressionsNode = new DefaultMutableTreeNode("Expressions");
        chartingNode = new DefaultMutableTreeNode("Charting");

        root.add(patternsNode);
        root.add(aggregationsNode);
        root.add(expressionsNode);
        root.add(chartingNode);

        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setName("ChartingTreeEditorView.tree");
        tree.setCellRenderer(new TreeRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        expandAll();

        int treeWidth = 400;
        Dimension treeSize = new Dimension(treeWidth, -1);
        tree.setMinimumSize(treeSize);
        tree.setPreferredSize(treeSize);

        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node != null) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                        deleteNode(node);
                    }
                }
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                    tree.setSelectionRow(row);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                    showContextMenu(node, e.getComponent(), e.getX(), e.getY());

                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        if (node != null) {
                            Object userObject = node.getUserObject();

                            if (userObject instanceof PatternModel) {
                                PatternModel patternModel = (PatternModel) userObject;
                                showPatternModelEditor(patternModel);
                            } else if (userObject instanceof ChartSeriesModel) {
                                ChartSeriesModel chartSeriesModel = (ChartSeriesModel) userObject;
                                editChartSeriesModel(chartSeriesModel);
                            } else if (userObject instanceof AggregationConfiguration) {
                                AggregationConfiguration model = (AggregationConfiguration) userObject;
                                editAggregationConfiguration(model);
                            } else if (userObject instanceof ExpressionConfiguration) {
                                ExpressionConfiguration model = (ExpressionConfiguration) userObject;
                                editExpressionConfiguration(model);
                            } else if (userObject instanceof LineChartModel) {
                                showLineChartEditorDialog(userObject);
                            } else if (userObject instanceof PieChartModel) {
                                showPieChartEditorDialog(userObject);
                            } else if (userObject instanceof TableChartModel) {
                                showTableChartEditorDialog(userObject);
                            }
                        }
                    }
                }
            }

        });

        chartingPanel = new NewChartingView();

        // Build the split pane
        JSplitPane pane = new JSplitPane();
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(treeSize);
        pane.setLeftComponent(scrollPane);
        pane.setRightComponent(chartingPanel);
        pane.setDividerLocation(treeWidth);
        pane.setDividerSize(3);

        add(pane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        Logger.setLevel(Logger.trace, Observable.class);

        VLFrame frame = new VLFrame("Chart Editor", "/icons/ViewDetailed.png");
        ChartingTreeEditorView treeEditorView = new ChartingTreeEditorView();
        frame.add(treeEditorView);
        frame.setVisible(true);

        final NewChartingModel newChartingModel = new NewChartingModel();

        final File config = new File("charting.xml");
        if (config.exists()) {
            logger.info("Loading charting configuration from '{}'", config.getAbsoluteFile());
            Xml xml = new Xml(FileUtils.read(config));
            newChartingModel.fromXml(xml.getRoot());
        }

        newChartingModel.addListener(new ObservableListener() {
            @Override
            public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                logger.info("Charting configuration changed, saving to '{}'", config.getAbsoluteFile());
                String xml = newChartingModel.toXml("chartingModel");
                FileUtils.write(xml, config);
            }
        });

        final NewChartingController newChartingController = new NewChartingController(newChartingModel, new SystemTimeProvider());

        final LogEventMultiplexer multiplexer = new LogEventMultiplexer();

        // Wire in the charting controller to the event flow
        multiplexer.addLogEventListener(newChartingController.getLogEventMultiplexer());

        final KryoVersion1Decoder decoder = new KryoVersion1Decoder();
        final File file = new File("D:\\Development\\July2012\\HSBCLogs\\20130308.140000.logdata");
        WorkerThread.executeOngoing("Reader", new Runnable() {
            @Override
            public void run() {
                try {
                    decoder.readFileInternal(file, new StreamListener<LogEventBlockElement>() {
                        @Override
                        public void onNewItem(LogEventBlockElement t) {
                            // Ignore blocks, we dont care
                        }
                    }, multiplexer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        treeEditorView.bind(newChartingController);
    }

    public void bind(NewChartingController controller) {
        this.controller = controller;
        this.model = controller.getModel();

        chartingPanel.bind(controller);

        setupNextIds(model);

        model.getPages().addListenerAndNotifyCurrent(new ObservableListListener<PageModel>() {
            @Override
            public void onAdded(final PageModel t) {

                SwingHelper.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(t) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public String toString() {
                                return t.getName().get();
                            }
                        };
                        treeModel.insertNodeInto(node, chartingNode, chartingNode.getChildCount());
                        pageCounterparts.put(t, node);

                        bindPage(node, t);
                    }
                });

                t.getName().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override
                    public void onPropertyChanged(String oldValue, String newValue) {
                        treeModel.nodeChanged(pageCounterparts.get(t));
                    }
                });
            }

            @Override
            public void onRemoved(final PageModel t, int index) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        DefaultMutableTreeNode remove = pageCounterparts.remove(t);
                        treeModel.removeNodeFromParent(remove);
                    }
                });
            }

            @Override
            public void onCleared() {
            }
        });

        model.getPatternModels().addListenerAndNotifyCurrent(new ObservableListListener<PatternModel>() {
            @Override
            public void onAdded(final PatternModel t) {
                SwingHelper.invoke(new Runnable() {
                    @Override
                    public void run() {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(t) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public String toString() {
                                return "[" + t.getPatternID().get() + "] " + t.getName().get();
                            }
                        };
                        treeModel.insertNodeInto(node, patternsNode, patternsNode.getChildCount());
                        patternCounterparts.put(t, node);
                    }
                });
            }

            @Override
            public void onRemoved(final PatternModel t, int index) {
                SwingHelper.invoke(new Runnable() {
                    @Override
                    public void run() {
                        DefaultMutableTreeNode remove = patternCounterparts.remove(t);
                        treeModel.removeNodeFromParent(remove);
                    }
                });
            }

            @Override
            public void onCleared() {
            }
        });

        model.getAggregationConfigurations().addListenerAndNotifyCurrent(new ObservableListListener<AggregationConfiguration>() {
            @Override
            public void onAdded(final AggregationConfiguration t) {
                SwingHelper.invoke(new Runnable() {
                    @Override
                    public void run() {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(t) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public String toString() {
                                return "[" + t.getAggregationId().get() + "] " + t.getName().get();
                            }
                        };
                        treeModel.insertNodeInto(node, aggregationsNode, aggregationsNode.getChildCount());
                        aggregationsCounterparts.put(t, node);
                    }
                });
            }

            @Override
            public void onRemoved(final AggregationConfiguration t, int index) {
                SwingHelper.invoke(new Runnable() {
                    @Override
                    public void run() {
                        DefaultMutableTreeNode remove = aggregationsCounterparts.remove(t);
                        treeModel.removeNodeFromParent(remove);
                    }
                });
            }

            @Override
            public void onCleared() {
            }
        });

        model.getExpressionConfigurations().addListenerAndNotifyCurrent(new ObservableListListener<ExpressionConfiguration>() {
            @Override
            public void onAdded(final ExpressionConfiguration t) {
                SwingHelper.invoke(new Runnable() {
                    @Override
                    public void run() {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(t) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public String toString() {
                                return "[" + t.getId().get() + "] " + t.getName().get();
                            }
                        };
                        treeModel.insertNodeInto(node, expressionsNode, expressionsNode.getChildCount());
                        expressionCounterparts.put(t, node);
                    }
                });
            }

            @Override
            public void onRemoved(final ExpressionConfiguration t, int index) {
                SwingHelper.invoke(new Runnable() {
                    @Override
                    public void run() {
                        DefaultMutableTreeNode remove = expressionCounterparts.remove(t);
                        treeModel.removeNodeFromParent(remove);
                    }
                });
            }

            @Override
            public void onCleared() {
            }
        });

        // Auto expand some nodes
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
                Enumeration<?> breadthFirstEnumeration = root.breadthFirstEnumeration();
                while (breadthFirstEnumeration.hasMoreElements()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) breadthFirstEnumeration.nextElement();

                    Object userObject = node.getUserObject();
                    if (userObject instanceof PageModel || node == patternsNode || node == aggregationsNode || node == expressionsNode) {
                        tree.expandPath(new TreePath(node.getPath()));
                    }
                }

            }
        });
    }

    private void setupNextIds(NewChartingModel model) {
        int max = -1;
        for (AggregationConfiguration aggregationConfiguration : model.getAggregationConfigurations()) {
            max = Math.max(max, aggregationConfiguration.getAggregationId().get());
        }

        nextAggregationId.set(max + 1);

        max = -1;
        for (ExpressionConfiguration expressionConfiguration : model.getExpressionConfigurations()) {
            max = Math.max(max, expressionConfiguration.getId().get());
        }

        nextExpressionId.set(max + 1);
    }

    private void bindPage(final DefaultMutableTreeNode node, final PageModel t) {

        t.getChartingModels().addListenerAndNotifyCurrent(new ObservableListListener<LineChartModel>() {
            @Override
            public void onAdded(final LineChartModel lineChartModel) {
                bindLineChart(node, t, lineChartModel);
            }

            @Override
            public void onRemoved(LineChartModel t, int index) {
                DefaultMutableTreeNode remove = lineChartCounterparts.remove(t);
                treeModel.removeNodeFromParent(remove);
            }

            @Override
            public void onCleared() {
            }

        });

        t.getPieChartModels().addListenerAndNotifyCurrent(new ObservableListListener<PieChartModel>() {
            @Override
            public void onAdded(final PieChartModel pieChartModel) {
                bindPieChart(node, t, pieChartModel);
            }

            @Override
            public void onRemoved(PieChartModel t, int index) {
                DefaultMutableTreeNode remove = pieChartCounterparts.remove(t);
                treeModel.removeNodeFromParent(remove);
            }

            @Override
            public void onCleared() {
            }

        });

        t.getTableChartModels().addListenerAndNotifyCurrent(new ObservableListListener<TableChartModel>() {
            @Override
            public void onAdded(final TableChartModel tableChartModel) {
                bindTableChart(node, t, tableChartModel);
            }

            @Override
            public void onRemoved(TableChartModel t, int index) {
                DefaultMutableTreeNode remove = tableChartCounterparts.remove(t);
                treeModel.removeNodeFromParent(remove);
            }

            @Override
            public void onCleared() {
            }

        });
    }

    private void bindLineChart(final DefaultMutableTreeNode node, final PageModel t, final LineChartModel lineChartModel) {
        lineChartModel.setParentPage(t);

        final DefaultMutableTreeNode chartNode = new DefaultMutableTreeNode(lineChartModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public String toString() {
                return lineChartModel.getTitle().get();
            }
        };
        treeModel.insertNodeInto(chartNode, node, node.getChildCount());
        lineChartCounterparts.put(lineChartModel, chartNode);

        ObservableList<ChartSeriesModel> matcherModels = lineChartModel.getMatcherModels();
        matcherModels.addListenerAndNotifyCurrent(new ObservableListListener<ChartSeriesModel>() {
            @Override
            public void onAdded(final ChartSeriesModel chartSeriesModel) {
                logger.debug("Chart series model '{}' has been added to chart '{}'", chartSeriesModel, lineChartModel);
                chartSeriesModel.setParentChart(lineChartModel);
                DefaultMutableTreeNode seriesNode = new DefaultMutableTreeNode(chartSeriesModel) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String toString() {
                        String formatted = formatChartSeriesModelTitle(chartSeriesModel);
                        return formatted;
                    }
                };
                treeModel.insertNodeInto(seriesNode, chartNode, chartNode.getChildCount());
                seriesCounterparts.put(chartSeriesModel, seriesNode);
            }

            @Override
            public void onRemoved(ChartSeriesModel t, int index) {
                logger.info("Chart series model '{}' has been removed from chart '{}'", t, lineChartModel);
                DefaultMutableTreeNode remove = seriesCounterparts.remove(t);
                treeModel.removeNodeFromParent(remove);
            }

            @Override
            public void onCleared() {
            }
        });
    }

    private String formatChartSeriesModelTitle(ChartSeriesModel chartSeriesModel) {


        ExpressionConfiguration expressionConfiguration = chartSeriesModel.getExistingExpression().get();
        AggregationConfiguration aggregationConfiguration = chartSeriesModel.getExistingAggregation().get();

        String formatted;

        if(expressionConfiguration != null) {
            formatted = "Expr : [" + expressionConfiguration.getId().get() + "] " + expressionConfiguration.getName().get();
        }else if(aggregationConfiguration != null) {
            formatted = "Aggr : [" + aggregationConfiguration.getAggregationId().get() + "] " + aggregationConfiguration.getName().get();
        }else {


            formatted = model.getPatternNameForID(chartSeriesModel.getPatternID().get()) +
                          "/" +
                          model.getPatternLabelForID(chartSeriesModel.getPatternID().get(), chartSeriesModel.getLabelIndex().get()) +
                          "/" +
                          chartSeriesModel.getType().get();
        }

        return formatted;

    }

    private void bindPieChart(final DefaultMutableTreeNode node, final PageModel t, final PieChartModel pieChartModel) {
        pieChartModel.setParentPage(t);

        final DefaultMutableTreeNode chartNode = new DefaultMutableTreeNode(pieChartModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public String toString() {
                return pieChartModel.getTitle().get();
            }
        };
        treeModel.insertNodeInto(chartNode, node, node.getChildCount());
        pieChartCounterparts.put(pieChartModel, chartNode);

        ObservableList<ChartSeriesModel> matcherModels = pieChartModel.getMatcherModels();
        matcherModels.addListenerAndNotifyCurrent(new ObservableListListener<ChartSeriesModel>() {
            @Override
            public void onAdded(final ChartSeriesModel chartSeriesModel) {
                logger.debug("Chart series model '{}' has been added to chart '{}'", chartSeriesModel, pieChartModel);

                // TODO : need to work out how to get around this
                // chartSeriesModel.setParentChart(pieChartModel);

                DefaultMutableTreeNode seriesNode = new DefaultMutableTreeNode(chartSeriesModel) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String toString() {
                        String name = model.getPatternNameForID(chartSeriesModel.getPatternID().get()) +
                                      "/" +
                                      model.getPatternLabelForID(chartSeriesModel.getPatternID().get(), chartSeriesModel.getLabelIndex().get()) +
                                      "/" +
                                      chartSeriesModel.getType().get();
                        return name;
                    }
                };
                treeModel.insertNodeInto(seriesNode, chartNode, chartNode.getChildCount());
                seriesCounterparts.put(chartSeriesModel, seriesNode);
            }

            @Override
            public void onRemoved(ChartSeriesModel t, int index) {
                logger.info("Chart series model '{}' has been removed from chart '{}'", t, pieChartModel);
                DefaultMutableTreeNode remove = seriesCounterparts.remove(t);
                treeModel.removeNodeFromParent(remove);
            }

            @Override
            public void onCleared() {
            }
        });
    }

    private void bindTableChart(final DefaultMutableTreeNode node, final PageModel t, final TableChartModel tableChartModel) {
        tableChartModel.setParentPage(t);

        final DefaultMutableTreeNode chartNode = new DefaultMutableTreeNode(tableChartModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public String toString() {
                return tableChartModel.getTitle().get();
            }
        };
        treeModel.insertNodeInto(chartNode, node, node.getChildCount());
        tableChartCounterparts.put(tableChartModel, chartNode);

        ObservableList<ChartSeriesModel> matcherModels = tableChartModel.getMatcherModels();
        matcherModels.addListenerAndNotifyCurrent(new ObservableListListener<ChartSeriesModel>() {
            @Override
            public void onAdded(final ChartSeriesModel chartSeriesModel) {
                logger.debug("Chart series model '{}' has been added to table chart '{}'", chartSeriesModel, tableChartModel);

                // TODO : need to work out how to get around this
                // chartSeriesModel.setParentChart(pieChartModel);

                DefaultMutableTreeNode seriesNode = new DefaultMutableTreeNode(chartSeriesModel) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String toString() {
                        String name = model.getPatternNameForID(chartSeriesModel.getPatternID().get()) +
                                      "/" +
                                      model.getPatternLabelForID(chartSeriesModel.getPatternID().get(), chartSeriesModel.getLabelIndex().get()) +
                                      "/" +
                                      chartSeriesModel.getType().get();
                        return name;
                    }
                };
                treeModel.insertNodeInto(seriesNode, chartNode, chartNode.getChildCount());
                seriesCounterparts.put(chartSeriesModel, seriesNode);
            }

            @Override
            public void onRemoved(ChartSeriesModel t, int index) {
                logger.info("Chart series model '{}' has been removed from table chart '{}'", t, tableChartModel);
                DefaultMutableTreeNode remove = seriesCounterparts.remove(t);
                treeModel.removeNodeFromParent(remove);
            }

            @Override
            public void onCleared() {
            }
        });
    }

    // protected void addBarChart() {
    // DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    // Object userObject = node.getUserObject();
    //
    // if (userObject instanceof PageModel) {
    // PageModel pageModel = (PageModel) userObject;
    // BarChartModel lineChartModel = new BarChartModel();
    // pageModel.getBarChartingModels().add(lineChartModel);
    // }
    // }

    private void addAggregation() {

        EditorDialog editorDialog = new EditorDialog() {
        };

        final AggregationConfiguration editable = new AggregationConfiguration();

        final AggregationConfigurationEditor editor = new AggregationConfigurationEditor();
        editor.bind(controller, editable, ChartingTreeEditorView.this.model);

        editorDialog.getEventSource().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                Boolean option = event.getPayload();
                if (option) {
                    editor.commitEditingChanges();
                    editable.getAggregationId().set(nextAggregationId.getAndIncrement());
                    model.getAggregationConfigurations().add(editable);
                } else {
                    // Dont do anything
                }
            }
        });

        editorDialog.setSize(500, 500);
        editorDialog.setName("Data aggregation editor");
        editorDialog.setModal(true);
        editorDialog.show("Create data aggregation", editor, getParentRecursive());

    }

    private void addExpression() {

        EditorDialog editorDialog = new EditorDialog() {
        };

        final ExpressionConfiguration editable = new ExpressionConfiguration();

        final ExpressionConfigurationEditor editor = new ExpressionConfigurationEditor();
        editor.bind(controller, editable, ChartingTreeEditorView.this.model);

        editorDialog.getEventSource().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                Boolean option = event.getPayload();
                if (option) {
                    editor.commitEditingChanges();
                    editable.getId().set(nextExpressionId.getAndIncrement());
                    model.getExpressionConfigurations().add(editable);
                } else {
                    // Dont do anything
                }
            }
        });

        editorDialog.setSize(500, 500);
        editorDialog.setName("Expression editor");
        editorDialog.setModal(true);
        editorDialog.show("Create expression", editor, getParentRecursive());

    }

    protected void addLineChart() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Object userObject = node.getUserObject();

        if (userObject instanceof PageModel) {
            PageModel pageModel = (PageModel) userObject;
            LineChartModel lineChartModel = new LineChartModel();
            pageModel.getChartingModels().add(lineChartModel);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TreePath find = find(root, "Chart title");
                    tree.setExpandsSelectedPaths(true);
                    tree.setSelectionPath(find);
                }
            });

        }
    }

    private void addPage() {

        // String showInputDialog =
        // JOptionPane.showInputDialog("What would you like the page to be called?");
        // if (showInputDialog != null) {
        // PageModel pageModel = new PageModel();
        // pageModel.getName().set(showInputDialog);
        // model.getPages().add(pageModel);
        // }
        //
        // tree.setSelectionPath(find(root, showInputDialog));

        PageModel pageModel = new PageModel();
        pageModel.getName().set("New page");
        controller.getModel().getPages().add(pageModel);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TreePath find = find(root, "New page");
                tree.setExpandsSelectedPaths(true);
                tree.setSelectionPath(find);
            }
        });
    }

    protected void addPieChart() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Object userObject = node.getUserObject();

        if (userObject instanceof PageModel) {
            PageModel pageModel = (PageModel) userObject;
            PieChartModel chartModel = new PieChartModel();
            pageModel.getPieChartModels().add(chartModel);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TreePath find = find(root, "Chart title");
                    tree.setExpandsSelectedPaths(true);
                    tree.setSelectionPath(find);
                }
            });

        }
    }

    private void addSeriesToLineChartModel(final LineChartModel lineChartModel) {
        final ChartSeriesModel model = new ChartSeriesModel();

        EditorDialog editorDialog = new EditorDialog() {
        };
        ChartSeriesModelEditor editor = new ChartSeriesModelEditor();
        editor.bind(controller, model, ChartingTreeEditorView.this.model);

        editorDialog.getEventSource().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                Boolean option = event.getPayload();
                if (option) {
                    logger.info("Type of newly created series is {}", model.getType().asString());
                    lineChartModel.getMatcherModels().add(model);
                } else {
                    // Dont do anything
                }
            }
        });

        editorDialog.setModal(true);
        editorDialog.show("Edit chart series", editor, getParentRecursive());
    }

    private void addSeriesToPieChartModel(final PieChartModel pieChartModel) {
        final ChartSeriesModel model = new ChartSeriesModel();

        EditorDialog editorDialog = new EditorDialog() {
        };
        ChartSeriesModelEditor editor = new ChartSeriesModelEditor();
        editor.bind(controller, model, ChartingTreeEditorView.this.model);

        editorDialog.getEventSource().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                Boolean option = event.getPayload();
                if (option) {
                    logger.info("Type of newly created series is {}", model.getType().asString());
                    pieChartModel.getMatcherModels().add(model);
                } else {
                    // Dont do anything
                }
            }
        });

        editorDialog.setModal(true);
        editorDialog.show("Edit chart series", editor, getParentRecursive());
    }

    private void addSeriesToTableChartModel(final TableChartModel tableChartModel) {
        final ChartSeriesModel model = new ChartSeriesModel();

        EditorDialog editorDialog = new EditorDialog() {
        };
        ChartSeriesModelEditor editor = new ChartSeriesModelEditor();
        editor.bind(controller, model, ChartingTreeEditorView.this.model);

        editorDialog.getEventSource().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                Boolean option = event.getPayload();
                if (option) {
                    logger.info("Type of newly created series is {}", model.getType().asString());
                    tableChartModel.getMatcherModels().add(model);
                } else {
                    // Dont do anything
                }
            }
        });

        editorDialog.setModal(true);
        editorDialog.show("Edit chart series", editor, getParentRecursive());
    }

    protected void addTableChart() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Object userObject = node.getUserObject();

        if (userObject instanceof PageModel) {
            PageModel pageModel = (PageModel) userObject;
            TableChartModel chartModel = new TableChartModel();
            pageModel.getTableChartModels().add(chartModel);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TreePath find = find(root, "Chart title");
                    tree.setExpandsSelectedPaths(true);
                    tree.setSelectionPath(find);
                }
            });

        }
    }

    protected void deleteNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();

        logger.info("Attempting to delete node '{}' containing user object '{}'", node, userObject);
        if (userObject instanceof ChartSeriesModel) {
            // Removing the object from its parent should with any luck remove
            // the node from the
            // tree via model listeners!
            ChartSeriesModel chartSeriesModel = (ChartSeriesModel) userObject;
            LineChartModel parentChart = chartSeriesModel.getParentChart();
            logger.info("Removing series '{}' from parent chart '{}'", chartSeriesModel, parentChart);
            parentChart.getMatcherModels().remove(chartSeriesModel);
        } else if (userObject instanceof LineChartModel) {
            LineChartModel lineChartModel = (LineChartModel) userObject;
            PageModel parentPage = lineChartModel.getParentPage();
            parentPage.getChartingModels().remove(lineChartModel);
        }
    }

    protected void editAggregationConfiguration(final AggregationConfiguration model) {

        EditorDialog editorDialog = new EditorDialog() {
        };
        final AggregationConfiguration editable = new AggregationConfiguration();

        editable.getName().set(model.getName().get());
        editable.getLabelIndex().set(model.getLabelIndex().get());
        editable.getPatternID().set(model.getPatternID().get());
        editable.getType().set(model.getType().get());
        editable.getInterval().set(model.getInterval().get());
        editable.getGroupBy().set(model.getGroupBy().get());

        // jshaw - this is potentially messy, as we are passing references to the original filters
        // into the editor - ideally we should be passing in copies
        // TODO : pass in copies
        editable.getFilters().set(model.getFilters());

        final AggregationConfigurationEditor editor = new AggregationConfigurationEditor();
        editor.bind(controller, editable, ChartingTreeEditorView.this.model);

        editorDialog.getEventSource().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                Boolean option = event.getPayload();
                if (option) {
                    editor.commitEditingChanges();

                    // Update the actual model from the values in the editable copy
                    model.getName().set(editable.getName().get());
                    model.getLabelIndex().set(editable.getLabelIndex().get());
                    model.getPatternID().set(editable.getPatternID().get());
                    model.getType().set(editable.getType().get());
                    model.getInterval().set(editable.getInterval().get());
                    model.getGroupBy().set(editable.getGroupBy().get());

                    // jshaw - see comment about copying filters a few lines up
                    model.getFilters().set(editable.getFilters());
                } else {
                    // Dont do anything
                }
            }
        });

        editorDialog.setSize(500, 500);
        editorDialog.setName("Aggregation editor");
        editorDialog.setModal(true);
        editorDialog.show("Edit aggregation", editor, getParentRecursive());

    }

    protected void editChartSeriesModel(final ChartSeriesModel chartSeriesModel) {

        EditorDialog editorDialog = new EditorDialog() {
        };
        final ChartSeriesModel editable = new ChartSeriesModel();

        editable.getLabelIndex().set(chartSeriesModel.getLabelIndex().get());
        editable.getExistingAggregation().set(chartSeriesModel.getExistingAggregation().get());
        // editable.getLegend().set(chartSeriesModel.getLegend().get());
        editable.getPatternID().set(chartSeriesModel.getPatternID().get());
        editable.getType().set(chartSeriesModel.getType().get());
        editable.getInterval().set(chartSeriesModel.getInterval().get());
        editable.getGroupBy().set(chartSeriesModel.getGroupBy().get());
        editable.getGenerateEmptyTicks().set(chartSeriesModel.getGenerateEmptyTicks().get());

        // jshaw - this is potentially messy, as we are passing references to the original filters
        // into the editor - ideally we should be passing in copies
        // TODO : pass in copies
        editable.getFilters().set(chartSeriesModel.getFilters());

        final ChartSeriesModelEditor editor = new ChartSeriesModelEditor();
        editor.bind(controller, editable, ChartingTreeEditorView.this.model);

        editorDialog.getEventSource().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                Boolean option = event.getPayload();
                if (option) {
                    editor.commitEditingChanges();

                    // Update the actual model from the values in the editable copy
                    chartSeriesModel.getLabelIndex().set(editable.getLabelIndex().get());
                    // chartSeriesModel.getLegend().set(editable.getLegend().get());
                    chartSeriesModel.getExistingAggregation().set(editable.getExistingAggregation().get());
                    chartSeriesModel.getPatternID().set(editable.getPatternID().get());
                    chartSeriesModel.getType().set(editable.getType().get());
                    chartSeriesModel.getInterval().set(editable.getInterval().get());
                    chartSeriesModel.getEventParts().set(editable.getEventParts().get());
                    chartSeriesModel.getGroupBy().set(editable.getGroupBy().get());
                    chartSeriesModel.getGenerateEmptyTicks().set(editable.getGenerateEmptyTicks().get());

                    // jshaw - see comment about copying filters a few lines up
                    chartSeriesModel.getFilters().set(editable.getFilters());
                } else {
                    // Dont do anything
                }
            }
        });

        editorDialog.setSize(500, 500);
        editorDialog.setName("Chart series editor");
        editorDialog.setModal(true);
        editorDialog.show("Edit chart series", editor, getParentRecursive());

    }

    protected void editExpressionConfiguration(final ExpressionConfiguration model) {

        EditorDialog editorDialog = new EditorDialog() {
        };
        final ExpressionConfiguration editable = new ExpressionConfiguration();

        editable.getName().set(model.getName().get());
        editable.getId().set(model.getId().get());
        editable.getExpression().set(model.getExpression().get());
        editable.getGroupBy().set(model.getGroupBy().get());

        final ExpressionConfigurationEditor editor = new ExpressionConfigurationEditor();
        editor.bind(controller, editable, ChartingTreeEditorView.this.model);

        editorDialog.getEventSource().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                Boolean option = event.getPayload();
                if (option) {
                    editor.commitEditingChanges();

                    model.getName().set(editable.getName().get());
                    model.getId().set(editable.getId().get());
                    model.getExpression().set(editable.getExpression().get());
                    model.getGroupBy().set(editable.getGroupBy().get());

                } else {
                    // Dont do anything
                }
            }
        });

        editorDialog.setSize(500, 500);
        editorDialog.setName("Expression editor");
        editorDialog.setModal(true);
        editorDialog.show("Edit expression", editor, getParentRecursive());

    }

    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private TreePath find(DefaultMutableTreeNode root, String s) {
        TreePath found = null;
        @SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().equalsIgnoreCase(s)) {
                found = new TreePath(node.getPath());
                break;
            }
        }
        return found;
    }

    private Container getParentRecursive() {
        boolean done = false;
        Container parent = getParent();
        while (!done) {
            parent = parent.getParent();

            if (parent instanceof JDialog) {
                // JDialog parentDialog = (JDialog) parent;
                // dialog = new JDialog(parentDialog);
                done = true;
            } else if (parent instanceof JFrame) {
                // JFrame frame = (JFrame) parent;
                // dialog = new JDialog(frame);
                done = true;
            } else if (parent == null) {
                done = true;
            }
        }
        return parent;
    }

    private void removeChartSeries(final ChartSeriesModel chartSeriesModel) {
        ObservableList<ChartSeriesModel> found = null;

        ObservableList<PageModel> pages = model.getPages();
        for (PageModel pageModel : pages) {
            ObservableList<LineChartModel> chartingModels = pageModel.getChartingModels();
            for (LineChartModel lineChartModel : chartingModels) {
                ObservableList<ChartSeriesModel> matcherModels = lineChartModel.getMatcherModels();
                for (ChartSeriesModel otherChartSeriesModel : matcherModels) {
                    if (otherChartSeriesModel == chartSeriesModel) {
                        found = matcherModels;
                    }
                }
            }

            ObservableList<PieChartModel> pieChartModels = pageModel.getPieChartModels();
            for (PieChartModel pieChartModel : pieChartModels) {
                ObservableList<ChartSeriesModel> matcherModels = pieChartModel.getMatcherModels();
                for (ChartSeriesModel otherChartSeriesModel : matcherModels) {
                    if (otherChartSeriesModel == chartSeriesModel) {
                        found = matcherModels;
                    }
                }
            }

            ObservableList<TableChartModel> tableChartModels = pageModel.getTableChartModels();
            for (TableChartModel tableChartModel : tableChartModels) {
                ObservableList<ChartSeriesModel> matcherModels = tableChartModel.getMatcherModels();
                for (ChartSeriesModel otherChartSeriesModel : matcherModels) {
                    if (otherChartSeriesModel == chartSeriesModel) {
                        found = matcherModels;
                    }
                }
            }
        }

        if (found == null) {
            logger.warn("Couldn't find a chart with that series - unable to remove it. This is a bug.");
        } else {
            found.remove(chartSeriesModel);
        }
    }

    protected void showContextMenu(DefaultMutableTreeNode node, Component component, int x, int y) {

        final JPopupMenu popupMenu = new JPopupMenu();

        if (node == patternsNode) {
            JMenuItem addPattern = new JMenuItem("Add pattern");
            popupMenu.add(addPattern);

            addPattern.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showPatternModelEditor(null);
                }
            });
        } else if (node == chartingNode) {
            JMenuItem addPage = new JMenuItem("Add page");
            popupMenu.add(addPage);

            addPage.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addPage();
                }
            });

        } else if (node == aggregationsNode) {
            JMenuItem addPage = new JMenuItem("Add aggregation");
            popupMenu.add(addPage);

            addPage.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addAggregation();
                }
            });

        } else if (node == expressionsNode) {
            JMenuItem menuItem = new JMenuItem("Add expression");
            popupMenu.add(menuItem);
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addExpression();
                }
            });

        } else {

            Object userObject = node.getUserObject();
            if (userObject instanceof LineChartModel) {
                final LineChartModel lineChartModel = (LineChartModel) userObject;

                JMenuItem addSeries = new JMenuItem("Add series");
                popupMenu.add(addSeries);

                addSeries.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addSeriesToLineChartModel(lineChartModel);
                    }
                });

                JMenuItem removeChart = new JMenuItem("Remove chart");
                popupMenu.add(removeChart);

                removeChart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(ChartingTreeEditorView.this,
                                                                              "Are you sure you want to delete this chart?");
                        if (showConfirmDialog == JOptionPane.YES_OPTION) {
                            controller.removeChart(lineChartModel);
                        }
                    }
                });

            } else if (userObject instanceof PieChartModel) {
                final PieChartModel pieChartModel = (PieChartModel) userObject;

                JMenuItem addSeries = new JMenuItem("Add series");
                popupMenu.add(addSeries);

                addSeries.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addSeriesToPieChartModel(pieChartModel);
                    }
                });

                JMenuItem removeChart = new JMenuItem("Remove chart");
                popupMenu.add(removeChart);

                removeChart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(ChartingTreeEditorView.this,
                                                                              "Are you sure you want to delete this chart?");
                        if (showConfirmDialog == JOptionPane.YES_OPTION) {
                            controller.removeChart(pieChartModel);
                        }
                    }
                });

            } else if (userObject instanceof TableChartModel) {
                final TableChartModel tableChartModel = (TableChartModel) userObject;

                JMenuItem addSeries = new JMenuItem("Add series");
                popupMenu.add(addSeries);

                addSeries.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addSeriesToTableChartModel(tableChartModel);
                    }
                });

                JMenuItem removeChart = new JMenuItem("Remove chart");
                popupMenu.add(removeChart);

                removeChart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(ChartingTreeEditorView.this,
                                                                              "Are you sure you want to delete this table chart?");
                        if (showConfirmDialog == JOptionPane.YES_OPTION) {
                            controller.removeChart(tableChartModel);
                        }
                    }
                });

            } else if (userObject instanceof PageModel) {
                final PageModel pageModel = (PageModel) userObject;

                JMenuItem addChart = new JMenuItem("Add line chart");
                addChart.setIcon(Icons.load("/icons/chart.png"));
                popupMenu.add(addChart);
                addChart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addLineChart();
                    }
                });

                JMenuItem addPieChart = new JMenuItem("Add pie chart");
                addPieChart.setIcon(Icons.load("/icons/piechart.png"));
                popupMenu.add(addPieChart);
                addPieChart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addPieChart();
                    }
                });

                JMenuItem addTableChart = new JMenuItem("Add table");
                addTableChart.setIcon(Icons.load("/icons/table.png"));
                popupMenu.add(addTableChart);
                addTableChart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addTableChart();
                    }
                });

                JMenuItem editName = new JMenuItem("Rename page");
                popupMenu.add(editName);

                editName.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String showInputDialog = JOptionPane.showInputDialog("Rename page?", pageModel.getName().get());
                        if (showInputDialog != null) {
                            pageModel.getName().set(showInputDialog);
                        }
                    }
                });

                JMenuItem removePage = new JMenuItem("Remove page");
                popupMenu.add(removePage);

                removePage.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(ChartingTreeEditorView.this,
                                                                              "Are you sure you want to delete the page? All of its charts will be removed to!");
                        if (showConfirmDialog == JOptionPane.YES_OPTION) {
                            model.getPages().remove(pageModel);
                        }
                    }
                });
            } else if (userObject instanceof PatternModel) {
                final PatternModel patternModel = (PatternModel) userObject;

                JMenuItem removePage = new JMenuItem("Remove pattern");
                popupMenu.add(removePage);

                removePage.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(ChartingTreeEditorView.this,
                                                                              "Are you sure you want to delete this pattern?");
                        if (showConfirmDialog == JOptionPane.YES_OPTION) {
                            model.getPatternModels().remove(patternModel);
                        }
                    }
                });
            } else if (userObject instanceof ChartSeriesModel) {
                final ChartSeriesModel chartSeriesModel = (ChartSeriesModel) userObject;

                JMenuItem removeSeries = new JMenuItem("Remove series");
                popupMenu.add(removeSeries);

                removeSeries.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(ChartingTreeEditorView.this,
                                                                              "Are you sure you want to delete this series?");
                        if (showConfirmDialog == JOptionPane.YES_OPTION) {
                            removeChartSeries(chartSeriesModel);
                        }
                    }

                });

                final JMenuItem pasteFilters = new JMenuItem("Paste filters");
                if (copyFilters == null) {
                    pasteFilters.setEnabled(false);
                }

                JMenuItem copyFilters = new JMenuItem("Copy filters");

                popupMenu.addSeparator();
                popupMenu.add(copyFilters);
                popupMenu.add(pasteFilters);

                pasteFilters.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (ChartingTreeEditorView.this.copyFilters != null) {
                            ObservableList<ChartSeriesFilterModel> duplicate = ChartingTreeEditorView.this.copyFilters.duplicate();
                            logger.info("Setting duplicated filter list : '{}'", duplicate);
                            chartSeriesModel.getFilters().set(duplicate);
                        }
                    }
                });

                copyFilters.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ObservableList<ChartSeriesFilterModel> duplicateFilters = chartSeriesModel.getFilters().duplicate();
                        ChartingTreeEditorView.this.copyFilters = duplicateFilters;
                    }
                });

            }
        }

        popupMenu.show(component, x, y);
    }

    private void showLineChartEditorDialog(Object userObject) {
        LineChartModel lineChartModel = (LineChartModel) userObject;

        JDialog dialog = CentredDialog.create("Chart Editor", this);
        dialog.setBackground(Color.white);
        dialog.getContentPane().setBackground(Color.white);
        final ChartModelEditor chartModelEditor = new ChartModelEditor();
        dialog.getContentPane().setLayout(new MigLayout("fill", "[fill, grow]", "[fill, grow]"));
        dialog.setSize(400, 430);
        CentredDialog.centre(dialog, getParentRecursive());
        dialog.getContentPane().add(chartModelEditor);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                chartModelEditor.unbind();
            }
        });

        chartModelEditor.bind(controller, lineChartModel);

        dialog.setIconImage(Icons.load("/icons/charting.png").getImage());
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    private void showPatternModelEditor(PatternModel patternModel) {

        JDialog dialog = null;

        boolean done = false;
        Container parent = getParent();
        while (!done) {
            parent = parent.getParent();

            if (parent instanceof JDialog) {
                JDialog parentDialog = (JDialog) parent;
                dialog = new JDialog(parentDialog);
                done = true;
            } else if (parent instanceof JFrame) {
                JFrame frame = (JFrame) parent;
                dialog = new JDialog(frame);
                done = true;
            } else if (parent == null) {
                done = true;
            }
        }

        if (dialog == null) {
            dialog = new JDialog();
        }

        final JDialog finalDialog = dialog;

        final ComprehensivePatternModelEditor patternModelEditor = new ComprehensivePatternModelEditor();
        dialog.getContentPane().setLayout(new MigLayout("fill, ins 2", "[fill, grow]", "[fill, grow]"));
        dialog.getContentPane().add(patternModelEditor);

        patternModelEditor.getDoneStream().addListener(new StreamListener<Boolean>() {
            public void onNewItem(Boolean t) {
                finalDialog.dispose();
            }
        });

        int screenHeight = parent.getHeight();
        int screenWidth = parent.getWidth();

        // int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        // int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;

        int width = (int) (screenWidth * 0.9);
        int height = (int) (screenHeight * 0.9f);

        int offsetX = (int) (screenWidth * 0.1) / 2;
        int offsetY = (int) (screenHeight * 0.1f) / 2;

        dialog.setLocation(parent.getX() + offsetX, parent.getY() + offsetY);
        // dialog.pack();
        dialog.setSize(width, height);

        controller.getLogEventMultiplexer().addLogEventListener(patternModelEditor);
        patternModelEditor.bind(patternModel, controller);

        // Capture the last model added, so we can select it later
        final Pointer<PatternModel> lastAdded = new Pointer<PatternModel>(null);
        controller.getModel().getPatternModels().addListener(new ObservableListListener<PatternModel>() {
            @Override
            public void onRemoved(PatternModel t, int index) {
            }

            @Override
            public void onCleared() {
            }

            @Override
            public void onAdded(PatternModel t) {
                lastAdded.value = t;
            }
        });

        dialog.setName("Pattern Editor");
        dialog.setTitle("Pattern Editor");
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                controller.getLogEventMultiplexer().removeLogEventListener(patternModelEditor);
            }
        });

        if (lastAdded.value != null) {
            String name = StringUtils.format("[{}] {}", lastAdded.value.getPatternID(), lastAdded.value.getName());
            TreePath node = find(root, name);
            tree.setSelectionPath(node);
        }

    }

    private void showPieChartEditorDialog(Object userObject) {
        PieChartModel pieChartModel = (PieChartModel) userObject;

        JDialog dialog = CentredDialog.create("Chart Editor", this);
        dialog.setBackground(Color.white);
        dialog.getContentPane().setBackground(Color.white);
        final ChartModelEditor chartModelEditor = new ChartModelEditor();
        dialog.getContentPane().setLayout(new MigLayout("fill", "[fill, grow]", "[fill, grow]"));
        dialog.setSize(400, 490);
        CentredDialog.centre(dialog, getParentRecursive());
        dialog.getContentPane().add(chartModelEditor);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                chartModelEditor.unbind();
            }
        });

        chartModelEditor.bind(controller, pieChartModel);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    private void showTableChartEditorDialog(Object userObject) {
        TableChartModel tableChartModel = (TableChartModel) userObject;

        JDialog dialog = CentredDialog.create("Chart Editor", this);
        dialog.setBackground(Color.white);
        dialog.getContentPane().setBackground(Color.white);
        final ChartModelEditor chartModelEditor = new ChartModelEditor();
        dialog.getContentPane().setLayout(new MigLayout("fill", "[fill, grow]", "[fill, grow]"));
        dialog.setSize(400, 490);
        CentredDialog.centre(dialog, getParentRecursive());
        dialog.getContentPane().add(chartModelEditor);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                chartModelEditor.unbind();
            }
        });

        chartModelEditor.bind(controller, tableChartModel);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    class TreeRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

            if (node == patternsNode) {
                setIcon(Icons.load("/icons/patterns.png"));
            } else if (node == chartingNode) {
                setIcon(Icons.load("/icons/charting.png"));
            } else if (node == aggregationsNode) {
                setIcon(Icons.load("/icons/MenuItem.png"));
            } else if (node == expressionsNode) {
                setIcon(Icons.load("/icons/MenuItem.png"));
            } else {
                Object userObject = node.getUserObject();
                if (userObject instanceof LineChartModel) {
                    setIcon(Icons.load("/icons/chart.png"));
                } else if (userObject instanceof PieChartModel) {
                    setIcon(Icons.load("/icons/piechart.png"));
                } else if (userObject instanceof TableChartModel) {
                    setIcon(Icons.load("/icons/table.png"));
                } else if (userObject instanceof ChartSeriesModel) {
                    setIcon(Icons.load("/icons/series.png"));
                } else if (userObject instanceof PatternModel) {
                    setIcon(Icons.load("/icons/pattern.png"));
                } else if (userObject instanceof PageModel) {
                    setIcon(Icons.load("/icons/chartpage.png"));
                }
            }

            return treeCellRendererComponent;

        }
    }

}
