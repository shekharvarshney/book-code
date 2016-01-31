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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.social.twitter.api.SearchOperations;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.instrument.InstrumentService;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.tradingbot.TradingAppTestConstants;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.FXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;

@SuppressWarnings("unchecked")
public class SignalFactoryFXTweetHandlerTest {

	private static final String userId = "SignalFactory";
	private static final long latestTweetId = 100010L;

	private void newTradeType1(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn(
				"Forex Signal | Sell GBPCAD@1.90823 | SL:1.91223 | TP:1.90023 | 2015.02.06 19:33 GMT | #fx #forex #fb");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof NewFXTradeTweet);
		NewFXTradeTweet<String> newTrade = (NewFXTradeTweet<String>) tradeTweet;
		assertEquals(TradingSignal.SHORT, newTrade.getAction());
		assertEquals(new TradeableInstrument<String>("GBP_CAD"), newTrade.getInstrument());
		assertEquals(1.91223, newTrade.getStopLoss(), TradingAppTestConstants.precision);
		assertEquals(1.90023, newTrade.getTakeProfit(), TradingAppTestConstants.precision);
		assertEquals(1.90823, newTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void newTradeType2(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn(
				"Forex Signal | Buy USDCHF@0.92296 | SL:0.91896 | TP:0.93096 | 2015.02.06 16:31 GMT | #fx #forex #fb");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof NewFXTradeTweet);
		NewFXTradeTweet<String> newTrade = (NewFXTradeTweet<String>) tradeTweet;
		assertEquals(TradingSignal.LONG, newTrade.getAction());
		assertEquals(new TradeableInstrument<String>("USD_CHF"), newTrade.getInstrument());
		assertEquals(0.91896, newTrade.getStopLoss(), TradingAppTestConstants.precision);
		assertEquals(0.93096, newTrade.getTakeProfit(), TradingAppTestConstants.precision);
		assertEquals(0.92296, newTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void closeTradeType1(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText())
				.thenReturn(
						"Forex Signal | Close(TP) Buy EURNZD@1.54189 | Profit: +48 pips | 2015.02.06 14:06 GMT | #fx #forex #fb");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof CloseFXTradeTweet);
		CloseFXTradeTweet<String> closeTrade = (CloseFXTradeTweet<String>) tradeTweet;
		assertEquals(new TradeableInstrument<String>("EUR_NZD"), closeTrade.getInstrument());
		assertEquals(48.0, closeTrade.getProfit(), TradingAppTestConstants.precision);
		assertEquals(1.54189, closeTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void closeTradeType2(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText())
				.thenReturn(
						"Forex Signal | Close(SL) Sell GBPCHF@1.41640 | Loss: -40 pips | 2015.02.06 11:26 GMT | #fx #forex #fb");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNotNull(tradeTweet);
		assertTrue(tradeTweet instanceof CloseFXTradeTweet);
		CloseFXTradeTweet<String> closeTrade = (CloseFXTradeTweet<String>) tradeTweet;
		assertEquals(new TradeableInstrument<String>("GBP_CHF"), closeTrade.getInstrument());
		assertEquals(-40.0, closeTrade.getProfit(), TradingAppTestConstants.precision);
		assertEquals(1.4164, closeTrade.getPrice(), TradingAppTestConstants.precision);
	}

	private void nonTradeType(AbstractFXTweetHandler<String> tweetHandler) {
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn("Pick your favorite provider and start making profits today!!");
		FXTradeTweet<String> tradeTweet = tweetHandler.handleTweet(tweet);
		assertNull(tradeTweet);
	}

	@Test
	public void handleTweetTest() {
		AbstractFXTweetHandler<String> tweetHandler = new SignalFactoryFXTweetHandler(userId);
		ProviderHelper providerHelper = mock(ProviderHelper.class);
		tweetHandler.providerHelper = providerHelper;
		when(providerHelper.fromIsoFormat(eq("GBPCAD"))).thenReturn("GBP_CAD");
		when(providerHelper.fromIsoFormat(eq("USDCHF"))).thenReturn("USD_CHF");
		when(providerHelper.fromIsoFormat(eq("EURNZD"))).thenReturn("EUR_NZD");
		when(providerHelper.fromIsoFormat(eq("GBPCHF"))).thenReturn("GBP_CHF");

		newTradeType1(tweetHandler);
		newTradeType2(tweetHandler);
		closeTradeType1(tweetHandler);
		closeTradeType2(tweetHandler);
		nonTradeType(tweetHandler);

	}

	@Test
	public void findHistoricPnlTweetsForInstrumentTest() {
		AbstractFXTweetHandler<String> tweetHandler = new SignalFactoryFXTweetHandler(userId);
		ProviderHelper providerHelper = mock(ProviderHelper.class);
		Twitter twitter = mock(Twitter.class);
		tweetHandler.providerHelper = providerHelper;
		tweetHandler.twitter = twitter;

		SearchOperations searchOperations = mock(SearchOperations.class);
		when(twitter.searchOperations()).thenReturn(searchOperations);

		TradeableInstrument<String> nzdusd = new TradeableInstrument<String>("NZD_USD");
		when(providerHelper.toIsoFormat(eq("NZD_USD"))).thenReturn("NZDUSD");
		SearchResults searchResults = returnHistoricPnlSearchResults();
		String query = "Profit: OR Loss: from:SignalFactory";
		when(searchOperations.search(eq(query))).thenReturn(searchResults);
		Collection<Tweet> tweets = tweetHandler.findHistoricPnlTweetsForInstrument(nzdusd);
		assertEquals(1, tweets.size());
	}

	@Test
	public void findNewTweetsTest() {
		AbstractFXTweetHandler<String> tweetHandler = new SignalFactoryFXTweetHandler(userId);
		Twitter twitter = mock(Twitter.class);
		InstrumentService<String> instrumentService = mock(InstrumentService.class);
		tweetHandler.instrumentService = instrumentService;
		tweetHandler.twitter = twitter;
		assertEquals(userId, tweetHandler.getUserId());
		SearchOperations searchOperations = mock(SearchOperations.class);
		when(twitter.searchOperations()).thenReturn(searchOperations);

		SearchResults searchResults = returnInitialSearchResults();
		String query = String.format(AbstractFXTweetHandler.FROM_USER_SINCE_TIME_TWITTER_QRY_TMPL, userId,
				tweetHandler.startTimeAsStr);
		when(searchOperations.search(eq(query))).thenReturn(searchResults);
		Collection<Tweet> tweets = tweetHandler.findNewTweets();
		assertEquals(1, tweets.size());
		assertEquals(latestTweetId, tweets.iterator().next().getId());

		searchResults = returnSubsequentSearchResults();
		query = String.format(AbstractFXTweetHandler.FROM_USER_SINCE_ID_TWITTER_QRY_TMPL, userId, latestTweetId);
		when(searchOperations.search(eq(query))).thenReturn(searchResults);
		tweets = tweetHandler.findNewTweets();
		assertEquals(3, tweets.size());
		assertEquals(latestTweetId + 3, tweetHandler.lastTweetId.longValue());
	}

	private SearchResults returnHistoricPnlSearchResults() {
		SearchResults searchResults = mock(SearchResults.class);
		List<Tweet> tweets = Lists.newArrayList();
		when(searchResults.getTweets()).thenReturn(tweets);
		Tweet tweet = mock(Tweet.class);
		when(tweet.getText()).thenReturn(
				"Forex Signal | Close(SL) Sell NZDUSD@0.7744 | Loss: -40 pips | 2015.02.06 11:26 GMT | #fx #forex #fb");
		tweets.add(tweet);
		return searchResults;
	}

	private SearchResults returnSubsequentSearchResults() {
		SearchResults searchResults = mock(SearchResults.class);
		List<Tweet> tweets = Lists.newArrayList();
		when(searchResults.getTweets()).thenReturn(tweets);
		for (int i = 3; i > 0; i--) {
			Tweet tweet = mock(Tweet.class);
			when(tweet.getId()).thenReturn(latestTweetId + i);
			tweets.add(tweet);
		}
		return searchResults;
	}

	private SearchResults returnInitialSearchResults() {
		SearchResults searchResults = mock(SearchResults.class);
		List<Tweet> tweets = Lists.newArrayList();
		DateTime now = DateTime.now();
		when(searchResults.getTweets()).thenReturn(tweets);

		Tweet tweet1 = mock(Tweet.class);
		when(tweet1.getCreatedAt()).thenReturn(now.plusMinutes(1).toDate());
		when(tweet1.getId()).thenReturn(latestTweetId);
		tweets.add(tweet1);

		Tweet tweet2 = mock(Tweet.class);
		when(tweet2.getCreatedAt()).thenReturn(now.minusMinutes(1).toDate());
		when(tweet2.getId()).thenReturn(100001L);
		tweets.add(tweet2);
		return searchResults;
	}

}
