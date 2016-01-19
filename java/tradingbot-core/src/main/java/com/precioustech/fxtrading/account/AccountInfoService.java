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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;
import com.precioustech.fxtrading.utils.TradingUtils;

/**
 * 
 * @author Shekhar Varshney
 *
 * @param <K>
 *            type of accountId
 * @param <N>
 *            type of The type of instrumentId in class TradeableInstrument
 * @see TradeableInstrument
 */
public class AccountInfoService<K, N> {

	private final AccountDataProvider<K> accountDataProvider;
	private final BaseTradingConfig baseTradingConfig;
	private final CurrentPriceInfoProvider<N> currentPriceInfoProvider;
	private final ProviderHelper providerHelper;
	private Comparator<Account<K>> accountComparator = new MarginAvailableComparator<K>();

	public AccountInfoService(AccountDataProvider<K> accountDataProvider,
			CurrentPriceInfoProvider<N> currentPriceInfoProvider, BaseTradingConfig baseTradingConfig,
			ProviderHelper providerHelper) {
		this.accountDataProvider = accountDataProvider;
		this.baseTradingConfig = baseTradingConfig;
		this.currentPriceInfoProvider = currentPriceInfoProvider;
		this.providerHelper = providerHelper;
	}

	public Collection<Account<K>> getAllAccounts() {
		return this.accountDataProvider.getLatestAccountInfo();
	}

	public Account<K> getAccountInfo(K accountId) {
		return this.accountDataProvider.getLatestAccountInfo(accountId);
	}

	static class MarginAvailableComparator<K> implements Comparator<Account<K>> {

		@Override
		public int compare(Account<K> ai1, Account<K> ai2) {
			if (ai1.getMarginAvailable() > ai2.getMarginAvailable()) {
				return -1;
			} else if (ai1.getMarginAvailable() < ai2.getMarginAvailable()) {
				return 1;
			}
			return 0;
		}

	}

	public Collection<K> findAccountsToTrade() {
		List<Account<K>> accounts = Lists.newArrayList(getAllAccounts());
		Collection<K> accountsFound = Lists.newArrayList();
		Collections.sort(accounts, accountComparator);
		for (Account<K> account : accounts) {
			if (account.getAmountAvailableRatio() >= baseTradingConfig.getMinReserveRatio()
					&& account.getNetAssetValue() >= baseTradingConfig.getMinAmountRequired()) {
				accountsFound.add(account.getAccountId());
			}
		}
		return accountsFound;
	}

	/*
	 * ({BASE} / {Home Currency}) * units) / (margin ratio)
	For example, suppose:
	Home Currency = USD
	Currency Pair = GBP/CHF
	Base = GBP; Quote = CHF
	Base / Home Currency = GBP/USD = 1.5819
	Units = 1000
	Margin Ratio = 20:1
	Then, margin used:
	= (1.5819 * 1000) / 20
	= 79.095 USD
	 */
	@SuppressWarnings("unchecked")
	public double calculateMarginForTrade(Account<K> accountInfo, TradeableInstrument<N> instrument, int units) {
		String tokens[] = TradingUtils.splitInstrumentPair(instrument.getInstrument());
		String baseCurrency = tokens[0];
		double price = 1.0;

		if (!baseCurrency.equals(accountInfo.getCurrency())) {
			String currencyPair = this.providerHelper.fromIsoFormat(baseCurrency + accountInfo.getCurrency());

			Map<TradeableInstrument<N>, Price<N>> priceInfoMap = this.currentPriceInfoProvider
					.getCurrentPricesForInstruments(Lists.newArrayList(new TradeableInstrument<N>(currencyPair)));
			if (priceInfoMap.isEmpty()) {/*this means we got the currency pair inverted*/
				/*example when the home currency is GBP and instrument is USDJPY*/
				currencyPair = this.providerHelper.fromIsoFormat(accountInfo.getCurrency() + baseCurrency);
				priceInfoMap = this.currentPriceInfoProvider.getCurrentPricesForInstruments(Lists
						.newArrayList(new TradeableInstrument<N>(currencyPair)));
				if (priceInfoMap.isEmpty()) {// something else is wrong here
					return Double.MAX_VALUE;
				}
				Price<N> priceInfo = priceInfoMap.values().iterator().next();
				price = 1.0 / ((priceInfo.getBidPrice() + priceInfo.getAskPrice()) / 2.0);/*take avg of bid and ask prices*/
			} else {
				Price<N> priceInfo = priceInfoMap.values().iterator().next();
				price = (priceInfo.getBidPrice() + priceInfo.getAskPrice()) / 2.0;/*take avg of bid and ask prices*/
			}

		}
		return price * units * accountInfo.getMarginRate();
	}

	public double calculateMarginForTrade(K accountId, TradeableInstrument<N> instrument, int units) {
		return calculateMarginForTrade(getAccountInfo(accountId), instrument, units);
	}
}
