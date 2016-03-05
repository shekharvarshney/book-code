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
package com.precioustech.fxtrading.account.transaction;

import org.joda.time.DateTime;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class Transaction<M, N, T> {

	private final M transactionId;
	private final Event transactionType;
	private final N accountId;
	private final TradeableInstrument<T> instrument;
	private final long units;
	private final DateTime transactionTime;
	private final double price;
	private final double interest;
	private final double pnl;
	private final TradingSignal side;
	private M linkedTransactionId;

	public Transaction(M transactionId, Event transactionType, N accountId, String instrument, long units,
			TradingSignal side, DateTime transactionTime, double price, double interest, double pnl) {
		super();
		this.transactionId = transactionId;
		this.transactionType = transactionType;
		this.accountId = accountId;
		this.instrument = new TradeableInstrument<T>(instrument);
		this.units = units;
		this.side = side;
		this.transactionTime = transactionTime;
		this.price = price;
		this.interest = interest;
		this.pnl = pnl;
	}

	public TradingSignal getSide() {
		return this.side;
	}

	public M getTransactionId() {
		return transactionId;
	}

	public Event getTransactionType() {
		return transactionType;
	}

	public N getAccountId() {
		return accountId;
	}

	public TradeableInstrument<T> getInstrument() {
		return instrument;
	}

	public long getUnits() {
		return units;
	}

	public DateTime getTransactionTime() {
		return transactionTime;
	}

	public double getPrice() {
		return price;
	}

	public double getInterest() {
		return interest;
	}

	public double getPnl() {
		return pnl;
	}

	public M getLinkedTransactionId() {
		return linkedTransactionId;
	}

	public void setLinkedTransactionId(M linkedTransactionId) {
		this.linkedTransactionId = linkedTransactionId;
	}

	@Override
	public String toString() {
		return "Transaction [transactionId=" + transactionId + ", transactionType=" + transactionType + ", accountId="
				+ accountId + ", instrument=" + instrument.getInstrument() + ", units=" + units + ", transactionTime="
				+ transactionTime + ", price=" + price + ", interest=" + interest + ", pnl=" + pnl + ", side=" + side
				+ ", linkedTransactionId=" + linkedTransactionId + "]";
	}
}
