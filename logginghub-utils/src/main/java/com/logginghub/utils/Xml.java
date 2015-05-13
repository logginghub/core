package com.logginghub.utils;

import com.logginghub.utils.logging.Logger;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

public class Xml {

    private static final Logger logger = Logger.getLoggerFor(Xml.class);

    public Xml(String content) {
        parseInternal(content);
    }

    public static interface XmlEntryVisitor {
        void visit(XmlEntry entry);
    }

    public static class XmlEntry {

        private String tagName;
        private List<XmlEntry> children = new ArrayList<XmlEntry>();
        private Metadata attributes = new Metadata();
        private String tagData;
        private String elementData;
        private XmlEntry parent;

        public XmlEntry(XmlEntry parent) {
            this.parent = parent;
        }

        public void setTag(String tagName) {
            this.tagName = tagName;
        }

        public void add(XmlEntry entry) {
            children.add(entry);
        }

        public void dump(PrintStream out, int level) {

            StringBuilder builder = new StringBuilder();

            builder.append(StringUtils.repeat(" ", level * 4));
            builder.append(tagName);

            if (!attributes.isEmpty()) {
                builder.append(" attributes=").append(attributes);
            }

            if (elementData != null && elementData.length() > 0) {
                builder.append(" elementData=[").append(elementData).append("]");
            }

            out.println(builder.toString());
            for (XmlEntry xmlEntry : children) {
                xmlEntry.dump(out, level + 1);
            }
        }

        public void setTagData(String tagData) {
            this.tagData = tagData;
        }

        public String toString() {
            return tagName;
        }

        public String getTagName() {
            return tagName;
        }

        public List<XmlEntry> getChildren() {
            return children;
        }

        public String getAttribute(String string) {
            return attributes.getString(string);

        }

        public Metadata getAttributes() {
            return attributes;
        }

        public String path(String path) {
            return pathInternal(path, true);
        }

        public XmlEntry nodePath(String path) {

            logger.trace("Walking into '{}' with search string '{}'", this, path);
            if (path.length() == 0) {
                // Rock bottom
                return this;
            }
            else {

                String nextTagName = StringUtils.before(path, ".");
                int index = 0;

                String strippedTagName;

                if (nextTagName.contains("[")) {
                    strippedTagName = StringUtils.beforeLast(nextTagName, "[");
                    index = Integer.parseInt(StringUtils.between(nextTagName, "[", "]"));
                }
                else {
                    strippedTagName = nextTagName;
                }

                List<XmlEntry> matching = find(strippedTagName);

                if (matching.isEmpty()) {
                    return null;
                }
                else {
                    String remaining;
                    if (path.equals(nextTagName)) {
                        remaining = "";
                    }
                    else {
                        remaining = path.substring(nextTagName.length() + 1);
                    }
                    logger.trace("Pathing into element '{}' with remaining search '{}'", matching.get(0), remaining);
                    return matching.get(index).nodePath(remaining);
                }
            }

        }

        public String pathInternal(String path, boolean strict) {

            logger.trace("Walking into '{}' with search string '{}'", this, path);
            if (path.length() == 0) {
                // Rock bottom
                return getElementData();
            }
            else {

                String nextTagName = StringUtils.before(path, ".");

                List<XmlEntry> matching = find(nextTagName);

                if (matching.isEmpty()) {
                    // Hmmm, no child entry for this, maybe they want an
                    // attribute?
                    String attribute = attributes.getString(nextTagName);

                    if (attribute == null && strict) {
                        throw new IllegalArgumentException("Failed to walk path");
                    }

                    return attribute;
                }
                else {

                    String remaining;
                    if (path.equals(nextTagName)) {
                        remaining = "";
                    }
                    else {
                        remaining = path.substring(nextTagName.length() + 1);
                    }
                    logger.trace("Pathing into element '{}' with remaining search '{}'", matching.get(0), remaining);
                    return matching.get(0).pathInternal(remaining, strict);
                }
            }
        }

        public String pathInternal(boolean strict, String... elements) {

            logger.trace("Walking into '{}' with search string '{}'", this, Arrays.toString(elements));
            if (elements.length == 0) {
                // Rock bottom
                return getElementData();
            }
            else {

                String nextTagName = elements[0];

                List<XmlEntry> matching = find(nextTagName);

                if (matching.isEmpty()) {
                    // Hmmm, no child entry for this, maybe they want an
                    // attribute?
                    String attribute = attributes.getString(nextTagName);

                    if (attribute == null && strict) {
                        throw new IllegalArgumentException("Failed to walk path");
                    }

                    return attribute;
                }
                else {

                    String[] remaining = stripFirstElement(elements);

                    logger.trace("Pathing into element '{}' with remaining search '{}'", matching.get(0), Arrays.toString(remaining));
                    return matching.get(0).pathInternal(strict, remaining);
                }
            }
        }

        public String pathRelaxed(String... elements) {
            return pathInternal(false, elements);
        }

        public String pathRelaxed(String path) {
            return pathInternal(path, false);
        }

        public List<XmlEntry> find(String tagName) {
            List<XmlEntry> matching = new ArrayList<Xml.XmlEntry>();
            for (XmlEntry xmlEntry : children) {
                if (xmlEntry.getTagName().equals(tagName)) {
                    matching.add(xmlEntry);
                }
            }
            return matching;
        }

        public void setElementData(String elementData) {
            this.elementData = elementData;
        }

        public String getElementData() {
            return elementData;
        }

        public List<XmlEntry> findAllRecursive(String tag) {
            List<XmlEntry> results = new ArrayList<XmlEntry>();
            findAllRecursive(tag, results);
            return results;
        }

        private void findAllRecursive(String tag, List<XmlEntry> results) {
            List<XmlEntry> found = find(tag);
            results.addAll(found);

            for (XmlEntry xmlEntry : children) {
                xmlEntry.findAllRecursive(tag, results);
            }
        }

        public void extract(String selector, XmlEntryVisitor visitor) {
            if (tagName.equals(selector)) {
                visitor.visit(this);
            }
            for (XmlEntry xmlEntry : children) {
                xmlEntry.extract(selector, visitor);
            }
        }

        public XmlEntry getParent() {
            return parent;

        }

        public XmlEntry getNode(String tagname) {
            XmlEntry entry = null;
            for (XmlEntry xmlEntry : children) {
                if (xmlEntry.getTagName().equals(tagname)) {
                    entry = xmlEntry;
                    break;
                }
            }
            return entry;
        }

        public Boolean getBooleanAttribute(String string) {
            return attributes.getBoolean(string, false);
        }

        public void dump() {
            dump(System.out, 0);
        }
    }

    private Stack<XmlEntry> entryStack = new Stack<Xml.XmlEntry>();
    private XmlEntry root = null;

    public XmlEntry getRoot() {
        return root;
    }

    private void parseInternal(String content) {

        Scanner scanner = new Scanner(content);
        scanner.useDelimiter("<");

        XmlEntry currentEntry = null;
        String nextToken = null;
        boolean inComment = false;
        try {
            while (scanner.hasNext()) {
                nextToken = scanner.next().trim();
                // String originalToken = nextToken;
                // nextToken = nextToken.replaceAll("\r|\n", " ");
                logger.trace("Parsing token '{}'", nextToken);
                logger.moreIndent();

                if (nextToken.length() == 0) {
                    // Whatever
                }
                else if (nextToken.startsWith("!DOCTYPE")) {
                    // Whatever
                }
                else if (nextToken.startsWith("?xml")) {
                    // Same, not interested
                }
                else {

                    String fullElement = StringUtils.before(nextToken, ">").trim();
                    String tagName = fullElement.split("\\s")[0].trim();

                    logger.trace("Found tag name '{}'", tagName);

                    if (tagName.startsWith("/")) {
                        // Closing tag
                        if (entryStack.isEmpty()) {
                            // That must be the end
                        }
                        else {
                            currentEntry = entryStack.pop();
                            logger.lessOutdent();
                        }
                        logger.trace("Tag '{}' closed, current entry is now '{}'", tagName, currentEntry);
                    }
                    else if (fullElement.endsWith("/")) {

                        logger.moreIndent();
                        XmlEntry entry = new XmlEntry(currentEntry);
                        String nameWithoutSlash = tagName;
                        if (nameWithoutSlash.endsWith("/")) {
                            nameWithoutSlash = nameWithoutSlash.substring(0, nameWithoutSlash.length() - 1);
                        }
                        entry.setTag(nameWithoutSlash);
                        entry.setTagData(nextToken);

                        StringUtilsTokeniser st = new StringUtilsTokeniser(fullElement.trim());
                        st.skipWord();
                        String attributes = st.upToOutsideQuotes('/').trim();
                        
//                        String attributes = StringUtils.between(fullElement, tagName, "/").trim();
                        logger.trace("Parsing attributes '{}'", attributes);
                         entry.getAttributes().parse(attributes);
                        logger.trace("Attributes are '{}'", attributes);

                        // Someone went to the length of putting a self closing
                        // element in here, so set the element value to empty
                        // string to show this
                        if (attributes.length() == 0) {
                            entry.elementData = "";
                            logger.trace("Setting element data to empty string as there are no attributes");
                        }

                        if (currentEntry != null) {
                            logger.trace("Adding '{}' to parent entry '{}'", entry, currentEntry);
                            currentEntry.add(entry);
                        }
                        else {
                            root = entry;
                        }

                        logger.trace("Tag '{}' self closed, current entry is now '{}'", tagName, currentEntry);
                        logger.lessOutdent();
                    }
                    else if (tagName.startsWith("!--")) {
                        // Comment, ignore it
                        inComment = true;
                        if (nextToken.endsWith("-->")) {
                            // Single-line comment, do nothing
                        }
                        else {
                            // Multi-line comment, hoover it up
                            scanner.useDelimiter("-->");
                            String next = scanner.next();
                            scanner.useDelimiter("<");
                            String next2 = scanner.next();
                        }
                    }
                    else {
                        // Opening tag
                        XmlEntry entry = new XmlEntry(currentEntry);
                        entry.setTag(tagName);
                        entry.setTagData(nextToken);

                        // Is there anything else in the tag?
                        if (fullElement.trim().length() > tagName.trim().length()) {
                            String attributes = StringUtils.after(fullElement, tagName).trim();
                            entry.getAttributes().parse(attributes);
                            logger.trace("Attributes are '{}'", attributes);
                        }

                        if (nextToken.contains(">")) {
                            String elementData = StringUtils.after(nextToken, ">").trim();
                            if (elementData.length() > 0) {
                                entry.setElementData(elementData);
                                logger.trace("Element data is '{}'", elementData);
                            }
                        }

                        if (currentEntry != null) {
                            logger.trace("Adding '{}' to parent entry '{}'", entry, currentEntry);
                            entryStack.push(currentEntry);
                            currentEntry.add(entry);
                        }
                        else {
                            root = entry;
                        }

                        currentEntry = entry;
                        logger.moreIndent();
                    }
                }

                logger.lessOutdent();
            }
        }
        catch (RuntimeException e) {
//            try {
//                // For debugging
//                parseInternal(content);
//            }
//            catch (RuntimeException ee) {}
            throw new RuntimeException("Parsing failed on token '" + nextToken + "'", e);
        }
        logger.lessOutdent();
    }

    public void dump(PrintStream out) {

        root.dump(out, 0);

    }

    public XmlEntry nodePath(String path) {

        // Need to strip off the root element so the root node can process it
        // properly...
        String tagName = StringUtils.before(path, ".");
        if (root == null) {
            logger.warning("The XML document was blank");
            return null;
        }
        else if (root.getTagName().equals(tagName)) {
            if (tagName.equals(path)) {
                return root;
            }
            else {
                String remaining = path.substring(tagName.length() + 1);
                logger.trace("Walking into '{}' with search string '{}'", root, remaining);
                return root.nodePath(remaining);
            }
        }
        else {
            return null;
        }

    }

    public String path(String path) {

        // Need to strip off the root element so the root node can process it
        // properly...

        String tagName = StringUtils.before(path, ".");
        if (root == null) {
            logger.warning("The XML document was blank");
        }
        else if (root.getTagName().equals(tagName)) {
            String remaining = path.substring(tagName.length() + 1);
            logger.trace("Walking into '{}' with search string '{}'", root, remaining);
            return root.path(remaining);
        }

        return "";
    }

    public String pathRelaxed(String... elements) {
        // Need to strip off the root element so the root node can process it
        // properly...

        String tagName = elements[0];
        if (root == null) {
            logger.warning("The XML document was blank");
        }
        else if (root.getTagName().equals(tagName)) {
            return root.pathRelaxed(stripFirstElement(elements));
        }

        return "";
    }

    private static String[] stripFirstElement(String[] elements) {
        String[] sub = new String[elements.length - 1];
        System.arraycopy(elements, 1, sub, 0, sub.length);
        return sub;

    }

    public String pathRelaxed(String path) {

        // Need to strip off the root element so the root node can process it
        // properly...

        String tagName = StringUtils.before(path, ".");
        if (root == null) {
            logger.warning("The XML document was blank");
        }
        else if (root.getTagName().equals(tagName)) {
            String remaining = path.substring(tagName.length() + 1);
            return root.pathRelaxed(remaining);
        }

        return "";
    }

    public static Xml parse(File file) {
        String page = FileUtils.read(file);
        Xml xml = new Xml(page);
        return xml;
    }

    public static Xml parse(String url) {
        String page = FileUtils.readUrl(url);
        Xml xml = new Xml(page);
        return xml;
    }

    public void dump() {
        dump(System.out);
    }

    public void extract(String selector, XmlEntryVisitor visitor) {
        root.extract(selector, visitor);
    }

    public boolean pathExists(String path) {
        String pathRelaxed = pathRelaxed(path);
        return pathRelaxed != null && pathRelaxed.length() > 0;

    }

}
