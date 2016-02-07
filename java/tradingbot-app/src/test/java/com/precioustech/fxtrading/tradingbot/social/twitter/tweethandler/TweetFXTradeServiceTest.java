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
package com.precioustech.fxtrading.tradingbot.social.twitter.tweethandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.springframework.social.twitter.api.TimelineOperations;
import org.springframework.social.twitter.api.Twitter;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.events.EventPayLoadToTweet;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEvents;

public class TweetFXTradeServiceTest {

	@SuppressWarnings("unchecked")
	@Test
	public void handleEvent() {
		Twitter twitter = mock(Twitter.class);
		Map<Event, EventPayLoadToTweet<JSONObject, TradeEventPayLoad>> eventPayLoadToTweetMap = Maps.newHashMap();

		EventPayLoadToTweet<JSONObject, TradeEventPayLoad> handler = mock(EventPayLoadToTweet.class);
		eventPayLoadToTweetMap.put(TradeEvents.TAKE_PROFIT_FILLED, handler);

		TweetFXTradeService<JSONObject, TradeEventPayLoad> service = new TweetFXTradeService<JSONObject, TradeEventPayLoad>();
		service.twitter = twitter;
		service.eventPayLoadToTweetMap = eventPayLoadToTweetMap;

		EventBus eventBus = new EventBus();
		eventBus.register(service);

		JSONObject jsonPayload = mock(JSONObject.class);

		TradeEventPayLoad payload = new TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED, jsonPayload);

		String tweet = "hello Twitter";

		when(handler.toTweet(payload)).thenReturn(tweet);

		TimelineOperations operations = mock(TimelineOperations.class);
		when(twitter.timelineOperations()).thenReturn(operations);
		eventBus.post(payload);
		verify(operations, times(1)).updateStatus(tweet);
	}
}
