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
package com.precioustech.fxtrading.oanda.restapi.trade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;
import com.precioustech.fxtrading.trade.Trade;

public class OandaTradeManagementProviderTest {

	@Test
	public void modifyTradeTest() throws Exception {
		OandaTradeManagementProvider service = new OandaTradeManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);
		final long tradeId = OandaTestConstants.tradeId;
		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/trades/1800805337", service
				.getTradeForAccountUrl(tradeId, OandaTestConstants.accountId));
		OandaTradeManagementProvider spy = doMockStuff("src/test/resources/tradesForAccount123456.txt", service);

		final double stopLoss = 150.0;
		final double takeProfit = 110.0;
		spy.modifyTrade(OandaTestConstants.accountId, tradeId, stopLoss, takeProfit);
		verify(spy, times(1)).createPatchCommand(OandaTestConstants.accountId, tradeId, stopLoss, takeProfit);

	}

	@Test
	public void closeTradeTest() throws Exception {
		OandaTradeManagementProvider service = new OandaTradeManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);
		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/trades/1800805337", service
				.getTradeForAccountUrl(OandaTestConstants.tradeId, OandaTestConstants.accountId));
		OandaTradeManagementProvider spy = doMockStuff("src/test/resources/tradesForAccount123456.txt", service);

		boolean success = spy.closeTrade(OandaTestConstants.tradeId, OandaTestConstants.accountId);
		assertTrue(success);
		verify(spy.getHttpClient(), times(1)).execute(any(HttpDelete.class));
	}

	@Test
	public void givenTradeForAccTest() throws Exception {
		OandaTradeManagementProvider service = new OandaTradeManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);

		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/trades/1800805337", service
				.getTradeForAccountUrl(OandaTestConstants.tradeId, OandaTestConstants.accountId));

		OandaTradeManagementProvider spy = doMockStuff("src/test/resources/trade1800805337ForAccount123456.txt",
				service);
		Trade<Long, String, Long> trade = spy.getTradeForAccount(OandaTestConstants.tradeId,
				OandaTestConstants.accountId);
		assertEquals(TradingSignal.SHORT, trade.getSide());
		assertEquals(3000L, trade.getUnits());
		assertEquals(120.521, trade.getExecutionPrice(), OandaTestConstants.precision);
		assertEquals(105.521, trade.getTakeProfitPrice(), OandaTestConstants.precision);
		assertEquals(121.521, trade.getStopLoss(), OandaTestConstants.precision);
	}

	private OandaTradeManagementProvider doMockStuff(String fname, OandaTradeManagementProvider service)
			throws Exception {
		OandaTradeManagementProvider spy = spy(service);
		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);
		OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);
		return spy;
	}

	@Test
	public void allTradesForAccTest() throws Exception {
		OandaTradeManagementProvider service = new OandaTradeManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);

		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/trades", service
				.getTradesInfoUrl(OandaTestConstants.accountId));

		OandaTradeManagementProvider spy = doMockStuff("src/test/resources/tradesForAccount123456.txt", service);

		Collection<Trade<Long, String, Long>> trades = spy.getTradesForAccount(OandaTestConstants.accountId);
		assertEquals(2, trades.size());

		Iterator<Trade<Long, String, Long>> itr = trades.iterator();
		Trade<Long, String, Long> trade1 = itr.next();
		Trade<Long, String, Long> trade2 = itr.next();

		assertEquals(TradingSignal.SHORT, trade1.getSide());
		assertEquals(3000L, trade1.getUnits());
		assertEquals(120.521, trade1.getExecutionPrice(), OandaTestConstants.precision);
		assertEquals(105.521, trade1.getTakeProfitPrice(), OandaTestConstants.precision);
		assertEquals(121.521, trade1.getStopLoss(), OandaTestConstants.precision);

		assertEquals(TradingSignal.LONG, trade2.getSide());
		assertEquals(3000L, trade2.getUnits());
		assertEquals(1.0098, trade2.getExecutionPrice(), OandaTestConstants.precision);
		assertEquals(1.15979, trade2.getTakeProfitPrice(), OandaTestConstants.precision);
		assertEquals(0.9854, trade2.getStopLoss(), OandaTestConstants.precision);

	}
}
