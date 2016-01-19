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
package com.precioustech.fxtrading.oanda.restapi.streaming;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.account.Account;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.events.EventCallback;
import com.precioustech.fxtrading.events.EventCallbackImpl;
import com.precioustech.fxtrading.heartbeats.HeartBeatCallback;
import com.precioustech.fxtrading.heartbeats.HeartBeatCallbackImpl;
import com.precioustech.fxtrading.heartbeats.HeartBeatPayLoad;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.MarketDataPayLoad;
import com.precioustech.fxtrading.marketdata.MarketEventCallback;
import com.precioustech.fxtrading.marketdata.MarketEventHandlerImpl;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.oanda.restapi.OandaTestUtils;
import com.precioustech.fxtrading.oanda.restapi.events.OrderEventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.streaming.events.OandaEventsStreamingService;
import com.precioustech.fxtrading.oanda.restapi.streaming.marketdata.OandaMarketDataStreamingService;

public class OandaStreamingServiceTest {

	private volatile int audcadCt;
	private volatile int nzdsgdCt;
	private volatile int tradeEventCt;
	private volatile int orderEventCt;
	private volatile int heartbeatCt;
	private static final int expectedPriceEvents = 668;// 1 for each
	// private static final Long anotherAccId = 234567L;
	private static final TradeableInstrument<String> AUDCAD = new TradeableInstrument<String>("AUD_CAD");
	private static final TradeableInstrument<String> NZDSGD = new TradeableInstrument<String>("NZD_SGD");
	private AtomicReference<MarketDataPayLoad<String>> audcadLastRef = new AtomicReference<MarketDataPayLoad<String>>();
	private AtomicReference<MarketDataPayLoad<String>> nzdsgdLastRef = new AtomicReference<MarketDataPayLoad<String>>();
	private static final String disconnectmsg = "{\"disconnect\":{\"code\":64,\"message\":\"bye\",\"moreInfo\":\"none\"}}";

	@Before
	public void reset() {
		heartbeatCt = 0;
	}

	@Test
	public void eventsStreaming() throws Exception {

		@SuppressWarnings("unchecked")
		AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
		Collection<Account<Long>> mockAccounts = getMockAccounts();
		when(accountDataProvider.getLatestAccountInfo()).thenReturn(mockAccounts);
		EventBus eventBus = new EventBus();
		eventBus.register(this);
		HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);
		EventCallback<JSONObject> eventCallback = new EventCallbackImpl<JSONObject>(eventBus);

		OandaStreamingService service = new OandaEventsStreamingService(OandaTestConstants.streaming_url,
				OandaTestConstants.accessToken, accountDataProvider, eventCallback, heartBeatCallback, "TESTEVTSTREAM");
		assertEquals("https://stream-fxtrade.oanda.com/v1/events?accountIds=123456%2C234567", service.getStreamingUrl());
		OandaStreamingService spy = setUpSpy(service, "src/test/resources/events.txt");
		assertEquals(6, heartbeatCt);
		assertEquals(1, this.orderEventCt);
		assertEquals(2, this.tradeEventCt);
		verify(spy, times(1)).handleDisconnect(disconnectmsg);
	}

	@SuppressWarnings("unchecked")
	private Collection<Account<Long>> getMockAccounts() {
		Collection<Account<Long>> mockAccounts = Lists.newArrayListWithExpectedSize(2);
		Account<Long> account1 = mock(Account.class);
		when(account1.getAccountId()).thenReturn(OandaTestConstants.accountId);
		Account<Long> account2 = mock(Account.class);
		when(account2.getAccountId()).thenReturn(OandaTestConstants.accountId2);

		mockAccounts.add(account1);
		mockAccounts.add(account2);
		return mockAccounts;
	}

	private OandaStreamingService setUpSpy(OandaStreamingService service, String fname) throws Exception {
		OandaStreamingService spy = spy(service);
		CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
		when(spy.getHttpClient()).thenReturn(mockHttpClient);
		when(spy.isStreaming()).thenReturn(service.isStreaming());
		OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);
		spy.startStreaming();
		do {
			Thread.sleep(2L);
		} while (spy.streamThread.isAlive());
		return spy;
	}

	@Test
	public void marketDataStreaming() throws Exception {
		Collection<TradeableInstrument<String>> instruments = Lists.newArrayList();
		EventBus eventBus = new EventBus();
		MarketEventCallback<String> mktEventCallback = new MarketEventHandlerImpl<String>(eventBus);
		HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);
		eventBus.register(this);
		instruments.add(AUDCAD);
		instruments.add(NZDSGD);
		OandaStreamingService service = new OandaMarketDataStreamingService(OandaTestConstants.streaming_url,
				OandaTestConstants.accessToken, OandaTestConstants.accountId, instruments, mktEventCallback,
				heartBeatCallback, "TESTMKTSTREAM");
		assertEquals("https://stream-fxtrade.oanda.com/v1/prices?accountId=123456&instruments=AUD_CAD%2CNZD_SGD",
				service.getStreamingUrl());
		OandaStreamingService spy = setUpSpy(service, "src/test/resources/marketData123456.txt");
		assertEquals(expectedPriceEvents / 2, audcadCt);
		assertEquals(expectedPriceEvents / 2, nzdsgdCt);
		assertEquals(expectedPriceEvents / 4, heartbeatCt);
		MarketDataPayLoad<String> audcadPayLoad = audcadLastRef.get();
		assertEquals(1.0149, audcadPayLoad.getBidPrice(), OandaTestConstants.precision);
		assertEquals(1.0151, audcadPayLoad.getAskPrice(), OandaTestConstants.precision);
		assertEquals(1401920421958L, audcadPayLoad.getEventDate().getMillis());
		MarketDataPayLoad<String> nzdsgdPayLoad = nzdsgdLastRef.get();
		assertEquals(1.0799, nzdsgdPayLoad.getBidPrice(), OandaTestConstants.precision);
		assertEquals(1.0801, nzdsgdPayLoad.getAskPrice(), OandaTestConstants.precision);
		assertEquals(1401920421958L, nzdsgdPayLoad.getEventDate().getMillis());
		verify(spy, times(1)).handleDisconnect(disconnectmsg);
	}

	@Subscribe
	public void dummyMarketDataSubscriber(MarketDataPayLoad<String> payLoad) {
		if (payLoad.getInstrument().equals(AUDCAD)) {
			this.audcadCt++;
			this.audcadLastRef.set(payLoad);
		} else {
			this.nzdsgdCt++;
			this.nzdsgdLastRef.set(payLoad);
		}
	}

	@Subscribe
	public void dummyTradeEventSubscriber(TradeEventPayLoad payLoad) {
		this.tradeEventCt++;
	}

	@Subscribe
	public void dummyOrderEventSubscriber(OrderEventPayLoad payLoad) {
		this.orderEventCt++;
	}

	@Subscribe
	public void dummyHeartBeatSubscriber(HeartBeatPayLoad<DateTime> payLoad) {
		heartbeatCt++;
	}
}
