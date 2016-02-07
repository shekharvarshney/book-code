package com.precioustech.fxtrading.tradingbot.strategies;

import java.util.concurrent.BlockingQueue;

import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.TradingDecision;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.MarketDataPayLoad;

public class FadeTheMoveStrategyDemo {

	private static double precision = 0.0001;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException {

		ApplicationContext appContext = new ClassPathXmlApplicationContext("fadethemove-demo.xml");
		FadeTheMoveStrategy<String> fadeTheMoveStrategy = appContext.getBean(FadeTheMoveStrategy.class);
		BlockingQueue<TradingDecision<String>> orderQueue = appContext.getBean(BlockingQueue.class);
		EventBus eventBus = new EventBus();
		eventBus.register(fadeTheMoveStrategy);

		TradeableInstrument<String> audchf = new TradeableInstrument<String>("AUD_CHF");
		TradeableInstrument<String> audjpy = new TradeableInstrument<String>("AUD_JPY");
		final double[] audchfPrices = { 0.7069, 0.7070, 0.7073, 0.7076, 0.7077, 0.7078, 0.708, 0.7082, 0.7084, 0.7085,
				0.7086, 0.7089, 0.7091, 0.7093, 0.7094, 0.7098, 0.71, 0.7102, 0.7105, 0.7104, 0.7103, 0.7105, 0.7109,
				0.7111, 0.7112, 0.7115, 0.7118 };
		final double[] audjpyPrices = { 84.233, 84.230, 84.225, 84.121, 84.123, 84.120, 84.117, 84.115, 84.10, 84.095,
				84.097, 84.089, 84.078, 84.071, 84.70, 84.069, 84.063, 84.012, 83.942, 83.927, 83.896, 83.878, 83.846,
				83.810, 83.798, 83.771, 83.743 };
		DateTime eventStartaudchf = DateTime.now().minusMinutes(10);
		DateTime eventStartaudjpy = DateTime.now().minusMinutes(10);
		for (int ctr = 0; ctr < Math.min(audchfPrices.length, audjpyPrices.length); ctr++) {
			eventStartaudchf = eventStartaudchf.plusSeconds(5);
			eventStartaudjpy = eventStartaudjpy.plusSeconds(5);
			eventBus.post(new MarketDataPayLoad<String>(audchf, audchfPrices[ctr] - precision, audchfPrices[ctr]
					+ precision, eventStartaudchf));
			eventBus.post(new MarketDataPayLoad<String>(audjpy, audjpyPrices[ctr] - precision * 100, audjpyPrices[ctr]
					+ precision * 100, eventStartaudjpy));
		}
		fadeTheMoveStrategy.analysePrices();
		for (int i = 1; i <= 2; i++) {
			TradingDecision<String> decision = orderQueue.take();
			System.out.println(String.format(
					"The strategy signalled to go %s on instrument %s at limit price %2.5f and take profit %2.5f ",
					decision.getSignal().name(), decision.getInstrument().getInstrument(), decision.getLimitPrice(),
					decision.getTakeProfitPrice()));
		}
	}
}
