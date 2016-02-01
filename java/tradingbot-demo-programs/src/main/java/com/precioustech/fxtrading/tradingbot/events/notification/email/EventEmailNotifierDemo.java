package com.precioustech.fxtrading.tradingbot.events.notification.email;

import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.events.EventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEvents;

public class EventEmailNotifierDemo {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		ApplicationContext appContext = 
				new ClassPathXmlApplicationContext("emailnotify-demo.xml");
		EventEmailNotifier<JSONObject> emailNotifier = 
				appContext.getBean(EventEmailNotifier.class);
		EventBus eventBus = new EventBus();
		eventBus.register(emailNotifier);

		Map<String, Object> payload = Maps.newHashMap();
		payload.put(OandaJsonKeys.instrument, "GBP_USD");
		payload.put(OandaJsonKeys.type, TradeEvents.TAKE_PROFIT_FILLED.name());
		payload.put(OandaJsonKeys.accountId, 123456l);
		payload.put(OandaJsonKeys.accountBalance, 127.8);
		payload.put(OandaJsonKeys.tradeId, 234567l);
		payload.put(OandaJsonKeys.pl, 11.8);
		payload.put(OandaJsonKeys.interest, 0.27);
		payload.put(OandaJsonKeys.units, 2700l);

		JSONObject jsonObj = new JSONObject(payload);
		eventBus.post(new EventPayLoad<JSONObject>(TradeEvents.TAKE_PROFIT_FILLED, jsonObj));
	}

}
