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

import java.util.Set;

import org.json.simple.JSONObject;

import com.google.common.collect.Sets;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.events.EventPayLoadToTweet;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.utils.OandaUtils;

public class OrderPayLoadToTweet implements EventPayLoadToTweet<JSONObject, OrderEventPayLoad> {

	private final Set<OrderEvents> orderEventsSupported = Sets.newHashSet(OrderEvents.ORDER_FILLED,
			OrderEvents.LIMIT_ORDER_CREATE);

	@Override
	public String toTweet(OrderEventPayLoad payLoad) {
		if (!orderEventsSupported.contains(payLoad.getEvent())) {
			return null;
		}
		final JSONObject jsonPayLoad = payLoad.getPayLoad();
		final String instrument = jsonPayLoad.get(OandaJsonKeys.instrument).toString();

		final String instrumentAsHashtag = OandaUtils.oandaToHashTagCcy(instrument);
		final long tradeUnits = (Long) jsonPayLoad.get(OandaJsonKeys.units);
		final double price = ((Number) jsonPayLoad.get(OandaJsonKeys.price)).doubleValue();
		final String side = jsonPayLoad.get(OandaJsonKeys.side).toString();
		TradingSignal signal = OandaUtils.toTradingSignal(side);

		switch (payLoad.getEvent()) {
		case ORDER_FILLED:
			return String.format("Opened %s position of %d units for %s@%2.5f", signal.name(), tradeUnits,
					instrumentAsHashtag, price);
		case LIMIT_ORDER_CREATE:
			return String.format("%s LIMIT order of %d units for %s@%2.5f", signal.name(), tradeUnits,
					instrumentAsHashtag, price);
		default:
			return null;
		}
	}

}
