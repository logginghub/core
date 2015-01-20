package com.logginghub.logging.api.levelsetting;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class InstanceFilter implements SerialisableObject {

    private String portFilter;
    private String hostFilter;
    private String nameFilter;
    private String pidFilter;
    private String ipFilter;

    public String getPortFilter() {
        return portFilter;
    }

    public void setPortFilter(String portFilter) {
        this.portFilter = portFilter;
    }

    public String getHostFilter() {
        return hostFilter;
    }

    public void setHostFilter(String hostFilter) {
        this.hostFilter = hostFilter;
    }

    public String getNameFilter() {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
    }

    public String getPidFilter() {
        return pidFilter;
    }

    public void setPidFilter(String pidFilter) {
        this.pidFilter = pidFilter;
    }

    public String getIpFilter() {
        return ipFilter;
    }

    public void setIpFilter(String ipFilter) {
        this.ipFilter = ipFilter;
    }

    @Override public void read(SofReader reader) throws SofException {
        this.portFilter = reader.readString(0);
        this.hostFilter = reader.readString(1);
        this.nameFilter = reader.readString(2);
        this.pidFilter = reader.readString(3);
        this.ipFilter = reader.readString(4);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, portFilter);
        writer.write(1, hostFilter);
        writer.write(2, nameFilter);
        writer.write(3, pidFilter);
        writer.write(4, ipFilter);
    }

    @Override public String toString() {
        return "InstanceFilter [portFilter=" +
               portFilter +
               ", hostFilter=" +
               hostFilter +
               ", nameFilter=" +
               nameFilter +
               ", pidFilter=" +
               pidFilter +
               ", ipFilter=" +
               ipFilter +
               "]";
    }
    

}
