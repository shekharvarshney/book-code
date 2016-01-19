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
package com.precioustech.fxtrading.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingTestConstants;

public class InstrumentServiceTest {

	private final double nonjpypip = 0.0001;
	private final double jpypip = 0.01;

	@Test
	public void testAll() {
		@SuppressWarnings("unchecked")
		InstrumentDataProvider<String> instrumentDataProvider = mock(InstrumentDataProvider.class);

		Collection<TradeableInstrument<String>> instruments = createInstruments();
		when(instrumentDataProvider.getInstruments()).thenReturn(instruments);
		InstrumentService<String> service = new InstrumentService<String>(instrumentDataProvider);
		Collection<TradeableInstrument<String>> usdpairs = service.getAllPairsWithCurrency("USD");
		assertEquals(4, usdpairs.size());
		TradeableInstrument<String> usdjpy = new TradeableInstrument<String>("USD_JPY");
		assertTrue(usdpairs.contains(usdjpy));
		Collection<TradeableInstrument<String>> jpypairs = service.getAllPairsWithCurrency("JPY");
		assertEquals(2, jpypairs.size());
		jpypairs.contains(usdjpy);
		Collection<TradeableInstrument<String>> xaupairs = service.getAllPairsWithCurrency("XAU");
		assertTrue(xaupairs.isEmpty());
		Collection<TradeableInstrument<String>> nullpairs = service.getAllPairsWithCurrency(null);
		assertTrue(nullpairs.isEmpty());
		assertEquals(nonjpypip, service.getPipForInstrument(new TradeableInstrument<String>("GBP_CHF")),
				TradingTestConstants.precision);
		assertEquals(jpypip, service.getPipForInstrument(usdjpy), TradingTestConstants.precision);
		assertEquals(1.0, service.getPipForInstrument(new TradeableInstrument<String>("XAU_EUR")),
				TradingTestConstants.precision);
	}

	@Test
	public void equalityTest() {
		TradeableInstrument<Long> usdjpy1 = new TradeableInstrument<Long>("USD_JPY", 10001L, "USDJPY currency pair");
		TradeableInstrument<Long> usdjpy2 = new TradeableInstrument<Long>("USD_JPY", 10002L, "USDJPY currency pair");
		TradeableInstrument<Long> usdjpy3 = new TradeableInstrument<Long>("USD_JPY", 10001L, "USDJPY currency pair");
		TradeableInstrument<Long> usdchf1 = new TradeableInstrument<Long>("USD_CHF", 10003L, "USDCHF currency pair");
		assertTrue(usdjpy1.equals(usdjpy3));
		assertFalse(usdjpy1.equals(usdjpy2));
		assertFalse(usdjpy3.equals(usdchf1));
	}

	private Collection<TradeableInstrument<String>> createInstruments() {
		Collection<TradeableInstrument<String>> instruments = Lists.newArrayList();

		instruments.add(new TradeableInstrument<String>("GBP_USD", nonjpypip, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<String>("GBP_CHF", nonjpypip, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<String>("EUR_USD", nonjpypip, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<String>("NZD_USD", nonjpypip, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<String>("USD_JPY", jpypip, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<String>("AUD_JPY", jpypip, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		return instruments;
	}
}
