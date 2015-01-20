package com.logginghub.logging.utils;

import com.logginghub.logging.api.patterns.InstanceDetails;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.filter.StringContainsFilter;

public class InstanceDetailsContainsFilter implements Filter<InstanceDetails> {
        
    private StringContainsFilter portFilter = new StringContainsFilter();
    private StringContainsFilter hostFilter = new StringContainsFilter();
    private StringContainsFilter nameFilter = new StringContainsFilter();
    private StringContainsFilter pidFilter = new StringContainsFilter();
    private StringContainsFilter ipFilter = new StringContainsFilter();

    public InstanceDetailsContainsFilter() {
        portFilter.setCaseSensitive(false);
        hostFilter.setCaseSensitive(false);
        nameFilter.setCaseSensitive(false);
        pidFilter.setCaseSensitive(false);
        ipFilter.setCaseSensitive(false);
    }
    
    public void setPortFilter(String portFilter) {
        this.portFilter.setValue(portFilter);
    }

    public void setHostFilter(String hostFilter) {
        this.hostFilter.setValue(hostFilter);
    }

    public void setNameFilter(String nameFilter) {
        this.nameFilter.setValue(nameFilter);
    }

    public void setPidFilter(String pidFilter) {
        this.pidFilter.setValue(pidFilter);
    }

    public void setIPFilter(String ipFilter) {
        this.ipFilter.setValue(ipFilter);
    }

    @Override public boolean passes(InstanceDetails t) {

        boolean passes = portFilter.passes(Integer.toString(t.getLocalPort())) &&
                         nameFilter.passes(t.getInstanceName()) &&
                         pidFilter.passes(Integer.toString(t.getPid())) &&
                         hostFilter.passes(t.getHostname()) &&
                         ipFilter.passes(t.getHostIP());

        return passes;

    }

}
