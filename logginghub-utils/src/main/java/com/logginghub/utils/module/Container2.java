package com.logginghub.utils.module;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logginghub.utils.Metadata;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.Xml;
import com.logginghub.utils.Xml.XmlEntry;
import com.logginghub.utils.module.Inject.Direction;

public class Container2 {

    private Container2 parent = null;
    private Object owner = null;
    private Map<Object, Container2> childContainers = new HashMap<Object, Container2>();
    private Map<String, Object> instancesByID = new HashMap<String, Object>();
    private List<Object> instances = new ArrayList<Object>();
    private List<Object> externalInstances = new ArrayList<Object>();
    private List<ClassResolver> classResolvers = new ArrayList<ClassResolver>();
    private ClassResolver defaultClassResolver;

    @Retention(RetentionPolicy.RUNTIME) public @interface Hint {
        String attribute();
    }

    public Container2() {
        defaultClassResolver = new ClassResolver() {
            public String resolve(String name) {
                return name;
            }
        };
    }

    @Override public String toString() {
        return dump("");
    }

    public Container2 fromXmlString(String string) throws ResolutionNotPossibleException {

        Xml xml = new Xml(string);

        XmlEntry root = xml.getRoot();

        processChildren(this, root);

        return this;
    }

    private void processChildren(Container2 container, XmlEntry root) throws ResolutionNotPossibleException {
        List<XmlEntry> children = root.getChildren();

        List<XmlEntry> stillToProcess = new ArrayList<XmlEntry>();
        stillToProcess.addAll(children);

        ResolutionNotPossibleException lastIssue = null;

        while (stillToProcess.size() > 0) {
            boolean progress = false;
            Iterator<XmlEntry> iterator = stillToProcess.iterator();
            while (iterator.hasNext()) {
                XmlEntry xmlEntry = (XmlEntry) iterator.next();
                try {
                    int index = children.indexOf(xmlEntry);
                    instantiateAndConfigureEntry(container, xmlEntry, index);
                    progress = true;
                    iterator.remove();
                }
                catch (ResolutionNotPossibleException e) {
                    // Leave this until later
                    lastIssue = e;
                }
            }

            if (!progress) {
                throw new ResolutionNotPossibleException(lastIssue);
            }

        }
    }

    private void instantiateAndConfigureEntry(Container2 container, XmlEntry entry, int index) throws ResolutionNotPossibleException {

        Object instance = instantiate(container, entry);
        injectDependencies(instance, container, entry);

        String id = entry.getAttribute("id");
        if (StringUtils.isNotNullOrEmpty(id)) {
            // TODO : check for existing id
            container.instancesByID.put(id, instance);
        }

        if (index <= container.instances.size()) {
            container.instances.add(index, instance);
        }
        else {
            container.instances.add(instance);
        }

        List<XmlEntry> childrenNotYetProcessed = new ArrayList<XmlEntry>();
        childrenNotYetProcessed.addAll(entry.getChildren());

        configure(entry, instance, childrenNotYetProcessed);

        try {
            // If any child elements were not use to configure sub-objects, they might actually be
            // modules in their own right
            if (childrenNotYetProcessed.size() > 0) {
                Container2 childContainer = new Container2();
                childContainer.parent = container;
                childContainer.owner = instance;
                processChildren(childContainer, entry);
                container.childContainers.put(instance, childContainer);
            }
        }
        catch (ResolutionNotPossibleException e) {
            // Remember to take this out of the container
            container.instances.remove(instance);
            throw e;
        }

    }

    private void injectDependencies(Object instance, Container2 container, XmlEntry entry) throws ResolutionNotPossibleException {
        Method[] methods = instance.getClass().getMethods();
        for (Method method : methods) {
            Inject annotation = method.getAnnotation(Inject.class);
            if (annotation != null) {
                if (method.getParameterTypes().length == 1) {
                    Class<?> paramaterClass = method.getParameterTypes()[0];

                    Container2 targetContainer = container;
                    if (annotation.direction() == Direction.Parent) {
                        targetContainer = container.parent;
                    }

                    Object resolved;
                    String refKey = StringUtils.camelCase(StringUtils.after(method.getName(), "set")) + "Ref";
                    String ref = entry.getAttribute(refKey);
                    if (StringUtils.isNotNullOrEmpty(ref)) {
                        resolved = targetContainer.resolveByID(ref);
                        if (resolved == null) {
                            throw new ResolutionNotPossibleException(StringUtils.format("Failed to resolve injection dependency with ref '{}'", ref));
                        }
                    }
                    else {
                        resolved = targetContainer.resolve(paramaterClass);
                        if (resolved == null) {
                            throw new ResolutionNotPossibleException(StringUtils.format("Failed to resolve injection dependency with class '{}'",
                                                                                        paramaterClass.getName()));
                        }
                    }

                    try {
                        method.invoke(instance, new Object[] { resolved });
                    }
                    catch (IllegalArgumentException e) {
                        throw new ResolutionNotPossibleException(e);
                    }
                    catch (IllegalAccessException e) {
                        throw new ResolutionNotPossibleException(e);
                    }
                    catch (InvocationTargetException e) {
                        throw new ResolutionNotPossibleException(e);
                    }

                }
            }
        }
    }

    private Object instantiate(Container2 container, XmlEntry entry) throws ResolutionNotPossibleException {
        Object instance = null;
        try {
            String classHint = entry.getTagName();
            Class<?> clazz = attemptClassResolution(classHint);

            // Have a look through the constructors and see what we can do
            Constructor[] constructors = clazz.getConstructors();
            for (Constructor constructor : constructors) {
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 0) {
                    // Default constructor, win
                    instance = constructor.newInstance((Object[]) null);
                    break;
                }
            }

            if (instance == null) {
                // No luck with the default constructor. We might need to inject.
                Constructor constructor = constructors[0];
                Class[] parameterTypes = constructor.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];
                Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> paramaterClass = parameterTypes[i];

                    Object resolved;
                    String refKey = StringUtils.camelCase(paramaterClass.getSimpleName()) + "Ref";
                    String ref = entry.getAttribute(refKey);
                    if (StringUtils.isNotNullOrEmpty(ref)) {
                        resolved = container.resolveByID(ref);
                    }
                    else {
                        resolved = container.resolve(paramaterClass);
                    }

                    if (resolved == null) {
                        Annotation[] annotations = parameterAnnotations[i];
                        for (Annotation annotation : annotations) {
                            if (annotation instanceof Hint) {
                                Hint hint = (Hint) annotation;
                                String attribute = hint.attribute();
                                resolved = entry.getAttribute(attribute);
                            }
                        }
                    }

                    if (resolved == null) {
                        // Lets see if we can poke something out the attributes into the
                        // constructor! Ultra-dirty!
                        Metadata attributes = entry.getAttributes();
                        Collection<Object> values = attributes.values();
                        for (Object object : values) {
                            if (paramaterClass.isAssignableFrom(object.getClass())) {
                                resolved = object;
                                break;
                            }
                        }
                    }

                    if (resolved == null) {
                        throw new ResolutionNotPossibleException();
                    }

                    parameters[i] = resolved;
                }

                instance = constructor.newInstance(parameters);
            }

        }
        catch (IllegalArgumentException e) {
            throw new ResolutionNotPossibleException(e);
        }
        catch (InstantiationException e) {
            throw new ResolutionNotPossibleException(e);
        }
        catch (IllegalAccessException e) {
            throw new ResolutionNotPossibleException(e);
        }
        catch (InvocationTargetException e) {
            throw new ResolutionNotPossibleException(e);
        }
        return instance;
    }

    private Object instantiateAndConfigureSimple(Class<?> clazz, XmlEntry entry, List<XmlEntry> childrenNotYetProcessed)
                    throws ResolutionNotPossibleException {
        Object instance = null;
        try {
            // Have a look through the constructors and see what we can do
            Constructor[] constructors = clazz.getConstructors();
            for (Constructor constructor : constructors) {
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 0) {
                    // Default constructor, win
                    instance = constructor.newInstance((Object[]) null);
                    break;
                }
            }
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        configure(entry, instance, childrenNotYetProcessed);

        return instance;
    }

    private Class<?> attemptClassResolution(String classHint) throws ResolutionNotPossibleException {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        Class<?> clazz = null;
        if (classResolvers.size() > 0) {
            for (ClassResolver classResolver : classResolvers) {
                String classname = classResolver.resolve(classHint);
                clazz = attempt(builder, classname);
                if (clazz != null) {
                    break;
                }
            }
        }
        else {
            String classname = defaultClassResolver.resolve(classHint);
            clazz = attempt(builder, classname);
        }

        if (clazz == null) {
            throw new ResolutionNotPossibleError(new ClassNotFoundException(builder.toString()));
        }

        return clazz;
    }

    private Class<?> attempt(StringUtilsBuilder builder, String classname) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(classname);
        }
        catch (ClassNotFoundException e) {
            builder.append(classname);
        }
        catch (NoClassDefFoundError e) {
            builder.append(classname + " - " + e.getMessage());
        }
        return clazz;
    }

    private Object resolveByID(String ref) {

        Object found = instancesByID.get(ref);

        if (found == null && parent != null) {
            found = parent.resolveByID(ref);
        }

        return found;
    }

    private Object resolve(Class<?> paramaterClass) {

        Object found = null;

        // First check out siblings
        for (Object object : instances) {
            if (paramaterClass.isAssignableFrom(object.getClass())) {
                found = object;
                break;
            }
        }

        // Next any external objects we may have been provided with
        for (Object object : externalInstances) {
            if (paramaterClass.isAssignableFrom(object.getClass())) {
                found = object;
                break;
            }
        }

        // Now check our owner
        if (found == null && owner != null) {
            if (paramaterClass.isAssignableFrom(owner.getClass())) {
                found = owner;
            }
        }

        // Finally recurse back up to our parent container
        if (found == null && parent != null) {
            found = parent.resolve(paramaterClass);
        }

        return found;

    }

    public void addExternalInstance(Object object) {
        externalInstances.add(object);
    }

    public void addInstance(Object object) {
        instances.add(object);
    }

    public Map<String, Object> getInstancesByID() {
        return instancesByID;
    }

    public List<Object> getInstances() {
        return instances;
    }

    private void configure(XmlEntry entry, Object instance, List<XmlEntry> childrenNotYetProcessed) throws ResolutionNotPossibleException {

        Metadata attributes = entry.getAttributes();

        Set<Object> keySet = attributes.keySet();
        for (Object key : keySet) {
            Object value = attributes.get(key);
            if (key.toString().equals("id") || key.toString().endsWith("Ref")) {
                applyPropertyQuietly(instance, key.toString(), value);
            }
            else {
                applyProperty(instance, key.toString(), value.toString());
            }
        }

        // See if we have any child fields in the xml
        List<XmlEntry> children = entry.getChildren();
        for (XmlEntry xmlEntry : children) {

            String name = xmlEntry.getTagName();

            try {
                Field field = ReflectionUtils.findField(name, instance);
                if (field != null) {
                    if (List.class.isAssignableFrom(field.getType())) {
                        List<Object> list = (List<Object>) field.get(instance);
                        if (list == null) {
                            list = new ArrayList<Object>();
                            field.set(instance, list);
                        }

                        Class<?> childType = null;

                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) genericType;
                            for (Type t : pt.getActualTypeArguments()) {
                                childType = (Class<?>) t;
                                break;
                            }
                        }

                        // TODO : that last param is just made up
                        Object object = instantiateAndConfigureSimple(childType, xmlEntry, new ArrayList<XmlEntry>());
                        list.add(object);

                        childrenNotYetProcessed.remove(xmlEntry);
                    }
                }

            }
            catch (SecurityException e) {
                e.printStackTrace();
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    private static void applyProperty(Object instance, String string, String value) throws ResolutionNotPossibleException {
        try {
            ReflectionUtils.setFieldFromString(string, instance, value);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e) {
            throw new ResolutionNotPossibleException(StringUtils.format("Failed to set field '{}' on class '{}' to value '{}",
                                                                        string,
                                                                        instance.getClass(),
                                                                        value));
        }
    }

    private static void applyPropertyQuietly(Object instance, String string, Object value) {
        try {
            ReflectionUtils.setField(string, instance, value);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e) {}
    }

    public Container2 getChildren(Object node) {
        return childContainers.get(node);
    }

    public void initialise() {
        for (Object object : instances) {
            ReflectionUtils.invokeIfMethodExists("initialise", object, (Object[]) null);
        }

        for (Object object : instances) {
            Container2 childContainer = childContainers.get(object);
            if (childContainer != null) {
                childContainer.initialise();
            }
        }
    }

    public void start() {
        for (Object object : instances) {
            ReflectionUtils.invokeIfMethodExists("start", object, (Object[]) null);
        }

        for (Object object : instances) {
            Container2 childContainer = childContainers.get(object);
            if (childContainer != null) {
                childContainer.start();
            }
        }
    }

    public void stop() {
        for (Object object : instances) {
            ReflectionUtils.invokeIfMethodExists("stop", object, (Object[]) null);
        }

        for (Object object : instances) {
            Container2 childContainer = childContainers.get(object);
            if (childContainer != null) {
                childContainer.start();
            }
        }
    }

    public void addClassResolver(ClassResolver classResolver) {
        classResolvers.add(classResolver);
    }

    public void dump() {
        String dump = dump("");
        System.out.println(dump);
    }

    public String dump(String indent) {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        if (instances.size() > 0) {
            // builder.appendLine("{} | Instances:", indent);
            for (Object object : instances) {
                builder.appendLine("{} | {}", indent, object);

                Container2 container2 = childContainers.get(object);
                if (container2 != null) {
                    builder.appendLine("{}", container2.dump(indent + "  "));
                }
            }
        }
        return builder.toString();
    }
}
