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
package com.precioustech.fxtrading.marketdata;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.TradingTestConstants;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class MarketEventHandlerImplTest {

	private final double bid = 1.55;
	private final double ask = 1.5502;
	private final String gbpusd = "GBP_USD";
	private final int numSubscribers = 2;
	private final CountDownLatch done = new CountDownLatch(numSubscribers);

	@Test
	public void testRideEventBus() throws Exception {
		EventBus evtBus = new AsyncEventBus(Executors.newFixedThreadPool(numSubscribers));
		for (int i = 1; i <= numSubscribers; i++) {
			evtBus.register(new MarketDataSubscriber());
		}
		MarketEventCallback<String> callback = new MarketEventHandlerImpl<String>(evtBus);
		callback.onMarketEvent(new TradeableInstrument<String>(gbpusd), bid, ask, DateTime.now());
		done.await();
	}

	private class MarketDataSubscriber {

		@Subscribe
		@AllowConcurrentEvents
		public void processPayLoad(MarketDataPayLoad<String> payload) {
			assertEquals(gbpusd, payload.getInstrument().getInstrument());
			assertEquals(bid, payload.getBidPrice(), TradingTestConstants.precision);
			assertEquals(ask, payload.getAskPrice(), TradingTestConstants.precision);
			done.countDown();
		}
	}

}
