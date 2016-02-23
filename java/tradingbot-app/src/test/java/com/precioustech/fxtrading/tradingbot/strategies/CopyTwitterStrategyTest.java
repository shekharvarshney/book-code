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
package com.precioustech.fxtrading.tradingbot.strategies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.precioustech.fxtrading.TradingDecision;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;
import com.precioustech.fxtrading.tradingbot.TradingAppTestConstants;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.tweethandler.FXTweetHandler;

@SuppressWarnings("unchecked")
public class CopyTwitterStrategyTest {

	private static final double ZERO = 0.0;
	private String userIdfoo = "foo";
	private String userIdbar = "bar";

	@Test
	public void harvestNewTradeTest() {

		CopyTwitterStrategy<String> copyTwitterStrategy = new CopyTwitterStrategy<String>();
		FXTweetHandler<String> tweetHandlerFoo = createTweetHandlerFoo(copyTwitterStrategy);

		Collection<NewFXTradeTweet<String>> newTradeTweets = copyTwitterStrategy.harvestNewTradeTweets(userIdbar);
		assertTrue(newTradeTweets.isEmpty());

		Tweet tweet1 = mock(Tweet.class);
		Tweet tweet2 = mock(Tweet.class);
		Collection<Tweet> footweets = Lists.newArrayList(tweet1, tweet2);
		when(tweetHandlerFoo.findNewTweets()).thenReturn(footweets);
		when(tweetHandlerFoo.handleTweet(tweet1)).thenReturn(mock(CloseFXTradeTweet.class));
		when(tweetHandlerFoo.handleTweet(tweet2)).thenReturn(mock(NewFXTradeTweet.class));

		newTradeTweets = copyTwitterStrategy.harvestNewTradeTweets(userIdfoo);
		assertEquals(1, newTradeTweets.size());
	}

	private FXTweetHandler<String> createTweetHandlerFoo(CopyTwitterStrategy<String> copyTwitterStrategy) {
		Map<String, FXTweetHandler<String>> tweetHandlerMap = Maps.newHashMap();
		FXTweetHandler<String> tweetHandlerFoo = mock(FXTweetHandler.class);
		tweetHandlerMap.put(userIdfoo, tweetHandlerFoo);
		copyTwitterStrategy.tweetHandlerMap = tweetHandlerMap;
		return tweetHandlerFoo;
	}

	@Test
	public void harvestAndTradeTest() throws InterruptedException {
		CopyTwitterStrategy<String> copyTwitterStrategy = new CopyTwitterStrategy<String>();
		FXTweetHandler<String> tweetHandlerFoo = createTweetHandlerFoo(copyTwitterStrategy);
		BlockingQueue<TradingDecision<String>> orderQueue = new LinkedBlockingQueue<TradingDecision<String>>();
		copyTwitterStrategy.orderQueue = orderQueue;
		copyTwitterStrategy.init();
		Tweet tweet1 = mock(Tweet.class);
		Collection<Tweet> footweets = Lists.newArrayList(tweet1);
		NewFXTradeTweet<String> newTrade = mock(NewFXTradeTweet.class);
		when(tweetHandlerFoo.findNewTweets()).thenReturn(footweets);
		when(tweetHandlerFoo.handleTweet(tweet1)).thenReturn(newTrade);
		when(newTrade.getAction()).thenReturn(TradingSignal.SHORT);
		TradeableInstrument<String> euraud = new TradeableInstrument<String>("EUR_AUD");
		when(newTrade.getInstrument()).thenReturn(euraud);
		double[] profits = { 11.32, 17.7, 8.2, 19.0, 44.5, -11.0, 10, 25.5 };
		Collection<CloseFXTradeTweet<String>> closedTrades = createClosedTrades(profits);
		footweets = Lists.newArrayList();
		for (CloseFXTradeTweet<String> closeTradeTweet : closedTrades) {
			Tweet tweet = mock(Tweet.class);
			when(tweetHandlerFoo.handleTweet(tweet)).thenReturn(closeTradeTweet);
			footweets.add(tweet);
		}
		when(tweetHandlerFoo.findHistoricPnlTweetsForInstrument(euraud)).thenReturn(footweets);
		copyTwitterStrategy.harvestAndTrade();
		TradingDecision<String> decision = orderQueue.take();
		assertEquals(TradingSignal.SHORT, decision.getSignal());
		assertEquals(euraud, decision.getInstrument());
	}

	@Test
	public void harvestHistoricTradeTweetsTest() {
		CopyTwitterStrategy<String> copyTwitterStrategy = new CopyTwitterStrategy<String>();
		FXTweetHandler<String> tweetHandlerFoo = createTweetHandlerFoo(copyTwitterStrategy);
		TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");
		Collection<CloseFXTradeTweet<String>> closeTradeTweets = copyTwitterStrategy.harvestHistoricTradeTweets(
				userIdbar, gbpusd);
		assertTrue(closeTradeTweets.isEmpty());
		Tweet tweet1 = mock(Tweet.class);
		Tweet tweet2 = mock(Tweet.class);
		Collection<Tweet> footweets = Lists.newArrayList(tweet1, tweet2);
		when(tweetHandlerFoo.findHistoricPnlTweetsForInstrument(gbpusd)).thenReturn(footweets);
		when(tweetHandlerFoo.handleTweet(tweet1)).thenReturn(mock(CloseFXTradeTweet.class));
		when(tweetHandlerFoo.handleTweet(tweet2)).thenReturn(mock(CloseFXTradeTweet.class));
		closeTradeTweets = copyTwitterStrategy.harvestHistoricTradeTweets(userIdfoo, gbpusd);
		assertEquals(2, closeTradeTweets.size());
	}

	@Test
	public void analyseHistoricTweetsTest() {
		CopyTwitterStrategy<String> copyTwitterStrategy = new CopyTwitterStrategy<String>();

		double[] profits = { 13.45, 11.54, -8.9, 12.09, 21.67, 2.76, -3.5, 11.67, 9.21 };
		Collection<CloseFXTradeTweet<String>> closedTrades = createClosedTrades(profits);
		double takeProfit = 0.9812;
		double stopLoss = 0.949;
		double price = 0.9735;
		NewFXTradeTweet<String> newTrade = mock(NewFXTradeTweet.class);
		TradeableInstrument<String> usdchf = new TradeableInstrument<String>("USD_CHF");
		when(newTrade.getAction()).thenReturn(TradingSignal.LONG);
		when(newTrade.getInstrument()).thenReturn(usdchf);
		when(newTrade.getTakeProfit()).thenReturn(takeProfit);
		when(newTrade.getStopLoss()).thenReturn(stopLoss);
		when(newTrade.getPrice()).thenReturn(price);
		TradingDecision<String> tradingDecision = copyTwitterStrategy.analyseHistoricClosedTradesForInstrument(
				closedTrades, newTrade);
		assertEquals(TradingSignal.LONG, tradingDecision.getSignal());
		assertEquals(TradingDecision.SRCDECISION.SOCIAL_MEDIA, tradingDecision.getTradeSource());
		assertEquals(usdchf, tradingDecision.getInstrument());
		assertEquals(takeProfit, tradingDecision.getTakeProfitPrice(), ZERO);
		assertEquals(stopLoss, tradingDecision.getStopLossPrice(), ZERO);
		assertEquals(price, tradingDecision.getLimitPrice(), ZERO);

		double profits2[] = { -23.9, -10, -56.0, -12.9, -11.5, 8.5 };
		closedTrades = createClosedTrades(profits2);
		tradingDecision = copyTwitterStrategy.analyseHistoricClosedTradesForInstrument(closedTrades, newTrade);
		assertEquals(TradingSignal.SHORT, tradingDecision.getSignal());
		assertEquals(TradingDecision.SRCDECISION.SOCIAL_MEDIA, tradingDecision.getTradeSource());
		assertEquals(usdchf, tradingDecision.getInstrument());
		assertEquals(0.9658, tradingDecision.getTakeProfitPrice(), TradingAppTestConstants.precision);
		assertEquals(0.998, tradingDecision.getStopLossPrice(), TradingAppTestConstants.precision);
		assertEquals(price, tradingDecision.getLimitPrice(), ZERO);

		stopLoss = 1.1125;
		takeProfit = 1.0705;
		price = 1.0895;
		TradeableInstrument<String> eurchf = new TradeableInstrument<String>("EUR_CHF");
		NewFXTradeTweet<String> newTrade2 = mock(NewFXTradeTweet.class);
		when(newTrade2.getAction()).thenReturn(TradingSignal.SHORT);
		when(newTrade2.getInstrument()).thenReturn(eurchf);
		when(newTrade2.getTakeProfit()).thenReturn(takeProfit);
		when(newTrade2.getStopLoss()).thenReturn(stopLoss);
		when(newTrade2.getPrice()).thenReturn(price);

		tradingDecision = copyTwitterStrategy.analyseHistoricClosedTradesForInstrument(closedTrades, newTrade2);
		assertEquals(TradingSignal.LONG, tradingDecision.getSignal());
		assertEquals(TradingDecision.SRCDECISION.SOCIAL_MEDIA, tradingDecision.getTradeSource());
		assertEquals(eurchf, tradingDecision.getInstrument());
		assertEquals(1.1085, tradingDecision.getTakeProfitPrice(), TradingAppTestConstants.precision);
		assertEquals(1.0665, tradingDecision.getStopLossPrice(), TradingAppTestConstants.precision);
		assertEquals(price, tradingDecision.getLimitPrice(), ZERO);

		stopLoss = 15.0545;
		takeProfit = 15.0985;
		price = 0.0;
		TradeableInstrument<String> usdzar = new TradeableInstrument<>("USD_ZAR");
		NewFXTradeTweet<String> newTrade3 = mock(NewFXTradeTweet.class);
		when(newTrade3.getAction()).thenReturn(TradingSignal.LONG);
		when(newTrade3.getInstrument()).thenReturn(usdzar);
		when(newTrade3.getTakeProfit()).thenReturn(takeProfit);
		when(newTrade3.getStopLoss()).thenReturn(stopLoss);
		when(newTrade3.getPrice()).thenReturn(price);
		closedTrades = createClosedTrades(profits2);
		CurrentPriceInfoProvider<String> currPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
		copyTwitterStrategy.currentPriceInfoProvider = currPriceInfoProvider;
		Map<TradeableInstrument<String>, Price<String>> priceMap = Maps.newHashMap();
		Price<String> usdzarPrice = new Price<>(usdzar, 15.0715, 15.0727, DateTime.now());
		priceMap.put(usdzar, usdzarPrice);
		when(currPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(usdzar)))).thenReturn(priceMap);
		tradingDecision = copyTwitterStrategy.analyseHistoricClosedTradesForInstrument(closedTrades, newTrade3);
		assertEquals(TradingSignal.SHORT, tradingDecision.getSignal());
		assertEquals(TradingDecision.SRCDECISION.SOCIAL_MEDIA, tradingDecision.getTradeSource());
		assertEquals(usdzar, tradingDecision.getInstrument());
		assertEquals(15.0445, tradingDecision.getTakeProfitPrice(), TradingAppTestConstants.precision);
		assertEquals(15.0885, tradingDecision.getStopLossPrice(), TradingAppTestConstants.precision);
		assertEquals(15.0715, tradingDecision.getLimitPrice(), ZERO);
	}

	private Collection<CloseFXTradeTweet<String>> createClosedTrades(double[] profits) {
		Collection<CloseFXTradeTweet<String>> closedTrades = Lists.newArrayList();
		for (double profit : profits) {
			CloseFXTradeTweet<String> closeTrade = mock(CloseFXTradeTweet.class);
			when(closeTrade.getProfit()).thenReturn(profit);
			closedTrades.add(closeTrade);
		}
		return closedTrades;
	}
}
