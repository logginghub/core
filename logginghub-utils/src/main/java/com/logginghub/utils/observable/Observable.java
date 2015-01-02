package com.logginghub.utils.observable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.Xml;
import com.logginghub.utils.Xml.XmlEntry;
import com.logginghub.utils.logging.Logger;

public class Observable implements ObservableItemContainer, ObservableItem {

    public static String newline = StringUtils.newline;
    private static final Logger logger = Logger.getLoggerFor(Observable.class);
    private List<ObservableListener> listeners;
    private List<ObservableItem> childProperties;
    private Object counterpart;

    private ObservableItemContainer parent;

    // public ObservableItem copy() {
    // return copyObservable();
    // }

    // public <T> T copyObservable() {
    //
    // try {
    // Observable newInstance = this.getClass().newInstance();
    //
    // List<ObservableItem> childProperties2 = childProperties;
    // for (ObservableItem observableItem : childProperties2) {
    // newInstance.childProperties.add(observableItem.copy());
    // }
    //
    // return (T) newInstance;
    // }
    // catch (Exception e) {
    // throw new FormattedRuntimeException("Failed to copy observable", e);
    // }
    //
    // }

    // New create methods

    protected ObservableProperty<String> createStringProperty(String name, String initialValue) {
        ObservableProperty<String> property = new ObservableProperty<String>(initialValue);
        property.setName(name);
        property.setType(String.class);
        property.setParent(this);
        return property;
    }

    protected ObservableProperty<Boolean> createBooleanProperty(String name, boolean initialValue) {
        ObservableProperty<Boolean> property = new ObservableProperty<Boolean>(initialValue);
        property.setName(name);
        property.setType(Boolean.class);
        property.setParent(this);
        return property;
    }

    protected ObservableInteger createIntProperty(String name, int initialValue) {
        ObservableInteger property = new ObservableInteger(initialValue);
        property.setName(name);
        property.setType(Integer.class);
        property.setParent(this);
        return property;
    }

    protected ObservableDouble createDoubleProperty(String name, double initialValue) {
        ObservableDouble property = new ObservableDouble(initialValue);
        property.setName(name);
        property.setType(Double.class);
        property.setParent(this);
        return property;
    }

    protected ObservableLong createLongProperty(String name, long initialValue) {
        ObservableLong property = new ObservableLong(initialValue);
        property.setName(name);
        property.setType(Long.class);
        property.setParent(this);
        return property;
    }

    protected <T> ObservableProperty<T> createProperty(String name, Class<T> clazz, T initialValue) {
        ObservableProperty<T> property = new ObservableProperty<T>(initialValue);
        property.setName(name);
        property.setType(clazz);
        property.setParent(this);
        return property;
    }

    protected <T> ObservableList<T> createListProperty(String name, Class<T> type) {
        ObservableList<T> list = new ObservableList<T>(new ArrayList<T>());
        list.setParent(this);
        list.setContentClass(type);
        list.setName(name);
        return list;
    }

    public void onChildAdded(final ObservableItem child) {
        if (childProperties == null) {
            childProperties = new CopyOnWriteArrayList<ObservableItem>();
        }
        childProperties.add(child);
        child.addListener(new ObservablePropertyListener() {
            public void onPropertyChanged(Object oldValue, Object newValue) {
                Observable.this.onPropertyChanged(child);
            }
        });
    }

    public void onChildRemoved(ObservableItem child) {
        if (childProperties != null) {
            childProperties.remove(child);
        }
    }

    public List<ObservableItem> getChildProperties() {
        return Collections.unmodifiableList(childProperties);
    }

    public void onChildChanged(ObservableItem child) {
        if (listeners != null) {
            for (ObservableListener observableListener : listeners) {
                observableListener.onChanged(this, child);
            }
        }

        notifyParent();
    }

    private void notifyParent() {
        if (parent != null) {
            parent.onChildChanged(this);
        }
    }

    public void onPropertyChanged(Object property) {
        if (listeners != null) {
            for (ObservableListener observableListener : listeners) {
                observableListener.onChanged(this, property);
            }
        }
    }

    public void touch() {
        onPropertyChanged(this);
    }

    public synchronized void addListener(ObservableListener listener) {
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<ObservableListener>();
        }
        listeners.add(listener);
    }

    public synchronized void removeListener(ObservableListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * @deprecated This is bad idea. You can have more than one view for the same model.
     */
    @Deprecated @SuppressWarnings("unchecked") public <T> T getCounterpart() {
        return (T) counterpart;
    }

    /**
     * Quite often an object will exist in that you need to track side-by-side with the model. Its a
     * bit hacky, but putting this object here saves us countless map lookups and annonymous inner
     * class bindings, which have turned out to be much worse.
     * 
     * @param counterpart
     * @deprecated This is bad idea. You can have more than one view for the same model.
     */
    @Deprecated public void setCounterpart(Object counterpart) {
        if (counterpart != null && this.counterpart != null) {
            Exception e = new Exception();
            e.fillInStackTrace();
            logger.warn(e,
                        "WARNING - you are setting the counterpart on an object that already has a counterpart; this is probably a bug as you'll lose the reference to the original. This is why this counterpart stuff was a bad idea, the model can have multiple views after all.");
        }
        this.counterpart = counterpart;
    }

    public String toXml(String name) {
        logger.trace("Encoding {} : name {}", this, name);

        final StringBuilder builder = new StringBuilder();

        if (childProperties != null) {

            builder.append("<").append(name);

            // First pass for attributes
            logger.trace("Encoding attributes...");
            int attributes = 0;
            for (ObservableItem observableItem : childProperties) {

                logger.trace("Processing child {} ({})", observableItem, observableItem.getClass().getSimpleName());
                String div = " ";

                if (observableItem instanceof ObservableProperty<?>) {
                    ObservableProperty<?> observableProperty = (ObservableProperty<?>) observableItem;

                    Class<?> type = observableProperty.getType();
                    if (Observable.class.isAssignableFrom(type)) {
                        // This looks like an element
                    }
                    else {
                        logger.trace("Processing as attribute");
                        builder.append(div);
                        builder.append(observableProperty.getName())
                               .append("=\"")
                               .append(StringUtils.toXML(observableProperty.asString()))
                               .append("\"");

                        attributes++;
                    }
                }
                else if (observableItem instanceof AbstractObservableProperty) {
                    logger.trace("Processing as attribute");
                    AbstractObservableProperty observableProperty = (AbstractObservableProperty) observableItem;
                    // For long/int etc
                    builder.append(div);
                    builder.append(observableProperty.getName()).append("=\"").append(observableProperty.asString()).append("\"");
                    attributes++;
                }
            }

            if (attributes == childProperties.size()) {
                // Everything was attributes - auto close
                builder.append("/>");
            }
            else {
                // We have some elements
                builder.append(">\r\n");

                logger.trace("Encoding sub-elements...");
                for (ObservableItem observableItem : childProperties) {

                    logger.trace("Processing child {} ({})", observableItem, observableItem.getClass().getSimpleName());

                    if (observableItem instanceof ObservableList<?>) {
                        logger.trace("Processing as a list...");
                        ObservableList<?> observableList = (ObservableList<?>) observableItem;

                        String listName = observableList.getName();
                        // String elementName = listName.substring(0,
                        // listName.length() - 1);
                        String elementName = observableList.getContentClass().getSimpleName().toLowerCase();
                        builder.append("<").append(listName).append(">\r\n");

                        Class<?> contentClass = observableList.getContentClass();
                        if (Observable.class.isAssignableFrom(contentClass)) {
                            Iterator<?> iterator = observableList.iterator();
                            String div = "";
                            while (iterator.hasNext()) {
                                builder.append(div);
                                Observable observable = (Observable) iterator.next();
                                logger.trace("Encoding {}", observable.getClass().getSimpleName());
                                String xml = observable.toXml(elementName);
                                builder.append(xml);
                                div = newline;
                            }
                        }
                        else {
                            Iterator<?> iterator = observableList.iterator();
                            String div = "";
                            while (iterator.hasNext()) {
                                String item = (String) iterator.next().toString();
                                builder.append(div)
                                       .append("<")
                                       .append(elementName)
                                       .append(">")
                                       .append(item)
                                       .append("</")
                                       .append(elementName)
                                       .append(">");
                                div = newline;
                            }
                        }

                        builder.append("\r\n</").append(listName).append(">\r\n");

                    }
                    else if (observableItem instanceof AbstractObservableProperty<?>) {
                        AbstractObservableProperty<?> abstractObservableProperty = (AbstractObservableProperty<?>) observableItem;

                        if (abstractObservableProperty instanceof ObservableProperty<?>) {
                            ObservableProperty<?> observableProperty = (ObservableProperty<?>) observableItem;
                            Class<?> type = observableProperty.getType();
                            if (Observable.class.isAssignableFrom(type)) {
                                logger.trace("Processing as sub-object...");
                                // This looks like an element, encode it
                                Observable observable = (Observable) observableProperty.get();;
                                String xml = observable.toXml(((AbstractObservableProperty) observableItem).getName());
                                builder.append(xml).append(newline);

                            }
                            else {
                                // This looks like an attribute, ignore it
                            }
                        }
                        else {
                            // This looks like an attribute, ignore it
                        }

                    }
                    else {
                        throw new NotImplementedException();
                    }
                }

                builder.append("</").append(name).append(">");
            }
        }
        else {
            builder.append("<").append(name).append("/>");
        }
        return builder.toString();

    }

    public String toJSON(String name) {
        return toJSON(name, true, true);
    }

    public String toJSON() {
        return toJSON("", true, false);
    }
    
    public String toJSON(String name, boolean topObject, boolean namedObject) {
        logger.trace("Encoding {} : name {}", this, name);

        final StringBuilder builder = new StringBuilder();

        if (childProperties != null) {

            if (namedObject) {
                if (topObject) {
                    builder.append("{\"").append(name).append("\":{");
                }
                else {
                    builder.append("\"").append(name).append("\":{");
                }
            }
            else {
                builder.append("{");
            }

            // First pass for attributes
            logger.trace("Encoding attributes...");
            int attributes = 0;
            String div = "";
            for (ObservableItem observableItem : childProperties) {

                logger.trace("Processing child {} ({})", observableItem, observableItem.getClass().getSimpleName());

                if (observableItem instanceof ObservableProperty<?>) {
                    ObservableProperty<?> observableProperty = (ObservableProperty<?>) observableItem;

                    Class<?> type = observableProperty.getType();
                    if (Observable.class.isAssignableFrom(type)) {
                        // This looks like an element
                    }
                    else if (type == Boolean.class) {
                        logger.trace("Processing as boolean or numeric attribute");
                        builder.append(div);
                        builder.append("\"")
                               .append(observableProperty.getName())
                               .append("\"")
                               .append(":")
                               .append(StringUtils.toJSON(observableProperty.asString()))
                               .append("");

                        attributes++;
                    }
                    else {
                        logger.trace("Processing as string attribute");
                        builder.append(div);
                        builder.append("\"")
                               .append(observableProperty.getName())
                               .append("\"")
                               .append(":\"")
                               .append(StringUtils.toJSON(observableProperty.asString()))
                               .append("\"");

                        attributes++;
                    }
                }
                else if (observableItem instanceof AbstractObservableProperty) {
                    logger.trace("Processing as attribute");
                    AbstractObservableProperty observableProperty = (AbstractObservableProperty) observableItem;
                    // For long/int etc
                    builder.append(div);
                    builder.append("\"")
                           .append(observableProperty.getName())
                           .append("\"")
                           .append(":")
                           .append(observableProperty.asString())
                           .append("");
                    attributes++;
                }

                div = ",";
            }

            if (attributes == childProperties.size()) {
                // Everything was attributes - auto close
                builder.append("}");
            }
            else {
                // We have some elements
                // builder.append(">\r\n");

                logger.trace("Encoding sub-elements...");
                for (ObservableItem observableItem : childProperties) {

                    logger.trace("Processing child {} ({})", observableItem, observableItem.getClass().getSimpleName());

                    if (observableItem instanceof ObservableList<?>) {
                        logger.trace("Processing as a list...");
                        ObservableList<?> observableList = (ObservableList<?>) observableItem;

                        String listName = observableList.getName();
                        // String elementName = listName.substring(0,
                        // listName.length() - 1);
                        String elementName = observableList.getContentClass().getSimpleName().toLowerCase();

                        if (attributes > 0) {
                            builder.append(",");
                        }

                        builder.append("\"").append(listName).append("\":{");
                        builder.append("\"").append(elementName).append("\":[");

                        Class<?> contentClass = observableList.getContentClass();
                        if (Observable.class.isAssignableFrom(contentClass)) {
                            Iterator<?> iterator = observableList.iterator();
                            div = "";
                            while (iterator.hasNext()) {
                                builder.append(div);
                                Observable observable = (Observable) iterator.next();
                                logger.trace("Encoding {}", observable.getClass().getSimpleName());
                                String json = observable.toJSON(elementName, false, false);
                                builder.append(json);
                                div = ",";

                            }
                        }
                        else {
                            throw new NotImplementedException("Not sure what this bit was for, none of the tests touch it");
                            // Iterator<?> iterator = observableList.iterator();
                            // div = "";
                            // while (iterator.hasNext()) {
                            // String item = (String) iterator.next().toString();
                            // builder.append(div)
                            // .append("<")
                            // .append(elementName)
                            // .append(">")
                            // .append(item)
                            // .append("</")
                            // .append(elementName)
                            // .append(">");
                            // div = ",";
                            // }
                        }

                        builder.append("]}");

                    }
                    else if (observableItem instanceof AbstractObservableProperty<?>) {
                        AbstractObservableProperty<?> abstractObservableProperty = (AbstractObservableProperty<?>) observableItem;

                        if (abstractObservableProperty instanceof ObservableProperty<?>) {
                            ObservableProperty<?> observableProperty = (ObservableProperty<?>) observableItem;
                            Class<?> type = observableProperty.getType();
                            if (Observable.class.isAssignableFrom(type)) {
                                logger.trace("Processing as sub-object...");
                                // This looks like an element, encode it
                                Observable observable = (Observable) observableProperty.get();;
                                String json = observable.toJSON(((AbstractObservableProperty<?>) observableItem).getName(), false, true);

                                if (attributes > 0) {
                                    builder.append(",");
                                }
                                builder.append(json);
                                attributes++;

                            }
                            else {
                                // This looks like an attribute, ignore it
                            }
                        }
                        else {
                            // This looks like an attribute, ignore it
                        }

                    }
                    else {
                        throw new NotImplementedException();
                    }
                }

                builder.append("}");
            }

            if (topObject) {
                builder.append("}");
            }
        }
        else {
            builder.append("{\"").append(name).append("\":{}}");
        }
        return builder.toString();

    }

    public void bindToFile(final String rootNode, final File file) {
        if (file.exists()) {
            fromXml(file);
        }

        addListener(new ObservableListener() {
            public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                String xml = toXml(rootNode);
                FileUtils.write(xml, file);
            }
        });
    }
    
    public void toFile(String rootNode, File file) {
        String xml = toXml(rootNode);
        FileUtils.write(xml, file);
    }

    public void fromXml(File config) {
        if (config.exists()) {
            Xml xml = new Xml(FileUtils.read(config));
            fromXml(xml.getRoot());
        }
    }

    public void fromXml(String xml) {
        XmlEntry root = new Xml(xml).getRoot();
        fromXml(root);
    }

    public void fromXml(XmlEntry xml) {

        logger.trace("Decoding object {} from xml", this);

        if (childProperties != null) {
            for (ObservableItem observableItem : childProperties) {
                logger.trace("Decoding {}", observableItem.getClass());

                Metadata attributes = xml.getAttributes();
                logger.trace("Attributes are : {}", attributes);

                if (observableItem instanceof ObservableProperty<?>) {
                    ObservableProperty property = (ObservableProperty) observableItem;

                    Class<?> type = property.getType();
                    logger.trace("Type is {}", type.getName());
                    if (Observable.class.isAssignableFrom(type)) {
                        // Its a sub object - decode the element
                        try {
                            Observable element = (Observable) type.newInstance();

                            XmlEntry nodePath = xml.nodePath(property.getName());
                            element.fromXml(nodePath);
                            property.set(element);
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }
                    else {
                        // Its probably an attribute
                        logger.trace("Decoding property name={} type={}...", property.getName(), property.getType());

                        if (type == String.class) {
                            property.set(StringUtils.fromXML(attributes.getString(property.getName())));
                        }
                        else if (type == Boolean.class) {
                            property.set(attributes.getBoolean(property.getName(), (Boolean) property.get()));
                        }
                        else if (Enum.class.isAssignableFrom(type)) {
                            String enumName = attributes.getString(property.getName());
                            Class<? extends Enum> foo = (Class<? extends Enum>) type;
                            Object valueOf = Enum.valueOf(foo, enumName);
                            property.set(valueOf);
                        }
                        else {
                            throw new NotImplementedException(type.toString());
                        }

                        logger.trace("Decoded : {} = {}", property, property.get());
                    }
                }
                else if (observableItem instanceof ObservableInteger) {
                    ObservableInteger observableInteger = (ObservableInteger) observableItem;
                    observableInteger.set(attributes.getInt(observableInteger.getName(), observableInteger.get()));
                }
                else if (observableItem instanceof ObservableLong) {
                    ObservableLong observableLong = (ObservableLong) observableItem;
                    observableLong.set(attributes.getLong(observableLong.getName(), observableLong.get()));
                }
                else if (observableItem instanceof ObservableDouble) {
                    ObservableDouble observableDouble = (ObservableDouble) observableItem;
                    observableDouble.set(attributes.getDouble(observableDouble.getName(), observableDouble.get()));
                }
                else if (observableItem instanceof ObservableList) {
                    logger.trace("Decoding list...");
                    ObservableList observableList = (ObservableList) observableItem;

                    String name = observableList.getName();
                    XmlEntry path = xml.nodePath(name);
                    if (path != null) {

                        // String subElements = name.substring(0, name.length()
                        // - 1);                        
                        String subElements = observableList.getContentClass().getSimpleName().toLowerCase();

                        Class<?> contentClass = observableList.getContentClass();

                        List<XmlEntry> find = path.find(subElements);
                        for (XmlEntry xmlEntry : find) {
                            try {

                                Object object;
                                if (contentClass == Integer.class) {
                                    object = Integer.parseInt(xmlEntry.getElementData());
                                }
                                else {
                                    object = contentClass.newInstance();
                                    if (object instanceof Observable) {
                                        Observable item = (Observable) object;
                                        item.fromXml(xmlEntry);
                                    }
                                    else if (object instanceof String) {
                                        object = xmlEntry.getElementData();
                                    }
                                    else {
                                        throw new NotImplementedException(contentClass.getName());
                                    }
                                }
                                observableList.add(object);
                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                }
            }
        }
    }

    public void setParent(ObservableItemContainer parent) {
        this.parent = parent;
    }

    public void addListener(ObservablePropertyListener observablePropertyListener) {
        // TODO : not sure what this will do?!
    }

    public Observable duplicate() {

        Class<? extends Observable> clazz = this.getClass();
        Observable duplicate = ReflectionUtils.instantiate(clazz);
        duplicate.counterpart = this.counterpart;
        duplicate.parent = null;

        // By instantiating a new class, we'll have created duplicate child properties. All we need
        // to do now is copy over their values

        // Copy over duplicates of any child properties
        for (ObservableItem duplicateItem : duplicate.childProperties) {

            if (duplicateItem instanceof ObservableProperty) {
                ObservableProperty duplicateProperty = (ObservableProperty)duplicateItem;

                String name = duplicateProperty.getName();

                for (ObservableItem thisItem: this.childProperties) {
                    ObservableProperty<? extends Object> thisProperty = (ObservableProperty<?>) thisItem;
                    if (thisProperty.getName().equals(name)) {
                        Object value = thisProperty.get();
                        duplicateProperty.set(value);
                        break;
                    }
                }
            }else{
                throw new NotImplementedException();
            }

        }

        return duplicate;
    }

    @Override public String toString() {

        StringBuilder builder = new StringBuilder();

        String simpleName = getClass().getSimpleName();
        builder.append(simpleName).append(" (0x").append(Integer.toHexString(System.identityHashCode(this))).append(")").append(" [");
        if (childProperties != null) {
            String div = "";
            for (ObservableItem observableItem : childProperties) {
                if (observableItem instanceof AbstractObservableProperty) {
                    AbstractObservableProperty abstractObservableProperty = (AbstractObservableProperty) observableItem;
                    String name = abstractObservableProperty.getName();
                    builder.append(div).append(name).append("=").append(abstractObservableProperty.asString());
                }
                else {
                    builder.append(div).append("?").append("=").append(observableItem.toString());
                }

                div = ",";
            }
        }
        builder.append("]");

        return builder.toString();
    }

    
}
