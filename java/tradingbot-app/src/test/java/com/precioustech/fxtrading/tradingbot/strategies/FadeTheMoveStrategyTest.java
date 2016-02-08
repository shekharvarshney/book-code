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
package com.precioustech.fxtrading.tradingbot.strategies;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingDecision;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.MarketDataPayLoad;
import com.precioustech.fxtrading.marketdata.PipJumpCutOffCalculator;
import com.precioustech.fxtrading.tradingbot.TradingAppTestConstants;
import com.precioustech.fxtrading.tradingbot.TradingConfig;

public class FadeTheMoveStrategyTest {

	@SuppressWarnings("unchecked")
	@Test
	public void analysePricesTest() throws Exception {
		TradingConfig tradingConfig = mock(TradingConfig.class);
		// when(tradingConfig.getFadeTheMoveJumpReqdToTrade()).thenReturn(45);
		PipJumpCutOffCalculator<String> pipCalculator = mock(PipJumpCutOffCalculator.class);

		when(tradingConfig.getFadeTheMoveDistanceToTrade()).thenReturn(25);
		when(tradingConfig.getFadeTheMovePipsDesired()).thenReturn(10);
		when(tradingConfig.getFadeTheMovePriceExpiry()).thenReturn(15);
		BlockingQueue<TradingDecision<String>> orderQueue = new LinkedBlockingQueue<TradingDecision<String>>();
		TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
		TradeableInstrument<String> audchf = new TradeableInstrument<String>("AUD_CHF");

		when(pipCalculator.calculatePipJumpCutOff(eq(eurusd))).thenReturn(45.0);
		when(pipCalculator.calculatePipJumpCutOff(eq(audchf))).thenReturn(29.0);

		FadeTheMoveStrategy<String> strategy = new FadeTheMoveStrategy<String>(Lists.newArrayList(eurusd, audchf));
		InstrumentService<String> instrumentService = mock(InstrumentService.class);
		when(instrumentService.getPipForInstrument(any(TradeableInstrument.class))).thenReturn(0.0001);
		strategy.tradingConfig = tradingConfig;
		strategy.orderQueue = orderQueue;
		strategy.instrumentService = instrumentService;
		strategy.pipJumpCutOffCalculator = pipCalculator;
		strategy.init();
		final double[] eurusdPrices = { 1.1345, 1.1341, 1.1339, 1.1338, 1.1333, 1.1332, 1.1331, 1.1330, 1.1328, 1.1325,
				1.1324, 1.1322, 1.1320, 1.1317, 1.1316, 1.1314, 1.1311, 1.1309, 1.1310, 1.1313, 1.1308, 1.1305, 1.1302,
				1.1300, 1.1298, 1.1296, 1.1293 };
		DateTime eventStarteurusd = DateTime.now().minusMinutes(10);
		for (double price : eurusdPrices) {
			eventStarteurusd = eventStarteurusd.plusSeconds(5);
			strategy.handleMarketDataEvent(
					new MarketDataPayLoad<String>(eurusd, price - TradingAppTestConstants.precision,
							price + TradingAppTestConstants.precision, eventStarteurusd));
		}

		final double[] audchfPrices = { 0.7069, 0.7070, 0.7073, 0.7076, 0.7077, 0.7078, 0.708, 0.7082, 0.7084, 0.7085,
				0.7086, 0.7089, 0.7091, 0.7093, 0.7094, 0.7098, 0.71, 0.7102, 0.7105, 0.7104, 0.7103, 0.7105, 0.7109,
				0.7111, 0.7112, 0.7115, 0.7118 };
		DateTime eventStartaudchf = DateTime.now().minusMinutes(10);
		for (double price : audchfPrices) {
			eventStartaudchf = eventStartaudchf.plusSeconds(5);
			strategy.handleMarketDataEvent(
					new MarketDataPayLoad<String>(audchf, price - TradingAppTestConstants.precision,
							price + TradingAppTestConstants.precision, eventStartaudchf));
		}

		strategy.analysePrices();
		for (int i = 1; i <= 2; i++) {/*
										 * 2 decisions expected for eurusd and
										 * audchf
										 */
			TradingDecision<String> decision = orderQueue.take();
			assertEquals(TradingDecision.SRCDECISION.FADE_THE_MOVE, decision.getTradeSource());
			if (eurusd.equals(decision.getInstrument())) {
				assertEquals(TradingSignal.LONG, decision.getSignal());
				assertEquals(1.1269, decision.getLimitPrice(), TradingAppTestConstants.precision);
				assertEquals(1.1279, decision.getTakeProfitPrice(), TradingAppTestConstants.precision);
				assertEquals(eurusd, decision.getInstrument());
			} else {
				assertEquals(TradingSignal.SHORT, decision.getSignal());
				assertEquals(audchf, decision.getInstrument());
				assertEquals(0.7142, decision.getLimitPrice(), TradingAppTestConstants.precision);
				assertEquals(0.7132, decision.getTakeProfitPrice(), TradingAppTestConstants.precision);
			}
		}
	}
}
