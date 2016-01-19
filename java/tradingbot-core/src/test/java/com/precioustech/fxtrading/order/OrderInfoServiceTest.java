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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.TradingTestConstants;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

@SuppressWarnings("unchecked")
public class OrderInfoServiceTest<N> {

	@Test
	public void netPositionCountForCurrencyTest() {
		final OrderManagementProvider<Long, N, Long> orderManagementProvider = mock(OrderManagementProvider.class);
		OrderInfoService<Long, N, Long> service = new OrderInfoService<Long, N, Long>(orderManagementProvider);
		Collection<Order<N, Long>> orders = createOrders();
		when(orderManagementProvider.allPendingOrders()).thenReturn(orders);
		assertEquals(0, service.findNetPositionCountForCurrency("EUR"));
		assertEquals(2, service.findNetPositionCountForCurrency("JPY"));
		assertEquals(-1, service.findNetPositionCountForCurrency("GBP"));
		assertEquals(0, service.findNetPositionCountForCurrency("XAU"));

		/*test other wrapper methods as well. may have to change if caching is introduced*/
		service.pendingOrderForAccount(TradingTestConstants.orderId, TradingTestConstants.accountId);
		verify(orderManagementProvider, times(1)).pendingOrderForAccount(TradingTestConstants.orderId,
				TradingTestConstants.accountId);

		service.pendingOrdersForAccount(TradingTestConstants.accountId2);
		verify(orderManagementProvider, times(1)).pendingOrdersForAccount(TradingTestConstants.accountId2);

		TradeableInstrument<N> usdchf = new TradeableInstrument<N>("USD_CHF");
		service.pendingOrdersForInstrument(usdchf);
		verify(orderManagementProvider, times(1)).pendingOrdersForInstrument(usdchf);
	}

	private Collection<Order<N, Long>> createOrders() {
		Collection<Order<N, Long>> orders = Lists.newArrayList();

		Order<N, Long> order1 = mock(Order.class);
		TradeableInstrument<N> eurjpy = new TradeableInstrument<N>("EUR_JPY");
		when(order1.getInstrument()).thenReturn(eurjpy);
		when(order1.getSide()).thenReturn(TradingSignal.SHORT);
		orders.add(order1);

		Order<N, Long> order2 = mock(Order.class);
		TradeableInstrument<N> eurusd = new TradeableInstrument<N>("EUR_USD");
		when(order2.getInstrument()).thenReturn(eurusd);
		when(order2.getSide()).thenReturn(TradingSignal.LONG);
		orders.add(order2);

		Order<N, Long> order3 = mock(Order.class);
		TradeableInstrument<N> gbpjpy = new TradeableInstrument<N>("GBP_JPY");
		when(order3.getInstrument()).thenReturn(gbpjpy);
		when(order3.getSide()).thenReturn(TradingSignal.SHORT);
		orders.add(order3);

		return orders;
	}
}
