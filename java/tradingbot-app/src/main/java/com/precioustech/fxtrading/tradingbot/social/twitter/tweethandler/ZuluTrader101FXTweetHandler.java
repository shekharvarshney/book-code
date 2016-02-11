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

import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;

import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.FXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;

public class ZuluTrader101FXTweetHandler extends AbstractFXTweetHandler<String> {

	/*
	 * New Trade examples: 
	 * 1) Bought 0.43 Lots #EURUSD 1.1371 | Auto-copy FREE at http://goo.gl/moaYzx #Forex #Finance #Money 
	 * 2) Bought 0.69 Lots #GBPCHF 1.4614 SL 1.45817 TP 1.46617 | Auto-copy FREE at http://goo.gl/moaYzx #Forex #Finance #Money
	 * 3) Sold 0.34 Lots #EURUSD 1.13694 | Auto-copy FREE at http://goo.gl/moaYzx  #Forex #Finance #Money
	 * 4) Sold 0.43 Lots #EURUSD 1.13117 SL 1.13281 TP 1.12963 | Auto-copy FREE at http://goo.gl/moaYzx  #Forex #Finance #Money
	 * 5) Bought 0.66 Lots #EURGBP 0.73532 SL 0.70534 | Auto-copy FREE at http://goo.gl/moaYzx  #Forex #Finance #Money
	 * 
	 * Close Trade examples:
	 * 
	 * 1) Closed Buy 0.64 Lots #USDJPY 118.773 for +17.7 pips, total for today -136.7 pips
	 * 2) Closed Sell 0.69 Lots #NZDUSD 0.75273 for +8.4 pips, total for today -1072.8 pips
	 * 3) Closed Buy 7.0 Lots #XAUUSD 1207.94 for -549.0 pips, total for today -1731.4 pips
	 */

	protected ZuluTrader101FXTweetHandler(String userId) {
		super(userId);
	}

	protected int idxOfTP(String[] tokens) {
		int idx = 0;
		for (String token : tokens) {
			if ("TP".equals(token)) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	protected int idxOfSL(String[] tokens) {
		int idx = 0;
		for (String token : tokens) {
			if ("SL".equals(token)) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	@Override
	protected NewFXTradeTweet<String> parseNewTrade(String[] tokens) {

		String currencyPair = null;
		try {
			String ccyWithHashTag = tokens[3];
			currencyPair = this.providerHelper.fromHashTagCurrency(ccyWithHashTag);
			double price = Double.parseDouble(tokens[4]);
			TradingSignal signal = BOUGHT.equals(tokens[0]) ? TradingSignal.LONG : TradingSignal.SHORT;
			double stopLoss = 0.0;
			double takeProfit = 0.0;
			int idxTp = idxOfTP(tokens);
			if (idxTp != -1) {
				takeProfit = Double.parseDouble(tokens[idxTp + 1]);
			}
			int idxSl = idxOfSL(tokens);
			if (idxSl != -1) {
				stopLoss = Double.parseDouble(tokens[idxSl + 1]);
			}
			return new NewFXTradeTweet<String>(new TradeableInstrument<String>(currencyPair), price, stopLoss,
					takeProfit, signal);
		} catch (Exception e) {
			LOG.info(String.format(" got err %s parsing tweet tokens for new trade ", e.getMessage()));
			return null;
		}

	}

	@Override
	protected CloseFXTradeTweet<String> parseCloseTrade(String[] tokens) {

		String currencyPair = null;
		try {
			String ccyWithHashTag = tokens[4];
			currencyPair = this.providerHelper.fromHashTagCurrency(ccyWithHashTag);
			String strPnlPips = tokens[7];
			return new CloseFXTradeTweet<String>(new TradeableInstrument<String>(currencyPair), Double
					.parseDouble(strPnlPips), Double.parseDouble(tokens[5]));
		} catch (Exception e) {
			LOG.info(String.format(" got err %s parsing tweet tokens for close trade:", e.getMessage()));
			return null;
		}

	}

	@Override
	public FXTradeTweet<String> handleTweet(Tweet tweet) {
		String tweetTxt = tweet.getText();
		String tokens[] = tweetTxt.trim().split(TradingConstants.SPACE_RGX);
		if (tweetTxt.startsWith(CLOSED)) {
			return parseCloseTrade(tokens);
		} else if (tweetTxt.startsWith(BOUGHT) || tweetTxt.startsWith(SOLD)) {
			return parseNewTrade(tokens);
		}
		return null;
	}

	@Override
	public Collection<Tweet> findHistoricPnlTweetsForInstrument(TradeableInstrument<String> instrument) {
		String isoInstr = TradingConstants.HASHTAG + this.providerHelper.toIsoFormat(instrument.getInstrument());
		SearchResults results = twitter.searchOperations().search(
				String.format("from:%s \"Closed Buy\" OR \"Closed Sell\" %s", getUserId(), isoInstr));
		return results.getTweets();
	}

}
