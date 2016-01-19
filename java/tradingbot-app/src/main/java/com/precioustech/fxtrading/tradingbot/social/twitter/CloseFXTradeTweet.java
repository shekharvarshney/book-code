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

import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class CloseFXTradeTweet<T> extends FXTradeTweet<T> {
	private final double profit, price;

	public CloseFXTradeTweet(TradeableInstrument<T> instrument, double profit, double price) {
		super(instrument, price);
		this.profit = profit;
		this.price = price;
	}

	public double getProfit() {
		return profit;
	}

	@Override
	public double getPrice() {
		return price;
	}

}
