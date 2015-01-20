package com.logginghub.logging.frontend.modules;

import com.logginghub.logging.frontend.brainscan.BrainScanController;
import com.logginghub.logging.frontend.brainscan.BrainScanViewPanel;
import com.logginghub.logging.frontend.brainscan.CountingTreeMap;
import com.logginghub.logging.frontend.brainscan.StrobeRequestPanel;
import com.logginghub.logging.frontend.modules.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.modules.configuration.StackTraceViewConfiguration;
import com.logginghub.logging.frontend.services.EnvironmentNotificationService;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.messages.*;
import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileVisitor;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Inject;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ProxyServiceDiscovery;
import com.logginghub.utils.module.ServiceDiscovery;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class StackTraceViewModule implements Module<StackTraceViewConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(StackTraceViewModule.class);
    private StackTraceViewConfiguration configuration;
    private EnvironmentMessagingService messagingService;
    private EnvironmentNotificationService environmentNotificationService;
    private BrainScanViewPanel brainScanViewPanel = new BrainScanViewPanel();
    private BrainScanController controller;
    private String layout;

    private JPanel outputPanel = new JPanel();

    private String name;
    private LayoutService layoutService;
    private boolean requestsSent = false;

    @Override public void configure(StackTraceViewConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        messagingService = discovery.findService(EnvironmentMessagingService.class, configuration.getEnvironmentRef());
        environmentNotificationService = discovery.findService(EnvironmentNotificationService.class,
                                                               configuration.getEnvironmentRef());

        layoutService = discovery.findService(LayoutService.class);
        initialise();
    }

    public void initialise() {
        sendInitialRequests();
        brainScanViewPanel.setName("Stack analysis");
        layoutService.add(brainScanViewPanel, layout);

//        StrobeRequestPanel strobeRequestPanel = new StrobeRequestPanel();
//        strobeRequestPanel.getRequestStream().addDestination(new Destination<StackStrobeRequest>() {
//            @Override public void send(StackStrobeRequest stackStrobeRequest) {
////                messagingService.send(stackStrobeRequest);
//            }
//        });
//
//        JPanel resultsPanel = new JPanel(new MigLayout("fill,debug", "[fill, grow]", "[fill, grow]"));
//        JLabel comp = new JLabel("<html><h1>I'm some text</h1></html>");
//
//        resultsPanel.add(comp);
//
//        outputPanel.setLayout(new MigLayout("fill,debug", "[fill, grow]", "[fill, grow]"));
//
//        outputPanel.add(strobeRequestPanel, "cell 0 0");
//        outputPanel.add(resultsPanel, "cell 1 0");
//
//        layoutService.add(outputPanel, layout);
//
//        sendInitialRequests();
    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Inject
    public void setEnvironmentNotificationService(EnvironmentNotificationService environmentNotificationService) {
        this.environmentNotificationService = environmentNotificationService;
    }

    @Inject public void setMessagingService(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Override public void start() {
        environmentNotificationService.addListener(new EnvironmentNotificationListener() {
            @Override public void onHubConnectionEstablished(HubConfiguration hubConfiguration) {
            }

            @Override public void onHubConnectionLost(HubConfiguration hubConfiguration) {
            }

            @Override public void onEnvironmentConnectionLost() {
            }

            @Override public void onEnvironmentConnectionEstablished() {
                sendInitialRequests();
            }

            @Override public void onTotalEnvironmentConnectionEstablished() {
            }

            @Override public void onTotalEnvironmentConnectionLost() {
            }
        });

        if (environmentNotificationService.isEnvironmentConnectionEstablished()) {
            sendInitialRequests();
        }

        controller = new BrainScanController(messagingService);
        brainScanViewPanel.bind(controller);

        // countingTreeMap.count("c:/").count("temp/").count("file2", 20);

        // countingTreeMap.count("c:/", 0).count("temp/", 30);
        // countingTreeMap.count("c:/", 0).count("foo/", 30);
        // countingTreeMap.count("d:/", 20);
        // countingTreeMap.count("c:/").count("docs/").count("doc1", 30);
        // countingTreeMap.count("d:/").count("foo").count("moo", 50);

        if (false) {
            WorkerThread.execute("Scanner", new Runnable() {

                @Override public void run() {

                    FileUtils.visitChildrenRecursively(new File("c:\\"), new FileVisitor() {
                        @Override public void visitFile(File file) {

                            if (file.isFile()) {
                                List<File> parents = new ArrayList<File>();
                                File pointer = file.getParentFile();
                                while (pointer != null) {
                                    parents.add(0, pointer);
                                    pointer = pointer.getParentFile();
                                }
                                parents.add(file);

                                CountingTreeMap countingTreeMap = controller.getCountingTreeMap();
                                synchronized (countingTreeMap) {
                                    CountingTreeMap mapPointer = countingTreeMap;
                                    for (File target : parents) {
                                        boolean isRoot = isRoot(target);
                                        String name;
                                        if (isRoot) {
                                            name = target.toString();
                                        } else {
                                            name = target.getName();
                                        }

                                        mapPointer = mapPointer.get(name);
                                    }

                                    mapPointer.count(file.length());
                                }
                            }
                        }

                    });

                    System.out.println("DONE");

                }
            });
        }

        // TimerUtils.every("REpainters", 100, TimeUnit.MILLISECONDS, new Runnable() {
        // @Override public void run() {
        // brainScanViewPanel.getTreeMapComponent().repaint();
        // }
        // });

    }

    private void sendInitialRequests() {

        // Make sure we only do this once
        synchronized (this) {
            if (requestsSent == true) {
                return;
            }

            requestsSent = true;
        }

        logger.info("Environment connection established, subscribing to snapshot updates");

        // TODO : refactor these out to fields
        final Set<String> hideThreads = new HashSet<String>();
        hideThreads.add("Reference Handler");
        hideThreads.add("LoggingHub-stackCaptureThread");
        hideThreads.add("LoggingHub-strobeExecutor");
        hideThreads.add("Attach Listener");
        hideThreads.add("DestroyJavaVM");

        final Set<String> hideClasses = new HashSet<String>();
        hideClasses.add("java.net.PlainSocketImpl");
        hideClasses.add("java.net.SocketInputStream");

        messagingService.subscribe(Channels.stackSnapshots, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                StackSnapshot snapshot = (StackSnapshot) t.getPayload();
                processSnapshot(hideThreads, hideClasses, snapshot);
            }

            private void processSnapshot(final Set<String> hideThreads,
                                         final Set<String> hideClasses,
                                         StackSnapshot snapshot) {
                StackTrace[] traces = snapshot.getTraces();
                for (StackTrace stackTrace : traces) {

                    // TODO : move more into the controller
                    controller.processStackTrace(snapshot, stackTrace);

                    String threadState = stackTrace.getThreadState();
                    if (threadState.equals("RUNNABLE")) {

                        if (!hideThreads.contains(stackTrace.getThreadName())) {
                            StackTraceItem[] items = stackTrace.getItems();
                            if (items.length > 0) {

//                                logger.info("{}", Arrays.toString(items));

                                boolean onlyTop = false;
                                if (onlyTop) {

                                    StackTraceItem focus = items[0];
                                    // System.out.println(focus);

                                    // for (StackTraceItem stackTraceItem : items) {

                                    String className = focus.getClassName();
                                    if (!hideClasses.contains(className)) {
                                        String[] split = className.split("\\.");

                                        CountingTreeMap countingTreeMap = controller.getCountingTreeMap();
                                        CountingTreeMap mapPointer = countingTreeMap;
                                        for (String string : split) {
                                            mapPointer = mapPointer.get(string);
                                        }

                                        mapPointer.count(focus.getMethodName(), 1);
                                    }
                                    // }
                                } else {
                                    for (StackTraceItem stackTraceItem : items) {

                                        String className = stackTraceItem.getClassName();
                                        if (!hideClasses.contains(className)) {
                                            String[] split = className.split("\\.");

                                            CountingTreeMap countingTreeMap = controller.getCountingTreeMap();
                                            CountingTreeMap mapPointer = countingTreeMap;
                                            for (String string : split) {
                                                mapPointer = mapPointer.get(string);
                                            }

                                            mapPointer.count(stackTraceItem.getMethodName(), 1);
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        });

    }

    @Override public void stop() {
    }

    private boolean isRoot(File target) {
        boolean isRoot = false;
        File[] listRoots = File.listRoots();
        for (File root : listRoots) {
            if (target.equals(root)) {
                isRoot = true;
                break;
            }
        }
        return isRoot;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                MainFrameModule frame = new MainFrameModule();
                frame.setName("Stack Viewer");

                //            MenuBarModule menuBar = new MenuBarModule();
                //            menuBar.setFrameService(frame);
                //            menuBar.setLayoutService(frame);
                //
                //            TabbedPaneModule tab = new TabbedPaneModule();
                //            tab.setLayoutService(frame);

                EnvironmentModule environmentModule = new EnvironmentModule();
                EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration();
                environmentConfiguration.getHubs().add(new HubConfiguration("localhost", 15000));
                environmentConfiguration.getHubs().add(new HubConfiguration("localhost", 15001));
                environmentModule.configure(environmentConfiguration, new ProxyServiceDiscovery());

                StackTraceViewModule stack = new StackTraceViewModule();
                stack.setLayoutService(frame);
                stack.setEnvironmentNotificationService(environmentModule);
                stack.setMessagingService(environmentModule);

                frame.initialise();
                //            menuBar.initialise();
                //            tab.initialise();
                stack.initialise();

                frame.start();
                //            menuBar.start();
                //            tab.start();
                stack.start();
            }
        });
    }
}
