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
package com.precioustech.fxtrading.oanda.restapi.trade.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.oanda.restapi.OandaConstants;
import com.precioustech.fxtrading.oanda.restapi.events.AccountEventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.events.AccountEvents;
import com.precioustech.fxtrading.oanda.restapi.events.OrderEventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.events.OrderEvents;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEvents;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;
import com.precioustech.fxtrading.order.OrderType;

public class OandaUtilsTest {

	@Test
	public void toOandaTransactionTypeTest() {
		assertEquals(TradeEvents.TRADE_CLOSE, OandaUtils.toOandaTransactionType("TRADE_CLOSE"));
		assertEquals(OrderEvents.ORDER_FILLED, OandaUtils.toOandaTransactionType("ORDER_FILLED"));
		assertEquals(AccountEvents.MARGIN_CLOSEOUT, OandaUtils.toOandaTransactionType("MARGIN_CLOSEOUT"));
		assertNull(OandaUtils.toOandaTransactionType("FOO"));
	}

	@Test
	public void toOandaEventPayLoadTest() {
		assertTrue(OandaUtils.toOandaEventPayLoad("TRADE_UPDATE", null) instanceof TradeEventPayLoad);
		assertTrue(OandaUtils.toOandaEventPayLoad("ORDER_FILLED", null) instanceof OrderEventPayLoad);
		assertTrue(OandaUtils.toOandaEventPayLoad("FEE", null) instanceof AccountEventPayLoad);
		assertNull(OandaUtils.toOandaEventPayLoad("BAR", null));
	}

	@Test
	public void splitOandaCcyTest() {
		String[] pair = OandaUtils.splitCcyPair("GBP_USD");
		assertEquals(2, pair.length);
		assertEquals("GBP", pair[0]);
		assertEquals("USD", pair[1]);
	}

	@Test
	public void toOandaCcyTest() {
		assertEquals("USD_ZAR", OandaUtils.toOandaCcy("USD", "ZAR"));
		try {
			OandaUtils.toOandaCcy("us", "ZAR");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// pass
		}
		try {
			OandaUtils.toOandaCcy("USD", "za");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	@Test
	public void isoCcyToOandaCcyTest() {
		assertEquals("GBP_CHF", OandaUtils.isoCcyToOandaCcy("GBPCHF"));
		try {
			OandaUtils.isoCcyToOandaCcy("gbpch");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	@Test
	public void hashTagCcyToOandaCcyTest() {
		assertEquals("AUD_CAD", OandaUtils.hashTagCcyToOandaCcy("#AUDCAD"));
		try {
			OandaUtils.hashTagCcyToOandaCcy("$AUDCAD");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// pass
		}
		try {
			OandaUtils.hashTagCcyToOandaCcy("AUDCAD");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	@Test
	public void toTradingSignalTest() {
		assertEquals(TradingSignal.LONG, OandaUtils.toTradingSignal(OandaConstants.BUY));
		assertEquals(TradingSignal.SHORT, OandaUtils.toTradingSignal(OandaConstants.SELL));
		assertEquals(TradingSignal.NONE, OandaUtils.toTradingSignal("Sell"));
		assertEquals(TradingSignal.NONE, OandaUtils.toTradingSignal("foo"));
	}

	@Test
	public void toOrderTypeTest() {
		assertEquals(OrderType.MARKET, OandaUtils.toOrderType(OandaConstants.ORDER_MARKET));
		assertEquals(OrderType.LIMIT, OandaUtils.toOrderType(OandaConstants.ORDER_LIMIT));
		assertEquals(OrderType.LIMIT, OandaUtils.toOrderType(OandaConstants.ORDER_MARKET_IF_TOUCHED));
		try {
			OandaUtils.toOrderType("foo");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}
}
