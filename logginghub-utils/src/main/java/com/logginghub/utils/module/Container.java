package com.logginghub.utils.module;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Pair;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.StacktraceUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.logging.Logger;

public class Container<T> implements Module<T> {

    private static final Logger logger = Logger.getLoggerFor(Container.class);
    private T configuration;

    private List<Module<?>> modules = new ArrayList<Module<?>>();
    private List<Module<?>> successfullyConfiguredModules = new ArrayList<Module<?>>();

    public List<Module<?>> getModules() {
        return modules;
    }

    protected List<Module<?>> getSuccessfullyConfiguredModules() {
        return successfullyConfiguredModules;
    }

    public void configure(T configuration, ServiceDiscovery serviceDiscovery) {
        this.configuration = configuration;

        this.modules.clear();

        List<String> warnings = new ArrayList<String>();
        List<Throwable> actualWarnings = new ArrayList<Throwable>();

        List<Pair<Object, Object>> modules = new ArrayList<Pair<Object, Object>>();

        Method[] methods = configuration.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                if (method.getReturnType().isAssignableFrom(List.class)) {
                    logger.finest("Found list accessor '{}'", method.getName());
                    try {
                        List<?> result = (List<?>) method.invoke(configuration, (Object[]) null);
                        Iterator<?> iterator = result.iterator();
                        while (iterator.hasNext()) {
                            Object object = (Object) iterator.next();

                            Class<? extends Object> objectClass = object.getClass();
                            Configures annotation = objectClass.getAnnotation(Configures.class);
                            if (annotation != null) {
                                Class<?> value = annotation.value();

                                logger.fine("Found configuration object '{}' which instantiates '{}'", objectClass.getName(), value.getName());
                                Object moduleInstance = ReflectionUtils.instantiate(value);

                                if (moduleInstance instanceof Module<?>) {
                                    Module<?> module = (Module<?>) moduleInstance;
                                    this.modules.add(module);
                                    modules.add(new Pair(object, module));
                                }
                                else {
                                    warnings.add(StringUtils.format("Instance of '{}' - doesn't implement Module<?>", value.getSimpleName()));
                                }
                            }
                            else {
                                warnings.add(StringUtils.format("Found configuration object '{}' but it didn't have the @Configures annotation",
                                                                objectClass.getName()));
                            }

                        }

                    }
                    catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (!warnings.isEmpty()) {
            StringUtilsBuilder sb = new StringUtilsBuilder();
            for (String string : warnings) {
                logger.warn(string);
                sb.appendLine(string);
            }

            throw new FormattedRuntimeException("There were problems loading the configuration : {}{}", StringUtils.newline, sb.toString());

        }

        if (this.modules.size() == 0) {
            throw new FormattedRuntimeException("Failed to load any modules, please check your configuration!");
        }

        List<Exception> problems = new ArrayList<Exception>();

        while (modules.size() > 0) {
            boolean progressedAtLeastOne = false;

            // Reset the problem collections, fingers crossed this attempt will be clean!
            warnings.clear();
            actualWarnings.clear();
            problems.clear();

            Iterator<Pair<Object, Object>> iterator = modules.iterator();
            while (iterator.hasNext()) {
                Pair<Object, Object> pair = (Pair<Object, Object>) iterator.next();

                Object moduleConfiguration = pair.getA();
                Object moduleInstance = pair.getB();

                try {
                    logger.fine("Attempting configuration of '{}'", moduleInstance.getClass().getSimpleName());
                    ReflectionUtils.invoke(moduleInstance, "configure", moduleConfiguration, serviceDiscovery);
                    iterator.remove();
                    progressedAtLeastOne |= true;
                    logger.info("Successfully configured module '{}'", moduleInstance.getClass().getSimpleName());
                    successfullyConfiguredModules.add((Module<?>) moduleInstance);
                }
                catch (Exception e) {

                    logger.fine(e, "Failed in this attempt to configure '{}' : {}", moduleInstance.getClass().getSimpleName());

                    boolean isWarning = false;
                    if (e instanceof RuntimeException) {
                        if (e.getCause() instanceof InvocationTargetException) {
                            InvocationTargetException invocationTargetException = (InvocationTargetException) e.getCause();
                            Throwable cause = invocationTargetException.getCause();
                            actualWarnings.add(cause);
                            if (e.getCause().getCause() instanceof FormattedRuntimeException) {
                                warnings.add(StacktraceUtils.getRootMessage(e));
                                isWarning = true;
                            }
                        }
                    }

                    if (!isWarning) {
                        problems.add(e);
                        actualWarnings.add(e);
                    }
                }
            }

            if (!progressedAtLeastOne) {
                break;
            }
        }

        if (!problems.isEmpty()) {
            StringUtilsBuilder sb = new StringUtilsBuilder();
            logger.severe("Failed to configure dependencies - exception follow:");
            for (Exception exception : problems) {
                logger.severe(exception, exception.getMessage());
                sb.appendLine(StacktraceUtils.combineMessages(exception));
            }
            throw new FormattedRuntimeException("There were serious problems loading the configuration : {}{}", StringUtils.newline, sb.toString());
        }

        if (!warnings.isEmpty()) {
            StringUtilsBuilder sb = new StringUtilsBuilder();
            for (String string : warnings) {
                sb.appendLine(string);
            }

            throw new FormattedRuntimeException("There were problems loading the configuration : {}{}", StringUtils.newline, sb.toString());

        }

        logger.info("Successfully configured all modules");
    }

    public boolean isConfigured(Module<?> module) {
        return successfullyConfiguredModules.contains(module);
    }

    // TODO : put these in a base class for aggregate modules?
    public void start() {
        for (Module<?> module : modules) {
            module.start();
        }
    }

    public void stop() {
        for (Module<?> module : modules) {
            module.stop();
        }
    }

    @SuppressWarnings("unchecked") public static <T extends Module<Y>, Y> T fromConfiguration(Object configuration) {

        Module<Y> module;

        Class<? extends Object> objectClass = configuration.getClass();
        Configures annotation = objectClass.getAnnotation(Configures.class);
        if (annotation != null) {
            Class<?> value = annotation.value();

            logger.fine("Found configuration object '{}' which instantiates '{}'", objectClass.getName(), value.getName());
            Object moduleInstance = ReflectionUtils.instantiate(value);

            if (moduleInstance instanceof Module<?>) {
                module = (Module<Y>) moduleInstance;
                Y config = (Y) configuration;
                module.configure(config, new ProxyServiceDiscovery());
            }
            else {
                throw new FormattedRuntimeException("Instance of '{}' - doesn't implement Module<?>", value.getSimpleName());
            }
        }
        else {
            throw new FormattedRuntimeException("Configuration object '{}' doesn't have the @Configures annotation", objectClass.getName());
        }

        return (T) module;
    }

    /**
     * @deprecated This isn't a good idea, as you can't control the return values from dependent
     *             services via the proxy.
     * @param configuration
     * @param clazz
     * @return
     */
    @Deprecated @SuppressWarnings("unchecked") public static <T extends Module<Y>, Y> T fromConfiguration(Object configuration, Class<T> clazz) {

        Module<Y> module;

        Class<? extends Object> objectClass = configuration.getClass();
        Configures annotation = objectClass.getAnnotation(Configures.class);
        if (annotation != null) {
            Class<?> value = annotation.value();

            logger.fine("Found configuration object '{}' which instantiates '{}'", objectClass.getName(), value.getName());
            Object moduleInstance = ReflectionUtils.instantiate(value);

            if (moduleInstance instanceof Module<?>) {
                module = (Module<Y>) moduleInstance;
                Y config = (Y) configuration;
                module.configure(config, new ProxyServiceDiscovery());
            }
            else {
                throw new FormattedRuntimeException("Instance of '{}' - doesn't implement Module<?>", value.getSimpleName());
            }
        }
        else {
            throw new FormattedRuntimeException("Configuration object '{}' doesn't have the @Configures annotation", objectClass.getName());
        }

        return (T) module;
    }

    // public void setHackIncludeModule(Module<?> hackModule) {
    // this.hackModule = hackModule;
    // }

    public Module<?> getFirst(Class<?> clazz) {
        Module<?> found = null;
        for (Module<?> module : modules) {
            if (module.getClass().equals(clazz)) {
                found = module;
                break;
            }
        }
        return found;
    }

    public void setExternallyConfigured(Module<?> module) {
        successfullyConfiguredModules.add(module);
    }
}
