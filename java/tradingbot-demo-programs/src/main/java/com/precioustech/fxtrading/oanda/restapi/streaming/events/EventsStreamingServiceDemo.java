package com.precioustech.fxtrading.oanda.restapi.streaming.events;

import static com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys.type;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.precioustech.fxtrading.account.AccountDataProvider;
import com.precioustech.fxtrading.events.EventCallback;
import com.precioustech.fxtrading.events.EventCallbackImpl;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.heartbeats.HeartBeatCallback;
import com.precioustech.fxtrading.heartbeats.HeartBeatCallbackImpl;
import com.precioustech.fxtrading.oanda.restapi.account.OandaAccountDataProviderService;
import com.precioustech.fxtrading.streaming.events.EventsStreamingService;

public class EventsStreamingServiceDemo {

	private static final Logger LOG = Logger.getLogger(EventsStreamingServiceDemo.class);

	private static void usage(String[] args) {
		if (args.length != 4) {
			LOG.error("Usage: EventsStreamingServiceDemo <url> <url2> <username> <accesstoken>");
			System.exit(1);
		}
	}

	private static class EventSubscriber {

		@Subscribe
		@AllowConcurrentEvents
		public void handleEvent(EventPayLoad<JSONObject> payLoad) {
			String transactionType = payLoad.getPayLoad().get(type).toString();
			LOG.info(String.format("Type:%s, payload=%s", transactionType, payLoad.getPayLoad()));
		}
	}

	public static void main(String[] args) throws Exception {
		usage(args);
		String url = args[0];
		String url2 = args[1];
		String userName = args[2];
		String accessToken = args[3];
		final String heartBeatSourceId = "DEMO_EVTDATASTREAM";

		EventBus eventBus = new EventBus();
		eventBus.register(new EventSubscriber());
		HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);
		AccountDataProvider<Long> accountDataProvider = new OandaAccountDataProviderService(url2, userName, accessToken);
		EventCallback<JSONObject> eventCallback = new EventCallbackImpl<JSONObject>(eventBus);

		EventsStreamingService evtStreamingService = new OandaEventsStreamingService(url, accessToken,
				accountDataProvider, eventCallback, heartBeatCallback, heartBeatSourceId);
		evtStreamingService.startEventsStreaming();
		// Run OrderExecutionServiceDemo in the next 60s
		Thread.sleep(60000L);
		evtStreamingService.stopEventsStreaming();
	}
}
