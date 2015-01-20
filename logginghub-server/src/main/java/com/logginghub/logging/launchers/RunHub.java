package com.logginghub.logging.launchers;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.utils.MainUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.SystemErrStream;

public class RunHub implements Closeable {

    private static final Logger logger = Logger.getLoggerFor(RunHub.class);
    private LoggingContainer container;

    public static void main(String[] args) throws Exception {
        mainInternal(args);
    }

    public static RunHub mainInternal(String... args) throws Exception {
        Logger.setLevelFromSystemProperty();
        SystemErrStream.gapThreshold = 1100;       

        String configurationFilename = MainUtils.getStringArgument(args, 0, "hub.xml");
        Out.out("config = {}", configurationFilename);
        boolean configSearch = MainUtils.getBooleanArgument(args, 1, false);

        File configurationFile = new File(configurationFilename);
        if (configSearch) {
            logger.info("Original configuration is here '{}', but the config parent search feature has been turned on", configurationFilename);
            File parentConfig = searchParentFolders(configurationFile);
            if (parentConfig != null) {
                configurationFile = parentConfig;
            }
        }

        if (configurationFile.exists()) {
            logger.info("Loading configuration from {}", configurationFile.getAbsolutePath());
            RunHub hub = new RunHub();
            hub.container = LoggingContainer.createContainer(configurationFile.getAbsolutePath());
            return hub;
        }
        else {
            logger.warning("Failed to find configuration file '{}'", configurationFile.getAbsolutePath());
            return null;
        }
    }

    private static File searchParentFolders(File configurationFile) {

        String filename = configurationFile.getName();
        File folder = configurationFile.getParentFile();

        try {
            folder = folder.getCanonicalFile();
        }
        catch (IOException e) {
            // Probably means this is a dodgy folder
            logger.warning(e,
                           "Failed to turn the configuration path provided ({}) into a real folder, can you double check its a legitimate folder?",
                           configurationFile.getAbsolutePath());
        }

        File found = configurationFile;
        if (folder != null) {
            File parent = folder.getParentFile();
            found = recursiveSearch(filename, parent);
        }

        return found;

    }

    private static File recursiveSearch(String filename, File parent) {
        logger.debug("Checking folder '{}' for a config file called '{}'", parent.getAbsolutePath(), filename);
        File found;
        File attempt = new File(parent, filename);
        if (attempt.exists()) {
            found = attempt;
            logger.debug("A parent configuration has been found here '{}'", found.getAbsolutePath());
        }
        else {

            File grandParent = parent.getParentFile();
            if (grandParent != null) {
                found = recursiveSearch(filename, grandParent);
            }
            else {
                found = null;
            }
        }

        return found;

    }

    @Override public void close() throws IOException {
        if(container != null) {
            container.stop();
        }
    }

    public <T> T getFirst(Class<T> clazz) {
        return (T)container.getFirst(clazz);
    }

}
