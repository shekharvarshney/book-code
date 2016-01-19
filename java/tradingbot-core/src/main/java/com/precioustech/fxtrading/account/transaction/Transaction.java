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

public class Transaction<M, N> {
	private final M transactionId;
	private final String transactionType;
	private final N accountId;
	private final String instrument;
	private final long units;
	private final DateTime transactionTime;
	private final double price;
	private final double interest;
	private final double pnl;
	private final M linkedTransactionId;

	public Transaction(M transactionId, String transactionType, N accountId, String instrument, long units,
			DateTime transactionTime, double price, double interest, double pnl, M linkedTransactionId) {
		super();
		this.transactionId = transactionId;
		this.transactionType = transactionType;
		this.accountId = accountId;
		this.instrument = instrument;
		this.units = units;
		this.transactionTime = transactionTime;
		this.price = price;
		this.interest = interest;
		this.pnl = pnl;
		this.linkedTransactionId = linkedTransactionId;
	}

	public M getTransactionId() {
		return transactionId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public N getAccountId() {
		return accountId;
	}

	public String getInstrument() {
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
}
