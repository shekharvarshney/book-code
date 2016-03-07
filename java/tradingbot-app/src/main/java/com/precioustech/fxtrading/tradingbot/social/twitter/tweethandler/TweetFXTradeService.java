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

import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Twitter;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.events.EventPayLoadToTweet;

public class TweetFXTradeService<K, T extends EventPayLoad<K>> {
	
	private static final Logger LOG = Logger.getLogger(TweetFXTradeService.class);

	@Autowired
	Twitter twitter;

	@Resource
	Map<Event, EventPayLoadToTweet<K, T>> eventPayLoadToTweetMap;

	
	@Subscribe
	@AllowConcurrentEvents
	public void handleEvent(T payLoad) {
		EventPayLoadToTweet<K, T> eventPayLoadToTweet = this.eventPayLoadToTweetMap.get(payLoad.getEvent());
		if (eventPayLoadToTweet != null) {
			try {
				String tweet = eventPayLoadToTweet.toTweet(payLoad);
				if (tweet != null) {
					twitter.timelineOperations().updateStatus(tweet);
				}
			} catch (Exception ex) {
				LOG.error("error encountered whilst handling payload->" + payLoad.getPayLoad(), ex);
			}
		}
	}
	

}
