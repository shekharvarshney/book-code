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

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;

public class TheDailyIncomeFXTweetHandler extends ZuluTrader101FXTweetHandler {

	protected TheDailyIncomeFXTweetHandler(String userId) {
		super(userId);
	}

	@Override
	protected NewFXTradeTweet<String> parseNewTrade(String[] tokens) {

		String currencyPair = null;
		try {
			String ccyWithHashTag = tokens[1];
			currencyPair = this.providerHelper.fromHashTagCurrency(ccyWithHashTag);
			double price = Double.parseDouble(tokens[2]);
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
			LOG.info(String.format(" got err %s parsing tweet tokens for new trade for user %s", e.getMessage(),
					getUserId()));
			return null;
		}
	}

	@Override
	protected CloseFXTradeTweet<String> parseCloseTrade(String[] tokens) {

		String currencyPair = null;
		try {
			String ccyWithHashTag = tokens[2];
			currencyPair = providerHelper.fromHashTagCurrency(ccyWithHashTag);
			String strPnlPips = tokens[5];
			return new CloseFXTradeTweet<String>(new TradeableInstrument<String>(currencyPair),
					Double.parseDouble(strPnlPips), Double.parseDouble(tokens[3]));
		} catch (Exception e) {
			LOG.info(String.format(" got err %s parsing tweet tokens for close trade for user %s", e.getMessage(),
					getUserId()));
			return null;
		}

	}

}
