package com.logginghub.logging.servers;

import java.util.Arrays;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFilterFactory;
import com.logginghub.logging.LogEventFilterFactory.LogField;
import com.logginghub.logging.filters.CompositeOrFilter;
import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.utils.Is;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringMatcherFactory.MatcherType;
import com.logginghub.utils.filter.Filter;

public class FilterHelper extends CompositeOrFilter {
    
    public void addFilter(FilterConfiguration filterConfiguration) {

        Is.notNullOrEmpty(filterConfiguration.getField(),
                          "The filter 'field' property must be set to one of : {}",
                          Arrays.toString(LogField.values()));
        
        Is.notNullOrEmpty(filterConfiguration.getType(),
                          "The filter 'type' property must be set to one of : {}",
                          Arrays.toString(MatcherType.values()));
        
        Is.notNullOrEmpty(filterConfiguration.getPattern(), "The filter 'pattern' property must be set");

        LogField field = LogField.valueOf(StringUtils.capitalise(filterConfiguration.getField()));
        MatcherType type = MatcherType.valueOf(StringUtils.capitalise(filterConfiguration.getType()));
        String pattern = filterConfiguration.getPattern();

        Filter<LogEvent> filter = LogEventFilterFactory.createFilterForField(field, type, pattern);

        addFilter(filter);
    }
}
