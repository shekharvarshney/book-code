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
package com.precioustech.fxtrading.tradingbot.social.twitter;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class NewFXTradeTweet<T> extends FXTradeTweet<T> {
	private final double stopLoss, takeProfit;
	private final TradingSignal action;
	private final String str;

	public NewFXTradeTweet(TradeableInstrument<T> instrument, double price, double stopLoss, double takeProfit,
			TradingSignal action) {
		super(instrument, price);
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
		this.action = action;
		this.str = String.format("%s@%3.5f TP: %3.5f: SL: %3.5f %s", instrument.getInstrument(), price, takeProfit,
				stopLoss, action.name());
	}

	@Override
	public String toString() {
		return str;
	}

	public TradingSignal getAction() {
		return this.action;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

}
