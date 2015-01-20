package com.logginghub.logging.frontend.modules;

import java.io.File;

import com.logginghub.logging.frontend.modules.configuration.FrontendContainerConfiguration;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.module.ClassResolver;
import com.logginghub.utils.module.Container;
import com.logginghub.utils.module.Container2;
import com.logginghub.utils.module.PeerServiceDiscovery;
import com.logginghub.utils.module.ResolutionNotPossibleException;

public class FrontendContainer extends Container<FrontendContainerConfiguration> {

    public static FrontendContainer fromConfiguration(File file) throws ResolutionNotPossibleException {
        return fromConfiguration(file.getAbsolutePath());
    }

    public static FrontendContainer fromConfigurationOld(String filename) {
        FrontendContainerConfiguration loadConfiguration = JAXBConfiguration.loadConfiguration(FrontendContainerConfiguration.class, filename);

        FrontendContainer container = new FrontendContainer();
        try {
            container.configure(loadConfiguration, new PeerServiceDiscovery(container));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        container.start();
        return container;
    }
    
    public static FrontendContainer fromConfiguration(String filename) throws ResolutionNotPossibleException {
        
        try {
            Container2 container = new Container2();
            container.addClassResolver(new ClassResolver() {
                @Override public String resolve(String name) {
                    return "com.logginghub.logging.frontend.modules." + StringUtils.capitalise(name) + "Module";
                }
            });
            container.fromXmlString(FileUtils.read(new File(filename)));
            container.dump();
            container.initialise();
            container.start();
        }
        catch (ResolutionNotPossibleException e) {
            throw e;
        }
        
        // TODO : sort this out
        return null;
    }

    public static void main(String[] args)  {

        if (args.length != 1) {
            System.out.println("Please provide a configuarion file as the first argument.");
        }
        String filename = args[0];

        try {
            fromConfiguration(filename);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
