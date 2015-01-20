package com.logginghub.logging.frontend.analysis;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WildcardMatcher;

public class SourceWildcardChunkedResultFilter implements ChunkedResultFilter, LabelOverride
{
    private WildcardMatcher matcher;
    private String legend;

    public SourceWildcardChunkedResultFilter()
    {
        
    }
    
    public void setPattern(String pattern)
    {
        matcher = new WildcardMatcher(pattern);
    }
    
    public boolean passes(ChunkedResult result)
    {  
        return matcher.matches(result.getSource());
    }

    public void setLegend(String legend)
    {
        this.legend = legend;
    }

    public String getLegend()
    {
        return legend;
    }

    public String getLabel()
    {
        return legend;
    }
    
    @Override public String toString()
    {
        return StringUtils.reflectionToString(this);
    }
}
