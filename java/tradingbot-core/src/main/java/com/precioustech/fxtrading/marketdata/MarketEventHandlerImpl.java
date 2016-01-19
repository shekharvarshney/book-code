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

import org.joda.time.DateTime;

import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class MarketEventHandlerImpl<T> implements MarketEventCallback<T> {

	private final EventBus eventBus;

	public MarketEventHandlerImpl(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void onMarketEvent(TradeableInstrument<T> instrument, double bid, double ask, DateTime eventDate) {
		MarketDataPayLoad<T> payload = new MarketDataPayLoad<T>(instrument, bid, ask, eventDate);
		eventBus.post(payload);

	}

}
