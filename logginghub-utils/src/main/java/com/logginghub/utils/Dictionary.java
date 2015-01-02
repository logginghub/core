package com.logginghub.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Dictionary {

    private AtomicInteger nextID = new AtomicInteger(0);

    private Map<String, Integer> dictionary = new HashMap<String, Integer>();
    private Map<Integer, String> reverseDictionary = new HashMap<Integer, String>();

    public int getID(String word) {
        Integer id = dictionary.get(word);
        if (id == null) {
            id = nextID.getAndIncrement();
            set(id, word);
        }
        return id;
    }

    public String getString(int id) {
        return reverseDictionary.get(id);
    }

    private void set(int id, String word) {
        dictionary.put(word, id);
        reverseDictionary.put(id, word);
    }

    public void writeTo(File dictionaryFile) {
        // TODO : use SOF
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dictionaryFile)));

            Set<Entry<Integer, String>> entrySet = reverseDictionary.entrySet();
            dos.writeInt(entrySet.size());

            for (Entry<Integer, String> entry : entrySet) {
                dos.writeInt(entry.getKey());

                String value = entry.getValue();
                if (value == null) {
                    dos.writeInt(-1);
                }
                else {
                    dos.writeInt(entry.getValue().getBytes().length);
                    dos.write(entry.getValue().getBytes());
                }
            }

        }
        catch (IOException e) {
            throw new FormattedRuntimeException(e, "Failed to write dictionary file to '{}'", dictionaryFile.getAbsolutePath());
        }
        finally {
            FileUtils.closeQuietly(dos);
        }
    }

    public static Dictionary readFrom(File dictionaryFile) {
        Dictionary dictionary = new Dictionary();

        // TODO : use SOF
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(dictionaryFile)));

            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                int key = dis.readInt();

                String value;

                int stringLength = dis.readInt();
                if (stringLength == -1) {
                    value = null;
                }else{
                    int length = dis.readInt();
                    byte[] data = new byte[length];
                    dis.read(data);
                    value = new String(data);
                }

                dictionary.set(key, value);
            }

        }
        catch (IOException e) {
            throw new FormattedRuntimeException(e, "Failed to read from dictionary file '{}'", dictionaryFile.getAbsolutePath());
        }
        finally {
            FileUtils.closeQuietly(dis);
        }

        return dictionary;
    }
}
