package com.precioustech.fxtrading.remotecontrol;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public interface RemoteControl<M, N, L> {

	void suspendTrading();

	void resumeTrading();

	String ping();

	void suspendTradingInCurrency(String currency);

	void resumeTradingInCurrency(String currency);

	void suspendTradingInInstrument(TradeableInstrument<N> instrument);

	void resumeTradingInInstrument(TradeableInstrument<N> instrument);

	void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal);

	void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal, long units);

	void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal, long units, M accountId);

	void stopTrailingTransaction(L transactionId);
}
