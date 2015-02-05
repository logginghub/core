package com.logginghub.logging;

import com.google.gson.Gson;
import com.logginghub.logging.messages.ReportDetails;
import com.logginghub.logging.messages.ReportExecuteResult;
import com.logginghub.logging.messages.ReportListResponse;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.Result;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 04/02/15.
 */
public class ReportsHelper {

    private static Logger logger = Logger.getLoggerFor(ReportsHelper.class);

    private final ReportsConfiguration reportsConfiguration;

    public ReportsHelper(String reportsConfigurationPath) {
        final String read = ResourceUtils.read(reportsConfigurationPath);
        Gson g = new Gson();
        reportsConfiguration = g.fromJson(read, ReportsConfiguration.class);
    }

    public Result<ReportExecuteResult> execute(String reportName) {

        Result<ReportExecuteResult> result;

        ReportsConfiguration.ReportConfiguration found = null;
        final List<ReportsConfiguration.ReportConfiguration> reports = reportsConfiguration.getReports();
        for (ReportsConfiguration.ReportConfiguration report : reports) {
            if (reportName.equals(report.getName())) {
                found = report;
                break;
            }
        }

        if (found == null) {
            result = Result.unsuccessful("No report found with name '{}'", reportName);
        } else {
            result = executeReport(found);
        }

        return result;
    }

    private Result<ReportExecuteResult> executeReport(ReportsConfiguration.ReportConfiguration found) {

        Result<ReportExecuteResult> result;

        List<String> commandsList = new ArrayList<String>();
        commandsList.add(found.getCommand());

        for (String argument : found.arguments) {
            commandsList.add(argument);
        }

        File workingDirectory;
        if (StringUtils.isNotNullOrEmpty(found.getWorkingDirectory())) {
            workingDirectory = new File(found.getWorkingDirectory());
        } else {
            workingDirectory = new File(new File("").getAbsolutePath());
        }

        String[] commands = commandsList.toArray(new String[commandsList.size()]);

        Map<String, String> environmentVaraibles = new HashMap<String, String>();

        if (found.isInheritEnvironment()) {
            environmentVaraibles.putAll(System.getenv());
        }

        environmentVaraibles.putAll(found.getEnvironment());

        String[] environment = new String[environmentVaraibles.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : environmentVaraibles.entrySet()) {
            environment[index++] = StringUtils.format("{}={}", entry.getKey(), entry.getValue());
        }

        try {
            final Process process = Runtime.getRuntime().exec(commands, environment, workingDirectory);

            final StringUtils.StringUtilsBuilder output = new StringUtils.StringUtilsBuilder();

            final WorkerThread inputGobbler = gobble(process.getInputStream(), "sysout", output);
            final WorkerThread errorGobbler = gobble(process.getErrorStream(), "syserr", output);

            try {
                int returnCode = process.waitFor();

                inputGobbler.join();
                errorGobbler.join();

                FileUtils.closeQuietly(process.getInputStream());
                FileUtils.closeQuietly(process.getErrorStream());

                ReportExecuteResult response = new ReportExecuteResult();
                response.setReturnCode(returnCode);

                final String outputString = output.toString();
                response.setResult(outputString);

                result = Result.successful(response);

            } catch (InterruptedException e) {
                result = Result.failed(e,
                        "Failed to execute report process - thread was interupted before the process terminated : {} : commands '{}' environment '{}' items workingDirectory '{}'",
                        e.getMessage(),
                        Arrays.toString(commands),
                        environment.length,
                        workingDirectory.getAbsolutePath());
            }

        } catch (IOException e) {
            result = Result.failed(e,
                    "Failed to execute report process : {} : commands '{}' environment '{}' items workingDirectory '{}'",
                    e.getMessage(),
                    Arrays.toString(commands),
                    environment.length,
                    workingDirectory.getAbsolutePath());
        }


        return result;
    }

    private WorkerThread gobble(final InputStream stream, final String type, final StringUtils.StringUtilsBuilder builder) {
        return WorkerThread.execute("ReportsHelper-Gobbler-" + type, new Runnable() {
            @Override public void run() {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        synchronized (builder) {
                            builder.appendLine(line);
                        }
                    }
                } catch (IOException e) {
                }
            }
        });
    }

    public ReportListResponse getReportList() {

        ReportListResponse response = new ReportListResponse();

        final List<ReportsConfiguration.ReportConfiguration> reports = reportsConfiguration.getReports();
        for (ReportsConfiguration.ReportConfiguration report : reports) {
            ReportDetails details = new ReportDetails();
            details.setName(report.getName());
            response.getReportDetails().add(details);
        }

        return response;
    }

    public final static class ReportsConfiguration {

        private List<ReportConfiguration> reports = new ArrayList<ReportConfiguration>();

        public List<ReportConfiguration> getReports() {
            return reports;
        }

        public final static class ReportConfiguration {

            private String name;
            private String command;
            private String workingDirectory;
            private boolean inheritEnvironment = true;

            private List<String> arguments = new ArrayList<String>();

            private Map<String, String> environment = new HashMap<String, String>();

            public boolean isInheritEnvironment() {
                return inheritEnvironment;
            }

            public void setInheritEnvironment(boolean inheritEnvironment) {
                this.inheritEnvironment = inheritEnvironment;
            }

            public String getCommand() {
                return command;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setCommand(String command) {
                this.command = command;
            }

            public String getWorkingDirectory() {
                return workingDirectory;
            }

            public Map<String, String> getEnvironment() {
                return environment;
            }

            public void setWorkingDirectory(String workingDirectory) {
                this.workingDirectory = workingDirectory;
            }

            public List<String> getArgument() {
                return arguments;
            }
        }

    }
}

