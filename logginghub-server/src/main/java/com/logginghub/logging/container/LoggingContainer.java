package com.logginghub.logging.container;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logginghub.logging.transaction.configuration.LoggingContainerConfiguration;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Container;
import com.logginghub.utils.module.PeerServiceDiscovery;
import com.logginghub.utils.module.ServiceDiscovery;

public class LoggingContainer extends Container<LoggingContainerConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(LoggingContainer.class);
    private LoggingContainerConfiguration configuration;

    public static void main(String[] args) {

        // Logger.setLevel(Container.class, Logger.fine);
        if (args.length > 0) {
            String resourcePath = args[0];
            createContainer(resourcePath);
        }
        else {
            System.err.println("You must provide the resource path to a valid container configuration xml file as the first parameter");
        }

    }

    public static LoggingContainer createContainer(String resourcePath) {

        String contents = ResourceUtils.read(resourcePath);

        contents = preprocess(contents);

        LoggingContainerConfiguration configuration = JAXBConfiguration.loadConfigurationFromString(LoggingContainerConfiguration.class, contents);

        LoggingContainer container = new LoggingContainer();
        ServiceDiscovery serviceDiscovery = new PeerServiceDiscovery(container);

        container.configure(configuration, serviceDiscovery);
        container.start();

        return container;
    }

    private static String preprocess(String contents) {

        StringBuilder builder = new StringBuilder();

        String pattern = "<import file=(.*)/>";
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(contents);

        int position = 0;
        while (matcher.find(position)) {

            int startOfReplacement = matcher.start();

            builder.append(contents.substring(position, startOfReplacement));

            String importFile = matcher.group(1);
            importFile = StringUtils.unquote(importFile);

            String importContent = ResourceUtils.read(importFile);
            builder.append(importContent);

            position = matcher.end();
        }

        // Bolt on the last bit
        builder.append(contents.substring(position, contents.length()));

        String result = builder.toString();
        return result;
    }

    public static LoggingContainer fromFile(File file) {
        LoggingContainerConfiguration configuration = LoggingContainerConfiguration.fromResource(file.getAbsolutePath());
        LoggingContainer container = new LoggingContainer();
        container.configure(configuration, new PeerServiceDiscovery(container));
        return container;
    }

    public static LoggingContainer fromFile(File file, ServiceDiscovery parentDiscovery) {
        LoggingContainerConfiguration configuration = LoggingContainerConfiguration.fromResource(file.getAbsolutePath());
        LoggingContainer container = new LoggingContainer();
        container.configure(configuration, new PeerServiceDiscovery(container, parentDiscovery));
        return container;
    }

    

}
