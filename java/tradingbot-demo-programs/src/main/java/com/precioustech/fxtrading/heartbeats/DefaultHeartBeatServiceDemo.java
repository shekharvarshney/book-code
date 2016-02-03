package com.precioustech.fxtrading.heartbeats;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.MarketEventCallback;
import com.precioustech.fxtrading.marketdata.MarketEventHandlerImpl;
import com.precioustech.fxtrading.oanda.restapi.streaming.marketdata.OandaMarketDataStreamingService;
import com.precioustech.fxtrading.streaming.heartbeats.HeartBeatStreamingService;

public class DefaultHeartBeatServiceDemo {

	private static final Logger LOG = Logger.getLogger(DefaultHeartBeatServiceDemo.class);

	private static void usageAndValidation(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: DefaultHeartBeatServiceDemo <url> <accountid> <accesstoken>");
			System.exit(1);
		} else {
			if (!StringUtils.isNumeric(args[1])) {
				LOG.error("Argument 2 should be numeric");
				System.exit(1);
			}
		}
	}

	private static class DataSubscriber {

		@Subscribe
		@AllowConcurrentEvents
		public void handleHeartBeats(HeartBeatPayLoad<DateTime> payLoad) {
			LOG.info(String.format("Heartbeat received @ %s from source %s", payLoad.getHeartBeatPayLoad(), payLoad
					.getHeartBeatSource()));
		}

	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		usageAndValidation(args);
		final String url = args[0];
		final Long accountId = Long.parseLong(args[1]);
		final String accessToken = args[2];
		final String heartbeatSourceId = "DEMO_MKTDATASTREAM";

		TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");

		Collection<TradeableInstrument<String>> instruments = Lists.newArrayList(eurusd);

		EventBus eventBus = new EventBus();

		MarketEventCallback<String> mktEventCallback = new MarketEventHandlerImpl<String>(eventBus);
		HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);

		OandaMarketDataStreamingService mktDataStreaminService = new OandaMarketDataStreamingService(url, accessToken,
				accountId, instruments, mktEventCallback, heartBeatCallback, heartbeatSourceId);
		mktDataStreaminService.startMarketDataStreaming();
		Collection<HeartBeatStreamingService> heartbeatstreamingLst = Lists.newArrayList();
		heartbeatstreamingLst.add(mktDataStreaminService);
		DefaultHeartBeatService heartBeatService = new DefaultHeartBeatService(heartbeatstreamingLst);
		eventBus.register(heartBeatService);
		eventBus.register(new DataSubscriber());
		heartBeatService.init();

		heartBeatService.warmUpTime = 5000L;
		Thread.sleep(30000L);
		mktDataStreaminService.stopMarketDataStreaming();
		Thread.sleep(20000L);
	}

}
