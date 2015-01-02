package com.logginghub.utils.filter;

public class StringContainsFilter implements Filter<String> {

    private String filterText;
    private boolean caseSensitive;

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setValue(String filterText) {
        this.filterText = filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public String getFilterText() {
        return filterText;
    }

    @Override public boolean passes(String t) {
        boolean passes;

        if (filterText == null) {
            passes = true;
        }
        else {

            if (caseSensitive) {
                passes = t.contains(filterText);
            }
            else {
                passes = t.toLowerCase().contains(filterText.toLowerCase());
            }
        }

        return passes;

    }

}
