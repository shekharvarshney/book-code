package com.precioustech.fxtrading.tradingbot.strategies;

import com.precioustech.fxtrading.TradingDecision;

public interface ProbabilisticTradeReview<T> {

	boolean shouldProceed(TradingDecision<T> tradingDecision);
}
