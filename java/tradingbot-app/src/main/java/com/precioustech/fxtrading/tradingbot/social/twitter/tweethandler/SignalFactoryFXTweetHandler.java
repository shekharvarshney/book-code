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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;

import com.google.common.collect.Lists;
import com.precioustech.fxtrading.TradingConstants;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.FXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;

public class SignalFactoryFXTweetHandler extends AbstractFXTweetHandler<String> {
	private static final String BUY = "Buy";
	private static final String SELL = "Sell";
	private static final String CLOSE = "Close";
	private static final String COLON = ":";
	private static final String AT_THE_RATE = "@";

	/*
	 * New Trade example: Forex Signal | Sell USDCHF@0.91902 | SL:0.92302 |
	 * TP:0.91102 | 2015.01.30 17:52 GMT | #fx #forex #fb
	 * 
	 * Close Trade example: Forex Signal | Close(SL) Buy NZDCHF@0.66566 | Loss:
	 * -40 pips | 2015.01.30 18:16 GMT | #fx #forex #fb
	 */

	@Override
	protected NewFXTradeTweet<String> parseNewTrade(String tokens[]) {
		String token1[] = tokens[1].trim().split(TradingConstants.SPACE_RGX);
		String action = token1[0];
		String tokens1spl[] = token1[1].split(AT_THE_RATE);
		String tokens2[] = tokens[2].trim().split(COLON);
		String tokens3[] = tokens[3].trim().split(COLON);

		return new NewFXTradeTweet<String>(new TradeableInstrument<String>(this.providerHelper
				.fromIsoFormat(tokens1spl[0])), Double.parseDouble(tokens1spl[1]), Double.parseDouble(tokens2[1]),
				Double.parseDouble(tokens3[1]), BUY.equals(action) ? TradingSignal.LONG : TradingSignal.SHORT);
	}

	@Override
	protected CloseFXTradeTweet<String> parseCloseTrade(String tokens[]) {
		String token1[] = tokens[1].trim().split(TradingConstants.SPACE_RGX);
		String tokens1spl[] = token1[token1.length - 1].split(AT_THE_RATE);
		String token2[] = tokens[2].trim().split(TradingConstants.SPACE_RGX);
		return new CloseFXTradeTweet<String>(new TradeableInstrument<String>(this.providerHelper
				.fromIsoFormat(tokens1spl[0])), Double.parseDouble(token2[1]), Double.parseDouble(tokens1spl[1]));
	}

	public SignalFactoryFXTweetHandler(String userid) {
		super(userid);
	}

	@Override
	public FXTradeTweet<String> handleTweet(Tweet tweet) {
		String tweetTxt = tweet.getText();
		String tokens[] = StringUtils.split(tweetTxt, TradingConstants.PIPE_CHR);
		if (tokens.length >= 5) {
			String action = tokens[1].trim();
			if (action.startsWith(BUY) || action.startsWith(SELL)) {
				return parseNewTrade(tokens);
			} else if (action.startsWith(CLOSE)) {
				return parseCloseTrade(tokens);
			}
		}
		return null;
	}

	@Override
	public Collection<Tweet> findHistoricPnlTweetsForInstrument(TradeableInstrument<String> instrument) {
		String isoInstr = this.providerHelper.toIsoFormat(instrument.getInstrument());

		/*
		 * And queries have suddenly stopped working. 
		 * something simple like from:SignalFactory GBPNZD is not working. 
		 * Check it out yourself on https://twitter.com/search-advanced.
		 * Apparently the only option is to get all tweets with phrase Profit or Loss
		 * and then use String contains to perform the step2 filtering 
		 */

		String query = String.format("Profit: OR Loss: from:%s", getUserId(), isoInstr);
		SearchResults results = twitter.searchOperations().search(query);
		List<Tweet> pnlTweets = results.getTweets();
		List<Tweet> filteredPnlTweets = Lists.newArrayList();
		for (Tweet pnlTweet : pnlTweets) {
			if (pnlTweet.getText().contains(isoInstr)) {
				filteredPnlTweets.add(pnlTweet);
			}
		}
		return filteredPnlTweets;
	}
}
