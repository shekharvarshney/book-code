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
package com.precioustech.fxtrading.account;

/**
 * A POJO that holds account information. No setters are provided as it is
 * envisaged that the final member variables will be initialized through the
 * constructor(s).
 * 
 * @author Shekhar Varshney
 *
 * @param <T>
 *            the type of accountId
 */
public class Account<T> {

	private final double totalBalance;
	private final double unrealisedPnl;
	private final double realisedPnl;
	private final double marginUsed;
	private final double marginAvailable;
	private final double netAssetValue;
	private final long openTrades;
	private final String currency;
	private final T accountId;
	private final String toStr;
	private final double amountAvailableRatio;/*The amount available to trade as a fraction of total amount*/
	private final double marginRate;/*The leverage offered on this account. for e.g. 0.05, 0.1 etc*/
	private final int hash;

	public Account(final double totalBalance, double marginAvailable, String currency, T accountId, double marginRate) {
		this(totalBalance, 0, 0, 0, marginAvailable, 0, currency, accountId, marginRate);
	}

	public Account(final double totalBalance, double unrealisedPnl, double realisedPnl, double marginUsed,
			double marginAvailable, long openTrades, String currency, T accountId, double marginRate) {
		this.totalBalance = totalBalance;
		this.unrealisedPnl = unrealisedPnl;
		this.realisedPnl = realisedPnl;
		this.marginUsed = marginUsed;
		this.marginAvailable = marginAvailable;
		this.openTrades = openTrades;
		this.currency = currency;
		this.accountId = accountId;
		this.amountAvailableRatio = this.marginAvailable / this.totalBalance;
		this.netAssetValue = this.marginUsed + this.marginAvailable;
		this.marginRate = marginRate;
		this.hash = calcHashCode();
		toStr = String.format("Currency=%s,NAV=%5.2f,Total Balance=%5.2f, UnrealisedPnl=%5.2f, "
				+ "RealisedPnl=%5.2f, MarginUsed=%5.2f, MarginAvailable=%5.2f,"
				+ " OpenTrades=%d, amountAvailableRatio=%1.2f, marginRate=%1.2f", currency, netAssetValue,
				totalBalance, unrealisedPnl, realisedPnl, marginUsed, marginAvailable, openTrades,
				this.amountAvailableRatio, this.marginRate);
	}

	public double getAmountAvailableRatio() {
		return amountAvailableRatio;
	}

	public double getMarginRate() {
		return marginRate;
	}

	@Override
	public String toString() {
		return this.toStr;
	}

	public T getAccountId() {
		return accountId;
	}

	public String getCurrency() {
		return currency;
	}

	public double getNetAssetValue() {
		return this.netAssetValue;
	}

	public double getTotalBalance() {
		return totalBalance;
	}

	public double getUnrealisedPnl() {
		return unrealisedPnl;
	}

	public double getRealisedPnl() {
		return realisedPnl;
	}

	public double getMarginUsed() {
		return marginUsed;
	}

	@Override
	public int hashCode() {
		return this.hash;
	}

	private int calcHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		Account<T> other = (Account<T>) obj;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		return true;
	}

	public double getMarginAvailable() {
		return marginAvailable;
	}

	public long getOpenTrades() {
		return openTrades;
	}

}
