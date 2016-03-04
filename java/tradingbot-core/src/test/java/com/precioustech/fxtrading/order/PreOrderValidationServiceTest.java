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
package com.precioustech.fxtrading.order;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.TradingTestConstants;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.MovingAverageCalculationService;
import com.precioustech.fxtrading.trade.TradeInfoService;

//TODO: if the launch configuration does not exist then have generics below will suppress the availability of run as junit.
//the trick is to move <M,N,K> to the test case and run the test. once the launch is created then move it back to the class.
@SuppressWarnings("unchecked")
public class PreOrderValidationServiceTest<M, N, K> {

	@Test
	public void priceInSafeZoneTest() {
		MovingAverageCalculationService<N> movingAvgCalcService = mock(MovingAverageCalculationService.class);
		BaseTradingConfig baseTradingCfg = mock(BaseTradingConfig.class);
		PreOrderValidationService<M, N, K> service = new PreOrderValidationService<M, N, K>(null, movingAvgCalcService,
				baseTradingCfg, null);
		when(baseTradingCfg.getMax10yrWmaOffset()).thenReturn(0.1);
		TradeableInstrument<N> eurusd = new TradeableInstrument<N>("EUR_USD");
		when(
				movingAvgCalcService.calculateWMA(eq(eurusd), eq(PreOrderValidationService.FIVE_YRS_IN_MTHS),
						eq(CandleStickGranularity.M))).thenReturn(1.22);

		assertTrue(service.isInSafeZone(TradingSignal.LONG, 1.3, eurusd));
		assertTrue(service.isInSafeZone(TradingSignal.SHORT, 1.11, eurusd));
		assertFalse(service.isInSafeZone(TradingSignal.LONG, 1.36, eurusd));
		assertFalse(service.isInSafeZone(TradingSignal.SHORT, 1.05, eurusd));
	}

	@Test
	public void instrumentNotAlreadyTradedTest() {
		TradeInfoService<M, N, Long> tradeInfoService = mock(TradeInfoService.class);
		OrderInfoService<M, N, Long> orderInfoService = mock(OrderInfoService.class);
		PreOrderValidationService<M, N, Long> service = new PreOrderValidationService<M, N, Long>(tradeInfoService,
				null, null, orderInfoService);
		Collection<Long> accountIds = Lists.newArrayList();
		accountIds.add(TradingTestConstants.accountId);
		accountIds.add(TradingTestConstants.accountId2);
		TradeableInstrument<N> gbpusd = new TradeableInstrument<N>("GBP_USD");
		TradeableInstrument<N> nzdjpy = new TradeableInstrument<N>("NZD_JPY");
		TradeableInstrument<N> audchf = new TradeableInstrument<N>("AUD_CHF");
		when(tradeInfoService.findAllAccountsWithInstrumentTrades(gbpusd)).thenReturn(accountIds);
		assertFalse(service.checkInstrumentNotAlreadyTraded(gbpusd));
		Collection<Long> emptyCollectionIds = Collections.emptyList();
		when(tradeInfoService.findAllAccountsWithInstrumentTrades(nzdjpy)).thenReturn(emptyCollectionIds);
		Collection<Order<N, M>> pendingOrders = Lists.newArrayList();
		when(orderInfoService.pendingOrdersForInstrument(nzdjpy)).thenReturn(pendingOrders);
		pendingOrders.add(mock(Order.class));
		pendingOrders.add(mock(Order.class));
		assertFalse(service.checkInstrumentNotAlreadyTraded(nzdjpy));
		Collection<Order<N, M>> emptyCollectionOrders = Collections.emptyList();
		when(tradeInfoService.findAllAccountsWithInstrumentTrades(audchf)).thenReturn(emptyCollectionIds);
		when(orderInfoService.pendingOrdersForInstrument(audchf)).thenReturn(emptyCollectionOrders);
		assertTrue(service.checkInstrumentNotAlreadyTraded(audchf));
	}

	@Test
	public void limitsForCcyTest() {
		final String AUD = "AUD";
		final String NZD = "NZD";
		final String CAD = "CAD";
		final String CHF = "CHF";
		TradeInfoService<M, N, Long> tradeInfoService = mock(TradeInfoService.class);
		OrderInfoService<M, N, Long> orderInfoService = mock(OrderInfoService.class);
		BaseTradingConfig baseTradingCfg = mock(BaseTradingConfig.class);
		PreOrderValidationService<M, N, Long> service = new PreOrderValidationService<M, N, Long>(tradeInfoService,
				null, baseTradingCfg, orderInfoService);
		TradeableInstrument<N> audnzd = new TradeableInstrument<N>(AUD + TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE
				+ NZD);
		when(baseTradingCfg.getMaxAllowedNetContracts()).thenReturn(4);
		when(tradeInfoService.findNetPositionCountForCurrency(AUD)).thenReturn(4);
		when(tradeInfoService.findNetPositionCountForCurrency(NZD)).thenReturn(3);
		when(orderInfoService.findNetPositionCountForCurrency(AUD)).thenReturn(-1);
		when(orderInfoService.findNetPositionCountForCurrency(NZD)).thenReturn(1);
		assertFalse(service.checkLimitsForCcy(audnzd, TradingSignal.SHORT));
		TradeableInstrument<N> cadchf = new TradeableInstrument<N>(CAD + TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE
				+ CHF);
		when(tradeInfoService.findNetPositionCountForCurrency(CAD)).thenReturn(5);
		when(tradeInfoService.findNetPositionCountForCurrency(CHF)).thenReturn(1);
		when(orderInfoService.findNetPositionCountForCurrency(CAD)).thenReturn(1);
		when(orderInfoService.findNetPositionCountForCurrency(CHF)).thenReturn(1);
		assertTrue(service.checkLimitsForCcy(cadchf, TradingSignal.SHORT));
	}
}
