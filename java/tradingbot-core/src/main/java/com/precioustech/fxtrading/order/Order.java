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
package com.precioustech.fxtrading.order;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

/**
 * 
 * @author Shekhar Varshney
 *
 * @param <M>
 *            The type of instrumentId in class TradeableInstrument
 * @param <N>
 *            The type of orderId
 * @see TradeableInstrument
 */
public class Order<M, N> {
	private final TradeableInstrument<M> instrument;
	private final long units;
	private final TradingSignal side;
	private final OrderType type;
	private final double takeProfit;
	private final double stopLoss;
	private N orderId;
	private final double price;

	/*
	 * orderId not included in constructor because normally it is assigned by the platform only after order is placed successfully.
	 */
	public Order(TradeableInstrument<M> instrument, long units, TradingSignal side, OrderType type, double price) {
		this(instrument, units, side, type, 0.0, 0.0, price);
	}

	public Order(TradeableInstrument<M> instrument, long units, TradingSignal side, OrderType type) {
		this(instrument, units, side, type, 0.0, 0.0);
	}

	public Order(TradeableInstrument<M> instrument, long units, TradingSignal side, OrderType type, double takeProfit,
			double stopLoss) {
		this(instrument, units, side, type, takeProfit, stopLoss, 0.0);
	}

	public Order(TradeableInstrument<M> instrument, long units, TradingSignal side, OrderType type, double takeProfit,
			double stopLoss, double price) {
		this.instrument = instrument;
		this.units = units;
		this.side = side;
		this.type = type;
		this.takeProfit = takeProfit;
		this.stopLoss = stopLoss;
		this.price = price;
	}

	public N getOrderId() {
		return orderId;
	}

	public void setOrderId(N orderId) {
		this.orderId = orderId;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public double getPrice() {
		return price;
	}

	public TradeableInstrument<M> getInstrument() {
		return instrument;
	}

	public long getUnits() {
		return units;
	}

	public TradingSignal getSide() {
		return side;
	}

	public OrderType getType() {
		return type;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

	@Override
	public String toString() {
		return "Order [instrument=" + instrument + ", units=" + units + ", side=" + side + ", type=" + type
				+ ", takeProfit=" + takeProfit + ", stopLoss=" + stopLoss + ", orderId=" + orderId + ", price=" + price
				+ "]";
	}
}
