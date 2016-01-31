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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.social.twitter.api.SearchOperations;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.tradingbot.TradingAppTestConstants;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.FXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;

public class ZuluTrader101FXTweetHandlerTest {

	private static final String userId = "ZuluTrader101";

	@Test
	public void handleTweetTest() {
		AbstractFXTweetHandler<String> tweetHandler = new ZuluTrader101FXTweetHandler(userId);
		ProviderHelper providerHelper = mock(ProviderHelper.class);
		tweetHandler.providerHelper = providerHelper;
		when(providerHelper.fromHashTagCurrency(eq("#EURUSD"))).thenReturn("EUR_USD");
		when(providerHelper.fromHashTagCurrency(eq("#USDJPY"))).thenReturn("USD_JPY");
		when(providerHelper.fromHashTagCurrency(eq("#GBPCHF"))).thenReturn("GBP_CHF");
		when(providerHelper.fromHashTagCurrency(eq("#EURGBP"))).thenReturn("EUR_GBP");
		when(providerHelper.fromHashTagCurrency(eq("#EURCHF"))).thenReturn("EUR_CHF");
		when(providerHelper.fromHashTagCurrency(eq("#NZDUSD"))).thenReturn("NZD_USD");
		newTradeType1(tweetHandler);
		newTradeType2(tweetHandler);
		newTradeType3(tweetHandler);
		newTradeType4(tweetHandler);
		newTradeType5(tweetHandler);
		newTradeType6(tweetHandler);
		closeTradeType1(tweetHandler);
		closeTradeType2(tweetHandler);
	}

	@Test
	public void findHistoricPnlTweetsForInstrumentTest() {
		AbstractFXTweetHandler<String> tweetHandler = new ZuluTrader101FXTweetHandler(userId);
		ProviderHelper providerHelper = mock(ProviderHelper.class);
		Twitter twitter = mock(Twitter.class);
		tweetHandler.providerHelper = providerHelper;
		tweetHandler.twitter = twitter;

		SearchOperations searchOperations = mock(SearchOperations.class);
		when(twitter.searchOperations()).thenReturn(searchOperations);

		TradeableInstrument<String> audusd = new TradeableInstrument<String>("AUD_USD");
		when(providerHelper.toIsoFormat(eq("AUD_USD"))).thenReturn("AUDUSD");
		SearchResults searchResults = returnHistoricPnlSearchResults();
		String query = "from:ZuluTrader101 \"Closed Buy\" OR \"Closed Sell\" #AUDUSD";
		when(searchOperations.search(eq(query))).thenReturn(searchResults);
		Collection<Tweet> tweets = tweetHandler.findHistoricPnlTweetsForInstrument(audusd);
		assertEquals(1, tweets.size());
	}

	private SearchResults returnHistoricPnlSearchResults() {
		SearchResults searchResults = mock(SearchResults.class);
		List<Tweet> tweets = Lists.newArrayList();
		when(searchResults.getTweets()).thenReturn(tweets);
		tweets.add(mock(Tweet.class));
		return searchResults;
	}

	private void closeTradeType1(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn(
				"Closed Buy 0.64 Lots #USDJPY 118.773 for +17.7 pips, total for today -136.7 pips");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof CloseFXTradeTweet);
		CloseFXTradeTweet<String> closeTrade = (CloseFXTradeTweet<String>) tradeTweet;

		assertEquals(new TradeableInstrument<String>("USD_JPY"), closeTrade.getInstrument());
		assertEquals(17.7, closeTrade.getProfit(), TradingAppTestConstants.precision);
		assertEquals(118.773, closeTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void closeTradeType2(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn(
				"Closed Sell 0.69 Lots #NZDUSD 0.75273 for -8.4 pips, total for today -1072.8 pips");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof CloseFXTradeTweet);
		CloseFXTradeTweet<String> closeTrade = (CloseFXTradeTweet<String>) tradeTweet;

		assertEquals(new TradeableInstrument<String>("NZD_USD"), closeTrade.getInstrument());
		assertEquals(-8.4, closeTrade.getProfit(), TradingAppTestConstants.precision);
		assertEquals(0.75273, closeTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void newTradeType1(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn(
				"Bought 0.43 Lots #EURUSD 1.1371 | Auto-copy FREE at http://goo.gl/moaYzx #Forex #Finance #Money ");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof NewFXTradeTweet);
		NewFXTradeTweet<String> newTrade = (NewFXTradeTweet<String>) tradeTweet;
		assertEquals(TradingSignal.LONG, newTrade.getAction());
		assertEquals(new TradeableInstrument<String>("EUR_USD"), newTrade.getInstrument());
		assertEquals(0.0, newTrade.getStopLoss(), TradingAppTestConstants.precision);
		assertEquals(0.0, newTrade.getTakeProfit(), TradingAppTestConstants.precision);
		assertEquals(1.1371, newTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void newTradeType2(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn(
				"Sold 0.34 Lots #EURUSD 1.13694 | Auto-copy FREE at http://goo.gl/moaYzx  #Forex #Finance #Money ");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof NewFXTradeTweet);
		NewFXTradeTweet<String> newTrade = (NewFXTradeTweet<String>) tradeTweet;
		assertEquals(TradingSignal.SHORT, newTrade.getAction());
		assertEquals(new TradeableInstrument<String>("EUR_USD"), newTrade.getInstrument());
		assertEquals(0.0, newTrade.getStopLoss(), TradingAppTestConstants.precision);
		assertEquals(0.0, newTrade.getTakeProfit(), TradingAppTestConstants.precision);
		assertEquals(1.13694, newTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void newTradeType3(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn(
				"Sold 3.69 Lots #USDJPY 118.897 | Auto-copy FREE at http://goo.gl/moaYzx  #Forex #Finance #Money");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof NewFXTradeTweet);
		NewFXTradeTweet<String> newTrade = (NewFXTradeTweet<String>) tradeTweet;
		assertEquals(TradingSignal.SHORT, newTrade.getAction());
		assertEquals(new TradeableInstrument<String>("USD_JPY"), newTrade.getInstrument());
		assertEquals(0.0, newTrade.getStopLoss(), TradingAppTestConstants.precision);
		assertEquals(0.0, newTrade.getTakeProfit(), TradingAppTestConstants.precision);
		assertEquals(118.897, newTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void newTradeType4(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText())
				.thenReturn(
						"Bought 0.69 Lots #GBPCHF 1.4614 SL 1.45817 TP 1.46617 | Auto-copy FREE at http://goo.gl/moaYzx #Forex #Finance #Money");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof NewFXTradeTweet);
		NewFXTradeTweet<String> newTrade = (NewFXTradeTweet<String>) tradeTweet;
		assertEquals(TradingSignal.LONG, newTrade.getAction());
		assertEquals(new TradeableInstrument<String>("GBP_CHF"), newTrade.getInstrument());
		assertEquals(1.45817, newTrade.getStopLoss(), TradingAppTestConstants.precision);
		assertEquals(1.46617, newTrade.getTakeProfit(), TradingAppTestConstants.precision);
		assertEquals(1.4614, newTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void newTradeType5(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText())
				.thenReturn(
						"Bought 0.66 Lots #EURGBP 0.7360 SL 0.70534 | Auto-copy FREE at http://goo.gl/moaYzx  #Forex #Finance #Money");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof NewFXTradeTweet);
		NewFXTradeTweet<String> newTrade = (NewFXTradeTweet<String>) tradeTweet;
		assertEquals(TradingSignal.LONG, newTrade.getAction());
		assertEquals(new TradeableInstrument<String>("EUR_GBP"), newTrade.getInstrument());
		assertEquals(0.70534, newTrade.getStopLoss(), TradingAppTestConstants.precision);
		assertEquals(0.0, newTrade.getTakeProfit(), TradingAppTestConstants.precision);
		assertEquals(0.7360, newTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void newTradeType6(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText())
				.thenReturn(
						"Bought 0.66 Lots #EURCHF 1.07 TP 1.0725 | Auto-copy FREE at http://goo.gl/moaYzx  #Forex #Finance #Money");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof NewFXTradeTweet);
		NewFXTradeTweet<String> newTrade = (NewFXTradeTweet<String>) tradeTweet;
		assertEquals(TradingSignal.LONG, newTrade.getAction());
		assertEquals(new TradeableInstrument<String>("EUR_CHF"), newTrade.getInstrument());
		assertEquals(0.0, newTrade.getStopLoss(), TradingAppTestConstants.precision);
		assertEquals(1.0725, newTrade.getTakeProfit(), TradingAppTestConstants.precision);
		assertEquals(1.07, newTrade.getPrice(), TradingAppTestConstants.precision);
	}

}
