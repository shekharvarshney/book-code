package com.precioustech.fxtrading.remotecontrol;

import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.ObjectWrapper;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.events.notification.email.EmailPayLoad;
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

	}

	@Override
	public void suspendTradingInInstrument(TradeableInstrument<N> instrument) {

	}

	@Override
	public void resumeTradingInInstrument(TradeableInstrument<N> instrument) {

	}

	@Override
	public void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal) {

	}

	@Override
	public void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal, long units) {

	}

	@Override
	public void placeMarketOrder(TradeableInstrument<N> instrument, TradingSignal signal, long units, M accountId) {

	}

	@Override
	public void stopTrailingTransaction(L transactionId) {
		this.eventBus.post(new ObjectWrapper<L>(transactionId));
	}

	@Override
	public void ping(String response) {
		this.eventBus.post(new EmailPayLoad("Ping Response from TradingBot", response));
	}

}
