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

/**
 * A callback handler for a market data event. The separate streaming event
 * handler upstream, is responsible for handling and parsing the incoming event
 * from the market data source and invoke the onMarketEvent of this handler,
 * which in turn can disseminate the event if required, further downstream.
 * Ideally, the implementer of this interface, would drop the event on a queue
 * for asynchronous processing or use an event bus for synchronous processing.
 * 
 * @author Shekhar Varshney
 *
 * @param <T>
 *            The type of instrumentId in class TradeableInstrument
 * @see TradeableInstrument
 * @see EventBus
 */
public interface MarketEventCallback<T> {
	/**
	 * A method, invoked by the upstream handler of streaming market data
	 * events. This invocation of this method is synchronous, therefore the
	 * method should return asap, to make sure that the upstream events do not
	 * queue up.
	 * 
	 * @param instrument
	 * @param bid
	 * @param ask
	 * @param eventDate
	 */
	void onMarketEvent(TradeableInstrument<T> instrument, double bid, double ask, DateTime eventDate);
}
