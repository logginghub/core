package com.logginghub.logging.frontend.charting;

import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.charting.model.AggregationConfiguration;
import com.logginghub.logging.frontend.charting.model.ExpressionConfiguration;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.charting.model.Stream;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableListAdaptor;
import com.logginghub.utils.observable.ObservablePropertyListener;
import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract out the processing for expressions from the {@link NewChartingController}
 */
public class ExpressionController {

    private static final Logger logger = Logger.getLoggerFor(ExpressionController.class);
    private Stream<ChunkedResult> stream = new Stream<ChunkedResult>();
    private String expression;

    private Map<String, VariableState> latestVariableValues = new HashMap<String, VariableState>();
    private ExpressionConfiguration expressionConfiguration;

    public void bind(ExpressionConfiguration expressionConfiguration, final NewChartingController chartingController, final NewChartingModel model) {
        this.expressionConfiguration = expressionConfiguration;

        final StreamListener<ChunkedResult> resultStreamListener = new StreamListener<ChunkedResult>() {
            @Override
            public void onNewItem(ChunkedResult chunkedResult) {
                processUpdate(chunkedResult);
            }
        };

        expressionConfiguration.getExpression().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {

            @Override
            public void onPropertyChanged(String oldValue, final String expression) {

                ExpressionController.this.expression = expression;

                model.getAggregationConfigurations().addListenerAndNotifyCurrent(new ObservableListAdaptor<AggregationConfiguration>() {
                    @Override
                    public void onAdded(final AggregationConfiguration aggregationConfiguration) {

                        aggregationConfiguration.getName().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                            @Override
                            public void onPropertyChanged(String oldValue, String aggregationName) {

                                if (expression.contains(aggregationName)) {


                                    Stream<ChunkedResult> resultStreamForAggregation = chartingController.getResultStreamForAggregation(
                                            aggregationConfiguration);


                                    logger.debug("Attaching expression controller for expression '{}' to aggregation '{}'",
                                                expression,
                                                aggregationConfiguration);
                                    resultStreamForAggregation.addListener(resultStreamListener);

                                }

                            }
                        });

                    }

                    @Override
                    public void onRemoved(AggregationConfiguration aggregationConfiguration, int index) {
                        Stream<ChunkedResult> resultStreamForAggregation = chartingController.getResultStreamForAggregation(aggregationConfiguration);
                        if (resultStreamForAggregation != null) {
                            resultStreamForAggregation.removeListener(resultStreamListener);
                        }
                    }
                });


            }
        });

        // Find named aggregations that are part of our expression

    }

    private void processUpdate(ChunkedResult aggregationResult) {

        String aggregationName = aggregationResult.getAggregationName();

        if (StringUtils.isNotNullOrEmpty(aggregationName)) {

            String groupBy = aggregationResult.getGroupBy();
            String groupByValues = aggregationResult.getSource();
            double value = aggregationResult.getValue();
            long startOfCurrentChunk = aggregationResult.getStartOfCurrentChunk();

            VariableState variableState = latestVariableValues.get(groupByValues);
            if (variableState == null) {
                variableState = new VariableState(groupByValues);
                latestVariableValues.put(groupByValues, variableState);
            }

            if (startOfCurrentChunk > variableState.farthestTime) {
                variableState.values.clear();
                variableState.farthestTime = startOfCurrentChunk;
            }

            logger.debug("Updating group '{}' with expression variables : {} : from result {}",
                        groupByValues,
                        variableState.values.toString(),
                        aggregationResult);

            variableState.values.put(aggregationName, value);

            Expression expression = new Expression(this.expression);
            for (Entry<String, Double> variableNameAndValue : variableState.values.entrySet()) {
                expression.with(variableNameAndValue.getKey(), variableNameAndValue.getValue().toString());
            }


            try {
                BigDecimal result = expression.eval();

                ChunkedResult expressionResult = new ChunkedResult(aggregationResult.getStartOfCurrentChunk(),
                                                                   aggregationResult.getStartOfCurrentChunk(),
                                                                   result.doubleValue(),
                                                                   "expression",
                                                                   expressionConfiguration.getName().get(),
                                                                   groupBy,
                                                                   groupByValues);


                logger.debug("Result : {}", expressionResult);

                stream.send(expressionResult);

            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().startsWith("Unknown operator or function")) {
                    // We have a missing expression
                    try {
                        logger.debug("Potentially missing expression in '{}'", ReflectionUtils.getField("expression", expression));
                    } catch (NoSuchFieldException e1) {
                        logger.debug(e, "Expression evaluation failed");
                    } catch (IllegalAccessException e1) {
                        logger.debug(e, "Expression evaluation failed");
                    }
                } else {
                    logger.debug(e, "Expression evaluation failed");
                }
            }
        }


    }

    public Stream<ChunkedResult> getStream() {
        return stream;
    }

    class VariableState {
        private final Map<String, Double> values = new HashMap<String, Double>();
        private final String group;
        private long farthestTime;

        VariableState(String group) {
            this.group = group;
        }
    }
}
