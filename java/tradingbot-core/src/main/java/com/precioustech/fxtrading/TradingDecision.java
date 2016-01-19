/*
 *  Copyright 2015 Shekhar Varshney
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.precioustech.fxtrading;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class TradingDecision<T> {
	private final TradingSignal signal;
	// private final double bidPriceAtDecision, askPriceAtDecision;
	private final TradeableInstrument<T> instrument;
	private final double takeProfitPrice;
	private final double stopLossPrice;
	private final SRCDECISION tradeSource;
	private final double limitPrice;

	public enum SRCDECISION {
		/*INTERNAL,*/SOCIAL_MEDIA, SPIKE, FADE_THE_MOVE, OTHER/*, CCY_EVENT*/
	}

	public TradingDecision(TradeableInstrument<T> instrument, TradingSignal signal) {
		this(instrument, signal, SRCDECISION.OTHER);
	}

	public TradingDecision(TradeableInstrument<T> instrument, TradingSignal signal, SRCDECISION tradeSource) {
		this(instrument, signal, 0.0, SRCDECISION.OTHER);
	}

	public TradingDecision(TradeableInstrument<T> instrument, TradingSignal signal, double takeProfitPrice) {
		this(instrument, signal, takeProfitPrice, SRCDECISION.OTHER);
	}

	public TradingDecision(TradeableInstrument<T> instrument, TradingSignal signal, double takeProfitPrice,
			SRCDECISION tradeSource) {
		this(instrument, signal, takeProfitPrice, 0.0, tradeSource);
	}

	public TradingDecision(TradeableInstrument<T> instrument, TradingSignal signal, double takeProfitPrice,
			double stopLossPrice) {
		this(instrument, signal, takeProfitPrice, stopLossPrice, SRCDECISION.OTHER);
	}

	public TradingDecision(TradeableInstrument<T> instrument, TradingSignal signal, double takeProfitPrice,
			double stopLossPrice, SRCDECISION tradeSource) {
		this(instrument, signal, takeProfitPrice, stopLossPrice, 0.0, tradeSource);
	}

	public TradingDecision(TradeableInstrument<T> instrument, TradingSignal signal, double takeProfitPrice,
			double stopLossPrice, double limitPrice) {
		this(instrument, signal, takeProfitPrice, stopLossPrice, limitPrice, SRCDECISION.OTHER);
	}

	public TradingDecision(TradeableInstrument<T> instrument, TradingSignal signal, double takeProfitPrice,
			double stopLossPrice, double limitPrice, SRCDECISION tradeSource) {
		this.signal = signal;
		this.instrument = instrument;
		this.limitPrice = limitPrice;
		this.tradeSource = tradeSource;
		this.takeProfitPrice = takeProfitPrice;
		this.stopLossPrice = stopLossPrice;
	}

	public SRCDECISION getTradeSource() {
		return tradeSource;
	}

	public double getLimitPrice() {
		return this.limitPrice;
	}

	public double getTakeProfitPrice() {
		return this.takeProfitPrice;
	}

	public double getStopLossPrice() {
		return this.stopLossPrice;
	}

	public TradeableInstrument<T> getInstrument() {
		return instrument;
	}

	public TradingSignal getSignal() {
		return signal;
	}
}
