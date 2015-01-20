package com.logginghub.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.ResourceUtils;


@SuppressWarnings("restriction") public class JAXBConfiguration {
    
    public static <T> T loadConfiguration(Class<T> t, String configurationPath) {
        try {
            InputStream resource = ResourceUtils.openStream(configurationPath);
            JAXBContext context = JAXBContext.newInstance(t);
            Unmarshaller um = context.createUnmarshaller();
            um.setEventHandler(new DefaultValidationEventHandler() {
                @Override public boolean handleEvent(ValidationEvent event) {
                    System.out.println(event);
                    return super.handleEvent(event);
                }
            });
			
            @SuppressWarnings("unchecked") T configuration = (T) um.unmarshal(new InputStreamReader(resource));
            return configuration;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from " + configurationPath, e);
        }
    }
    
    public static <T> void writeConfiguration(T t, String configurationPath) {
        try {
            JAXBContext context = JAXBContext.newInstance(t.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setEventHandler(new DefaultValidationEventHandler() {
                @Override public boolean handleEvent(ValidationEvent event) {
                    System.out.println(event);
                    return super.handleEvent(event);
                }
            });
            
            marshaller.marshal(t, new File(configurationPath));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to save configuration to " + configurationPath, e);
        }
    }

    public static <T> T loadConfigurationFromString(Class<T> t, String configurationText) {
        try {
            InputStream resource = new ByteArrayInputStream(configurationText.getBytes());
            JAXBContext context = JAXBContext.newInstance(t);
            Unmarshaller um = context.createUnmarshaller();
            um.setEventHandler(new DefaultValidationEventHandler() {
                @Override public boolean handleEvent(ValidationEvent event) {
                    System.out.println(event);
                    return super.handleEvent(event);
                }
            });
            
            @SuppressWarnings("unchecked") T configuration = (T) um.unmarshal(new InputStreamReader(resource));
            return configuration;
        }
        catch (Exception e) {
            throw new FormattedRuntimeException(e, "Failed to load configuration from string '{}'", configurationText);
        }
         
    }

    
}
