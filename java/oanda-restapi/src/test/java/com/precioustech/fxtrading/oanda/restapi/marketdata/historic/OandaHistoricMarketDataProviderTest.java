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
package com.precioustech.fxtrading.oanda.restapi.marketdata.historic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.junit.Test;

import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStick;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;

public class OandaHistoricMarketDataProviderTest {

	@Test
	public void fromToUrlTest() {
		OandaHistoricMarketDataProvider service = new OandaHistoricMarketDataProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);

		String url = service.getFromToUrl(new TradeableInstrument<String>("CHF_JPY"), CandleStickGranularity.S5,
				new DateTime(1442950140000L), new DateTime(1442960940000L));
		assertEquals(
				"https://api-fxtrade.oanda.com/v1/candles?instrument=CHF_JPY&candleFormat=midpoint&granularity=S5&dailyAlignment=0&alignmentTimezone=GMT&start=1442950140000000&end=1442960940000000",
				url);
	}

	@Test
	public void countUrlTest() {
		OandaHistoricMarketDataProvider service = new OandaHistoricMarketDataProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);
		String url = service.getCountUrl(new TradeableInstrument<String>("GBP_USD"), CandleStickGranularity.H1, 2);
		assertEquals(
				"https://api-fxtrade.oanda.com/v1/candles?instrument=GBP_USD&candleFormat=midpoint&granularity=H1&dailyAlignment=0&alignmentTimezone=GMT&count=2",
				url);
	}

	@Test
	public void countCandlesTest() throws Exception {
		OandaHistoricMarketDataProvider service = new OandaHistoricMarketDataProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);
		OandaHistoricMarketDataProvider spy = spy(service);
		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);
		OandaTestUtils.mockHttpInteraction("src/test/resources/candlesCountM.txt", mockHttpClient);
		final int count = 2;
		List<CandleStick<String>> candles = spy.getCandleSticks(new TradeableInstrument<String>("GBP_USD"),
				CandleStickGranularity.M, count);
		assertEquals(count, candles.size());
		CandleStick<String> candle1 = candles.get(0);
		assertEquals(new DateTime(1442098800000L), candle1.getEventDate());
		assertEquals(1.54284, candle1.getLowPrice(), OandaTestConstants.precision);
		assertEquals(1.544695, candle1.getHighPrice(), OandaTestConstants.precision);

		CandleStick<String> candle2 = candles.get(1);
		assertEquals(new DateTime(1442185200000L), candle2.getEventDate());
		assertEquals(1.54376, candle2.getLowPrice(), OandaTestConstants.precision);
		assertEquals(1.54594, candle2.getHighPrice(), OandaTestConstants.precision);
	}

	@Test
	public void fromToCandlesTest() throws Exception {
		DateTime from = new DateTime(1430469000000L);
		DateTime to = new DateTime(1430469285000L);
		OandaHistoricMarketDataProvider service = new OandaHistoricMarketDataProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken);
		OandaHistoricMarketDataProvider spy = spy(service);
		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);
		OandaTestUtils.mockHttpInteraction("src/test/resources/candlesFromToS5.txt", mockHttpClient);
		List<CandleStick<String>> candles = spy.getCandleSticks(new TradeableInstrument<String>("GBP_CHF"),
				CandleStickGranularity.S5, from, to);
		assertEquals(58, candles.size());
		CandleStick<String> candle1 = candles.get(0);
		assertEquals(from, candle1.getEventDate());
		assertEquals(1.42897, candle1.getLowPrice(), OandaTestConstants.precision);
		assertEquals(1.431545, candle1.getHighPrice(), OandaTestConstants.precision);
		CandleStick<String> candle58 = candles.get(57);
		assertEquals(to, candle58.getEventDate());
		assertEquals(1.428515, candle58.getLowPrice(), OandaTestConstants.precision);
		assertEquals(1.42869, candle58.getHighPrice(), OandaTestConstants.precision);
	}
}
