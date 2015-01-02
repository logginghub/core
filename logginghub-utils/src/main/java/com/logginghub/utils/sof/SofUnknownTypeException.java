package com.logginghub.utils.sof;

import com.logginghub.utils.StringUtils;

public class SofUnknownTypeException extends SofException {
    private static final long serialVersionUID = 1L;
    private int version;
    private int length;
    private int flags;
    private int type;
    private int fields;

    public SofUnknownTypeException(int version, int length, int flags, int type, int fields) {
        this.version = version;
        this.length = length;
        this.flags = flags;
        this.type = type;
        this.fields = fields;
    }

    public String getHeaderSummary() {
        return StringUtils.format("[type={} version={} length={} flags={} fields={}]", type, version, length, flags, fields);
    }

    public int getVersion() {
        return version;
    }

    public int getFields() {
        return fields;
    }

    public int getFlags() {
        return flags;
    }

    public int getLength() {
        return length;
    }

    public int getType() {
        return type;
    }
}
