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
package com.precioustech.fxtrading.oanda.restapi.events;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.events.notification.email.EmailPayLoad;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.trade.TradeInfoService;

public class TradeEventHandlerTest {

	@Test
	public void generatePayLoad() {
		TradeEventHandler eventHandler = new TradeEventHandler(null);
		// its ok if the we pass null to the constructor here as its not used
		JSONObject jsonPayLoad = mock(JSONObject.class);
		EventPayLoad<JSONObject> payLoad = new TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED, jsonPayLoad);
		when(jsonPayLoad.get(OandaJsonKeys.instrument)).thenReturn("GBP_USD");
		when(jsonPayLoad.get(OandaJsonKeys.type)).thenReturn(TradeEvents.TAKE_PROFIT_FILLED.name());
		when(jsonPayLoad.get(OandaJsonKeys.accountId)).thenReturn(OandaTestConstants.accountId);
		when(jsonPayLoad.get(OandaJsonKeys.accountBalance)).thenReturn(100.00);
		when(jsonPayLoad.get(OandaJsonKeys.tradeId)).thenReturn(OandaTestConstants.tradeId);
		when(jsonPayLoad.get(OandaJsonKeys.pl)).thenReturn(22.45);
		when(jsonPayLoad.get(OandaJsonKeys.interest)).thenReturn(-1.45);
		when(jsonPayLoad.get(OandaJsonKeys.units)).thenReturn(10000L);
		EmailPayLoad emailPayLoad = eventHandler.generate(payLoad);
		assertEquals("Order event TAKE_PROFIT_FILLED for GBP_USD", emailPayLoad.getSubject());
		assertEquals(
				"Trade event TAKE_PROFIT_FILLED received for account 123456. Trade id=1800805337. Pnl=22.450, Interest=-1.450, Trade Units=10000. Account balance after the event=100.00",
				emailPayLoad.getBody());
	}

	@Test
	public void unSupportedTradeEvent() {
		JSONObject jsonPayLoad = mock(JSONObject.class);
		TradeEventPayLoad payLoad = new TradeEventPayLoad(TradeEvents.MIGRATE_TRADE_CLOSE, jsonPayLoad);
		@SuppressWarnings("unchecked")
		TradeInfoService<Long, String, Long> tradeInfoService = mock(TradeInfoService.class);
		TradeEventHandler eventHandler = new TradeEventHandler(tradeInfoService);
		eventHandler.handleEvent(payLoad);
		verify(tradeInfoService, times(0)).refreshTradesForAccount(OandaTestConstants.accountId);
	}

	@Test
	public void tradeEvent() {
		JSONObject jsonPayLoad = mock(JSONObject.class);
		TradeEventPayLoad payLoad = new TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED, jsonPayLoad);
		when(jsonPayLoad.get(OandaJsonKeys.accountId)).thenReturn(OandaTestConstants.accountId);
		@SuppressWarnings("unchecked")
		TradeInfoService<Long, String, Long> tradeInfoService = mock(TradeInfoService.class);
		TradeEventHandler eventHandler = new TradeEventHandler(tradeInfoService);
		eventHandler.handleEvent(payLoad);
		verify(tradeInfoService, times(1)).refreshTradesForAccount(OandaTestConstants.accountId);
	}
}
