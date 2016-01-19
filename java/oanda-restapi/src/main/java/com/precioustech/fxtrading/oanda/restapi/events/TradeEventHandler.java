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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.events.EventHandler;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.events.notification.email.EmailContentGenerator;
import com.precioustech.fxtrading.events.notification.email.EmailPayLoad;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.trade.TradeInfoService;

public class TradeEventHandler implements EventHandler<JSONObject, TradeEventPayLoad>,
		EmailContentGenerator<JSONObject> {

	private final Set<TradeEvents> tradeEventsSupported = Sets.newHashSet(TradeEvents.STOP_LOSS_FILLED,
			TradeEvents.TRADE_CLOSE, TradeEvents.TAKE_PROFIT_FILLED);
	private final TradeInfoService<Long, String, Long> tradeInfoService;

	public TradeEventHandler(TradeInfoService<Long, String, Long> tradeInfoService) {
		this.tradeInfoService = tradeInfoService;
	}

	@Override
	@Subscribe
	@AllowConcurrentEvents
	public void handleEvent(TradeEventPayLoad payLoad) {
		Preconditions.checkNotNull(payLoad);
		if (!tradeEventsSupported.contains(payLoad.getEvent())) {
			return;
		}
		JSONObject jsonPayLoad = payLoad.getPayLoad();
		long accountId = (Long) jsonPayLoad.get(OandaJsonKeys.accountId);
		tradeInfoService.refreshTradesForAccount(accountId);
	}

	@Override
	public EmailPayLoad generate(EventPayLoad<JSONObject> payLoad) {
		JSONObject jsonPayLoad = payLoad.getPayLoad();
		TradeableInstrument<String> instrument = new TradeableInstrument<String>(jsonPayLoad.get(
				OandaJsonKeys.instrument).toString());
		final String type = jsonPayLoad.get(OandaJsonKeys.type).toString();
		final long accountId = (Long) jsonPayLoad.get(OandaJsonKeys.accountId);
		final double accountBalance = ((Number) jsonPayLoad.get(OandaJsonKeys.accountBalance)).doubleValue();
		final long tradeId = (Long) jsonPayLoad.get(OandaJsonKeys.tradeId);
		final double pnl = ((Number) jsonPayLoad.get(OandaJsonKeys.pl)).doubleValue();
		final double interest = ((Number) jsonPayLoad.get(OandaJsonKeys.interest)).doubleValue();
		final long tradeUnits = (Long) jsonPayLoad.get(OandaJsonKeys.units);
		final String emailMsg = String
				.format("Trade event %s received for account %d. Trade id=%d. Pnl=%5.3f, Interest=%5.3f, Trade Units=%d. Account balance after the event=%5.2f",
						type, accountId, tradeId, pnl, interest, tradeUnits, accountBalance);
		final String subject = String.format("Order event %s for %s", type, instrument.getInstrument());
		return new EmailPayLoad(subject, emailMsg);
	}

}
