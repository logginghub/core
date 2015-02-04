package com.logginghub.logging.frontend.views.stack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.logginghub.utils.StringUtils.StringUtilsBuilder;

public class CountingTreeMap {

    private long count = 0;
    private Map<String, CountingTreeMap> children = new HashMap<String, CountingTreeMap>();
    private String key;
    private CountingTreeMap parent;

    public CountingTreeMap(String key) {
        this.key = key;
    }

    public CountingTreeMap() {
        this.key = null;
    }

    public String getKey() {
        return key;
    }

    public Set<String> getKeys() {
        return children.keySet();
    }

    public Collection<CountingTreeMap> getValues() {
        return children.values();
    }

    public CountingTreeMap get(String string) {
        CountingTreeMap child = children.get(string);
        if (child == null) {
            child = new CountingTreeMap(string);
            child.parent = this;
            children.put(string, child);
        }
        return child;
    }

    public CountingTreeMap countDontPropagate(String key, long amount) {
        CountingTreeMap child = get(key);
        child.countDontPropagate(amount);
        return child;
    }
    
    

    public CountingTreeMap count(String key, long amount) {
        CountingTreeMap child = get(key);
        child.count(amount);
        return child;
    }

    public CountingTreeMap count(String key) {
        return count(key, 1);
    }

    public long getCount(String string) {
        long count = 0;
        CountingTreeMap onlyIfExists = children.get(string);
        if (onlyIfExists != null) {
            count = onlyIfExists.getCount();
        }
        return count;
    }

    public long getCount() {
        return count;
    }

    public void clear() {
        count = 0;
        children.clear();
    }

    public void countDontPropagate(long amount) {
        count += amount;
    }
    
    public void count(long i) {
        count += i;
        if (parent != null) {
            parent.count(i);
        }
    }

    public String toString(String indent) {
        StringUtilsBuilder builder = new StringUtilsBuilder();

        builder.appendLine("{} | {} : {}", indent, key, count);
        Collection<CountingTreeMap> children2 = children.values();
        for (CountingTreeMap countingTreeMap : children2) {
            builder.append(countingTreeMap.toString(indent + "  "));
        }

        return builder.toString();
    }

    @Override public String toString() {
        return toString("");

    }

    public int getDepth() {
        if (parent == null) {
            return 0;
        }
        else {
            return parent.getDepth() + 1;
        }

    }

    public String getPath() {
        if (parent == null) {
            return getKey() != null ? getKey() : "";
        }
        else {
            return parent.getPath() + "/" + getKey();
        }
    }

    public boolean isLeaf() {
        return children.isEmpty();
         
    }

    

}
