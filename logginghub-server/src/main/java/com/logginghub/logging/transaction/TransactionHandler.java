package com.logginghub.logging.transaction;

public interface TransactionHandler {
    void onTransactionSucceeded(TransactionModel transactionModel);
    void onTransactionTimedOut(TransactionModel transactionModel);
}
