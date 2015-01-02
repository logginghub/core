package com.logginghub.utils.module;

import java.util.List;

import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.NamedModule;
import com.logginghub.utils.logging.Logger;

public class PeerServiceDiscovery implements ServiceDiscovery {

    private Container container;

    private static final Logger logger = Logger.getLoggerFor(PeerServiceDiscovery.class);
    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(ExceptionPolicy.Policy.RethrowOnAny);

    private ServiceDiscovery parentDiscovery;

    private boolean allowContainerToProvider = false;

    public PeerServiceDiscovery(Container container, boolean allowContainerToProvide, ServiceDiscovery parentDiscovery) {
        this.container = container;
        this.allowContainerToProvider = allowContainerToProvide;
        this.parentDiscovery = parentDiscovery;
    }

    public PeerServiceDiscovery(Container container) {
        this(container, false, null);
    }

    public PeerServiceDiscovery(Container container, ServiceDiscovery parentDiscovery) {
        this(container, false, parentDiscovery);
    }

    public ExceptionPolicy getExceptionPolicy() {
        return exceptionPolicy;
    }

    public void setExceptionPolicy(ExceptionPolicy exceptionPolicy) {
        this.exceptionPolicy = exceptionPolicy;
    }

    @SuppressWarnings("unchecked") public <T> T findService(Class<T> type, String name) {

        T found = null;

        if (found == null && allowContainerToProvider) {
            if (type.isInstance(container)) {
                if (name != null && name.length() > 0) {
                    if (container instanceof NamedModule) {
                        NamedModule<?> namedModule = (NamedModule<?>) container;
                        String moduleName = namedModule.getName();
                        if (moduleName.equals(name)) {
                            found = (T) container;
                        }

                    }
                }
                else {
                    // Fine, no name provided
                    found = (T) container;
                }
            }
        }

        if (found == null) {
            List<Module<?>> modules = container.getModules();
            for (Module<?> module : modules) {

                if (type.isInstance(module)) {
                    if (name != null && name.length() > 0) {
                        if (module instanceof NamedModule) {
                            NamedModule<?> namedModule = (NamedModule<?>) module;
                            String moduleName = namedModule.getName();
                            if (moduleName == null) {
                                // TODO : commenting this warning out, because of the container
                                // initiailisation order some modules might not have been configured
                                // even though they are in the module list :/
//                                logger.warn("Null module name found for module type '{}'", namedModule.getClass().getName());
                            }
                            else {
                                if (moduleName.equals(name)) {
                                    found = (T) module;
                                    break;
                                }
                            }

                        }
                    }
                    else {
                        // Fine, no name provided
                        found = (T) module;
                        break;
                    }
                }
            }
        }

        if (found == null && parentDiscovery != null) {
            found = parentDiscovery.findService(type, name);
        }

        if (found == null) {
            exceptionPolicy.handle("Failed to find an instance providing '{}' in this container", type.getName());
        }else{
            if(!container.isConfigured((Module) found)) {
                throw new RuntimeException("Module '" + found.getClass().getSimpleName() + " 'hasn't been configured yet, and cannot provide this service yet");
            }
        }

        return found;

    }

    public <T> T findService(Class<T> type) {
        return findService(type, (String) null);
    }

    public <T> T findService(Class<T> type, Class<?> generic) {

        T found = null;
        List<Module<?>> modules = container.getModules();
        for (Module<?> module : modules) {
            if (type.isInstance(module)) {
                Provides annotation = module.getClass().getAnnotation(Provides.class);
                if (annotation != null) {
                    Class<?>[] annotationGenericType = annotation.value();
                    for (Class<?> class1 : annotationGenericType) {
                        if (generic.equals(class1)) {
                            found = (T) module;
                            break;
                        }
                    }

                    if (found != null) {
                        break;
                    }

                }

            }
        }

        if (found == null && parentDiscovery != null) {
            found = parentDiscovery.findService(type, generic);
        }

        if (found == null) {
            exceptionPolicy.handle("Failed to find an instance providing '{}' with generic type '{}' in this container",
                                   type.getName(),
                                   generic.getName());
        }

        return found;
    }

    public <T> T findService(Class<T> type, Class<?> generic, String name) {

        T found = null;
        if (name == null) {
            found = findService(type, generic);
        }
        else {

            List<Module<?>> modules = container.getModules();
            for (Module<?> module : modules) {
                if (type.isInstance(module)) {
                    if (module instanceof NamedModule) {
                        NamedModule namedModule = (NamedModule) module;
                        String moduleName = namedModule.getName();

                        if ((name != null && moduleName != null && moduleName.equals(name)) || name == null) {
                            Provides annotation = module.getClass().getAnnotation(Provides.class);
                            if (annotation != null) {
                                Class<?>[] annotationGenericType = annotation.value();
                                for (Class<?> class1 : annotationGenericType) {
                                    if (generic.equals(class1)) {
                                        found = (T) module;
                                        break;
                                    }
                                }

                                if (found != null) {
                                    break;
                                }
                            }
                        }
                    }

                }
            }

            if (found == null && parentDiscovery != null) {
                found = parentDiscovery.findService(type, generic, name);
            }

            if (found == null) {
                exceptionPolicy.handle("Failed to find an instance providing '{}' with generic type '{}' and name '{}' in this container",
                                       type.getName(),
                                       generic.getName(),
                                       name);
            }
        }

        return found;

    }
}
