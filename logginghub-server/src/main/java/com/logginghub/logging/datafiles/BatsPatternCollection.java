package com.logginghub.logging.datafiles;

import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.modules.PatternCollection;

public class BatsPatternCollection extends PatternCollection {

    public BatsPatternCollection() {
        add(new PatternModel(0,
                             "onAddOrder",
                             "onAddOrder processed successfully in {time} ms : timestamp = '[timestamp]', daySpecificOrderId = '[orderId]', isBuy = '[isBuy]', sharesBeingAddedToBook = '[shares]', symbol = '[symbol]', price = '[price]'"));
        add(new PatternModel(1,
                             "onLevel1Price",
                             "onLevel1Price instrumentId='[symbol]' bid='[bid]' ask='[ask]' bidQuantity='[bidQuantity]', askQuantity='[askQuantity]', bidOrders='[bidOrders]', askOrders='[askOrders]'"));
        add(new PatternModel(2,
                             "onOrderCancelled",
                             "onOrderCancelled processed successfully in {time} ms : timestamp = '[timestamp]', daySpecificOrderId = '[orderId]', sharesBeingCancelled = '[shares]'"));
        add(new PatternModel(3,
                             "onOrderExecuted",
                             "onOrderExecuted processed successfully in {time} ms : timestamp = '[timestamp]', daySpecificOrderId = '[orderId]', sharesBeingExecuted = '[shares]', executionId = '[executionId]', executionFlags = '[flags]'"));
        add(new PatternModel(4,
                             "onTradeExecuted",
                             "onTradeExecuted : timestamp = '[timestamp]', obfuscatedOrderId = '[obfuscatedOrderId]', isBuy = '[isBuy]', sharesBeingExecuted = '[shares]', symbol = '[symbol]', price = '[price]', dayUniqueExecutionId = '[executionId]', tradeFlags = '[tradeFlags]'"));
        add(new PatternModel(5,
                             "onSymbolClear",
                             "onSymbolClear : timestamp = '[timestamp]', symbol = '[symbol]'"));
        add(new PatternModel(6,
                             "onStatistics",
                             "onStatistics : timestamp = '[timestamp]', symbol = '[symbol]', price = '[price]', statisticType = '[statisticType]', priceDetermination = '[priceDetermination]'"));
        add(new PatternModel(7,
                             "onTradingStatus",
                             "onTradingStatus : timestamp = '[timestamp]', symbol = '[symbol]', status = '[status]', reserved = '[reserved]'"));
        add(new PatternModel(8,
                             "onAuctionUpdate",
                             "onAuctionUpdate : timestamp = '[timestamp]', symbol = '[symbol]', auctionType = '[auctionType]', referencePrice = '[referencePrice]', buyShares= '[buyShares]', sellShares = '[sellShares]', indicativePrice = '[indicativePrice]'"));

    }
}
