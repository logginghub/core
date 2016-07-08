package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapMessage implements SerialisableObject {

    private Map<String, String> content = new HashMap<String, String>();

    public MapMessage(String key, String data) {
        put(key, data);
    }
    
    public MapMessage() {}

    public void put(String key, String data) {
        content.put(key, data);
    }
    
    public void put(String key, int value) {
        content.put(key, Integer.toString(value));
    }
    
    public void put(String key, long value) {
        content.put(key, Long.toString(value));
    }
    
    public void put(String key, Enum<?> enumValue) {
        content.put(key, enumValue.name());
    }

    public String get(String key) {
        return content.get(key);
    }

    public void read(SofReader reader) throws SofException {
        int index = 0;
        int count = reader.readInt(index++);
        for(int i = 0; i < count; i++) {
            String key = reader.readString(index++);
            String value = reader.readString(index++);
            content.put(key, value);
        }
    }

    public void write(SofWriter writer) throws SofException {
        int index = 0;
        writer.write(index++, content.size());
        Set<Entry<String, String>> entrySet = content.entrySet();
        Iterator<Entry<String, String>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Entry<String, String> next = iterator.next();
            writer.write(index++, next.getKey());
            writer.write(index++, next.getValue());
        }
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));         
    }
    
    public long getLong(String key) {
        return Long.parseLong(get(key));         
    }

    

    

    

}
