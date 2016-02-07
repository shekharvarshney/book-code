package com.precioustech.fxtrading.tradingbot.social.twitter.tweethandler;

import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.oanda.restapi.OandaJsonKeys;
import com.precioustech.fxtrading.oanda.restapi.events.OrderEventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.events.OrderEvents;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEventPayLoad;
import com.precioustech.fxtrading.oanda.restapi.events.TradeEvents;

public class TweetFXTradeServiceDemo {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		final Long accountId = Long.parseLong(System.getProperty("oanda.accountId"));
		ApplicationContext appContext = new ClassPathXmlApplicationContext("tweetfxtrade-demo.xml");

		TweetFXTradeService<JSONObject, TradeEventPayLoad> tweetFXTradeService = appContext
				.getBean(TweetFXTradeService.class);

		EventBus eventBus = new EventBus();
		eventBus.register(tweetFXTradeService);

		Map<String, Object> payload = Maps.newHashMap();
		payload.put(OandaJsonKeys.instrument, "EUR_NZD");
		payload.put(OandaJsonKeys.type, TradeEvents.TAKE_PROFIT_FILLED.name());
		payload.put(OandaJsonKeys.accountId, accountId);
		payload.put(OandaJsonKeys.accountBalance, 11246.4015);
		payload.put(OandaJsonKeys.tradeId, 10102198125L);
		payload.put(OandaJsonKeys.pl, 0.1919);
		payload.put(OandaJsonKeys.interest, 0.001);
		payload.put(OandaJsonKeys.units, 200l);
		payload.put(OandaJsonKeys.id, 10102206906L);
		payload.put(OandaJsonKeys.price, 1.67282);
		payload.put(OandaJsonKeys.side, "buy");
		TradeEventPayLoad tradePayLoad = new TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED, new JSONObject(payload));
		eventBus.post(tradePayLoad);

		payload = Maps.newHashMap();
		payload.put(OandaJsonKeys.instrument, "EUR_CHF");
		payload.put(OandaJsonKeys.type, TradeEvents.TRADE_CLOSE.name());
		payload.put(OandaJsonKeys.accountId, accountId);
		payload.put(OandaJsonKeys.accountBalance, 11247.6644);
		payload.put(OandaJsonKeys.tradeId, 10102566665L);
		payload.put(OandaJsonKeys.pl, 0.2538);
		payload.put(OandaJsonKeys.interest, 0.001);
		payload.put(OandaJsonKeys.units, 200l);
		payload.put(OandaJsonKeys.id, 10102707605L);
		payload.put(OandaJsonKeys.price, 1.10642);
		payload.put(OandaJsonKeys.side, "sell");
		tradePayLoad = new TradeEventPayLoad(TradeEvents.TRADE_CLOSE, new JSONObject(payload));
		eventBus.post(tradePayLoad);

		payload = Maps.newHashMap();
		payload.put(OandaJsonKeys.instrument, "USD_CHF");
		payload.put(OandaJsonKeys.type, OrderEvents.LIMIT_ORDER_CREATE.name());
		payload.put(OandaJsonKeys.accountId, accountId);
		payload.put(OandaJsonKeys.units, 200l);
		payload.put(OandaJsonKeys.id, 10102576739L);
		payload.put(OandaJsonKeys.price, 0.99198);
		payload.put(OandaJsonKeys.side, "buy");
		OrderEventPayLoad orderPayLoad = new OrderEventPayLoad(OrderEvents.LIMIT_ORDER_CREATE, new JSONObject(payload));
		eventBus.post(orderPayLoad);

		payload = Maps.newHashMap();
		payload.put(OandaJsonKeys.instrument, "USD_CAD");
		payload.put(OandaJsonKeys.type, OrderEvents.ORDER_FILLED.name());
		payload.put(OandaJsonKeys.accountId, accountId);
		payload.put(OandaJsonKeys.accountBalance, 11246.4015);
		payload.put(OandaJsonKeys.orderId, 10102576858L);
		payload.put(OandaJsonKeys.pl, 0.0);
		payload.put(OandaJsonKeys.interest, 0.0);
		payload.put(OandaJsonKeys.units, 200l);
		payload.put(OandaJsonKeys.id, 10102576859L);
		payload.put(OandaJsonKeys.price, 1.38789);
		payload.put(OandaJsonKeys.side, "sell");
		orderPayLoad = new OrderEventPayLoad(OrderEvents.ORDER_FILLED, new JSONObject(payload));
		eventBus.post(orderPayLoad);
	}

}
