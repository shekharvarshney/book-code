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

public class SFOsignalsTweetHandler extends AbstractFXTweetHandler<String> {

	/*
	 * New Trade egs
	 *
	 * #Forex #Signal: SELL #GBPUSD @ 1.43511 #Forex #Signal: BUY #AUDNZD @
	 * 1.10903
	 * 
	 * Close Trade egs #Forex #Signal: CLOSE SELL #GBPNZD @ 2.07824 with P&L:
	 * +0.04%
	 */
	private static final String CLOSE = "CLOSE";
	private static final String SELL = "SELL";
	private static final String BUY = "BUY";

	protected SFOsignalsTweetHandler(String userId) {
		super(userId);

	}

	@Override
	public FXTradeTweet<String> handleTweet(Tweet tweet) {
		String tweetTxt = tweet.getText();
		String[] tokens = tweetTxt.trim().split(TradingConstants.SPACE_RGX);
		if (CLOSE.equals(tokens[2])) {
			return parseCloseTrade(tokens);
		} else if (BUY.equals(tokens[2]) || SELL.equals(tokens[2])) {
			return parseNewTrade(tokens);
		}
		return null;
	}

	@Override
	public Collection<Tweet> findHistoricPnlTweetsForInstrument(TradeableInstrument<String> instrument) {
		String isoInstr = TradingConstants.HASHTAG + this.providerHelper.toIsoFormat(instrument.getInstrument());
		SearchResults results = this.twitter.searchOperations()
				.search(String.format("\"%s\" %s from:%s", CLOSE, isoInstr, this.getUserId()));
		return results.getTweets();
	}

	@Override
	protected NewFXTradeTweet<String> parseNewTrade(String[] tokens) {
		return new NewFXTradeTweet<>(new TradeableInstrument<>(this.providerHelper.fromHashTagCurrency(tokens[3])),
				Double.parseDouble(tokens[5]), 0.0, 0.0,
				BUY.equals(tokens[2]) ? TradingSignal.LONG : TradingSignal.SHORT);
	}

	@Override
	protected CloseFXTradeTweet<String> parseCloseTrade(String[] tokens) {
		return new CloseFXTradeTweet<>(new TradeableInstrument<>(this.providerHelper.fromHashTagCurrency(tokens[4])),
				Double.parseDouble(StringUtils.removeEnd(tokens[9], "%")), Double.parseDouble(tokens[6]));
	}

}
