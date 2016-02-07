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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;
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
		assertEquals(200, transaction.getUnits());
		assertEquals(TradeEvents.TRADE_CLOSE, transaction.getTransactionType());
		assertEquals(accountId, transaction.getAccountId());
		assertEquals(0.0, transaction.getInterest(), OandaTestConstants.precision);
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
