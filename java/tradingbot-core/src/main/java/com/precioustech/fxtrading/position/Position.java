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
package com.precioustech.fxtrading.position;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class Position<T> {

	private final TradeableInstrument<T> instrument;
	private final long units;
	private final TradingSignal side;
	private final double averagePrice;

	public Position(TradeableInstrument<T> instrument, long units, TradingSignal side, double averagePrice) {
		this.instrument = instrument;
		this.units = units;
		this.side = side;
		this.averagePrice = averagePrice;
	}

	public TradeableInstrument<T> getInstrument() {
		return instrument;
	}

	public long getUnits() {
		return units;
	}

	public TradingSignal getSide() {
		return side;
	}

	public double getAveragePrice() {
		return averagePrice;
	}
}
