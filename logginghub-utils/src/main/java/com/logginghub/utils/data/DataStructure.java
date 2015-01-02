package com.logginghub.utils.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class DataStructure implements SerialisableObject {

    private int type;

    private Map<Integer, DataElement> keyElements = new HashMap<Integer, DataElement>();
    private Map<Integer, DataElement> valueElements = new HashMap<Integer, DataElement>();


    public enum Keys {
        // remember not to change the order!!
        host,
        ip,
        processName,
        pid,
        interfaceName, 
        device
        // remember not to change the order!!

    }

    public enum Values {
        
        // remember not to change the order!!
        SIGAR_OS_Cpu_User_Time("User time"),
        SIGAR_OS_Cpu_System_Time("System time"),
        SIGAR_OS_Cpu_Wait_Time("Wait time"),
        SIGAR_OS_Cpu_Idle_Time("Idle time"),
        SIGAR_OS_Network_Bytes_Sent("Network sent"),
        SIGAR_OS_Network_Bytes_Received("Network received"),
        SIGAR_OS_Memory_Actual_Free("Actual free"),
        SIGAR_OS_Memory_Actual_Used("Actual used"),
        SIGAR_OS_Memory_Free("Memory free"),
        SIGAR_OS_Memory_Free_Percent("Memory free (%)"),
        SIGAR_OS_Memory_Ram("Memory RAM"),
        SIGAR_OS_Memory_Total("Memory total"),
        SIGAR_OS_Memory_Used("Memory used"),
        SIGAR_OS_Memory_Used_Percent("Memory used (%)"),
        SIGAR_OS_Process_Cpu_User_Time("Process user time"),
        SIGAR_OS_Process_Cpu_System_Time("Process system time"),
        SIGAR_OS_Process_Cpu_Percentage("Process CPU percentage"),
        SIGAR_OS_Process_Memory_Size("Process memory size"),
        SIGAR_OS_Process_Memory_Resident("Prcocess memory resident"),
        JVM_Process_Memory_Maximum("JVM maximum memory"),
        JVM_Process_Memory_Total("JVM total memory"),
        JVM_Process_Memory_Used("JVM used memory"), 
        VMSTAT_Processes_Run_Queue("Run queue length"),
        VMSTAT_Processes_Blocking("Blocking processes"),
        VMSTAT_Memory_Swap("Memory used for swap"),
        VMSTAT_Memory_Free("Free memory (vmstat)"),
        VMSTAT_Memory_Buffers("Memory used for buffers"),
        VMSTAT_Memory_Cache("Memory used for caches"),
        VMSTAT_Memory_Inactive("Inactive cache pages"),
        VMSTAT_Memory_Active("Active cache pages"),
        VMSTAT_Swap_In("Pages swapped from disk to memory"),
        VMSTAT_Swap_Out("Pages swapped from memory to disk"),
        VMSTAT_IO_Blocks_In("IO blocks in"),
        VMSTAT_IO_Blocks_Out("IO blocks out"),
        VMSTAT_System_Interupts("Interupts"),
        VMSTAT_System_Context_Switches("Context switches"),
        VMSTAT_CPU_User("User time (vmstat)"),
        VMSTAT_CPU_System("System time (vmstat)"),
        VMSTAT_CPU_Idle("Idle time (vmstat)"),
        VMSTAT_CPU_Waiting("Wait time (vmstat)"),        
        IOSTAT_Reads_Requested("Number of reads requested"), 
        IOSTAT_Writes_Requested("Number of writes requested"), 
        IOSTAT_Reads_Completed("Number of reads completed"), 
        IOSTAT_Writes_Completed("Number of writes completed"), 
        IOSTAT_Read_Amount("Sectors read (kb)"),
        IOSTAT_Write_Amount("Sectors written (kb)"), 
        IOSTAT_Average_Request_Size("Average request size"), 
        IOSTAT_Average_Request_Queue_Length("Average length of the request queue"), 
        IOSTAT_Request_Served_Time("Average time for I/O requests to be served"), 
        IOSTAT_Read_Requests_Served_Time("Average time for read requests to be serviced"), 
        IOSTAT_Write_Requests_Served_Time("Average time for write requests to be serviced"), 
        IOSTAT_Service_Time("Average service time for requests - DO NOT TRUST THIS FIELD!"), 
        IOSTAT_Device_Utilisation("Percentage of CPU time spent issuing requests"), 
        Top("Output from top"), 
        Netstat("Output from netstat"), 
        NetstatStatistics("Output from netstat --statistics"),
        Custom("User defined content"),
        VMSTAT_CPU_Stolen("Stolen time (vmstat)");
        // remember not to change the order!!
        
        private Values(String description2) {
            this.description = description2;
        }
        
        private String description;
        
        public String getDescription() {
            return description;
        }
    } 

    /**
     * Serialisation constructor.
     */
    public DataStructure() {}

    public DataStructure(Types type) {
        this.type = type.ordinal();
    }

    public DataStructure(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    // TODO : use fixed integers in the enum ctor
    // DO NOT REORDER THIS ENUM
    public enum Types {
        Telemetry, 
        PatternisedLogEvent
    }

    public void addKey(Keys key, int value) {
        keyElements.put(key.ordinal(), new DataElement(value, DataElement.Type.Int));
    }

    public void addKey(Keys key, long value) {
        keyElements.put(key.ordinal(), new DataElement(value, DataElement.Type.Long));
    }

    public void addKey(Keys key, String value) {
        keyElements.put(key.ordinal(), new DataElement(value, DataElement.Type.String));
    }

    public void addValue(Values data, int value) {
        valueElements.put(data.ordinal(), new DataElement(value, DataElement.Type.Int));
    }
    
    public void addValue(Values data, String value) {
        valueElements.put(data.ordinal(), new DataElement(value, DataElement.Type.String));
    }
    
    public void addValue(Values data, double value) {
        valueElements.put(data.ordinal(), new DataElement(value, DataElement.Type.Double));
    }

    public void addValue(Values data, long value) {
        valueElements.put(data.ordinal(), new DataElement(value, DataElement.Type.Long));
    }
    
    public void addValue(Values data, DataStructure value) {
        valueElements.put(data.ordinal(), new DataElement(value, DataElement.Type.DataStructure));
    }

    public static DataStructure fromByteArray(byte[] encoded) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
        DataInputStream dis = new DataInputStream(bais);

        DataStructure dataStructure;

        int version = dis.read();
        if (version == 1) {
            int type = dis.read();
            dataStructure = new DataStructure(type);

            int keys = bais.read();            
            for (int i = 0; i < keys; i++) {
                int keyIndex = dis.readInt();
                DataElement element = DataElement.read(dis);
                dataStructure.keyElements.put(keyIndex, element);
            }

            int values = bais.read();
            for (int i = 0; i < values; i++) {
                int valueIndex = dis.readInt();
                DataElement element = DataElement.read(dis);
                dataStructure.valueElements.put(valueIndex, element);
            }
        }
        else {
            throw new IllegalArgumentException("Dont know how to decode version " + version);
        }

        return dataStructure;
    }

    public byte[] toByteArray() throws IOException {

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(boas);

        int version = 1;
        dos.write(version);
        if (type > 255) {
            throw new IllegalArgumentException("Type is greater than a byte, need to add a new encoding type!");
        }
        dos.write(type);

        Set<Entry<Integer, DataElement>> entrySet = keyElements.entrySet();
        int keyCount = entrySet.size();
        dos.write(keyCount);
        for (Entry<Integer, DataElement> entry : entrySet) {
            dos.writeInt(entry.getKey());
            entry.getValue().write(dos);
        }

        entrySet = valueElements.entrySet();
        int valueCount = entrySet.size();
        dos.write(valueCount);
        for (Entry<Integer, DataElement> entry : entrySet) {
            dos.writeInt(entry.getKey());
            entry.getValue().write(dos);
        }

        return boas.toByteArray();
    }

    public DataElement getKey(int i) {
        return keyElements.get(i);
    }

    public DataElement getKey(Keys key) {
        return keyElements.get(key.ordinal());
    }

    public DataElement getValue(Values value) {
		return valueElements.get(value.ordinal());
	}

	public DataElement getValue(int i) {
		return valueElements.get(i);
	}

	public void addKey(int i, String value) {
		keyElements.put(i, new DataElement(value, DataElement.Type.String));
	}

	public void addValue(int i, String value) {
		valueElements.put(i, new DataElement(value, DataElement.Type.String));
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();

		builder.append(getTypeDescription());

		builder.append(" [");
		String div = "";
		Set<Entry<Integer, DataElement>> entrySet = keyElements.entrySet();
		for (Entry<Integer, DataElement> entry : entrySet) {

			String keyDescription = getKeyDescription(entry.getKey());
			String objectDescription = entry.getValue().toString();

			builder.append(div);
			builder.append(keyDescription).append(" = '").append(objectDescription).append("'");
			div = ", ";
		}

		builder.append("] {");

		div = "";
		entrySet = valueElements.entrySet();
		for (Entry<Integer, DataElement> entry : entrySet) {

			String keyDescription = getValueDescription(entry.getKey());
			String objectDescription = entry.getValue().toString();

			builder.append(div);
			builder.append(keyDescription).append(" = '").append(objectDescription).append("'");
			div = ", ";
		}

		builder.append("}");

		return builder.toString();

	}

	private String getKeyDescription(int key) {
		String description;
		if (key >= 0 && key < Keys.values().length) {
			description = Keys.values()[key].name();
		} else {
			description = Integer.toString(key);
		}

		return description;
	}

	private String getValueDescription(int value) {
		String description;
		if (value >= 0 && value < Values.values().length) {
			description = Values.values()[value].name();
		} else {
			description = Integer.toString(value);
		}

		return description;

	}

	private String getTypeDescription() {
		String description;
		if (type >= 0 && type < Types.values().length) {
			description = Types.values()[type].name();
		} else {
			description = Integer.toString(type);
		}

		return description;

	}

	public Double getDoubleValue(Values value) {
		DataElement element = getValue(value);
		Double result;
		if (element != null) {
			result = element.asDouble();
		} else {
			result = null;
		}

		return result;
	}

	public String getStringValue(Values value) {
		DataElement element = getValue(value);
		String result;
		if (element != null) {
			result = element.asString();
		} else {
			result = null;
		}

		return result;
	}

	public String getStringKey(Keys key) {
		DataElement element = getKey(key);
		String result;
		if (element != null) {
			result = element.asString();
		} else {
			result = null;
		}

		return result;

	}

	public Long getLongValue(Values value) {
		DataElement element = getValue(value);
		Long result;
		if (element != null) {
			result = element.asLong();
		} else {
			result = null;
		}

		return result;
	}

	public Integer getIntValue(Values value) {
		DataElement element = getValue(value);
		Integer result;
		if (element != null) {
			result = element.asInteger();
		} else {
			result = null;
		}

		return result;
	}

	public Long getLongKey(Keys key) {
		DataElement element = getKey(key);
		Long result;
		if (element != null) {
			result = element.asLong();
		} else {
			result = null;
		}

		return result;

	}

	public Set<Values> getValueKeys() {
		Set<Integer> keySet = valueElements.keySet();
		Set<Values> values = new HashSet<DataStructure.Values>();
		for (Integer integer : keySet) {
			values.add(Values.values()[integer]);
		}
		return values;
	}

	public boolean containsValue(Values value) {
		return valueElements.containsKey(value.ordinal());
	}

	public boolean containsKey(Keys key) {
		return keyElements.containsKey(key.ordinal());
	}


    public void read(SofReader reader) throws SofException {
        int field = 0;
        
        this.type = reader.readInt(field++);
        
        int keyCount = reader.readInt(field++);
        for(int i = 0; i < keyCount; i++){
            Integer key = reader.readIntObject(field++);
            DataElement value = (DataElement) reader.readObject(field++);

            this.keyElements.put(key, value);
        }
        
        int dataCount = reader.readInt(field++);
        for(int i = 0; i < dataCount; i++){
            int key = reader.readIntObject(field++);
            DataElement value = (DataElement) reader.readObject(field++);

            this.valueElements.put(key, value);
        }
    }

    public void write(SofWriter writer) throws SofException {
        // TODO : this adhoc encoding isn't really good for anyone, we really should add Map to the
        // list of things sof can encode/decode
        int field = 0;

        writer.write(field++, type);
        
        Set<Entry<Integer, DataElement>> keySet = keyElements.entrySet();
        int keyCount = keySet.size();
        writer.write(field++, keyCount);
        for (Entry<Integer, DataElement> entry : keySet) {
            writer.write(field++, entry.getKey());
            writer.write(field++, entry.getValue());
        }

        Set<Entry<Integer, DataElement>> dataSet = valueElements.entrySet();
        int dataCount = dataSet.size();
        writer.write(field++, dataCount);
        for (Entry<Integer, DataElement> entry : dataSet) {
            writer.write(field++, entry.getKey());
            writer.write(field++, entry.getValue());
        }
    }

}
