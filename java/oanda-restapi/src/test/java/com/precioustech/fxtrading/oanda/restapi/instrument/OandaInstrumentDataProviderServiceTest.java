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
package com.precioustech.fxtrading.oanda.restapi.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;

public class OandaInstrumentDataProviderServiceTest {

	@Test
	public void allInstruments() throws Exception {
		final OandaInstrumentDataProviderService service = new OandaInstrumentDataProviderService(
				OandaTestConstants.url, OandaTestConstants.accountId, OandaTestConstants.accessToken);
		assertEquals(
				"https://api-fxtrade.oanda.com/v1/instruments?accountId=123456&fields=instrument%2Cpip%2CinterestRate",
				service.getInstrumentsUrl());
		OandaInstrumentDataProviderService spy = spy(service);
		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);
		OandaTestUtils.mockHttpInteraction("src/test/resources/instruments.txt", mockHttpClient);
		Collection<TradeableInstrument<String>> allInstruments = spy.getInstruments();
		assertEquals(2, allInstruments.size());
		Iterator<TradeableInstrument<String>> itr = allInstruments.iterator();

		TradeableInstrument<String> instrument1 = itr.next();
		assertNotNull(instrument1.getInstrumentPairInterestRate());
		assertEquals("AUD_CAD", instrument1.getInstrument());
		assertEquals(0.0001, instrument1.getPip(), OandaTestConstants.precision);
		assertEquals(0.0164, instrument1.getInstrumentPairInterestRate().getBaseCurrencyBidInterestRate(),
				OandaTestConstants.precision);
		assertEquals(0.0274, instrument1.getInstrumentPairInterestRate().getBaseCurrencyAskInterestRate(),
				OandaTestConstants.precision);
		assertEquals(0.002, instrument1.getInstrumentPairInterestRate().getQuoteCurrencyBidInterestRate(),
				OandaTestConstants.precision);
		assertEquals(0.008, instrument1.getInstrumentPairInterestRate().getQuoteCurrencyAskInterestRate(),
				OandaTestConstants.precision);

		TradeableInstrument<String> instrument2 = itr.next();
		assertNotNull(instrument2.getInstrumentPairInterestRate());
		assertEquals("AUD_CHF", instrument2.getInstrument());
		assertEquals(0.0001, instrument2.getPip(), OandaTestConstants.precision);
		assertEquals(0.0164, instrument2.getInstrumentPairInterestRate().getBaseCurrencyBidInterestRate(),
				OandaTestConstants.precision);
		assertEquals(0.0274, instrument2.getInstrumentPairInterestRate().getBaseCurrencyAskInterestRate(),
				OandaTestConstants.precision);
		assertEquals(-0.013, instrument2.getInstrumentPairInterestRate().getQuoteCurrencyBidInterestRate(),
				OandaTestConstants.precision);
		assertEquals(0.003, instrument2.getInstrumentPairInterestRate().getQuoteCurrencyAskInterestRate(),
				OandaTestConstants.precision);
	}
}
