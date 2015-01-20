package com.logginghub.logging.transaction;

import java.util.ArrayList;
import java.util.List;

public class StateEngineModel {

    private List<EngineModel> engines = new ArrayList<EngineModel>();
    private List<TransactionModel> currentTransactions = new ArrayList<TransactionModel>();
    
    public EngineModel createNewEngine() {
        EngineModel engineModel = new EngineModel();
        engines.add(engineModel);
        return engineModel;
    }
    
    public List<EngineModel> getEngines() {
        return engines;
    }

    public TransactionModel createTransaction(String transactionID, StateNodeModel startingNode) {
        TransactionModel transactionModel = new TransactionModel(transactionID, startingNode);
        synchronized (currentTransactions) {
            currentTransactions.add(transactionModel);
        }
        
        return transactionModel;
    }
    
    public List<TransactionModel> getCurrentTransactions() {
        return currentTransactions;
    }

}
