package com.logginghub.logging.commandline;

import java.net.InetSocketAddress;
import java.util.List;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.api.patterns.PatternManagementAPI;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.Out;
import com.logginghub.utils.Result;

public class Verifier {
    public static void main(String[] args) throws ConnectorException {

        SocketClient client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress("vl-ec2", 15000));
        client.setAutoSubscribe(false);
        
        client.connect();
        Out.out("Connected");
        
        PatternManagementAPI patternManagementAPI = client.getPatternManagementAPI();
        requestPatternList(patternManagementAPI);
        
        Result<List<Aggregation>> result = patternManagementAPI.getAggregations();
        Out.out("Result was : {}", result.isSuccessful());
        
        if(result.isSuccessful()) {
            
            List<Aggregation> value = result.getValue();
            Out.out("Aggregation count : {}", value.size());
            
            for (Aggregation aggregation : value) {
                Out.out("{}", aggregation);
            }
        }
        
        Aggregation template = new Aggregation();
        template.setCaptureLabelIndex(6);
        template.setGroupBy("{event.sourceHost}");
        template.setInterval(1000);
        template.setPatternID(1);
        template.setType(AggregationType.LastValue);
        
        Result<Aggregation> createResult = patternManagementAPI.createAggregation(template);
        Out.out(createResult);
        
        
        client.close();
        
    }

    private static void requestPatternList(PatternManagementAPI patternManagementAPI) {
        Result<List<Pattern>> result = patternManagementAPI.getPatterns();
        Out.out("Result was : {}", result.isSuccessful());
                
        if(result.isSuccessful()) {
            
            List<Pattern> value = result.getValue();
            Out.out("Pattern count : {}", value.size());
            
            for (Pattern pattern : value) {
                Out.out("{}", pattern);
            }
        }
    }
}
