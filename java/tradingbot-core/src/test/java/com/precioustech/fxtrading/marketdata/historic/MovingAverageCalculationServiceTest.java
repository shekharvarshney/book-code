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
package com.precioustech.fxtrading.marketdata.historic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingTestConstants;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

@SuppressWarnings("unchecked")
public class MovingAverageCalculationServiceTest {

	@Test
	public void countBasedMovingAvgTest() {
		HistoricMarketDataProvider<String> historicMarketDataProvider = mock(HistoricMarketDataProvider.class);
		MovingAverageCalculationService<String> service = new MovingAverageCalculationService<String>(
				historicMarketDataProvider);
		final int candleStickCt = 7;
		TradeableInstrument<String> eurnzd = new TradeableInstrument<String>("EUR_NZD");
		List<CandleStick<String>> candleSticks = createEurNzdCandleSticks();
		when(historicMarketDataProvider.getCandleSticks(eurnzd, CandleStickGranularity.M, candleStickCt)).thenReturn(
				candleSticks);
		double wma = service.calculateWMA(eurnzd, candleStickCt, CandleStickGranularity.M);
		double sma = service.calculateSMA(eurnzd, candleStickCt, CandleStickGranularity.M);
		assertTrue(wma > sma);/* as the price has been ascending in our time frame*/
		assertTrue(1.66021 == round(wma));
		assertTrue(1.61664 == round(sma));
		ImmutablePair<Double, Double> smaWmaPair = service.calculateSMAandWMAasPair(eurnzd, candleStickCt,
				CandleStickGranularity.M);
		assertTrue(sma == smaWmaPair.left);
		assertTrue(wma == smaWmaPair.right);
	}

	@Test
	public void fromToBasedMovingAvgTest() {
		HistoricMarketDataProvider<String> historicMarketDataProvider = mock(HistoricMarketDataProvider.class);
		MovingAverageCalculationService<String> service = new MovingAverageCalculationService<String>(
				historicMarketDataProvider);
		DateTime from = new DateTime(1430469000000L);
		DateTime to = new DateTime(1430469055000L);
		TradeableInstrument<String> gbpchf = new TradeableInstrument<String>("GBP_CHF");
		List<CandleStick<String>> candleSticks = createGbpChfCandleSticks();
		when(historicMarketDataProvider.getCandleSticks(gbpchf, CandleStickGranularity.S5, from, to)).thenReturn(
				candleSticks);
		double wma = service.calculateWMA(gbpchf, from, to, CandleStickGranularity.S5);
		double sma = service.calculateSMA(gbpchf, from, to, CandleStickGranularity.S5);
		assertTrue(sma > wma);/* as the price has been descending in our time frame*/
		assertEquals(1.42938, round(wma), TradingTestConstants.precision);
		assertEquals(1.42959, round(sma), TradingTestConstants.precision);
		ImmutablePair<Double, Double> smaWmaPair = service.calculateSMAandWMAasPair(gbpchf, from, to,
				CandleStickGranularity.S5);
		assertEquals(sma, smaWmaPair.left, TradingTestConstants.precision);
		assertEquals(wma, smaWmaPair.right, TradingTestConstants.precision);
	}

	private double round(double v) {
		final double instrumentPrecision = 100000.00;
		return Math.round((v * instrumentPrecision)) / instrumentPrecision;
	}

	private List<CandleStick<String>> createGbpChfCandleSticks() {
		double[] closingPrices = { 1.42916, 1.430375, 1.429985, 1.430295, 1.430195, 1.429565, 1.429895, 1.42975,
				1.42945, 1.42902, 1.42874, 1.42864 };
		return createCandleSticks(closingPrices);
	}

	private List<CandleStick<String>> createEurNzdCandleSticks() {
		double[] closingPrices = { 1.4713, 1.519, 1.5787, 1.6288, 1.69, 1.7256, 1.7031 };
		return createCandleSticks(closingPrices);
	}

	private List<CandleStick<String>> createCandleSticks(double[] closingPrices) {
		List<CandleStick<String>> candleSticks = Lists.newArrayList();
		for (double closingPrice : closingPrices) {
			CandleStick<String> candle = mock(CandleStick.class);
			candleSticks.add(candle);
			when(candle.getClosePrice()).thenReturn(closingPrice);
		}
		return candleSticks;
	}
}
