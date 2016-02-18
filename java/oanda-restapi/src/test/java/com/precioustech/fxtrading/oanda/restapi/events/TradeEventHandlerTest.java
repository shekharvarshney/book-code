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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.account.transaction.Transaction;
import com.precioustech.fxtrading.account.transaction.TransactionDataProvider;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.events.notification.email.EmailPayLoad;
import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.OandaTestConstants;
import com.precioustech.fxtrading.trade.TradeInfoService;

public class TradeEventHandlerTest {

	@Test
	public void generatePayLoad() {
		TradeEventHandler eventHandler = new TradeEventHandler(null, null, null);
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
		assertEquals("Trade event TAKE_PROFIT_FILLED for GBP_USD", emailPayLoad.getSubject());
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
		TradeEventHandler eventHandler = new TradeEventHandler(tradeInfoService, null, null);
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
		TradeEventHandler eventHandler = new TradeEventHandler(tradeInfoService, null, null);
		eventHandler.handleEvent(payLoad);
		verify(tradeInfoService, times(1)).refreshTradesForAccount(OandaTestConstants.accountId);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void payLoadToTweet() {
		JSONObject jsonPayLoad = mock(JSONObject.class);
		TradeableInstrument<String> eurchf = new TradeableInstrument<String>("EUR_CHF");
		TradeEventPayLoad payLoad = new TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED, jsonPayLoad);
		when(jsonPayLoad.get(OandaJsonKeys.instrument)).thenReturn(eurchf.getInstrument());
		when(jsonPayLoad.get(OandaJsonKeys.units)).thenReturn(200l);
		when(jsonPayLoad.get(OandaJsonKeys.price)).thenReturn(1.10325);
		when(jsonPayLoad.get(OandaJsonKeys.tradeId)).thenReturn(OandaTestConstants.tradeId);
		when(jsonPayLoad.get(OandaJsonKeys.accountId)).thenReturn(OandaTestConstants.accountId);
		TransactionDataProvider<Long, Long, String> transactionDataProvider = mock(TransactionDataProvider.class);

		// test profit scenario with short
		Transaction<Long, Long, String> transaction = mock(Transaction.class);
		when(transactionDataProvider.getTransaction(OandaTestConstants.tradeId, OandaTestConstants.accountId))
				.thenReturn(transaction);
		when(transaction.getSide()).thenReturn(TradingSignal.SHORT);
		when(transaction.getPrice()).thenReturn(1.11);
		InstrumentService<String> instrumentService = mock(InstrumentService.class);
		when(instrumentService.getPipForInstrument(eq(eurchf))).thenReturn(0.0001);
		TradeEventHandler eventHandler = new TradeEventHandler(null, transactionDataProvider, instrumentService);
		String tweet = eventHandler.toTweet(payLoad);
		assertNotNull(tweet);
		assertEquals("Closed SHORT 200 units of #EURCHF@1.10325 for 67.5 pips.", tweet);

		// test loss scenario with short
		transaction = mock(Transaction.class);
		when(transactionDataProvider.getTransaction(OandaTestConstants.tradeId, OandaTestConstants.accountId))
				.thenReturn(transaction);
		when(transaction.getSide()).thenReturn(TradingSignal.SHORT);
		when(transaction.getPrice()).thenReturn(1.10);
		tweet = eventHandler.toTweet(payLoad);
		assertNotNull(tweet);
		assertEquals("Closed SHORT 200 units of #EURCHF@1.10325 for -32.5 pips.", tweet);

		// test profit scenario with long
		transaction = mock(Transaction.class);
		when(transactionDataProvider.getTransaction(OandaTestConstants.tradeId, OandaTestConstants.accountId))
				.thenReturn(transaction);
		when(transaction.getSide()).thenReturn(TradingSignal.LONG);
		when(transaction.getPrice()).thenReturn(1.10);
		tweet = eventHandler.toTweet(payLoad);
		assertNotNull(tweet);
		assertEquals("Closed LONG 200 units of #EURCHF@1.10325 for 32.5 pips.", tweet);

		// test loss scenario with long
		transaction = mock(Transaction.class);
		when(transactionDataProvider.getTransaction(OandaTestConstants.tradeId, OandaTestConstants.accountId))
				.thenReturn(transaction);
		when(transaction.getSide()).thenReturn(TradingSignal.LONG);
		when(transaction.getPrice()).thenReturn(1.11);
		tweet = eventHandler.toTweet(payLoad);
		assertNotNull(tweet);
		assertEquals("Closed LONG 200 units of #EURCHF@1.10325 for -67.5 pips.", tweet);

		when(transactionDataProvider.getTransaction(OandaTestConstants.tradeId, OandaTestConstants.accountId))
				.thenReturn(null);
		when(jsonPayLoad.get(OandaJsonKeys.side)).thenReturn("sell");
		tweet = eventHandler.toTweet(payLoad);
		assertNotNull(tweet);
		assertEquals("Closed LONG 200 units of #EURCHF@1.10325.", tweet);
		// unsupported event
		TradeEventPayLoad payload2 = new TradeEventPayLoad(TradeEvents.MIGRATE_TRADE_CLOSE, jsonPayLoad);
		assertNull(eventHandler.toTweet(payload2));

	}
}
