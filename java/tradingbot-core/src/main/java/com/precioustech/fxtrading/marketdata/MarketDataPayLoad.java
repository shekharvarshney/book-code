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
package com.precioustech.fxtrading.marketdata;

import org.joda.time.DateTime;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class MarketDataPayLoad<T> {
	private final double bidPrice, askPrice;
	private final TradeableInstrument<T> instrument;
	private final DateTime eventDate;

	public MarketDataPayLoad(TradeableInstrument<T> instrument, double bidPrice, double askPrice, DateTime eventDate) {
		this.bidPrice = bidPrice;
		this.askPrice = askPrice;
		this.instrument = instrument;
		this.eventDate = eventDate;
	}

	public double getBidPrice() {
		return bidPrice;
	}

	public double getAskPrice() {
		return askPrice;
	}

	public TradeableInstrument<T> getInstrument() {
		return instrument;
	}

	public DateTime getEventDate() {
		return eventDate;
	}
}
