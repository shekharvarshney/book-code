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
package com.precioustech.fxtrading.oanda.restapi.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;
import com.precioustech.fxtrading.position.Position;

public class OandaPositionManagementProviderTest {

	@Test
	public void positionForAccountTest() throws Exception {
		OandaPositionManagementProvider service = new OandaPositionManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);
		final TradeableInstrument<String> gbpchf = new TradeableInstrument<String>("GBP_CHF");
		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/positions/GBP_CHF", service
				.getPositionForInstrumentUrl(OandaTestConstants.accountId, gbpchf));
		OandaPositionManagementProvider spy = doMockStuff("src/test/resources/positionForInstrument.txt", service);
		Position<String> position = spy.getPositionForInstrument(OandaTestConstants.accountId, gbpchf);
		assertNotNull(position);
		assertEquals(gbpchf, position.getInstrument());
		assertEquals(TradingSignal.LONG, position.getSide());
		assertEquals(1.3093, position.getAveragePrice(), OandaTestConstants.precision);
		assertEquals(4516L, position.getUnits());
	}

	@Test
	public void closePositionTest() throws Exception {
		OandaPositionManagementProvider service = new OandaPositionManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);
		OandaPositionManagementProvider spy = doMockStuff("src/test/resources/positionForInstrument.txt", service);/*giving a filename although its of not much use here*/
		boolean success = spy.closePosition(OandaTestConstants.accountId, new TradeableInstrument<String>("AUD_NZD"));
		assertTrue(success);
		verify(spy.getHttpClient(), times(1)).execute(any(HttpDelete.class));
	}

	private OandaPositionManagementProvider doMockStuff(String fname, OandaPositionManagementProvider service)
			throws Exception {
		OandaPositionManagementProvider spy = spy(service);
		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);
		OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);
		return spy;
	}

	@Test
	public void positionsForAccountTest() throws Exception {
		OandaPositionManagementProvider service = new OandaPositionManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);
		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/positions", service
				.getPositionsForAccountUrl(OandaTestConstants.accountId));

		OandaPositionManagementProvider spy = doMockStuff("src/test/resources/positionsForAccount123456.txt", service);
		Collection<Position<String>> positions = spy.getPositionsForAccount(OandaTestConstants.accountId);
		assertEquals(4, positions.size());
		Iterator<Position<String>> itr = positions.iterator();

		Position<String> pos1 = itr.next();
		Position<String> pos2 = itr.next();
		Position<String> pos3 = itr.next();
		Position<String> pos4 = itr.next();

		assertEquals("EUR_USD", pos1.getInstrument().getInstrument());
		assertEquals(6723L, pos1.getUnits());
		assertEquals(TradingSignal.SHORT, pos1.getSide());
		assertEquals(1.2419, pos1.getAveragePrice(), OandaTestConstants.precision);

		assertEquals("GBP_USD", pos2.getInstrument().getInstrument());
		assertEquals(3000L, pos2.getUnits());
		assertEquals(TradingSignal.SHORT, pos2.getSide());
		assertEquals(1.5982, pos2.getAveragePrice(), OandaTestConstants.precision);

		assertEquals("USD_JPY", pos3.getInstrument().getInstrument());
		assertEquals(2388L, pos3.getUnits());
		assertEquals(TradingSignal.LONG, pos3.getSide());
		assertEquals(112.455, pos3.getAveragePrice(), OandaTestConstants.precision);

		assertEquals("EUR_CHF", pos4.getInstrument().getInstrument());
		assertEquals(11020L, pos4.getUnits());
		assertEquals(TradingSignal.SHORT, pos4.getSide());
		assertEquals(1.2306, pos4.getAveragePrice(), OandaTestConstants.precision);

	}
}
