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
package com.precioustech.fxtrading.marketdata.historic;

import org.joda.time.DateTime;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class CandleStick<T> {
	/*All prices are average of bid and ask ,i.e (bid+ask)/2*/
	private final double openPrice, highPrice, lowPrice, closePrice;
	private final DateTime eventDate;
	private final TradeableInstrument<T> instrument;
	private final CandleStickGranularity candleGranularity;
	private final String toStr;
	private final int hash;

	public CandleStick(double openPrice, double highPrice, double lowPrice, double closePrice, DateTime eventDate,
			TradeableInstrument<T> instrument, CandleStickGranularity candleGranularity) {
		super();
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.closePrice = closePrice;
		this.eventDate = eventDate;
		this.instrument = instrument;
		this.candleGranularity = candleGranularity;
		this.hash = calcHash();
		this.toStr = String.format(
				"Open=%2.5f, high=%2.5f, low=%2.5f,close=%2.5f,date=%s, instrument=%s, granularity=%s", openPrice,
				highPrice, lowPrice, closePrice, eventDate, instrument, candleGranularity.name());
	}

	private int calcHash() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((candleGranularity == null) ? -1 : candleGranularity.ordinal());
		result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
		result = prime * result + ((instrument == null) ? 0 : instrument.hashCode());
		return result;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CandleStick other = (CandleStick) obj;
		if (candleGranularity != other.candleGranularity)
			return false;
		if (eventDate == null) {
			if (other.eventDate != null)
				return false;
		} else if (!eventDate.equals(other.eventDate))
			return false;
		if (instrument == null) {
			if (other.instrument != null)
				return false;
		} else if (!instrument.equals(other.instrument))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.toStr;
	}

	public CandleStickGranularity getCandleGranularity() {
		return candleGranularity;
	}

	public TradeableInstrument<T> getInstrument() {
		return instrument;
	}

	public double getOpenPrice() {
		return openPrice;
	}

	public double getHighPrice() {
		return highPrice;
	}

	public double getLowPrice() {
		return lowPrice;
	}

	public double getClosePrice() {
		return closePrice;
	}

	public DateTime getEventDate() {
		return eventDate;
	}
}
