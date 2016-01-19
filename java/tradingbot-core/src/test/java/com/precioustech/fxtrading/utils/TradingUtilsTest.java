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
package com.precioustech.fxtrading.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.TradingTestConstants;

public class TradingUtilsTest {

	@Test
	public void getSignTest() {
		assertEquals(1, TradingUtils.getSign("CAD_CHF", TradingSignal.LONG, "CAD"));
		assertEquals(-1, TradingUtils.getSign("NZD_USD", TradingSignal.LONG, "USD"));
		assertEquals(-1, TradingUtils.getSign("AUD_JPY", TradingSignal.SHORT, "AUD"));
		assertEquals(1, TradingUtils.getSign("EUR_CHF", TradingSignal.SHORT, "CHF"));
		assertEquals(0, TradingUtils.getSign("EUR_GBP", TradingSignal.SHORT, "USD"));
	}

	@Test
	public void calculateTakeProfitPriceTest() {
		double tpPrice = TradingUtils.calculateTakeProfitPrice(0.0001, TradingSignal.LONG, 1.11351, 1.11364, 45);
		assertEquals(1.11814, tpPrice, TradingTestConstants.precision);
		tpPrice = TradingUtils.calculateTakeProfitPrice(0.01, TradingSignal.LONG, 121.456, 121.504, 20);
		assertEquals(121.704, tpPrice, TradingTestConstants.precision);
		tpPrice = TradingUtils.calculateTakeProfitPrice(0.0001, TradingSignal.SHORT, 0.95112, 0.95136, 12);
		assertEquals(0.94992, tpPrice, TradingTestConstants.precision);
		tpPrice = TradingUtils.calculateTakeProfitPrice(0.01, TradingSignal.SHORT, 135.895, 135.9, 123);
		assertEquals(134.665, tpPrice, TradingTestConstants.precision);
		tpPrice = TradingUtils.calculateTakeProfitPrice(0.01, TradingSignal.NONE, 135.895, 135.9, 123);
		assertEquals(0.0, tpPrice, TradingTestConstants.precision);
	}

	@Test
	public void executingRequestMsgTest() {
		HttpRequest req = mock(HttpRequest.class);
		RequestLine reqLine = mock(RequestLine.class);
		when(req.getRequestLine()).thenReturn(reqLine);
		when(reqLine.toString()).thenReturn("http://foo.bar");
		assertEquals("Executing request : http://foo.bar", TradingUtils.executingRequestMsg(req));
	}

	@Test
	public void toUnixTimeTest() {
		DateTime dt = new DateTime(1430469000000L);
		assertEquals(1430469000000000L, TradingUtils.toUnixTime(dt));
	}

	@Test
	public void toMillisFromNanosTest() {
		final long nanoinstant = 1430469789198919L;
		assertEquals(1430469789198L, TradingUtils.toMillisFromNanos(nanoinstant));
	}

	@Test
	public void splitCcyPairTest() {
		String currencies[] = TradingUtils.splitCcyPair("EUR_GBP", TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE);
		assertEquals(2, currencies.length);
		assertEquals("EUR", currencies[0]);
		assertEquals("GBP", currencies[1]);
		currencies = TradingUtils.splitCcyPair("EUR_CHF", TradingConstants.FWD_SLASH);
		assertEquals(1, currencies.length);
		assertEquals("EUR_CHF", currencies[0]);
	}

	@Test
	public void splitInstrumentPairTest() {
		String[] currencies = TradingUtils.splitInstrumentPair("EUR/USD");
		assertEquals(2, currencies.length);
		assertEquals("EUR", currencies[0]);
		assertEquals("USD", currencies[1]);
		try {
			TradingUtils.splitInstrumentPair(null);
			fail("Expected IllegalArgumentException because null was passed in");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			TradingUtils.splitInstrumentPair(StringUtils.EMPTY);
			fail("Expected IllegalArgumentException because empty string was passed in");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			TradingUtils.splitInstrumentPair("foo");
			fail("Expected IllegalArgumentException because unexpected string was passed in");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void responseToStringTest() throws Exception {
		HttpResponse resp = mock(HttpResponse.class);
		HttpEntity entity = mock(HttpEntity.class);
		when(resp.getEntity()).thenReturn(entity);
		StatusLine statusLine = mock(StatusLine.class);
		when(resp.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(entity.getContent()).thenReturn(new FileInputStream("src/test/resources/foobar.txt"));
		assertEquals("hello world foo!!", TradingUtils.responseToString(resp));
	}

	@Test
	public void isEmptyTest() {
		List<String> nameColl = Lists.newArrayList("foo", "bar");
		assertFalse(TradingUtils.isEmpty(nameColl));
		nameColl.clear();
		assertTrue(TradingUtils.isEmpty(nameColl));
		nameColl = null;
		assertTrue(TradingUtils.isEmpty(nameColl));
		Map<Integer, String> idNameMap = Maps.newHashMap();
		idNameMap.put(1, "foobar");
		assertFalse(TradingUtils.isEmpty(idNameMap));
		idNameMap.remove(1);
		assertTrue(TradingUtils.isEmpty(idNameMap));
		idNameMap = null;
		assertTrue(TradingUtils.isEmpty(idNameMap));
	}
}
