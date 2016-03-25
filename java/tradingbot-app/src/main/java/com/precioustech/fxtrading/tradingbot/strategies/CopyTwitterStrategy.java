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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Tweet;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingDecision;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.CurrentPriceInfoProvider;
import com.precioustech.fxtrading.marketdata.Price;
import com.precioustech.fxtrading.trade.strategies.TradingStrategy;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.FXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.tweethandler.FXTweetHandler;
import com.precioustech.fxtrading.tradingbot.social.twitter.tweethandler.TweetHarvester;

@TradingStrategy
public class CopyTwitterStrategy<T> implements TweetHarvester<T> {

	@Resource
	Map<String, FXTweetHandler<T>> tweetHandlerMap;
	@Resource(name = "orderQueue")
	BlockingQueue<TradingDecision<T>> orderQueue;
	@Autowired
	CurrentPriceInfoProvider<T> currentPriceInfoProvider;
	private static final Logger LOG = Logger.getLogger(CopyTwitterStrategy.class);
	private ExecutorService executorService = null;
	private static final double ACCURACY_DESIRED = 0.75;
	private static final int MIN_HISTORIC_TWEETS = 4;

	@PostConstruct
	public void init() {
		this.executorService = Executors.newFixedThreadPool(1);
	}

	@SuppressWarnings("unchecked")
	TradingDecision<T> analyseHistoricClosedTradesForInstrument(Collection<CloseFXTradeTweet<T>> closedTrades,
			NewFXTradeTweet<T> newTrade) {
		int lossCtr = 0;
		int profitCtr = 0;
		for (CloseFXTradeTweet<T> closedTrade : closedTrades) {
			if (closedTrade.getProfit() <= 0) {
				lossCtr++;
			} else {
				profitCtr++;
			}
		}
		TradingSignal signal = TradingSignal.NONE;
		if ((lossCtr != 0 || profitCtr != 0) && closedTrades.size() >= MIN_HISTORIC_TWEETS) {
			double profitAccuracy = profitCtr / ((profitCtr + lossCtr) * 1.0);
			double lossAccuracy = 1.0 - profitAccuracy;
			if (profitAccuracy >= ACCURACY_DESIRED) {
				signal = newTrade.getAction();
				return new TradingDecision<T>(newTrade.getInstrument(), signal, newTrade.getTakeProfit(),
						newTrade.getStopLoss(), newTrade.getPrice(), TradingDecision.SRCDECISION.SOCIAL_MEDIA);
			} else if (lossAccuracy >= ACCURACY_DESIRED) {
				// execute an opposite trade as the loss accuracy is quite high
				signal = newTrade.getAction().flip();
				double price = newTrade.getPrice();
				if (price == 0.0) {// price not provided, get current price
					Map<TradeableInstrument<T>, Price<T>> priceMap = this.currentPriceInfoProvider
							.getCurrentPricesForInstruments(Lists.newArrayList(newTrade.getInstrument()));
					Price<T> instrumentPrice = priceMap.get(newTrade.getInstrument());
					price = signal == TradingSignal.LONG ? instrumentPrice.getAskPrice()
							: instrumentPrice.getBidPrice();
				}
				final double takeProfit = newTrade.getTakeProfit() != 0 ? price + (price - newTrade.getTakeProfit())
						: newTrade.getTakeProfit();
				final double stopLoss = newTrade.getStopLoss() != 0.0 ? price + (price - newTrade.getStopLoss())
						: newTrade.getStopLoss();
				return new TradingDecision<T>(newTrade.getInstrument(), signal, takeProfit, stopLoss, price,
						TradingDecision.SRCDECISION.SOCIAL_MEDIA);
			}
		}
		return new TradingDecision<T>(newTrade.getInstrument(), signal);
	}

	// called by scheduler
	public synchronized void harvestAndTrade() {
		for (final String userId : tweetHandlerMap.keySet()) {
			this.executorService.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					Collection<NewFXTradeTweet<T>> newTradeTweets = harvestNewTradeTweets(userId);
					for (NewFXTradeTweet<T> newTradeTweet : newTradeTweets) {
						Collection<CloseFXTradeTweet<T>> pnlTweets = harvestHistoricTradeTweets(userId,
								newTradeTweet.getInstrument());
						TradingDecision<T> tradeDecision = analyseHistoricClosedTradesForInstrument(pnlTweets,
								newTradeTweet);
						if (tradeDecision.getSignal() != TradingSignal.NONE) {
							orderQueue.offer(tradeDecision);
						}
					}
					return null;
				}
			});
		}
	}

	@Override
	public Collection<NewFXTradeTweet<T>> harvestNewTradeTweets(String userId) {
		FXTweetHandler<T> tweetHandler = tweetHandlerMap.get(userId);
		if (tweetHandler == null) {
			return Collections.emptyList();
		}
		Collection<Tweet> tweets = tweetHandler.findNewTweets();
		if (tweets.size() > 0) {
			LOG.info(String.format("found %d new tweets for user %s", tweets.size(), userId));
		} else {
			return Collections.emptyList();
		}

		Collection<NewFXTradeTweet<T>> newTradeTweets = Lists.newArrayList();
		for (Tweet tweet : tweets) {
			FXTradeTweet<T> tradeTweet = tweetHandler.handleTweet(tweet);
			if (tradeTweet instanceof NewFXTradeTweet) {
				newTradeTweets.add((NewFXTradeTweet<T>) tradeTweet);
			}
		}

		return newTradeTweets;
	}

	@Override
	public Collection<CloseFXTradeTweet<T>> harvestHistoricTradeTweets(String userId,
			TradeableInstrument<T> instrument) {
		FXTweetHandler<T> tweetHandler = tweetHandlerMap.get(userId);
		if (tweetHandler == null) {
			return Collections.emptyList();
		}
		Collection<Tweet> tweets = tweetHandler.findHistoricPnlTweetsForInstrument(instrument);
		if (tweets.size() > 0) {
			LOG.info(String.format("found %d historic pnl tweets for user %s and instrument %s", tweets.size(), userId,
					instrument.getInstrument()));
		} else {
			return Collections.emptyList();
		}
		Collection<CloseFXTradeTweet<T>> pnlTradeTweets = Lists.newArrayList();
		for (Tweet tweet : tweets) {
			FXTradeTweet<T> tradeTweet = tweetHandler.handleTweet(tweet);
			if (tradeTweet instanceof CloseFXTradeTweet) {
				pnlTradeTweets.add((CloseFXTradeTweet<T>) tradeTweet);
			}
		}
		return pnlTradeTweets;
	}
}
