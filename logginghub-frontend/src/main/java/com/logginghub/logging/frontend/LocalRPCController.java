package com.logginghub.logging.frontend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.logginghub.logging.filters.TimeFieldFilter;
import com.logginghub.logging.frontend.model.CustomDateFilterModel;
import com.logginghub.logging.frontend.model.CustomQuickFilterModel;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.LoggingFrontendModel;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Opens a locally bound port that accepts JSON formatted messages which can control aspects of the frontend, including settings filter values.
 */
public class LocalRPCController implements Asynchronous {
    private static final Logger logger = Logger.getLoggerFor(LocalRPCController.class);
    private final int port;
    private final LoggingMainPanel loggingMainPanel;
    private final LoggingFrontendModel model;
    private ServerSocket serverSocket;

    public LocalRPCController(LoggingMainPanel loggingMainPanel, LoggingFrontendModel model) {
        this.loggingMainPanel = loggingMainPanel;
        this.model = model;
        this.port = model.getLocalRPCPort().get();
    }

    @Override
    public synchronized void start() {
        stop();
        logger.info("Starting local RPC controller");

        WorkerThread.executeDaemonOngoing("LocalRPCController", new Runnable() {
            @Override
            public void run() {

                try {
                    ensureBound();
                } catch (IOException e) {
                    logger.warn(e, "Failed to bind local RPC port {}, will retry", port);
                }

                try {
                    acceptConnection();
                } catch (IOException e) {
                    logger.warn(e, "Failed to accept connection from local RPC port {}, will close socket and retry", port);
                    close();
                }

            }
        });


    }

    private synchronized void ensureBound() throws IOException {
        if (serverSocket == null) {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("localhost", port));
            logger.info("Bound to local RPC port {}", port);
        }
    }

    private void acceptConnection() throws IOException {

        logger.info("Waiting for new RPC connection...");
        final Socket socket = serverSocket.accept();

        WorkerThread.executeDaemon("LocalRPCController-connectionHandler", new Runnable() {
            @Override
            public void run() {

                logger.info("New RPC connection established, waiting for data...");

                try {
                    InputStream inputStream = socket.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        processLine(line);
                    }

                } catch (IOException e) {
                    logger.info(e, "RPC connection terminated");
                }

            }
        });

    }

    private synchronized void close() {
        if (serverSocket != null) {
            FileUtils.closeQuietly(serverSocket);
            serverSocket = null;
        }
    }

    private void processLine(String line) {
        logger.info("Processing incoming RPC line : '{}'", line);

        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(line);

        JsonObject asJsonObject = parse.getAsJsonObject();
        Set<Entry<String, JsonElement>> entries = asJsonObject.entrySet();
        for (Entry<String, JsonElement> entry : entries) {

            String environment = entry.getKey();
            EnvironmentModel environmentModel = model.getEnvironment(environment);

            JsonObject environmentSettings = entry.getValue().getAsJsonObject();

            JsonElement filter = environmentSettings.get("filters");
            if (filter != null) {

                JsonObject filterSettings = filter.getAsJsonObject();
                for (Entry<String, JsonElement> filterSettingEntry : filterSettings.entrySet()) {

                    String filterLabel = filterSettingEntry.getKey();
                    String value = filterSettingEntry.getValue().getAsString();

                    ObservableList<CustomQuickFilterModel> customFilters = environmentModel.getCustomFilters();
                    for (CustomQuickFilterModel customFilter : customFilters) {
                        if (customFilter.getLabel().get().equals(filterLabel)) {
                            logger.info("Setting custom filter label '{}' to new value '{}' for environment '{}'",
                                        customFilter.getLabel().get(),
                                        value,
                                        environment);
                            customFilter.getValue().set(value);
                        }
                    }

                    ObservableList<CustomDateFilterModel> customDateFilters = environmentModel.getCustomDateFilters();
                    for (CustomDateFilterModel customDateFilter : customDateFilters) {
                        if (customDateFilter.getLabel().get().equals(filterLabel)) {
                            logger.info("Setting custom date filter label '{}' to new value '{}' for environment '{}'",
                                        customDateFilter.getLabel().get(),
                                        value,
                                        environment);

                            if(value.trim().isEmpty()) {
                                customDateFilter.getValue().set(TimeFieldFilter.ACCEPT_ALL);
                            }else {
                                long time = TimeUtils.parseTime(value);
                                customDateFilter.getValue().set(time);
                            }
                        }
                    }

                }

            }

        }

    }

    @Override
    public synchronized void stop() {
        logger.info("Stopping local RPC controller");
    }
}
