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
package com.precioustech.fxtrading.tradingbot;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.precioustech.fxtrading.streaming.events.EventsStreamingService;
import com.precioustech.fxtrading.streaming.marketdata.MarketDataStreamingService;

public class FXTradingBot {

	private static final Logger LOG = Logger.getLogger(FXTradingBot.class);

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		if (args.length == 0) {
			LOG.fatal("Usage: FxTradingBot <Implementation Config FileName>");
			System.exit(1);
		}
		ApplicationContext appContext = new ClassPathXmlApplicationContext("tradingbot-app.xml", args[0]);

		MarketDataStreamingService marketDataStreamingService = appContext.getBean(MarketDataStreamingService.class);
		marketDataStreamingService.startMarketDataStreaming();
		EventsStreamingService eventStreamingService = appContext.getBean(EventsStreamingService.class);
		eventStreamingService.startEventsStreaming();

	}

}
