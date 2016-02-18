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

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;

import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.FXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;

public class ThirdBrainFx2TweetHandler extends AbstractFXTweetHandler<String> {
	private static final String BUY = "BUY";
	private static final String SELL = "SELL";
	private static final String TPCOLON = TP + TradingConstants.COLON;
	private static final String CLOSED = "closed";

	protected ThirdBrainFx2TweetHandler(String userId) {
		super(userId);
	}

	@Override
	public FXTradeTweet<String> handleTweet(Tweet tweet) {
		String tweetTxt = tweet.getText();
		String[] tokens = tweetTxt.trim().split(TradingConstants.SPACE_RGX);
		if ("strategy".equals(tokens[1])) {
			return this.parseNewTrade(tokens);
		}
		if (CLOSED.equals(tokens[1])) {
			return this.parseCloseTrade(tokens);
		}
		return null;
	}

	@Override
	public Collection<Tweet> findHistoricPnlTweetsForInstrument(TradeableInstrument<String> instrument) {
		String isoInstr = TradingConstants.HASHTAG + this.providerHelper.toIsoFormat(instrument.getInstrument());
		SearchResults results = this.twitter.searchOperations()
				.search(String.format("from:%s %s \"%s\"", this.getUserId(), isoInstr, CLOSED));
		return results.getTweets();
	}

	@Override
	protected NewFXTradeTweet<String> parseNewTrade(String[] tokens) {
		String ccyPair = this.deriveCcyPair(tokens);
		String side = tokens[3];
		double stopLoss = 0.0;
		double takeProfit = 0.0;
		try {
			if (tokens[6].startsWith(TPCOLON)) {
				String[] tokens2 = StringUtils.split((String) tokens[6], TradingConstants.COLON);
				takeProfit = Double.parseDouble(tokens2[1]);
			}
			if (SL.equals(tokens[4]) && tokens[5].startsWith(TradingConstants.COLON)) {
				stopLoss = Double.parseDouble(tokens[5].substring(1));
			}
			return new NewFXTradeTweet<>(new TradeableInstrument<>(ccyPair), 0.0, stopLoss, takeProfit,
					BUY.equals(side) ? TradingSignal.LONG : TradingSignal.SHORT);
		} catch (NumberFormatException nfe) {
			LOG.error(nfe);
			return null;
		}
	}

	private String deriveCcyPair(String[] tokens) {
		String hashtagCcy = tokens[0];
		String ccyPair = this.providerHelper.fromHashTagCurrency(hashtagCcy);
		return ccyPair;
	}

	@Override
	protected CloseFXTradeTweet<String> parseCloseTrade(String[] tokens) {
		String ccyPair = this.deriveCcyPair(tokens);
		String strPrice = tokens[9];
		String profitStr = tokens[11];
		if (profitStr.contains(TradingConstants.COLON)) {
			String[] tokens2 = StringUtils.split((String) profitStr, TradingConstants.COLON);
			return new CloseFXTradeTweet<>(new TradeableInstrument<>(ccyPair), Double.parseDouble(tokens2[1]),
					Double.parseDouble(strPrice));
		}
		return null;
	}
}