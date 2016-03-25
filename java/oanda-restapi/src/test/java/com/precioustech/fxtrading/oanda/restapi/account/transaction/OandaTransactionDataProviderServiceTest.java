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
package com.precioustech.fxtrading.oanda.restapi.account.transaction;

import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.accessToken;
import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.accountId;
import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.precision;
import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.transactionId;
import static com.precioustech.fxtrading.oanda.restapi.OandaTestConstants.url;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;
import com.precioustech.fxtrading.oanda.restapi.events.AccountEvents;
import com.precioustech.fxtrading.oanda.restapi.events.OrderEvents;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEvents;

public class OandaTransactionDataProviderServiceTest {

	@Test
	public void fetchTransctionTest() throws Exception {
		final OandaTransactionDataProviderService service = new OandaTransactionDataProviderService(url, accessToken);
		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/transactions/1800806000",
				service.getSingleAccountTransactionUrl(transactionId, accountId));
		OandaTransactionDataProviderService spy = createSpyAndCommonStuff("src/test/resources/transaction123456.txt",
				service);
		Transaction<Long, Long, String> transaction = spy.getTransaction(transactionId, accountId);
		assertNotNull(transaction);
		assertEquals("EUR_CHF", transaction.getInstrument().getInstrument());
		assertEquals(TradingSignal.SHORT, transaction.getSide());
		assertEquals(1.10642, transaction.getPrice(), OandaTestConstants.precision);
		assertEquals(0.2538, transaction.getPnl(), precision);
		assertEquals(new Long(200), transaction.getUnits());
		assertEquals(TradeEvents.TRADE_CLOSE, transaction.getTransactionType());
		assertEquals(accountId, transaction.getAccountId());
		assertEquals(0.0, transaction.getInterest(), OandaTestConstants.precision);
	}

	@Test
	public void historicTransactionsTest() throws Exception {
		final long minId = 175000000L;
		final OandaTransactionDataProviderService service = new OandaTransactionDataProviderService(url, accessToken);
		assertEquals(
				"https://api-fxtrade.oanda.com/v1/accounts/123456/transactions?minId=" + (minId + 1) + "&count=500",
				service.getAccountMinTransactionUrl(minId, accountId));
		OandaTransactionDataProviderService spy = createSpyAndCommonStuff("src/test/resources/historicTransactions.txt",
				service);
		List<Transaction<Long, Long, String>> allTransactions = spy.getTransactionsGreaterThanId(minId, accountId);
		assertFalse(allTransactions.isEmpty());
		assertEquals(9, allTransactions.size());

		// general not null checks common to all
		for (Transaction<Long, Long, String> transaction : allTransactions) {
			assertNotNull(transaction.getTransactionId());
			assertNotNull(transaction.getAccountId());
			assertNotNull(transaction.getTransactionTime());
			assertNotNull(transaction.getTransactionType());
			assertNotNull(transaction.getInstrument());
			assertEquals(accountId, transaction.getAccountId());
		}

		// TRADE_CLOSE
		Transaction<Long, Long, String> transaction = allTransactions.get(0);
		assertEquals(TradeEvents.TRADE_CLOSE, transaction.getTransactionType());
		assertEquals("EUR_USD", transaction.getInstrument().getInstrument());
		assertEquals(new Long(2), transaction.getUnits());
		assertEquals(TradingSignal.SHORT, transaction.getSide());
		assertEquals(1.25918, transaction.getPrice(), precision);
		assertEquals(0.0119, transaction.getPnl(), precision);
		assertEquals(new Long(176403879), transaction.getLinkedTransactionId());

		// TRADE_UPDATE
		transaction = allTransactions.get(1);
		assertEquals(TradeEvents.TRADE_UPDATE, transaction.getTransactionType());
		assertEquals("USD_SGD", transaction.getInstrument().getInstrument());
		assertEquals(new Long(3000), transaction.getUnits());
		assertEquals(new Long(1782311741), transaction.getLinkedTransactionId());

		// TAKE_PROFIT_FILLED
		transaction = allTransactions.get(2);
		assertEquals(TradeEvents.TAKE_PROFIT_FILLED, transaction.getTransactionType());
		assertEquals("USD_CHF", transaction.getInstrument().getInstrument());
		assertEquals(new Long(3000), transaction.getUnits());
		assertEquals(new Long(1782379135), transaction.getLinkedTransactionId());
		assertEquals(1.00877, transaction.getPrice(), precision);
		assertEquals(3.48, transaction.getPnl(), precision);
		assertEquals(TradingSignal.SHORT, transaction.getSide());
		assertEquals(0.0002, transaction.getInterest(), precision);

		// STOP_LOSS_FILLED
		transaction = allTransactions.get(3);
		assertEquals(TradeEvents.STOP_LOSS_FILLED, transaction.getTransactionType());
		assertEquals("USD_SGD", transaction.getInstrument().getInstrument());
		assertEquals(new Long(3000), transaction.getUnits());
		assertEquals(new Long(1782311741), transaction.getLinkedTransactionId());
		assertEquals(1.39101, transaction.getPrice(), precision);
		assertEquals(3.3039, transaction.getPnl(), precision);
		assertEquals(TradingSignal.SHORT, transaction.getSide());
		assertEquals(-0.0123, transaction.getInterest(), precision);

		// TRAILING_STOP_FILLED
		transaction = allTransactions.get(4);
		assertEquals(TradeEvents.TRAILING_STOP_FILLED, transaction.getTransactionType());
		assertEquals("EUR_USD", transaction.getInstrument().getInstrument());
		assertEquals(new Long(10), transaction.getUnits());
		assertEquals(new Long(175739352), transaction.getLinkedTransactionId());
		assertEquals(1.38137, transaction.getPrice(), precision);
		assertEquals(-0.0009, transaction.getPnl(), precision);
		assertEquals(TradingSignal.SHORT, transaction.getSide());
		assertEquals(0.0, transaction.getInterest(), precision);

		// LIMIT_ORDER_CREATE
		transaction = allTransactions.get(6);
		assertEquals(OrderEvents.LIMIT_ORDER_CREATE, transaction.getTransactionType());
		assertEquals("EUR_USD", transaction.getInstrument().getInstrument());
		assertEquals(new Long(2), transaction.getUnits());
		assertEquals(BigInteger.ZERO.longValue(), transaction.getLinkedTransactionId().longValue());
		assertEquals(1, transaction.getPrice(), precision);
		assertEquals(TradingSignal.LONG, transaction.getSide());

		// DAILY_INTEREST
		transaction = allTransactions.get(8);
		assertEquals(AccountEvents.DAILY_INTEREST, transaction.getTransactionType());
		assertEquals("AUD_USD", transaction.getInstrument().getInstrument());
		assertNull(transaction.getUnits());
		assertNull(transaction.getSide());

	}

	private OandaTransactionDataProviderService createSpyAndCommonStuff(String fname,
			OandaTransactionDataProviderService service) throws Exception {
		OandaTransactionDataProviderService spy = spy(service);

		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);

		OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);

		return spy;
	}

}
