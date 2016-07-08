package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Stores configuration details for expression that can combine values from different named aggregations.
 */
public class ExpressionConfiguration extends Observable {

    private ObservableInteger id = createIntProperty("id", -1);
    private ObservableProperty<String> name = createStringProperty("name", null);

    private ObservableProperty<String> expression = createStringProperty("expression", null);
    private ObservableProperty<String> groupBy = createStringProperty("groupBy", null);

    public ObservableProperty<String> getGroupBy() {
        return groupBy;
    }

    public ObservableProperty<String> getName() {
        return name;
    }

    public ObservableInteger getId() {
        return id;
    }

    public ObservableProperty<String> getExpression() {
        return expression;
    }
}
