package com.logginghub.logging.frontend.charting;

import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.charting.model.PageModel;
import com.logginghub.utils.DateFormatFactory;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewChartingView extends JPanel {
    private static final long serialVersionUID = 1L;
    private NewChartingController controller;
    private NewChartingModel model;
    private JTabbedPane tabbedPane;
    private Map<PageModel, PageView> counterparts = new HashMap<PageModel, PageView>();

    public NewChartingView() {
        setLayout(new MigLayout("", "[grow, fill]", "[grow, fill]"));
        tabbedPane = new JTabbedPane();
        add(tabbedPane);
    }

    public NewChartingController getController() {
        return controller;
    }
    
    public void bind(NewChartingController controller) {
        this.controller = controller;
        model = controller.getModel();

        model.getPages().addListenerAndNotifyCurrent(new ObservableListListener<PageModel>() {
            @Override public void onRemoved(PageModel t, int index) {
                removePage(t);
            }

            @Override public void onCleared() {
            }

            @Override public void onAdded(PageModel t) {
                createNewPage(t);
            }
        });
    }

    protected void removePage(PageModel t) {
        PageView pagePanel = counterparts.remove(t);
        tabbedPane.remove(pagePanel);

        // int foundIndex = -1;
        // int tabCount = tabbedPane.getTabCount();
        // for (int i = 0; i < tabCount; i++) {
        // String titleAt = tabbedPane.getTitleAt(i);
        // if (titleAt.equals(t.getName().get())) {
        // foundIndex = i;
        // break;
        // }
        // }
        //
        // if (foundIndex >= 0) {
        // tabbedPane.removeTabAt(foundIndex);
        // }
    }

    protected void createNewPage(PageModel t) {
        final PageView pagePanel = new PageView();
        counterparts.put(t, pagePanel);
        pagePanel.bind(controller, t);
        tabbedPane.add(t.getName().get(), pagePanel);

        final int index = tabbedPane.indexOfComponent(pagePanel);

        t.getName().addListener(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, final String newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        tabbedPane.setTitleAt(index, newValue);
                    }
                });

            }
        });
    }

    public Collection<PageView> getPageModelViews() {
        return counterparts.values();
    }

    public void saveChartData() {
        final File folder = new File("data/" + DateFormatFactory.getFileSafeOrdered(DateFormatFactory.utc).format(new Date()));
        folder.mkdirs();

        WorkerThread.execute("Data exporter", new Runnable() {
            public void run() {
                Collection<PageView> pageModelViews = getPageModelViews();
                for (PageView pageModelView : pageModelViews) {
                    Collection<LineChartView> chartViews = pageModelView.getChartViews();
                    for (LineChartView lineChartModelView : chartViews) {
                        lineChartModelView.saveChartData(folder);
                    }
                    
                    Collection<PieChartView> pieChartViews = pageModelView.getPieChartViews();
                    for (PieChartView pieChartModelView : pieChartViews) {
                        pieChartModelView.saveChartData(folder);
                    }

                    Collection<TableChartView> tableChartViews = pageModelView.getTableChartViews();
                    for (TableChartView tableChartView : tableChartViews) {
                        tableChartView.saveChartData(folder);
                    }
                }
            }
        });
    }

    public void saveChartImages() {
        final File folder = new File("images/" + DateFormatFactory.getFileSafeOrdered(DateFormatFactory.utc).format(new Date()));
        folder.mkdirs();
        WorkerThread.execute("Data exporter", new Runnable() {
            public void run() {
                Collection<PageView> pageModelViews = getPageModelViews();
                for (PageView pageModelView : pageModelViews) {
                    Collection<LineChartView> chartViews = pageModelView.getChartViews();
                    for (LineChartView lineChartModelView : chartViews) {
                        lineChartModelView.saveChartImages(folder);
                    }
                    
                    Collection<PieChartView> pieChartViews = pageModelView.getPieChartViews();
                    for (PieChartView pieChartModelView : pieChartViews) {
                        pieChartModelView.saveChartImages(folder);
                    }

                    Collection<TableChartView> tableChartViews = pageModelView.getTableChartViews();
                    for (TableChartView tableChartView : tableChartViews) {
                        tableChartView.saveChartImages(folder);
                    }
                }
            }
        });

    }

    public void clearChartData() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Collection<PageView> pageModelViews = getPageModelViews();
                for (PageView pageModelView : pageModelViews) {
                 
                    Collection<LineChartView> chartViews = pageModelView.getChartViews();
                    for (LineChartView lineChartModelView : chartViews) {
                        lineChartModelView.clearChartData();
                    }
                    
                    Collection<PieChartView> pieChartViews = pageModelView.getPieChartViews();
                    for (PieChartView pieChartModelView : pieChartViews) {
                        pieChartModelView.clearChartData();
                    }

                    Collection<TableChartView> tableChartViews = pageModelView.getTableChartViews();
                    for (TableChartView tableChartView : tableChartViews) {
                        tableChartView.clearChartData();
                    }
                }
            }
        });

    }

}
