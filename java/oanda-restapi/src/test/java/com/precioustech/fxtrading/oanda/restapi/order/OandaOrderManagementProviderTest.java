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
package com.precioustech.fxtrading.oanda.restapi.order;

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

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;
import com.precioustech.fxtrading.order.Order;
import com.precioustech.fxtrading.order.OrderType;

public class OandaOrderManagementProviderTest {

	@Test
	@SuppressWarnings("unchecked")
	public void allOrders() throws Exception {

		AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
		Account<Long> account = mock(Account.class);
		when(accountDataProvider.getLatestAccountInfo()).thenReturn(Lists.newArrayList(account));
		when(account.getAccountId()).thenReturn(OandaTestConstants.accountId);
		OandaOrderManagementProvider service = new OandaOrderManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken, accountDataProvider);
		OandaOrderManagementProvider spy = doMockStuff("src/test/resources/allOrders.txt", service);
		Collection<Order<String, Long>> pendingOrders = spy.allPendingOrders();
		assertEquals(2, pendingOrders.size());
		Iterator<Order<String, Long>> iterator = pendingOrders.iterator();
		Order<String, Long> order1 = iterator.next();
		Order<String, Long> order2 = iterator.next();

		assertEquals("USD_CAD", order1.getInstrument().getInstrument());
		assertEquals(TradingSignal.LONG, order1.getSide());
		assertEquals(OrderType.LIMIT, order1.getType());
		assertEquals(1.3, order1.getPrice(), OandaTestConstants.precision);
		assertEquals(1.2, order1.getStopLoss(), OandaTestConstants.precision);
		assertEquals(1.31, order1.getTakeProfit(), OandaTestConstants.precision);
		assertEquals(100l, order1.getUnits());

		assertEquals("EUR_USD", order2.getInstrument().getInstrument());
		assertEquals(TradingSignal.LONG, order2.getSide());
		assertEquals(OrderType.LIMIT, order2.getType());
		assertEquals(1.115, order2.getPrice(), OandaTestConstants.precision);
		assertEquals(150l, order2.getUnits(), OandaTestConstants.precision);
		assertEquals(0.0, order2.getStopLoss(), OandaTestConstants.precision);
		assertEquals(0.0, order2.getTakeProfit(), OandaTestConstants.precision);
	}

	@Test
	public void orderForAccount() throws Exception {
		OandaOrderManagementProvider service = new OandaOrderManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken, null);
		assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/orders/1001", service.orderForAccountUrl(
				OandaTestConstants.accountId, OandaTestConstants.orderId));
		OandaOrderManagementProvider spy = doMockStuff("src/test/resources/orderForAccount123456.txt", service);
		Order<String, Long> order = spy
				.pendingOrderForAccount(OandaTestConstants.orderId, OandaTestConstants.accountId);
		assertNotNull(order);
		assertEquals("USD_JPY", order.getInstrument().getInstrument());
		assertEquals(TradingSignal.SHORT, order.getSide());
		assertEquals(OrderType.LIMIT, order.getType());
		assertEquals(122.15, order.getPrice(), OandaTestConstants.precision);
		assertEquals(125l, order.getUnits());
		assertEquals(125.00, order.getStopLoss(), OandaTestConstants.precision);
		assertEquals(119.25, order.getTakeProfit(), OandaTestConstants.precision);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void modifyOrderTest() throws Exception {
		OandaOrderManagementProvider service = new OandaOrderManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken, null);
		OandaOrderManagementProvider spy = doMockStuff("src/test/resources/orderForAccount123456.txt", service);
		Order<String, Long> order = mock(Order.class);
		when(order.getTakeProfit()).thenReturn(119.45);
		when(order.getStopLoss()).thenReturn(124.75);
		when(order.getUnits()).thenReturn(1000l);
		when(order.getPrice()).thenReturn(122.0);
		when(order.getOrderId()).thenReturn(OandaTestConstants.orderId);
		spy.modifyOrder(order, OandaTestConstants.accountId);
		verify(spy, times(1)).createPatchCommand(order, OandaTestConstants.accountId);
		verify(order, times(1)).getOrderId();
		verify(order, times(1)).getTakeProfit();
		verify(order, times(1)).getStopLoss();
		verify(order, times(1)).getPrice();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void createOrderTest() throws Exception {
		OandaOrderManagementProvider service = new OandaOrderManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken, null);
		TradeableInstrument<String> eurjpy = new TradeableInstrument<String>("EUR_JPY");

		OandaOrderManagementProvider spy = doMockStuff("src/test/resources/newOrder.txt", service);
		Order<String, Long> orderMarket = mock(Order.class);
		when(orderMarket.getInstrument()).thenReturn(eurjpy);
		when(orderMarket.getSide()).thenReturn(TradingSignal.SHORT);
		when(orderMarket.getType()).thenReturn(OrderType.MARKET);
		when(orderMarket.getUnits()).thenReturn(150l);
		when(orderMarket.getTakeProfit()).thenReturn(132.65);
		when(orderMarket.getStopLoss()).thenReturn(136.00);
		// when(order.getPrice()).thenReturn(133.75);
		Long orderId = spy.placeOrder(orderMarket, OandaTestConstants.accountId);
		assertNotNull(orderId);
		verify(spy, times(1)).createPostCommand(orderMarket, OandaTestConstants.accountId);
		verify(orderMarket, times(1)).getInstrument();
		verify(orderMarket, times(3)).getType();
		verify(orderMarket, times(1)).getTakeProfit();
		verify(orderMarket, times(1)).getStopLoss();
		// verify(order, times(2)).getPrice();
		verify(orderMarket, times(1)).getUnits();
		verify(orderMarket, times(1)).getSide();

		spy = doMockStuff("src/test/resources/newOrderLimit.txt", service);
		Order<String, Long> orderLimit = mock(Order.class);
		TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
		when(orderLimit.getInstrument()).thenReturn(eurusd);
		when(orderLimit.getSide()).thenReturn(TradingSignal.SHORT);
		when(orderLimit.getType()).thenReturn(OrderType.LIMIT);
		when(orderLimit.getUnits()).thenReturn(10l);
		when(orderLimit.getTakeProfit()).thenReturn(1.09);
		when(orderLimit.getStopLoss()).thenReturn(0.0);
		when(orderLimit.getPrice()).thenReturn(1.10);

		orderId = spy.placeOrder(orderLimit, OandaTestConstants.accountId);
		assertNotNull(orderId);
		verify(spy, times(1)).createPostCommand(orderLimit, OandaTestConstants.accountId);
		verify(orderLimit, times(1)).getInstrument();
		verify(orderLimit, times(3)).getType();
		verify(orderLimit, times(1)).getTakeProfit();
		verify(orderLimit, times(1)).getStopLoss();
		verify(orderLimit, times(2)).getPrice();
		verify(orderLimit, times(1)).getUnits();
		verify(orderLimit, times(1)).getSide();
	}

	@Test
	public void closeOrderTest() throws Exception {
		OandaOrderManagementProvider service = new OandaOrderManagementProvider(OandaTestConstants.url,
				OandaTestConstants.accessToken, null);

		OandaOrderManagementProvider spy = doMockStuff("src/test/resources/orderForAccount123456.txt", service);

		boolean success = spy.closeOrder(OandaTestConstants.orderId, OandaTestConstants.accountId);
		assertTrue(success);
		verify(spy.getHttpClient(), times(1)).execute(any(HttpDelete.class));
	}

	private OandaOrderManagementProvider doMockStuff(String fname, OandaOrderManagementProvider service)
			throws Exception {
		OandaOrderManagementProvider spy = spy(service);
		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);
		OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);
		return spy;
	}
}
