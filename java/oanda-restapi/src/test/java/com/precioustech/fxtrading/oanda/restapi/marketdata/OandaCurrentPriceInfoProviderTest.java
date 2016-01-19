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
package com.precioustech.fxtrading.oanda.restapi.marketdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.Price;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;
import com.precioustech.fxtrading.utils.TradingUtils;

public class OandaCurrentPriceInfoProviderTest {

	@Test
	public void currentPricesTest() throws Exception {
		OandaCurrentPriceInfoProvider service = new OandaCurrentPriceInfoProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);

		OandaCurrentPriceInfoProvider spy = spy(service);

		TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");
		TradeableInstrument<String> gbpchf = new TradeableInstrument<String>("GBP_CHF");
		TradeableInstrument<String> gbpnzd = new TradeableInstrument<String>("GBP_NZD");

		@SuppressWarnings("unchecked")
		Collection<TradeableInstrument<String>> instruments = Lists.newArrayList(gbpusd, gbpchf, gbpnzd);

		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);

		OandaTestUtils.mockHttpInteraction("src/test/resources/currentPrices.txt", mockHttpClient);

		Map<TradeableInstrument<String>, Price<String>> prices = spy.getCurrentPricesForInstruments(instruments);
		assertEquals(instruments.size(), prices.size());
		assertTrue(prices.containsKey(gbpusd));
		assertTrue(prices.containsKey(gbpchf));
		assertTrue(prices.containsKey(gbpnzd));

		Price<String> gbpusdPrice = prices.get(gbpusd);
		assertEquals(new DateTime(TradingUtils.toMillisFromNanos(1442216738184236L)), gbpusdPrice.getPricePoint());
		assertEquals(1.54682, gbpusdPrice.getBidPrice(), OandaTestConstants.precision);
		assertEquals(1.547, gbpusdPrice.getAskPrice(), OandaTestConstants.precision);

		Price<String> gbpchfPrice = prices.get(gbpchf);
		assertEquals(new DateTime(TradingUtils.toMillisFromNanos(1442216737600312L)), gbpchfPrice.getPricePoint());
		assertEquals(1.50008, gbpchfPrice.getBidPrice(), OandaTestConstants.precision);
		assertEquals(1.50058, gbpchfPrice.getAskPrice(), OandaTestConstants.precision);

		Price<String> gbpnzdPrice = prices.get(gbpnzd);
		assertEquals(new DateTime(TradingUtils.toMillisFromNanos(1442216738184363L)), gbpnzdPrice.getPricePoint());
		assertEquals(2.44355, gbpnzdPrice.getBidPrice(), OandaTestConstants.precision);
		assertEquals(2.44473, gbpnzdPrice.getAskPrice(), OandaTestConstants.precision);
	}
}
