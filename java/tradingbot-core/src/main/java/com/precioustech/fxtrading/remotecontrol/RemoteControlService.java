package com.precioustech.fxtrading.remotecontrol;

import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class RemoteControlService<M, N, L> implements RemoteControl<M, N, L> {

	private final EventBus eventBus;

	public RemoteControlService(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void suspendTrading() {
		this.eventBus.post(Boolean.FALSE);
	}

	@Override
	public void resumeTrading() {
		this.eventBus.post(Boolean.TRUE);
	}

	@Override
	public void suspendTradingInCurrency(String currency) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resumeTradingInCurrency(String currency) {
		// TODO Auto-generated method stub

	}

	@Override
	public void suspendTradingInInstrument(TradeableInstrument<N> instrument) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resumeTradingInInstrument(TradeableInstrument<N> instrument) {
		// TODO Auto-generated method stub

	}

	@Override
	public void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal, long units) {
		// TODO Auto-generated method stub

	}

	@Override
	public void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal, long units, M accountId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopTrailingTransaction(L transactionId) {

	}

	@Override
	public String ping() {
		return null;
	}

}
