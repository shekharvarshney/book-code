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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingTestConstants;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;

@SuppressWarnings("unchecked")
public class AccountInfoServiceTest {

	private final double marginRate = 0.2;
	private final int units = 3000;

	@Test
	public void accountComparatorTest() {
		Comparator<Account<Long>> comparator = new AccountInfoService.MarginAvailableComparator<Long>();

		List<Account<Long>> accounts = createAccounts();
		List<Account<Long>> accountsCopy = Lists.newArrayList(accounts);
		Collections.sort(accounts, comparator);
		assertEquals(4, accounts.size());
		assertTrue(accounts.get(0) == accountsCopy.get(2));
		assertTrue(accounts.get(1) == accountsCopy.get(0));
		assertTrue(accounts.get(2) == accountsCopy.get(3));
		assertTrue(accounts.get(3) == accountsCopy.get(1));
	}

	@Test
	public void accountsToTradeTest() {
		BaseTradingConfig baseTradingConfig = mock(BaseTradingConfig.class);
		when(baseTradingConfig.getMinReserveRatio()).thenReturn(0.2);
		when(baseTradingConfig.getMinAmountRequired()).thenReturn(200.00);
		AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
		AccountInfoService<Long, String> accInfoService = new AccountInfoService<Long, String>(accountDataProvider,
				null, baseTradingConfig, null);
		List<Account<Long>> accounts = createAccounts();
		when(accountDataProvider.getLatestAccountInfo()).thenReturn(accounts);
		Collection<Long> eligibleAccounts = accInfoService.findAccountsToTrade();
		assertEquals(1, eligibleAccounts.size());
		long eligibleAccount = eligibleAccounts.iterator().next();
		assertEquals(1001L, eligibleAccount);

	}

	private List<Account<Long>> createAccounts() {
		Account<Long> account1 = mock(Account.class);
		when(account1.getMarginAvailable()).thenReturn(1178.9);
		when(account1.getAmountAvailableRatio()).thenReturn(0.24);
		when(account1.getNetAssetValue()).thenReturn(1346.81);
		when(account1.getAccountId()).thenReturn(1001L);
		Account<Long> account2 = mock(Account.class);
		when(account2.getMarginAvailable()).thenReturn(100.23);
		when(account2.getAmountAvailableRatio()).thenReturn(0.04);
		when(account2.getNetAssetValue()).thenReturn(198.2);
		when(account2.getAccountId()).thenReturn(1002L);
		Account<Long> account3 = mock(Account.class);
		when(account3.getMarginAvailable()).thenReturn(1572.82);
		when(account3.getAmountAvailableRatio()).thenReturn(0.19);
		when(account3.getNetAssetValue()).thenReturn(2612.31);
		when(account3.getAccountId()).thenReturn(1003L);
		Account<Long> account4 = mock(Account.class);
		when(account4.getMarginAvailable()).thenReturn(198.34);
		when(account4.getAmountAvailableRatio()).thenReturn(0.45);
		when(account4.getNetAssetValue()).thenReturn(199.15);
		when(account4.getAccountId()).thenReturn(1004L);
		return Lists.newArrayList(account1, account2, account3, account4);
	}

	@Test
	public void marginRateWhenAccountCurrencyNominated() {
		/*account currency CHF and calculate margin for GBPUSD, effectively using GBPCHF rate*/
		AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
		CurrentPriceInfoProvider<String> currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
		ProviderHelper providerHelper = mock(ProviderHelper.class);
		AccountInfoService<Long, String> accInfoService = new AccountInfoService<Long, String>(accountDataProvider,
				currentPriceInfoProvider, null, providerHelper);
		TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");
		TradeableInstrument<String> gbpchf = new TradeableInstrument<String>("GBP_CHF");
		Account<Long> account = mock(Account.class);
		when(accountDataProvider.getLatestAccountInfo(TradingTestConstants.accountId)).thenReturn(account);
		when(account.getCurrency()).thenReturn("CHF");
		when(account.getMarginRate()).thenReturn(marginRate);
		when(providerHelper.fromIsoFormat(eq("GBPCHF"))).thenReturn(gbpchf.getInstrument());
		Map<TradeableInstrument<String>, Price<String>> priceInfoMap = Maps.newHashMap();
		priceInfoMap.put(gbpchf, new Price<String>(gbpchf, 1.4811, 1.4813, DateTime.now()));
		when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(gbpchf)))).thenReturn(
				priceInfoMap);

		double marginRate = accInfoService.calculateMarginForTrade(TradingTestConstants.accountId, gbpusd, units);
		assertEquals(888.72, marginRate, TradingTestConstants.precision);
	}

	@Test
	public void marginRateWhenAccountCurrencyBase() {
		/*account currency EUR and calculate margin for AUDUSD, effectively using AUDEUR rate*/
		AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
		CurrentPriceInfoProvider<String> currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
		ProviderHelper providerHelper = mock(ProviderHelper.class);
		AccountInfoService<Long, String> accInfoService = new AccountInfoService<Long, String>(accountDataProvider,
				currentPriceInfoProvider, null, providerHelper);
		TradeableInstrument<String> audusd = new TradeableInstrument<String>("AUD_USD");
		TradeableInstrument<String> euraud = new TradeableInstrument<String>("EUR_AUD");
		TradeableInstrument<String> audeur = new TradeableInstrument<String>("AUD_EUR");
		Account<Long> account = mock(Account.class);
		when(accountDataProvider.getLatestAccountInfo(TradingTestConstants.accountId)).thenReturn(account);
		when(account.getCurrency()).thenReturn("EUR");
		when(account.getMarginRate()).thenReturn(marginRate);
		when(providerHelper.fromIsoFormat(eq("AUDEUR"))).thenReturn(audeur.getInstrument());
		when(providerHelper.fromIsoFormat(eq("EURAUD"))).thenReturn(euraud.getInstrument());
		Map<TradeableInstrument<String>, Price<String>> priceInfoMap = Maps.newHashMap();
		priceInfoMap.put(euraud, new Price<String>(euraud, 1.5636, 1.564, DateTime.now()));
		when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(audeur)))).thenReturn(
				Maps.<TradeableInstrument<String>, Price<String>> newHashMap());
		when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(euraud)))).thenReturn(
				priceInfoMap);

		double marginRate = accInfoService.calculateMarginForTrade(TradingTestConstants.accountId, audusd, units);
		assertEquals(383.6807, marginRate, TradingTestConstants.precision);
		marginRate = accInfoService.calculateMarginForTrade(TradingTestConstants.accountId, euraud, units);
		assertEquals(600.0, marginRate, TradingTestConstants.precision);
	}

}
